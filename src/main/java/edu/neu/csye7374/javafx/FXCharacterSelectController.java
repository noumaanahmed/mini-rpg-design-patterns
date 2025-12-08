package edu.neu.csye7374.javafx;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class FXCharacterSelectController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> difficultyBox;

    @FXML private ImageView warriorImage;
    @FXML private ImageView mageImage;

    @FXML private VBox warriorBox;
    @FXML private VBox mageBox;

    private String chosenClass = "warrior";

    @FXML
    public void initialize() {

        // Proper CSS loading — guaranteed to work
 warriorBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
    if (newScene != null) {

        // Character Select screen theme
        String cssPath = "/edu/neu/csye7374/assets/character_select.css";
        newScene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());

        // GLOBAL RPG THEME
        String globalCss = "/edu/neu/csye7374/assets/global_theme.css";
        newScene.getStylesheets().add(getClass().getResource(globalCss).toExternalForm());

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), newScene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
});


        difficultyBox.getItems().addAll("Easy", "Normal", "Hard");

        // Idle animations
        FXAnimationUtil.playWarriorIdle(
                warriorImage,
                "/edu/neu/csye7374/assets/sprites/warrior_idle.png",
                150
        );

        FXAnimationUtil.playMageIdle(
                mageImage,
                "/edu/neu/csye7374/assets/sprites/mage_idle.png",
                150
        );

        // alignment tweak
        warriorImage.setTranslateY(8);
        mageImage.setTranslateY(-18);

        warriorBox.setOnMouseClicked(e -> selectClass("warrior"));
        mageBox.setOnMouseClicked(e -> selectClass("mage"));

        selectClass("warrior");
    }

    private void selectClass(String cls) {
        chosenClass = cls;

        warriorBox.getStyleClass().remove("selected");
        mageBox.getStyleClass().remove("selected");

        if (cls.equals("warrior")) {
            warriorBox.getStyleClass().add("selected");
        } else {
            mageBox.getStyleClass().add("selected");
        }
    }

    @FXML
    public void onStartBattle() throws Exception {

        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Hero";

        int difficulty = difficultyBox.getSelectionModel().getSelectedIndex() + 1;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/edu/neu/csye7374/battle.fxml")
        );

        Scene battleScene = new Scene(loader.load());
        FXBattleController controller = loader.getController();
        controller.startGame(name, chosenClass, difficulty);

        Stage stage = (Stage) warriorImage.getScene().getWindow();

        // Fade out current root, then set battle scene and fade in
        Scene currentScene = warriorImage.getScene();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), currentScene.getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            stage.setScene(battleScene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), battleScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
}
