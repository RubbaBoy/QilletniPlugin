package dev.qilletni.intellij.execution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import java.awt.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hosts a per-run TCP server for receiving toolchain logs and streams them into a ConsoleView
 * shown in the "Qilletni Logs" tool window.
 */
public final class QilletniToolchainLogServer {
    // Colored content types for log levels (used only for the level token)
    private static final ConsoleViewContentType LVL_TRACE = new ConsoleViewContentType(
            "QILLETNI_LVL_TRACE", new TextAttributes(JBColor.GRAY, null, null, null, Font.BOLD));
    private static final ConsoleViewContentType LVL_DEBUG = new ConsoleViewContentType(
            "QILLETNI_LVL_DEBUG", new TextAttributes(JBColor.CYAN, null, null, null, Font.BOLD));
    private static final ConsoleViewContentType LVL_INFO = new ConsoleViewContentType(
            "QILLETNI_LVL_INFO", new TextAttributes(JBColor.GREEN, null, null, null, Font.BOLD));
    private static final ConsoleViewContentType LVL_WARN = new ConsoleViewContentType(
            "QILLETNI_LVL_WARN", new TextAttributes(JBColor.YELLOW, null, null, null, Font.BOLD));
    private static final Key<ConsoleView> CONSOLE_KEY = new Key<>("qilletni.logs.console");

    private final Project project;
    private final ServerSocketChannel server;
    private final ConsoleView console;
    private final String title;
    private final AtomicBoolean attached = new AtomicBoolean(false);
    private final com.intellij.execution.process.AnsiEscapeDecoder ansiDecoder = new com.intellij.execution.process.AnsiEscapeDecoder();

    public QilletniToolchainLogServer(Project project, @NlsSafe String tabTitle) throws IOException {
        this.project = project;
        this.server = ServerSocketChannel.open();
        this.server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        this.title = (tabTitle == null || tabTitle.isBlank()) ? defaultTabTitle() : tabTitle;
        this.console = findOrCreateConsole(project, this.title);
        // Ensure tool window tab is visible immediately; content is added in findOrCreateConsole
        ApplicationManager.getApplication().invokeLater(() -> {
            var tw = ToolWindowManager.getInstance(project).getToolWindow("Qilletni Logs");
            if (tw != null) {
                if (!tw.isAvailable()) tw.setAvailable(true, null);
                tw.activate(null, true, true);
            }
        });
    }

    public int getPort() throws IOException {
        var addr = (InetSocketAddress) server.getLocalAddress();
        return addr.getPort();
    }

    public ConsoleView getConsole() { return console; }

    public void attachTo(ProcessHandler handler) {
        if (!attached.compareAndSet(false, true)) return;
        // Ensure resources are disposed with the project as a safety net
        com.intellij.openapi.util.Disposer.register(project, new com.intellij.openapi.Disposable() {
            @Override
            public void dispose() {
                closeSilently();
            }
        });
        // Close server on process end as well
        handler.addProcessListener(new ProcessAdapter() {
            @Override public void processTerminated(ProcessEvent event) {
                closeSilently();
            }
        });
        // Start accept loop (single connection per run) on pooled thread for IDE compatibility
        com.intellij.util.concurrency.AppExecutorUtil.getAppExecutorService().execute(() -> acceptOnceAndPump(handler));
    }

