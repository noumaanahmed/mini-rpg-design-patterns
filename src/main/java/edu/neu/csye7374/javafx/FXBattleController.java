package edu.neu.csye7374.javafx;

import edu.neu.csye7374.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// IMPORTANT: make sure this import points to your Character class
import edu.neu.csye7374.Character;

public class FXBattleController {

    // ----------------- FXML WIRES -----------------
    @FXML private ImageView heroSprite;
    @FXML private ImageView heroHealOverlay;   // overlay for heal effect (on top of hero)
    @FXML private ImageView enemySprite;

    @FXML private Label heroHp;
    @FXML private Label enemyHp;

    @FXML private Button swordButton;          // generic attack (sword / staff)
    @FXML private Button fireballButton;       // mage-only fireball
    @FXML private Button healButton;

    @FXML private VBox logBox;                 // holds colored battle log labels

    // ----------------- GAME STATE -----------------
    private Character player;
    private Character enemy;
    private String playerType;                 // "warrior" or "mage"
    private boolean battleOver = false;

    // Sprite paths (classpath paths)
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

    // ----------------- ENTRY POINT FROM CHARACTER SELECT -----------------
    public void startGame(String name, String type, int difficulty) {

        GameConfig.getInstance().setDifficulty(difficulty);
        this.playerType = type.toLowerCase();

        // 1) Resolve sprite paths based on class
        loadSpritePaths();

        // 2) Build domain objects (uses your Design Patterns code)
        player = CharacterFactory.createCharacter(playerType, name);
        // start in aggressive mode
        player.setStrategy(new AggressiveAttack());

        enemy = new CharacterBuilder()
                .setName("Goblin")
                .setHealth(switch (difficulty) {
                    case 1 -> 50;
                    case 2 -> 80;
                    case 3 -> 120;
                    default -> 80;
                })
                .build();

        // Attach observers (Observer pattern)
        player.addObserver(new ConsoleLogger("FXLogger-Player"));
        enemy.addObserver(new ConsoleLogger("FXLogger-Goblin"));

        // 3) Configure buttons based on class
        configureButtonsForClass();

        // 4) Sprites & idle animations
        // HERO
        heroSprite.setPreserveRatio(true);
        heroSprite.setFitWidth(170);

        // Shift hero up a bit so they don't overlap the log area
        double heroY = heroSprite.getLayoutY();
        heroSprite.setLayoutY(heroY - 25);
        // keep HP label above hero
        heroHp.setLayoutY(heroSprite.getLayoutY() - 35);

        int idleSpeed = 200; // ms per frame – calm loop
        if (playerType.equals("mage")) {
            FXAnimationUtil.playMageIdle(heroSprite, idleHero, idleSpeed);
        } else {
            FXAnimationUtil.playWarriorIdle(heroSprite, idleHero, idleSpeed);
        }

        // GOBLIN – use idle sheet player
        enemySprite.setPreserveRatio(true);
        enemySprite.setFitWidth(170);
        enemySprite.setScaleX(-1); // face hero
        FXAnimationUtil.playGoblinIdle(enemySprite, goblinIdle, 130);

        // Heal overlay initially invisible
        heroHealOverlay.setVisible(false);
        heroHealOverlay.setMouseTransparent(true);

        // 5) Initial UI state
        updateLabels();
        logSystem("⚔️  A wild Goblin appears!");
        logSystem("🎮  " + player.getName() + " the " + capitalize(playerType) + " enters the fray.");
    }

    // ----------------- BUTTON CONFIG -----------------
    private void configureButtonsForClass() {
        if (playerType.equals("mage")) {
            swordButton.setText("Staff Attack");
            fireballButton.setText("Fireball");
            fireballButton.setManaged(true);
            fireballButton.setVisible(true);
        } else {
            swordButton.setText("Sword Attack");
            fireballButton.setManaged(false);
            fireballButton.setVisible(false);
        }
    }

    private void disableButtons() {
        swordButton.setDisable(true);
        fireballButton.setDisable(true);
        healButton.setDisable(true);
    }

    // ----------------- RESOURCE HELPERS -----------------
    private void loadSpritePaths() {
        // Hero idle / attacks
        idleHero = playerType.equals("mage")
                ? "/edu/neu/csye7374/assets/sprites/mage_idle.png"
                : "/edu/neu/csye7374/assets/sprites/warrior_idle.png";

        attackHero = playerType.equals("mage")
                ? "/edu/neu/csye7374/assets/sprites/mage_staff_attack.png"
                : "/edu/neu/csye7374/assets/sprites/warrior_attack.png";

        fireballHero = "/edu/neu/csye7374/assets/sprites/mage_fireball_cast.png";
        healEffect   = "/edu/neu/csye7374/assets/effects/healing_aura.png";

        // Goblin sprites
        goblinIdle   = "/edu/neu/csye7374/assets/sprites/goblin_idle.png";
        goblinHurt   = "/edu/neu/csye7374/assets/sprites/goblin_hurt.png";
        goblinAttack = "/edu/neu/csye7374/assets/sprites/goblin_attack.png";
        goblinDeath  = "/edu/neu/csye7374/assets/sprites/goblin_death.png";

        // Hero hurt / death
        warriorHurt  = "/edu/neu/csye7374/assets/sprites/warrior_hurt.png";
        mageHurt     = "/edu/neu/csye7374/assets/sprites/mage_hurt.png";
        heroDead     = playerType.equals("mage")
                ? "/edu/neu/csye7374/assets/sprites/mage_dead.png"
                : "/edu/neu/csye7374/assets/sprites/warrior_dead.png";
    }

