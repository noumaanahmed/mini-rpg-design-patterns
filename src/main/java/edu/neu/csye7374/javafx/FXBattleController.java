package edu.neu.csye7374.javafx;

import edu.neu.csye7374.*;
import edu.neu.csye7374.Character;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FXBattleController {

    @FXML private ImageView heroSprite;
    @FXML private ImageView heroHealOverlay;
    @FXML private ImageView enemySprite;

    @FXML private Label heroHp;
    @FXML private Label enemyHp;

    // NEW: mana UI
    @FXML private Label heroManaLabel;
    @FXML private Pane manaBarBg;
    @FXML private Pane manaBarFill;

    @FXML private Button swordButton;
    @FXML private Button fireballButton;
    @FXML private Button healButton;

    @FXML private VBox logBox;
    @FXML private Pane battlePane;

    // Domain / facade
    private GameFacade game;
    private Character player;
    private Character enemy;
    private String playerType;
    private boolean battleOver = false;

    // Sprite paths
    private String idleHero;
    private String attackHero;
    private String fireballHero;
    private String healEffect;

    private String goblinIdle;
    private String goblinHurt;
    private String goblinAttack;
    private String goblinDeath;

    private String warriorHurt;
    private String mageHurt;
    private String heroDead;

    // ---------------------------------------------------------
    // START GAME
    // ---------------------------------------------------------
    public void startGame(String name, String type, int difficulty) {

        this.playerType = type.toLowerCase();
        loadSpritePaths();

        // --- Facade + Observer wiring ---
        game = new GameFacade();
        CompositeObserver composite = new CompositeObserver(
                new ConsoleLogger("FXGame"),   // console (colored)
                new FxLogObserver(logBox)      // GUI log (effects only)
        );
        game.setObserver(composite);
        game.startNewGame(name, type, difficulty);

        // pull references from facade (read-only for GUI)
        this.player = game.getPlayer();
        this.enemy  = game.getEnemy();

        configureButtonsForClass();

        // ---------------------------------------------------------
        // POSITIONING — HP/Mana labels FOLLOW sprites (Option A)
        // ---------------------------------------------------------
        heroSprite.setLayoutX(200);
        heroSprite.setLayoutY(110);
        heroSprite.setFitHeight(190);
        heroSprite.setPreserveRatio(true);

        enemySprite.setLayoutX(550);
        enemySprite.setLayoutY(115);
        enemySprite.setFitHeight(150);
        enemySprite.setPreserveRatio(true);

        // HP labels bind to sprite positions
        heroHp.layoutXProperty().bind(heroSprite.layoutXProperty().add(10));
        heroHp.layoutYProperty().bind(heroSprite.layoutYProperty().subtract(35));

        enemyHp.layoutXProperty().bind(enemySprite.layoutXProperty().add(10));
        enemyHp.layoutYProperty().bind(enemySprite.layoutYProperty().subtract(35));

        // Mana label + bar anchored under hero HP (only visible for Mage)
        heroManaLabel.layoutXProperty().bind(heroHp.layoutXProperty());
        heroManaLabel.layoutYProperty().bind(heroHp.layoutYProperty().add(22));

        manaBarBg.layoutXProperty().bind(heroHp.layoutXProperty());
        manaBarBg.layoutYProperty().bind(heroHp.layoutYProperty().add(40));

        heroHealOverlay.setVisible(false);
        heroHealOverlay.setMouseTransparent(true);

        // ---------------------------------------------------------
        // PLAY IDLE ANIMATIONS
        // ---------------------------------------------------------
        if (playerType.equals("mage"))
            FXAnimationUtil.playMageIdle(heroSprite, idleHero, 200);
        else
            FXAnimationUtil.playWarriorIdle(heroSprite, idleHero, 200);

        FXAnimationUtil.playGoblinIdle(enemySprite, goblinIdle, 110);

        updateLabels();

        // GUI flavour text (not pattern logs)
        logSystem("⚔️  A wild Goblin appears!");
        logSystem("🎮  " + player.getName() + " the " + capitalize(playerType) + " enters the fray.");
    }

    // ---------------------------------------------------------
    // BUTTON CONFIG / ENABLE / DISABLE
    // ---------------------------------------------------------
    private void configureButtonsForClass() {
        if ("mage".equalsIgnoreCase(playerType)) {
            swordButton.setText("Staff Attack");
            fireballButton.setVisible(true);
            fireballButton.setManaged(true);
        } else {
            fireballButton.setVisible(false);
            fireballButton.setManaged(false);
        }
    }

    private void disableButtons() {
        swordButton.setDisable(true);
        fireballButton.setDisable(true);
        healButton.setDisable(true);
    }

    private void enableButtons() {
        if (battleOver) return;

        swordButton.setDisable(false);
        healButton.setDisable(false);
        if ("mage".equals(playerType)) {
            // Only enable Fireball if domain says we have enough mana
            fireballButton.setDisable(!player.canCastFireball());
        } else {
            fireballButton.setDisable(true);
        }
    }

    // ---------------------------------------------------------
    // LOAD SPRITE PATHS
    // ---------------------------------------------------------
    private void loadSpritePaths() {

        idleHero = playerType.equals("mage")
                ? "/edu/neu/csye7374/assets/sprites/mage_idle.png"
                : "/edu/neu/csye7374/assets/sprites/warrior_idle.png";

        attackHero = playerType.equals("mage")
                ? "/edu/neu/csye7374/assets/sprites/mage_staff_attack.png"
                : "/edu/neu/csye7374/assets/sprites/warrior_attack.png";

        fireballHero = "/edu/neu/csye7374/assets/sprites/mage_fireball_cast.png";
        healEffect   = "/edu/neu/csye7374/assets/effects/healing_aura.png";

        goblinIdle   = "/edu/neu/csye7374/assets/sprites/goblin_idle.png";
        goblinHurt   = "/edu/neu/csye7374/assets/sprites/goblin_hurt.png";
        goblinAttack = "/edu/neu/csye7374/assets/sprites/goblin_attack.png";
        goblinDeath  = "/edu/neu/csye7374/assets/sprites/goblin_death.png";

        warriorHurt  = "/edu/neu/csye7374/assets/sprites/warrior_hurt.png";
        mageHurt     = "/edu/neu/csye7374/assets/sprites/mage_hurt.png";

        heroDead     = playerType.equals("mage")
                ? "/edu/neu/csye7374/assets/sprites/mage_dead.png"
                : "/edu/neu/csye7374/assets/sprites/warrior_dead.png";
    }

    // ---------------------------------------------------------
    // HEAL BUTTON  (delegates to GameFacade)
    // ---------------------------------------------------------
    @FXML
    private void onHeal() {

        if (!readyForAction()) return;

        logPlayer("✨ " + player.getName() + " begins healing...");

        // Healing Aura Animation (purely visual)
        FXAnimationUtil.playHealingAura(
                heroSprite,
                heroHealOverlay,
                healEffect,
                battlePane
        );

        // Domain heal logic (Command) via Facade
        game.guiPlayerHeal();
        updateLabels();

        // Floating HP text (over HP label)
        spawnFloatingHealText(heroHp, "+12");

        logSystem("❤️  " + player.getName() +
                " heals to " + player.getHealth() + " HP.");

        if (checkBattleOutcome()) return;

        // Heal still allows goblin counter in this version
        scheduleGoblinCounter();
    }

    // ---------------------------------------------------------
    // FLOATING +HP TEXT
    // ---------------------------------------------------------
    private void spawnFloatingHealText(Label hpLabel, String text) {
        Text t = new Text(text);
        t.setStyle("-fx-fill: #66ff99; -fx-font-weight: bold; -fx-font-size: 20;");
        t.setLayoutX(hpLabel.getLayoutX() + 25);
        t.setLayoutY(hpLabel.getLayoutY() - 5);

        battlePane.getChildren().add(t);

        TranslateTransition rise = new TranslateTransition(Duration.millis(900), t);
        rise.setByY(-25);
        FadeTransition fade = new FadeTransition(Duration.millis(900), t);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(rise, fade);
        anim.setOnFinished(e -> battlePane.getChildren().remove(t));
        anim.play();
    }

    // ---------------------------------------------------------
    // BASIC CHECKS
    // ---------------------------------------------------------
    private boolean readyForAction() {
        return !battleOver && player.isAlive() && enemy.isAlive();
    }

    @FXML
    private void onSwordAttack() {
        if (!readyForAction()) return;
        handlePlayerAttack(false);
    }

    @FXML
    private void onFireball() {
        if (!readyForAction()) return;
        if (!"mage".equals(playerType)) return;
        if (!player.canCastFireball()) {
            // Safety: should already be disabled
            logSystem("💤 Not enough mana to cast Fireball!");
            return;
        }
        handlePlayerAttack(true);
    }

    // ---------------------------------------------------------
    // PLAYER ATTACK (all logic in GameFacade)
    // ---------------------------------------------------------
    private void handlePlayerAttack(boolean isFireball) {

        disableButtons();

        if ("mage".equals(playerType)) {

            if (isFireball) {
                logPlayer("🔥 " + player.getName() + " casts Fireball!");
                FXAnimationUtil.playMageFireball(heroSprite, fireballHero, idleHero);

                // Domain fireball + mana + punishment
                game.guiPlayerFireballAttack();

            } else {
                logPlayer("⚔️ " + player.getName() + " swings their staff!");
                FXAnimationUtil.playMageStaffAttack(heroSprite, attackHero, idleHero);

                // Domain staff attack
                game.guiPlayerSwordOrStaffAttack(true);
            }

        } else {
            logPlayer("⚔️ " + player.getName() + " swings their sword!");
            FXAnimationUtil.playWarriorAttack(heroSprite, attackHero, idleHero);

            // Domain warrior attack (Strategy + Decorator for crits)
            game.guiPlayerSwordOrStaffAttack(false);
        }

        updateLabels();

        // Goblin hurt animation (visual only)
        FXAnimationUtil.playGoblinHurt(enemySprite, goblinHurt, goblinIdle);

        if (checkBattleOutcome()) return;

        scheduleGoblinCounter();
    }

    // ---------------------------------------------------------
    // GOBLIN COUNTER (Facade handles attack logic)
    // ---------------------------------------------------------
    private void scheduleGoblinCounter() {
        PauseTransition p = new PauseTransition(Duration.millis(1300));
        p.setOnFinished(e -> goblinCounterAttack());
        p.play();
    }

    private void goblinCounterAttack() {

        if (battleOver || !player.isAlive() || !enemy.isAlive()) return;

        logEnemy("💢 Goblin counter-attacks!");

        FXAnimationUtil.playGoblinAttack(enemySprite, goblinAttack, goblinIdle);

        // Domain enemy attack logic
        game.guiEnemyAttack();
        updateLabels();

        if ("mage".equals(playerType))
            FXAnimationUtil.playMageHurt(heroSprite, mageHurt, idleHero);
        else
            FXAnimationUtil.playWarriorHurt(heroSprite, warriorHurt, idleHero);

        checkBattleOutcome();
    }

    // ---------------------------------------------------------
    // BATTLE END (auto-close game after dialog)
    // ---------------------------------------------------------
    private boolean checkBattleOutcome() {

        if (!enemy.isAlive()) {
            battleOver = true;
            disableButtons();
            logSystem("🏆 Goblin is defeated!");
            FXAnimationUtil.playGoblinDeath(enemySprite, goblinDeath);

            Platform.runLater(() ->
                    showAlert("Victory!", "You defeated the Goblin!")
            );

            return true;
        }

        if (!player.isAlive()) {
            battleOver = true;
            disableButtons();
            logSystem("☠️ " + player.getName() + " has fallen.");

            if ("mage".equals(playerType))
                FXAnimationUtil.playMageDeath(heroSprite, heroDead);
            else
                FXAnimationUtil.playWarriorDeath(heroSprite, heroDead);

            Platform.runLater(() ->
                    showAlert("Defeat...", player.getName() + " has fallen.")
            );

            return true;
        }

        enableButtons();
        return false;
    }

    // ---------------------------------------------------------
    // LABEL + MANA BAR UPDATES
    // ---------------------------------------------------------
    private void updateLabels() {
        heroHp.setText("HP: " + player.getHealth());
        enemyHp.setText("HP: " + enemy.getHealth());
        updateManaUI();
    }

    private void updateManaUI() {
        if (!"mage".equals(playerType)) {
            heroManaLabel.setVisible(false);
            manaBarBg.setVisible(false);
            return;
        }

        int mana = player.getMana();
        int maxMana = player.getMaxMana();

        heroManaLabel.setVisible(true);
        manaBarBg.setVisible(true);

        if (maxMana <= 0) {
            heroManaLabel.setText("Mana: 0/0");
            manaBarFill.setPrefWidth(0);
            fireballButton.setDisable(true);
            return;
        }

        heroManaLabel.setText("Mana: " + mana + "/" + maxMana);

        double ratio = (double) mana / maxMana;
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;

        double fullWidth = manaBarBg.getPrefWidth() > 0
                ? manaBarBg.getPrefWidth()
                : 140.0;
        manaBarFill.setPrefWidth(fullWidth * ratio);

        // Enable/disable Fireball based on domain rule
        fireballButton.setDisable(!player.canCastFireball());
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);

        // Block until the user dismisses the popup
        a.showAndWait();

        // After the dialog is closed, close the game window
        if (heroSprite != null && heroSprite.getScene() != null) {
            Stage stage = (Stage) heroSprite.getScene().getWindow();
            stage.close();
        }

        // Optional: also terminate the JavaFX application
        Platform.exit();
    }

    // ---------------------------------------------------------
    // LOGGING HELPERS (purely visual, domain logs come via FxLogObserver)
    // ---------------------------------------------------------
    private void logLine(String text, String colorHex) {
        Label line = new Label(text);
        line.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 14;");
        logBox.getChildren().add(line);
        if (logBox.getChildren().size() > 6)
            logBox.getChildren().remove(0);
    }

    private void logPlayer(String t) { logLine(t, "#ffeb3b"); }
    private void logEnemy(String t)  { logLine(t, "#ff7043"); }
    private void logSystem(String t) { logLine(t, "#b3e5fc"); }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
