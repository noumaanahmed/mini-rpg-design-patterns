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
 * This keeps the Observer + Bridge pattern in the domain layer,
 * while the GUI just displays messages.
 */
public class FxLogObserver implements GameObserver {

    private final VBox logBox;

    public FxLogObserver(VBox logBox) {
        this.logBox = logBox;
    }

    @Override
    public void onEvent(String message) {
        Platform.runLater(() -> {
            Label line = new Label(stripAnsi(message));
            line.setStyle("-fx-text-fill: white; -fx-font-size: 13;");

            logBox.getChildren().add(line);
            // keep last ~8 lines so the box does not overflow
            if (logBox.getChildren().size() > 8) {
                logBox.getChildren().remove(0);
            }
        });
    }

    // Remove ANSI color codes so the GUI text is clean
    private String stripAnsi(String msg) {
        return msg.replaceAll("\\u001B\\[[;\\d]*m", "");
    }
}
