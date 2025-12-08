package edu.neu.csye7374.javafx;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class FXTitleController {

    @FXML private AnchorPane titleRootPane;

    @FXML private ImageView halo;
    @FXML private ImageView titleLogo;
    @FXML private Button startButton;

@FXML
public void initialize() {

    titleRootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
        if (newScene != null) {

            String globalCss = "/edu/neu/csye7374/assets/global_theme.css";
            newScene.getStylesheets().add(
                    getClass().getResource(globalCss).toExternalForm()
            );

            // Fade-in effect
            FadeTransition fade = new FadeTransition(Duration.millis(350), newScene.getRoot());
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    });
}


        // // Subtle glow / pulse
        // ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.5), titleLogo);
        // pulse.setFromX(1.0);
        // pulse.setFromY(1.0);
        // pulse.setToX(1.03);
        // pulse.setToY(1.03);
        // pulse.setAutoReverse(true);
        // pulse.setCycleCount(Animation.INDEFINITE);
        // pulse.play();

    //         ScaleTransition haloPulse = new ScaleTransition(Duration.seconds(3.5), halo);
    // haloPulse.setFromX(1.0);
    // haloPulse.setFromY(1.0);
    // haloPulse.setToX(1.15);
    // haloPulse.setToY(1.15);
    // haloPulse.setCycleCount(Animation.INDEFINITE);
    // haloPulse.setAutoReverse(true);
    // haloPulse.play();
    

    @FXML
    public void onStartClicked() throws Exception {
        Stage stage = (Stage) startButton.getScene().getWindow();

        // Preload next scene
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/edu/neu/csye7374/character_select.fxml")
        );
        Scene nextScene = new Scene(loader.load());

        // Fade out current root
        Scene currentScene = startButton.getScene();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), currentScene.getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            stage.setScene(nextScene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), nextScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
}
