package edu.neu.csye7374.javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

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
            String cssPath = "/edu/neu/csye7374/assets/character_select.css";
            var url = getClass().getResource(cssPath);

            if (url == null) {
                System.out.println("ERROR: CSS NOT FOUND -> " + cssPath);
            } else {
                newScene.getStylesheets().add(url.toExternalForm());
                System.out.println("CSS Loaded: " + url);
            }
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
    mageImage.setTranslateY(-4);

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

        Scene scene = new Scene(loader.load());
        FXBattleController controller = loader.getController();
        controller.startGame(name, chosenClass, difficulty);

        Stage stage = (Stage) warriorImage.getScene().getWindow();
        stage.setScene(scene);
    }
}
