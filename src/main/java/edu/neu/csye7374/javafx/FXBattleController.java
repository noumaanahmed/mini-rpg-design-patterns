package edu.neu.csye7374.javafx;

import edu.neu.csye7374.*;
import edu.neu.csye7374.Character;
import edu.neu.csye7374.engine.ActionResult;
import edu.neu.csye7374.engine.GameEngine;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FXBattleController {

    // ---------------------------------------------------------
    // FXML COMPONENTS
    // ---------------------------------------------------------
    @FXML private ImageView heroSprite;
    @FXML private ImageView heroHealOverlay;
    @FXML private ImageView enemySprite;

    @FXML private Label heroHp;
    @FXML private Label enemyHp;

    // HP BAR PANES (background + fill)
    @FXML private Pane heroHpBarBg;
    @FXML private Pane heroHpBarFill;
    @FXML private Pane enemyHpBarBg;
    @FXML private Pane enemyHpBarFill;

    // Mana UI
    @FXML private Label heroManaLabel;
    @FXML private Pane manaBarBg;
    @FXML private Pane manaBarFill;

    @FXML private Button swordButton;
    @FXML private Button fireballButton;
    @FXML private Button healButton;

    @FXML private ScrollPane logScroll;
    @FXML private VBox logBox;
    @FXML private Pane battlePane;

    // ---------------------------------------------------------
    // DOMAIN / GAME STATE
    // ---------------------------------------------------------
    private GameFacade game;
    private GameEngine engine;

    private Character player;
    private Character enemy;
    private String playerType;
    private boolean battleOver = false;
    private int difficultyLevel = 1;

    private int playerMaxHp;
    private int enemyMaxHp;

    // ---------------------------------------------------------
    // SPRITE PATHS
    // ---------------------------------------------------------
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

    // Screen flash overlays (damage + crit)
    private Rectangle damageFlashOverlay;
    private Rectangle critFlashOverlay;

    // ---------------------------------------------------------
    // START GAME (called by Character Select screen)
    // ---------------------------------------------------------
    public void startGame(String name, String type, int difficulty) {

        this.playerType = type.toLowerCase();
        this.difficultyLevel = difficulty;

        // APPLY GLOBAL RPG THEME
        Scene scene = heroSprite.getScene();
        if (scene != null) {
            String globalCss = "/edu/neu/csye7374/assets/global_theme.css";
            scene.getStylesheets().add(
                    getClass().getResource(globalCss).toExternalForm()
            );
        }

        // --- Facade + Observer wiring ---
        game = new GameFacade();
        CompositeObserver composite = new CompositeObserver(
                new ConsoleLogger("FXGame"),
                new FxLogObserver(logBox)
        );
        game.setObserver(composite);
        game.startNewGame(name, type, difficulty);

        // --- GameEngine wiring ---
        engine = new GameEngine(game);
        this.player = engine.getPlayer();
        this.enemy  = engine.getEnemy();

        // Store max HP for bar percentages (initial HP is treated as "max")
        this.playerMaxHp = player.getHealth();
        this.enemyMaxHp  = enemy.getHealth();

        loadSpritePaths();
        configureButtonsForClass();
        setupScreenFlashOverlays();

        // ---------------------------------------------------------
        // POSITIONING — HP/Mana labels & bars follow sprites
        // ---------------------------------------------------------
        heroSprite.setLayoutX(200);
        heroSprite.setLayoutY(110);
        heroSprite.setFitHeight(190);
        heroSprite.setPreserveRatio(true);

        enemySprite.setLayoutX(550);
        enemySprite.setLayoutY(115);
        enemySprite.setFitHeight(150);
        enemySprite.setPreserveRatio(true);

        // HP LABELS just show "HP"; positioned under HP bar
        heroHp.setText("HP");
        enemyHp.setText("HP");

        heroHp.layoutXProperty().bind(heroSprite.layoutXProperty().add(10));
        heroHp.layoutYProperty().bind(heroSprite.layoutYProperty().subtract(28));

        enemyHp.layoutXProperty().bind(enemySprite.layoutXProperty().add(10));
        enemyHp.layoutYProperty().bind(enemySprite.layoutYProperty().subtract(28));

        // HP BAR BACKGROUNDS ABOVE THE TEXT
        if (heroHpBarBg != null) {
            heroHpBarBg.layoutXProperty().bind(heroSprite.layoutXProperty().add(10));
            heroHpBarBg.layoutYProperty().bind(heroSprite.layoutYProperty().subtract(46));
        }
        if (enemyHpBarBg != null) {
            enemyHpBarBg.layoutXProperty().bind(enemySprite.layoutXProperty().add(10));
            enemyHpBarBg.layoutYProperty().bind(enemySprite.layoutYProperty().subtract(46));
        }

        // Mana label + bar anchored under hero HP (only visible for Mage)
        heroManaLabel.layoutXProperty().bind(heroHp.layoutXProperty());
        heroManaLabel.layoutYProperty().bind(heroHp.layoutYProperty().add(20));

        manaBarBg.layoutXProperty().bind(heroHp.layoutXProperty());
        manaBarBg.layoutYProperty().bind(heroHp.layoutYProperty().add(38));

        heroHealOverlay.setVisible(false);
        heroHealOverlay.setMouseTransparent(true);

        // ---------------------------------------------------------
        // DIFFICULTY-BASED GOBLIN SCALE
        // ---------------------------------------------------------
        double goblinScale = switch (difficultyLevel) {
            case 1 -> 1.4;  // Easy
            case 2 -> 1.8;  // Normal
            case 3 -> 2.3;  // Hard
            default -> 1.8;
        };
        FXAnimationUtil.setGoblinScale(goblinScale);

        // ---------------------------------------------------------
        // PLAY IDLE ANIMATIONS
        // ---------------------------------------------------------
        if (playerType.equals("mage")) {
            FXAnimationUtil.playMageIdle(heroSprite, idleHero, 200);
        } else {
            FXAnimationUtil.playWarriorIdle(heroSprite, idleHero, 200);
        }

        FXAnimationUtil.playGoblinIdle(enemySprite, goblinIdle, 110);

        updateLabels();

        // GUI flavour text (not pattern logs)
        logSystem("⚔️  A wild Goblin appears!");
        logSystem("🎮  " + player.getName() + " the " + capitalize(playerType) +
                  " enters the fray (Difficulty: " + difficultyLevel + ").");

        // ---------------------------------------------------------
        // LOAD CSS FOR LOG WINDOW (ScrollPane transparency fix)
        // ---------------------------------------------------------
        Platform.runLater(() -> {
            if (battlePane.getScene() != null) {
                battlePane.getScene().getStylesheets().add(
                        getClass().getResource("/edu/neu/csye7374/assets/battle.css").toExternalForm()
                );
            }
        });
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
            // B2: once mana hits 0, button is disabled — no spammy message
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
    // SCREEN FLASH OVERLAYS (damage + critical)
    // ---------------------------------------------------------
    private void setupScreenFlashOverlays() {
        damageFlashOverlay = new Rectangle();
        damageFlashOverlay.setFill(Color.color(1, 0, 0, 0.45)); // red
        damageFlashOverlay.setOpacity(0);
        damageFlashOverlay.widthProperty().bind(battlePane.widthProperty());
        damageFlashOverlay.heightProperty().bind(battlePane.heightProperty());

        critFlashOverlay = new Rectangle();
        critFlashOverlay.setFill(Color.color(1, 1, 1, 0.85)); // white
        critFlashOverlay.setOpacity(0);
        critFlashOverlay.widthProperty().bind(battlePane.widthProperty());
        critFlashOverlay.heightProperty().bind(battlePane.heightProperty());

        battlePane.getChildren().addAll(damageFlashOverlay, critFlashOverlay);
    }

    private void playDamageFlash() {
        if (damageFlashOverlay == null) return;
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, e -> damageFlashOverlay.setOpacity(1.0)),
                new KeyFrame(Duration.millis(150), e -> damageFlashOverlay.setOpacity(0.0))
        );
        t.play();
    }

    private void playCriticalFlash() {
        if (critFlashOverlay == null) return;
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, e -> critFlashOverlay.setOpacity(1.0)),
                new KeyFrame(Duration.millis(220), e -> critFlashOverlay.setOpacity(0.0))
        );
        t.play();
    }

    private void flashSpriteWhite(ImageView sprite) {
        if (sprite == null) return;

        ColorAdjust colorAdjust = new ColorAdjust();
        sprite.setEffect(colorAdjust);

        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> colorAdjust.setBrightness(1.0)),
                new KeyFrame(Duration.millis(120), e -> colorAdjust.setBrightness(0.0))
        );

        flash.setOnFinished(e -> sprite.setEffect(null));
        flash.play();
    }

    private void flashSpriteGreen(ImageView sprite) {
        if (sprite == null) return;

        ColorAdjust colorAdjust = new ColorAdjust();
        sprite.setEffect(colorAdjust);

        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> colorAdjust.setHue(0.4)),  // green
                new KeyFrame(Duration.millis(180), e -> colorAdjust.setHue(0.0))
        );

        flash.setOnFinished(e -> sprite.setEffect(null));
        flash.play();
    }

    // ---------------------------------------------------------
    // HEAL BUTTON  (via GameEngine)
    // ---------------------------------------------------------
    @FXML
    private void onHeal() {
        if (!readyForAction()) return;

        disableButtons();
        logPlayer("✨ " + player.getName() + " begins healing...");

        // Healing Aura Animation (purely visual)
        FXAnimationUtil.playHealingAura(
                heroSprite,
                heroHealOverlay,
                healEffect,
                battlePane
        );
        flashSpriteGreen(heroSprite);

        // Domain heal via engine
        ActionResult result = engine.handlePlayerAction(GameEngine.PlayerAction.HEAL);

        // Update HP / mana UI
        updateLabels();

        // Floating HP text with actual heal amount
        int healed = result.getValue();
        if (healed > 0) {
            spawnFloatingHealText(heroHp, "+" + healed);
        }

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
        rise.setFromY(0);
        rise.setToY(-25);

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
        return !battleOver &&
               engine != null &&
               player != null && enemy != null &&
               player.isAlive() && enemy.isAlive();
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

        // B2: if button is disabled because of mana, just ignore.
        if (fireballButton.isDisabled()) {
            return;
        }

        handlePlayerAttack(true);
    }

    // ---------------------------------------------------------
    // PLAYER ATTACK (via GameEngine + ActionResult)
    // ---------------------------------------------------------
    private void handlePlayerAttack(boolean isFireball) {

        disableButtons();

        GameEngine.PlayerAction action =
                isFireball ? GameEngine.PlayerAction.FIREBALL : GameEngine.PlayerAction.BASIC_ATTACK;

        if ("mage".equals(playerType)) {
            if (isFireball) {
                logPlayer("🔥 " + player.getName() + " casts Fireball!");
                FXAnimationUtil.playMageFireball(heroSprite, fireballHero, idleHero);
            } else {
                logPlayer("⚔️ " + player.getName() + " swings their staff!");
                FXAnimationUtil.playMageStaffAttack(heroSprite, attackHero, idleHero);
            }
        } else {
            logPlayer("⚔️ " + player.getName() + " swings their sword!");
            FXAnimationUtil.playWarriorAttack(heroSprite, attackHero, idleHero);
        }

        // --- Domain logic via engine ---
        ActionResult result = engine.handlePlayerAction(action);

        // Use engine’s knowledge of crits to trigger FX
        if (result.isCritical()) {
            playCriticalFlash();
        }

        // Goblin took damage → red flash + hurt animation
        if (result.getValue() > 0 &&
            (result.getType() == ActionResult.ActionType.ATTACK ||
             result.getType() == ActionResult.ActionType.FIREBALL ||
             result.getType() == ActionResult.ActionType.ENEMY_DEATH)) {

            playDamageFlash();
            FXAnimationUtil.playGoblinHurt(enemySprite, goblinHurt, goblinIdle);
            flashSpriteWhite(enemySprite);
        }

        // Update HP & mana
        updateLabels();

        if (checkBattleOutcome()) return;

        scheduleGoblinCounter();
    }

    // ---------------------------------------------------------
    // GOBLIN COUNTER (via GameEngine)
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

        // Domain enemy attack logic via engine
        ActionResult result = engine.handleEnemyAttack();

        // Hero took damage → red flash & hurt animation
        if (result.getValue() > 0 &&
            (result.getType() == ActionResult.ActionType.ENEMY_ATTACK ||
             result.getType() == ActionResult.ActionType.PLAYER_DEATH)) {

            playDamageFlash();

            if ("mage".equals(playerType))
                FXAnimationUtil.playMageHurt(heroSprite, mageHurt, idleHero);
            else
                FXAnimationUtil.playWarriorHurt(heroSprite, warriorHurt, idleHero);

            flashSpriteWhite(heroSprite);
        }

        updateLabels();
        checkBattleOutcome();
    }

    // ---------------------------------------------------------
    // BATTLE END → CUSTOM ANIMATED POPUP
    // ---------------------------------------------------------
    private boolean checkBattleOutcome() {

        if (!enemy.isAlive()) {
            battleOver = true;
            disableButtons();
            logSystem("🏆 Goblin is defeated!");
            FXAnimationUtil.playGoblinDeath(enemySprite, goblinDeath);

            // Wait for death animation, then show overlay
            PauseTransition p = new PauseTransition(Duration.millis(900));
            p.setOnFinished(e -> showGameOverOverlay(true));
            p.play();
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

            PauseTransition p = new PauseTransition(Duration.millis(900));
            p.setOnFinished(e -> showGameOverOverlay(false));
            p.play();
            return true;
        }

        enableButtons();
        return false;
    }

    private void showGameOverOverlay(boolean victory) {
        // Semi-transparent dark layer
        Pane overlay = new Pane();
        overlay.setPrefSize(battlePane.getWidth(), battlePane.getHeight());
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);");

        VBox box = new VBox(10);
        box.setStyle(
                "-fx-background-color: rgba(20,20,40,0.95);" +
                "-fx-background-radius: 18;" +
                "-fx-padding: 18;"
        );
        box.setLayoutX(battlePane.getWidth() / 2.0 - 140);
        box.setLayoutY(battlePane.getHeight() / 2.0 - 70);

        Label title = new Label(victory ? "Victory!" : "Defeat...");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold;");

        Label msg = new Label(
                victory ? "You have defeated the Goblin." :
                        player.getName() + " has fallen in battle."
        );
        msg.setStyle("-fx-text-fill: #b0bec5; -fx-font-size: 14;");

        Button exit = new Button("Exit Game");
        exit.setStyle(
                "-fx-font-size: 16; -fx-padding: 6 18;" +
                "-fx-background-radius: 12;"
        );
        exit.setOnAction(e -> exitGame());

        box.getChildren().addAll(title, msg, exit);
        overlay.getChildren().add(box);
        battlePane.getChildren().add(overlay);

        // Fade + scale animation
        overlay.setOpacity(0);
        box.setScaleX(0.7);
        box.setScaleY(0.7);

        FadeTransition fade = new FadeTransition(Duration.millis(280), overlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(280), box);
        scale.setFromX(0.7);
        scale.setFromY(0.7);
        scale.setToX(1.0);
        scale.setToY(1.0);

        new ParallelTransition(fade, scale).play();
    }

    private void exitGame() {
        if (battlePane.getScene() != null) {
            Stage stage = (Stage) battlePane.getScene().getWindow();
            stage.close();
        }
        Platform.exit();
    }

    // ---------------------------------------------------------
    // LABEL + MANA BAR UPDATES
    // ---------------------------------------------------------
    private void updateLabels() {
        updateHpBars();
        updateManaUI();
    }

    private void updateHpBars() {
        if (player != null && heroHpBarBg != null && heroHpBarFill != null && playerMaxHp > 0) {
            double ratio = (double) player.getHealth() / playerMaxHp;
            if (ratio < 0) ratio = 0;
            if (ratio > 1) ratio = 1;

            double fullWidth = heroHpBarBg.getPrefWidth() > 0 ? heroHpBarBg.getPrefWidth() : 140.0;
            heroHpBarFill.setPrefWidth(fullWidth * ratio);

            String color = (ratio <= 0.25) ? "#e53935" : "#fb8c00";  // red / orange
            heroHpBarFill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        }

        if (enemy != null && enemyHpBarBg != null && enemyHpBarFill != null && enemyMaxHp > 0) {
            double ratio = (double) enemy.getHealth() / enemyMaxHp;
            if (ratio < 0) ratio = 0;
            if (ratio > 1) ratio = 1;

            double fullWidth = enemyHpBarBg.getPrefWidth() > 0 ? enemyHpBarBg.getPrefWidth() : 140.0;
            enemyHpBarFill.setPrefWidth(fullWidth * ratio);

            String color = (ratio <= 0.25) ? "#e53935" : "#fb8c00";
            enemyHpBarFill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        }

        // Just show label "HP" above bars (numbers are already in console logs)
        heroHp.setText("HP");
        enemyHp.setText("HP");
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

        fireballButton.setDisable(!player.canCastFireball());
    }

    // ---------------------------------------------------------
    // LOGGING HELPERS (visual logs with animation)
    // ---------------------------------------------------------
    private void logLine(String text, String colorHex) {

        Label line = new Label(text);

        // Base styling (white, bold, shadow)
        String baseStyle = """
            -fx-font-size: 14;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.9), 4, 0, 0, 0);
        """;

        // Apply white base, then override text color
        line.setStyle(baseStyle + "-fx-text-fill: " + colorHex + ";");

        logBox.getChildren().add(line);

        // --- Fade-in animation ---
        FadeTransition fade = new FadeTransition(Duration.millis(220), line);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // --- Slide-up animation ---
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), line);
        slide.setFromY(6);
        slide.setToY(0);
        slide.play();

        // --- Smooth auto-scroll ---
        Platform.runLater(() -> {
            if (logScroll == null) return;
            Timeline scrollAnim = new Timeline(
                    new KeyFrame(
                            Duration.millis(180),
                            new KeyValue(logScroll.vvalueProperty(), 1.0, Interpolator.EASE_BOTH)
                    )
            );
            scrollAnim.play();
        });
    }

    private void logPlayer(String t) { logLine(t, "#ffeb3b"); }
    private void logEnemy(String t)  { logLine(t, "#ff7043"); }
    private void logSystem(String t) { logLine(t, "#b3e5fc"); }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
