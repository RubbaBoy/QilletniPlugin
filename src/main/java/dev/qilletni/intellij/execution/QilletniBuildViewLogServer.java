package dev.qilletni.intellij.execution;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.build.BuildViewManager;
import com.intellij.build.DefaultBuildDescriptor;
import com.intellij.build.events.MessageEvent;
import com.intellij.build.events.impl.FinishBuildEventImpl;
import com.intellij.build.events.impl.MessageEventImpl;
import com.intellij.build.events.impl.OutputBuildEventImpl;
import com.intellij.build.events.impl.StartBuildEventImpl;
import com.intellij.build.events.impl.SuccessResultImpl;
import com.intellij.build.events.impl.FailureResultImpl;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

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
 * Log server for Build configurations that publishes events to the IntelliJ Build View.
 */
public final class QilletniBuildViewLogServer {
    private final Project project;
    private final ServerSocketChannel server;
    private final Object buildId = new Object();
    private final String title;
    private final String workingDir;
    private final AtomicBoolean attached = new AtomicBoolean(false);
    private volatile boolean hadErrors = false;

    public QilletniBuildViewLogServer(Project project, String title, String workingDir) throws IOException {
        this.project = project;
        this.server = ServerSocketChannel.open();
        this.server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        this.title = title == null || title.isBlank() ? "Qilletni Build" : title;
        this.workingDir = workingDir;
        // Start event
        var descriptor = new DefaultBuildDescriptor(buildId, this.title, this.workingDir, System.currentTimeMillis());
        startEvent(descriptor);
    }

    public int getPort() throws IOException {
        var addr = (InetSocketAddress) server.getLocalAddress();
        return addr.getPort();
    }

    public void attachTo(ProcessHandler handler) {
        if (!attached.compareAndSet(false, true)) return;
        handler.addProcessListener(new ProcessAdapter() {
            @Override public void processTerminated(ProcessEvent event) {
                finishEvent(!hadErrors);
                closeSilently();
            }
        });
        // Pooled thread for accept loop
        com.intellij.util.concurrency.AppExecutorUtil.getAppExecutorService().execute(() -> acceptOnceAndPump(handler));
        // Also close on project dispose as a safety net
        com.intellij.openapi.util.Disposer.register(project, () -> closeSilently());
    }

    private void acceptOnceAndPump(ProcessHandler handler) {
        try (ServerSocketChannel srv = this.server) {
            try (SocketChannel channel = srv.accept()) {
                if (channel == null) return;
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
            publishFormatted(obj);
        } catch (Throwable t) {
            // Publish raw as info with parser note under the single task (as MessageEvent for highlighting)
            publishMessage(MessageEvent.Kind.INFO, "[parser] malformed JSON: " + line, null);
        }
    }

    private void publishFormatted(JsonObject o) {
        var level = o.has("level") ? safeString(o.get("level")) : "INFO";
        var kind = toKind(level);
        if (kind == MessageEvent.Kind.ERROR) hadErrors = true;
        var thread = o.has("thread") ? safeString(o.get("thread")) : "?";
        String time = formatTime(o);
        var msg = o.has("message") ? safeString(o.get("message")) : "";
        var text = "[" + time + "] [" + thread + "/" + level + "]: " + msg;

        // Throwable details if present -> aggregate into MessageEvent details
        String details = null;
        if (o.has("thrown") && o.get("thrown").isJsonObject()) {
            var th = o.getAsJsonObject("thrown");
            var name = th.has("name") ? safeString(th.get("name")) : "Exception";
            var tmsg = th.has("localizedMessage") ? safeString(th.get("localizedMessage")) : (th.has("message") ? safeString(th.get("message")) : "");
            var sb = new StringBuilder();
            sb.append(name);
            if (!tmsg.isEmpty()) sb.append(": ").append(tmsg);
            sb.append('\n');
            if (th.has("extendedStackTrace") && th.get("extendedStackTrace").isJsonArray()) {
                for (JsonElement el : th.getAsJsonArray("extendedStackTrace")) {
                    if (!el.isJsonObject()) continue;
                    var e = el.getAsJsonObject();
                    var cls = e.has("class") ? safeString(e.get("class")) : "?";
                    var mth = e.has("method") ? safeString(e.get("method")) : "?";
                    var file = e.has("file") ? safeString(e.get("file")) : "Unknown Source";
                    int line = e.has("line") ? e.get("line").getAsInt() : -1;
                    sb.append('\t').append("at ")
                      .append(cls).append('.').append(mth)
                      .append('(').append(file);
                    if (line >= 0) sb.append(':').append(line);
                    sb.append(')').append('\n');
                }
            }
            details = sb.toString();
        }
        publishMessage(kind, text, details);
    }

    private void publishMessage(MessageEvent.Kind kind, String message, String details) {
        ApplicationManager.getApplication().invokeLater(() -> {
            var view = project.getService(BuildViewManager.class);
            if (view == null) return;
            // Stream under the single step as plain output; use stderr for ERROR/FATAL to render red
            boolean stdOut = kind != MessageEvent.Kind.ERROR;
            view.onEvent(buildId, new OutputBuildEventImpl(buildId, message + "\n", stdOut));
            if (details != null && !details.isEmpty()) {
                // Emit details (e.g., stacktrace) as additional lines under the same node
                var lines = details.split("\r?\n", -1);
                for (var line : lines) {
                    if (line.isEmpty()) continue;
                    view.onEvent(buildId, new OutputBuildEventImpl(buildId, line + "\n", stdOut));
                }
            }
        });
    }

    private static MessageEvent.Kind toKind(String lvl) {
        if (lvl == null) return MessageEvent.Kind.INFO;
        return switch (lvl) {
            case "TRACE", "DEBUG" -> MessageEvent.Kind.INFO;
            case "INFO" -> MessageEvent.Kind.INFO;
            case "WARN" -> MessageEvent.Kind.WARNING;
            case "ERROR", "FATAL" -> MessageEvent.Kind.ERROR;
            default -> MessageEvent.Kind.INFO;
        };
    }

    private static String safeString(JsonElement e) {
        return e == null || e.isJsonNull() ? "" : e.getAsString();
    }

    private static String formatTime(JsonObject o) {
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
        return Instant.ofEpochSecond(epoch, nano)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void startEvent(DefaultBuildDescriptor descriptor) {
        var view = project.getService(BuildViewManager.class);
        if (view != null) {
            view.onEvent(buildId, new StartBuildEventImpl(descriptor, "Build started"));
        }
    }

    private void finishEvent(boolean success) {
        var view = project.getService(BuildViewManager.class);
        if (view != null) {
            if (success) {
                view.onEvent(buildId, new FinishBuildEventImpl(buildId, null, System.currentTimeMillis(), "Finished", new SuccessResultImpl()));
            } else {
                view.onEvent(buildId, new FinishBuildEventImpl(buildId, null, System.currentTimeMillis(), "Failed", new FailureResultImpl()));
            }
        }
    }

    private void closeSilently() {
        try { server.close(); } catch (IOException ignored) {}
    }
}