    private void acceptOnceAndPump(ProcessHandler handler) {
        try (ServerSocketChannel srv = this.server) {
            try (SocketChannel channel = srv.accept()) {
                if (channel == null) return; // unlikely in blocking mode
                var buf = ByteBuffer.allocate(8192);
                var decoder = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE);
                var cbuf = CharBuffer.allocate(8192);
                var lineBuf = new StringBuilder(8192);
                while (!handler.isProcessTerminated()) {
                    var n = channel.read(buf);
                    if (n == -1) break;
                    if (n == 0) continue;
                    buf.flip();
                    while (true) {
                        CoderResult cr = decoder.decode(buf, cbuf, false);
                        cbuf.flip();
                        lineBuf.append(cbuf);
                        cbuf.clear();
                        if (cr.isUnderflow()) break;
                    }
                    buf.compact();

                    int nl;
                    while ((nl = lineBuf.indexOf("\n")) >= 0) {
                        var line = lineBuf.substring(0, nl);
                        lineBuf.delete(0, nl + 1);
                        handleJsonLine(line);
                    }
                }
                if (lineBuf.length() > 0) handleJsonLine(lineBuf.toString());
            }
        } catch (IOException ignored) {
        } finally {
            closeSilently();
        }
    }

    private void handleJsonLine(String line) {
        try {
            var obj = JsonParser.parseString(line).getAsJsonObject();
            printFormatted(obj);
        } catch (Throwable t) {
            var fallback = "[parser] malformed JSON: " + line + "\n";
            ApplicationManager.getApplication().invokeLater(() -> console.print(fallback, ConsoleViewContentType.SYSTEM_OUTPUT));
        }
        // Ensure tool window is visible on first data
        ApplicationManager.getApplication().invokeLater(() -> {
            var tw = ToolWindowManager.getInstance(project).getToolWindow("Qilletni Logs");
            if (tw != null) tw.activate(null, false, true);
        });
    }

    private void printFormatted(JsonObject o) {
        long epoch = 0L; int nano = 0;
        if (o.has("instant") && o.get("instant").isJsonObject()) {
            var inst = o.getAsJsonObject("instant");
            if (inst.has("epochSecond")) epoch = inst.get("epochSecond").getAsLong();
            if (inst.has("nanoOfSecond")) nano = inst.get("nanoOfSecond").getAsInt();
        } else if (o.has("timeMillis")) {
            var ms = o.get("timeMillis").getAsLong();
            var ins = Instant.ofEpochMilli(ms);
            epoch = ins.getEpochSecond(); nano = ins.getNano();
        } else {
            var now = Instant.now(); epoch = now.getEpochSecond(); nano = now.getNano();
        }
        var time = Instant.ofEpochSecond(epoch, nano)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        var thread = o.has("thread") ? safeString(o.get("thread")) : "?";
        var level = o.has("level") ? safeString(o.get("level")) : "?";
        var msg = o.has("message") ? safeString(o.get("message")) : "";

        // Format the entire line as a single string
        var formattedLine = new StringBuilder(512)
                .append('[').append(time).append("] [")
                .append(thread).append('/')
                .append(level)
                .append("]: ")
                .append(msg)
                .append('\n')
                .toString();

        // Colorize the entire line based on level
        var coloredLine = AnsiColorizer.colorize(level, formattedLine);

        ApplicationManager.getApplication().invokeLater(() -> {
            // Process ANSI codes and print with proper colors
            printWithAnsi(coloredLine);

            // Throwable rendering (also colorized with ANSI)
            if (o.has("thrown") && o.get("thrown").isJsonObject()) {
                var th = o.getAsJsonObject("thrown");
                var name = th.has("name") ? safeString(th.get("name")) : "Exception";
                var tmsg = th.has("localizedMessage") ? safeString(th.get("localizedMessage")) : (th.has("message") ? safeString(th.get("message")) : "");
                
                // Build exception header
                var exHeader = new StringBuilder().append(name);
                if (!tmsg.isEmpty()) exHeader.append(": ").append(tmsg);
                exHeader.append('\n');
                
                // Colorize and print with ANSI processing
                printWithAnsi(AnsiColorizer.colorize(level, exHeader.toString()));
                
                // Stack trace lines
                if (th.has("extendedStackTrace") && th.get("extendedStackTrace").isJsonArray()) {
                    for (JsonElement el : th.getAsJsonArray("extendedStackTrace")) {
                        if (!el.isJsonObject()) continue;
                        var e = el.getAsJsonObject();
                        var cls = e.has("class") ? safeString(e.get("class")) : "?";
                        var mth = e.has("method") ? safeString(e.get("method")) : "?";
                        var file = e.has("file") ? safeString(e.get("file")) : "Unknown Source";
                        int line = e.has("line") ? e.get("line").getAsInt() : -1;
                        var frameLine = new StringBuilder()
                                .append('\t').append("at ")
                                .append(cls).append('.').append(mth)
                                .append('(').append(file);
                        if (line >= 0) frameLine.append(':').append(line);
                        frameLine.append(')').append('\n');
                        
                        printWithAnsi(AnsiColorizer.colorize(level, frameLine.toString()));
                    }
                }
            }
        });
    }

    private static String safeString(JsonElement e) {
        return e == null || e.isJsonNull() ? "" : e.getAsString();
    }

    /**
     * Prints text with ANSI escape code processing.
     * The AnsiEscapeDecoder parses ANSI codes and calls the acceptor with colored text chunks.
     */
    private void printWithAnsi(String text) {
        ansiDecoder.escapeText(text, ProcessOutputTypes.STDOUT, new com.intellij.execution.process.AnsiEscapeDecoder.ColoredTextAcceptor() {
            @Override
            public void coloredTextAvailable(String chunk, com.intellij.openapi.util.Key outputType) {
                // Map ANSI color codes to ConsoleViewContentType
                ConsoleViewContentType contentType = ansiOutputTypeToContentType(outputType);
                console.print(chunk, contentType);
            }
        });
    }

    /**
     * Maps AnsiEscapeDecoder output types (which encode ANSI colors) to ConsoleViewContentType.
     * AnsiEscapeDecoder attaches TextAttributes to Keys to encode color information.
     */
    private static ConsoleViewContentType ansiOutputTypeToContentType(com.intellij.openapi.util.Key outputType) {
        // Check for stderr (errors)
        if (ProcessOutputTypes.STDERR.equals(outputType)) {
            return ConsoleViewContentType.ERROR_OUTPUT;
        }
        
        // AnsiEscapeDecoder stores TextAttributes in the Key's user data
        // Try to extract TextAttributes from the Key
        var attributes = ConsoleViewContentType.getConsoleViewType(outputType);
        if (attributes != null) {
            return attributes;
        }
        
        // Fallback: check if this is a standard ProcessOutputTypes constant
        if (ProcessOutputTypes.SYSTEM.equals(outputType)) {
            return ConsoleViewContentType.SYSTEM_OUTPUT;
        }
        
        // Default to normal output for stdout and unknown types
        return ConsoleViewContentType.NORMAL_OUTPUT;
    }

    private static String defaultTabTitle() {
        var t = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return "Logs " + t;
    }

    private void closeSilently() {
        try { server.close(); } catch (IOException ignored) {}
    }

    private static ConsoleView findOrCreateConsole(Project project, String title) {
        var tw = ToolWindowManager.getInstance(project).getToolWindow("Qilletni Logs");
        ConsoleView existing = null;
        if (tw != null) {
            for (var c : tw.getContentManager().getContents()) {
                if (title.equals(c.getDisplayName())) {
                    existing = c.getUserData(CONSOLE_KEY);
                    break;
                }
            }
        }
        if (existing != null) {
            existing.clear();
            return existing;
        }
        var console = new ConsoleViewImpl(project, false);
        // Add standard console filters
        console.addMessageFilter(new QilletniOutputHyperlinkFilter(project));
        try {
            // Best-effort: built-in exception hyperlinking
            console.addMessageFilter(new com.intellij.execution.filters.ExceptionFilter(
                    com.intellij.psi.search.GlobalSearchScope.allScope(project)));
        } catch (Throwable ignored) { }
        // Build a panel with console toolbar actions
        var actions = console.createConsoleActions();
        var group = new com.intellij.openapi.actionSystem.DefaultActionGroup(actions);
        var toolbar = com.intellij.openapi.actionSystem.ActionManager.getInstance()
                .createActionToolbar("QilletniLogsConsole", group, false);
        var panel = new com.intellij.openapi.ui.SimpleToolWindowPanel(false, true);
        toolbar.setTargetComponent(console.getComponent());
        panel.setToolbar(toolbar.getComponent());
        panel.setContent(console.getComponent());

        var content = ContentFactory.getInstance().createContent(panel, title, false);
        content.putUserData(CONSOLE_KEY, console);
        if (tw != null) {
            tw.getContentManager().addContent(content);
            tw.getContentManager().setSelectedContent(content);
        }
        return console;
    }
}
