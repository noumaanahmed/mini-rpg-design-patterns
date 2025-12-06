package edu.neu.csye7374.javafx;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class FXTitleController {

    @FXML private ImageView titleLogo;
    @FXML private Button startButton;

    @FXML
    public void initialize() {
        FadeTransition ft = new FadeTransition(Duration.seconds(2), titleLogo);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML
    public void onStartClicked() throws Exception {
        Stage stage = (Stage) startButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/edu/neu/csye7374/character_select.fxml")
        );
        stage.setScene(new Scene(loader.load()));
    }
}