    // ----------------- UI UTILITIES -----------------
    private void updateLabels() {
        heroHp.setText("HP: " + player.getHealth());
        enemyHp.setText("HP: " + enemy.getHealth());
    }

    private void logLine(String text, String colorHex) {
        Label line = new Label(text);
        line.setStyle(
                "-fx-text-fill: " + colorHex + ";" +
                "-fx-font-size: 14;" +
                "-fx-font-weight: bold;"
        );
        logBox.getChildren().add(line);
        if (logBox.getChildren().size() > 6) {
            logBox.getChildren().remove(0);
        }
    }

    private void logPlayer(String text) {
        logLine(text, "#ffeb3b"); // yellow
    }

    private void logEnemy(String text) {
        logLine(text, "#ff7043"); // orange-red
    }

    private void logSystem(String text) {
        logLine(text, "#b3e5fc"); // light blue
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ----------------- BUTTON HANDLERS -----------------

    @FXML
    private void onSwordAttack() {
        if (battleOver) return;
        handlePlayerAttack(false);
    }

    @FXML
    private void onFireball() {
        if (battleOver) return;
        if (!playerType.equals("mage")) return;
        handlePlayerAttack(true);
    }

    @FXML
    private void onHeal() {
        if (battleOver) return;

        logPlayer("✨ " + player.getName() + " focuses and begins to heal...");

        // Use shared util to position aura at the feet correctly
        FXAnimationUtil.playHealOverlay(heroSprite, heroHealOverlay, healEffect);

        // Domain logic – Character.heal handles everything and notifies observers
        player.heal(12);
        updateLabels();
        logSystem("❤️  " + player.getName() + " heals to " + player.getHealth() + " HP.");

        if (checkBattleOutcome()) return;

        // Goblin counter-attacks after heal
        scheduleGoblinCounterAttack();
    }

    // ----------------- ATTACK FLOW -----------------

    private void handlePlayerAttack(boolean isFireball) {
        if (battleOver) return;
        if (!player.isAlive() || !enemy.isAlive()) return;

        if (playerType.equals("mage")) {
            if (isFireball) {
                logPlayer("🔥 " + player.getName() + " prepares a FIREBALL!");
                player.setStrategy(new FireballAttack());
                FXAnimationUtil.playMageFireballAttack(heroSprite, fireballHero, idleHero);
            } else {
                logPlayer("⚔️  " + player.getName() + " swings their staff!");
                player.setStrategy(new AggressiveAttack());
                FXAnimationUtil.playMageStaffAttack(heroSprite, attackHero, idleHero);
            }
        } else {
            logPlayer("⚔️  " + player.getName() + " swings their sword!");
            player.setStrategy(new AggressiveAttack());
            FXAnimationUtil.playWarriorAttack(heroSprite, attackHero, idleHero);
        }

        // Domain logic – Strategy + Character does the damage
        player.attack(enemy);

        // Goblin hurt sequence
        FXAnimationUtil.playGoblinHurt(enemySprite, goblinHurt, goblinIdle);

        updateLabels();
        if (checkBattleOutcome()) return;

        scheduleGoblinCounterAttack();
    }

    private void scheduleGoblinCounterAttack() {
        PauseTransition pause = new PauseTransition(Duration.millis(650));
        pause.setOnFinished(e -> goblinCounterAttack());
        pause.play();
    }

    private void goblinCounterAttack() {
        if (battleOver) return;
        if (!enemy.isAlive() || !player.isAlive()) return;

        logEnemy("💢  Goblin counter-attacks!");

        FXAnimationUtil.playGoblinAttack(enemySprite, goblinAttack, goblinIdle);

        enemy.setStrategy(new AggressiveAttack());
        enemy.attack(player);

        if (playerType.equals("mage")) {
            FXAnimationUtil.playMageHurt(heroSprite, mageHurt, idleHero);
        } else {
            FXAnimationUtil.playWarriorHurt(heroSprite, warriorHurt, idleHero);
        }

        updateLabels();
        checkBattleOutcome();
    }

    // ----------------- ENDING / POPUPS -----------------

    private boolean checkBattleOutcome() {

        if (enemy.getHealth() <= 0 || !enemy.isAlive()) {
            battleOver = true;
            disableButtons();
            logSystem("🏆  Goblin is defeated!");

            FXAnimationUtil.playGoblinDeath(enemySprite, goblinDeath);

            Platform.runLater(() -> showResultAlert(
                    "Victory!",
                    "You defeated the Goblin!\n\n" + player.getName() + " survives the battle."
            ));
            return true;
        }

        if (player.getHealth() <= 0 || !player.isAlive()) {
            battleOver = true;
            disableButtons();
            logSystem("☠️  " + player.getName() + " has fallen...");

            if (playerType.equals("mage")) {
                FXAnimationUtil.playMageDeath(heroSprite, heroDead);
            } else {
                FXAnimationUtil.playWarriorDeath(heroSprite, heroDead);
            }

            Platform.runLater(() -> showResultAlert(
                    "Defeat...",
                    player.getName() + " was slain by the Goblin."
            ));
            return true;
        }

        return false;
    }

    private void showResultAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
