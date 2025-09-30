package dev.qilletni.intellij.execution;

/**
 * Utility for embedding ANSI escape codes in text to colorize console output.
 * ANSI codes are universally supported by IntelliJ console implementations.
 */
public final class AnsiColorizer {
    // ANSI escape codes
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String DIM_GRAY = "\u001B[90m";

    private AnsiColorizer() { /* utility class */ }

    /**
     * Wraps text with ANSI escape codes based on log level.
     * Supported by all IntelliJ console implementations including Build View.
     *
     * @param level the log level (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
     * @param text  the text to colorize
     * @return the text wrapped with appropriate ANSI color codes, or the original text if level is null/unrecognized
     */
    public static String colorize(String level, String text) {
        if (level == null || text == null) return text;
        return switch (level) {
            case "WARN" -> YELLOW + text + RESET;
            case "ERROR", "FATAL" -> RED + text + RESET;
            case "TRACE", "DEBUG" -> DIM_GRAY + text + RESET;
            default -> text; // INFO and others: no coloring
        };
    }
}
