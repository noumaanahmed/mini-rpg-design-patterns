package edu.neu.csye7374.javafx;

import edu.neu.csye7374.GameObserver;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * JavaFX Observer adapter:
 *  - Implements GameObserver
 *  - Pushes log messages into a VBox in the JavaFX UI
 *
 * IMPORTANT:
 *  - Design pattern logs (messages starting with "[Pattern") are
 *    filtered out here so that only *gameplay effects* show in the GUI.
 *  - The console (ConsoleLogger) still shows all pattern logs.
 */
public class FxLogObserver implements GameObserver {

    private final VBox logBox;

    public FxLogObserver(VBox logBox) {
        this.logBox = logBox;
    }

    @Override
    public void onEvent(String message) {
        // Skip pattern explanation logs in the GUI
        if (message != null && message.startsWith("[Pattern")) {
            return;
        }

        Platform.runLater(() -> {
            Label line = new Label(stripAnsi(message));
            // bright, readable log text
            line.setStyle("-fx-text-fill: #b3e5fc; -fx-font-size: 14;");

            logBox.getChildren().add(line);
            // keep last ~8 lines so the box does not overflow
            if (logBox.getChildren().size() > 8) {
                logBox.getChildren().remove(0);
            }
        });
    }

    // Remove ANSI color codes so the GUI text is clean
    private String stripAnsi(String msg) {
        if (msg == null) return "";
        return msg.replaceAll("\\u001B\\[[;\\d]*m", "");
    }
}
