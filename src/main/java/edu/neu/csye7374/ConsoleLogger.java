package edu.neu.csye7374;

/**
 * Design Pattern: Observer (Concrete Observer)
 * Design Pattern: Bridge (Console implementor)
 * --------------------------------------------
 * Prints game events to the standard console, with simple ANSI colors.
 *
 * NOTE: GUI logs go through FxLogObserver, which strips ANSI codes.
 */
public class ConsoleLogger implements GameObserver {

    private final String name;

    // ANSI color codes
    private static final String RESET   = "\u001B[0m";
    private static final String CYAN    = "\u001B[36m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String RED     = "\u001B[31m";
    private static final String MAGENTA = "\u001B[35m";

    public ConsoleLogger(String name) {
        this.name = name;
            onEvent("[Pattern][Bridge] ConsoleLogger attached for '" + name + "'.");

    }

    @Override
    public void onEvent(String message) {
        String colored = colorize(message);
        System.out.println("[" + name + "] " + colored);
    }

    private String colorize(String msg) {
        // If the message already has ANSI codes (e.g., CriticalStrikeDecorator),
        // just return it as-is.
        if (msg.contains("\u001B[")) {
            return msg;
        }

        String lower = msg.toLowerCase();

        if (msg.startsWith("[Pattern")) {
            return CYAN + msg + RESET;
        }
        if (lower.contains("critical")) {
            return MAGENTA + msg + RESET;
        }
        if (lower.contains("healed")) {
            return GREEN + msg + RESET;
        }
        if (lower.contains("defeated") || lower.contains("victory") || lower.contains("you defeated")) {
            return YELLOW + msg + RESET;
        }
        if (lower.contains("took") && lower.contains("damage")) {
            return RED + msg + RESET;
        }

        // Default: no special color
        return msg;
    }
}
