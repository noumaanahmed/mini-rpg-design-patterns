package edu.neu.csye7374;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Swing-based GUI for the mini RPG game.
 *
 * Demonstrates several design patterns via a graphical front-end:
 * - Singleton: GameConfig
 * - Factory + Builder: CharacterFactory, CharacterBuilder
 * - Strategy: AggressiveAttack, DefensiveAttack (mode: Attack vs Heal)
 * - Command: CommandInvoker with Commands wrapping player actions
 * - Observer: GameObserver (ConsoleLogger + TextAreaObserverAdapter)
 * - Adapter: TextAreaObserverAdapter adapts GameObserver to JTextArea
 *
 * NEW:
 * - Warrior vs Mage differentiation
 * - Mage has mana and can choose Staff Attack or Fireball (popup)
 * - If Mage has no mana for Fireball, they get punished
 * - Heal action does NOT trigger enemy attack
 */
public class GameGUI extends JFrame {

    // CardLayout for simple "scene transitions"
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);

    // Scenes
    private JPanel menuPanel;
    private JPanel createPanel;
    private JPanel battlePanel;

    // Shared log area (console text in GUI)
    private JTextArea logArea;

    // Battle screen components
    private JLabel heroSpriteLabel;
    private JLabel heroHpLabel;
    private JLabel goblinSpriteLabel;
    private JLabel goblinHpLabel;
    private JLabel strategyLabel;
    private JButton actionButton;         // Attack / Heal depending on strategy
    private JButton changeStrategyButton;

    // Domain objects
    private Character player;
    private Character enemy;
    private CommandInvoker invoker;

    // Observers
    private ConsoleLogger consoleLogger;
    private TextAreaObserverAdapter guiLogger;

    // Animation timer
    private Timer attackTimer;
    private int originalHeroX;
    private int heroOffset = 0;
    private boolean movingForward = true;

    // Player class & mana (tracked outside Character)
    private boolean playerIsMage;
    private int playerMana;
    private int playerMaxMana;

    // Heal behavior flag
    private boolean lastActionWasHeal = false;

    private final Random rand = new Random();

    public GameGUI() {
        super("Mini RPG - Design Patterns GUI");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        buildMenuScene();
        buildCreateScene();
        buildBattleScene();

        getContentPane().add(rootPanel);
        cardLayout.show(rootPanel, "menu");
    }

    // ==========================
    // Scene builders
    // ==========================

    private void buildMenuScene() {
        menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Mini RPG – Design Patterns", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));

        JButton startBtn = new JButton("Start New Game");
        JButton exitBtn = new JButton("Exit");

        JPanel btnPanel = new JPanel();
        btnPanel.add(startBtn);
        btnPanel.add(exitBtn);

        menuPanel.add(title, BorderLayout.CENTER);
        menuPanel.add(btnPanel, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> cardLayout.show(rootPanel, "create"));
        exitBtn.addActionListener(e -> dispose());

        rootPanel.add(menuPanel, "menu");
    }

    private void buildCreateScene() {
        createPanel = new JPanel(new BorderLayout());
        createPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Character Creation", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        createPanel.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));

        JTextField nameField = new JTextField("Hero");

        String[] classes = {"Warrior", "Mage"};
        JComboBox<String> classBox = new JComboBox<>(classes);

        String[] difficulties = {"Easy (1)", "Normal (2)", "Hard (3)"};
        JComboBox<String> diffBox = new JComboBox<>(difficulties);

        String[] strategies = {"Aggressive (Attack)", "Defensive (Heal)"};
        JComboBox<String> stratBox = new JComboBox<>(strategies);

        form.add(new JLabel("Name:"));
        form.add(nameField);

        form.add(new JLabel("Class:"));
        form.add(classBox);

        form.add(new JLabel("Difficulty:"));
        form.add(diffBox);

        form.add(new JLabel("Starting Strategy:"));
        form.add(stratBox);

        createPanel.add(form, BorderLayout.CENTER);

        JButton backBtn = new JButton("Back");
        JButton startBtn = new JButton("Begin Battle");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(backBtn);
        bottom.add(startBtn);

        backBtn.addActionListener(e -> cardLayout.show(rootPanel, "menu"));
        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) name = "Hero";

            int diff = diffBox.getSelectedIndex() + 1; // 1..3
            boolean aggressiveStart = stratBox.getSelectedIndex() == 0;
            String type = ((String) classBox.getSelectedItem()).toLowerCase();

            setupGame(name, type, diff, aggressiveStart);
            cardLayout.show(rootPanel, "battle");
        });

        createPanel.add(bottom, BorderLayout.SOUTH);

        rootPanel.add(createPanel, "create");
    }

    private void buildBattleScene() {
        battlePanel = new JPanel(new BorderLayout());
        battlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top label
        JLabel topLabel = new JLabel("Battle Scene", SwingConstants.CENTER);
        topLabel.setFont(topLabel.getFont().deriveFont(Font.BOLD, 20f));
        battlePanel.add(topLabel, BorderLayout.NORTH);

        // Center: hero vs goblin "sprites"
        JPanel center = new JPanel(null); // absolute layout for simple animation
        center.setPreferredSize(new Dimension(800, 250));

        heroSpriteLabel = new JLabel("HERO", SwingConstants.CENTER);
        heroSpriteLabel.setOpaque(true);
        heroSpriteLabel.setBackground(new Color(0xCCE5FF));
        heroSpriteLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        heroSpriteLabel.setBounds(100, 80, 120, 80);

        goblinSpriteLabel = new JLabel("GOBLIN", SwingConstants.CENTER);
        goblinSpriteLabel.setOpaque(true);
        goblinSpriteLabel.setBackground(new Color(0xFFCCCC));
        goblinSpriteLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        goblinSpriteLabel.setBounds(580, 80, 120, 80);

        heroHpLabel = new JLabel("Hero HP: 100", SwingConstants.CENTER);
        heroHpLabel.setBounds(100, 50, 200, 20);

        goblinHpLabel = new JLabel("Goblin HP: 80", SwingConstants.CENTER);
        goblinHpLabel.setBounds(580, 50, 120, 20);

        center.add(heroSpriteLabel);
        center.add(goblinSpriteLabel);
        center.add(heroHpLabel);
        center.add(goblinHpLabel);

        battlePanel.add(center, BorderLayout.CENTER);

        // Right: controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        strategyLabel = new JLabel("Strategy: [None]");
        actionButton = new JButton("Action");
        changeStrategyButton = new JButton("Change Strategy");
        JButton backToMenuBtn = new JButton("Back to Menu");

        rightPanel.add(strategyLabel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(actionButton);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(changeStrategyButton);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(backToMenuBtn);

        battlePanel.add(rightPanel, BorderLayout.EAST);

        // Bottom: log area
        logArea = new JTextArea(8, 60);
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Battle Log / Design Pattern Events"));
        battlePanel.add(scroll, BorderLayout.SOUTH);

        // Button actions
        actionButton.addActionListener(e -> onPlayerAction());
        changeStrategyButton.addActionListener(e -> onChangeStrategy());
        backToMenuBtn.addActionListener(e -> {
            // allow restarting a new game
            cardLayout.show(rootPanel, "menu");
        });

        rootPanel.add(battlePanel, "battle");
    }

    // ==========================
    // Game setup and logic
    // ==========================

    private void setupGame(String name, String type, int difficulty, boolean aggressiveStart) {
        logArea.setText("");

        // Singleton: store difficulty
        GameConfig config = GameConfig.getInstance();
        config.setDifficulty(difficulty);

        // Enemy HP scaling (same as console)
        int goblinHP;
        switch (difficulty) {
            case 1:
                goblinHP = 50;
                break;
            case 2:
                goblinHP = 80;
                break;
            case 3:
                goblinHP = 120;
                break;
            default:
                goblinHP = 80;
        }

        // Factory + Builder for player
        player = CharacterFactory.createCharacter(type, name);

        // Determine class
        playerIsMage = "mage".equalsIgnoreCase(type);
        if (playerIsMage) {
            playerMana = 40;
            playerMaxMana = 40;
        } else {
            playerMana = 0;
            playerMaxMana = 0;
        }

        // Builder for enemy
        CharacterBuilder enemyBuilder = new CharacterBuilder()
                .setName("Goblin")
                .setHealth(goblinHP);
        enemy = enemyBuilder.build();

        // Observers: console + GUI
        consoleLogger = new ConsoleLogger("GameLogger");
        guiLogger = new TextAreaObserverAdapter(logArea);

        player.addObserver(consoleLogger);
        player.addObserver(guiLogger);
        enemy.addObserver(consoleLogger);
        enemy.addObserver(guiLogger);

        // Strategy for player: controls whether Action = Attack or Heal
        if (aggressiveStart) {
            player.setStrategy(new AggressiveAttack());
        } else {
            player.setStrategy(new DefensiveAttack());
        }
        // Enemy always aggressive
        enemy.setStrategy(new AggressiveAttack());

        // Command invoker
        invoker = new CommandInvoker();
        lastActionWasHeal = false;

        updateStrategyLabel();
        updateHpLabels();

        logDesignEvent("[Factory + Builder] Created player '" + player.getName()
                + "' (" + (playerIsMage ? "Mage" : "Warrior") + ") and enemy 'Goblin' with HP " + goblinHP);
        if (playerIsMage) {
            logDesignEvent("[Resource] Mage starts with mana: " + playerMana + "/" + playerMaxMana);
        }
        logDesignEvent("[Strategy] Starting strategy: " + playerStrategyName());
        logDesignEvent("[Singleton] Difficulty set to: " + difficulty + " (via GameConfig)");
    }

    private void onPlayerAction() {
        if (player == null || enemy == null) return;
        if (!player.isAlive() || !enemy.isAlive()) return;

        boolean isAggressive = player.getStrategy() instanceof AggressiveAttack;

        invoker = new CommandInvoker();

        if (isAggressive) {
            lastActionWasHeal = false;
            // Command wraps the entire attack decision (staff vs fireball, etc.)
            Command attackCmd = () -> handleGuiPlayerAttack();
            invoker.addCommand(attackCmd);
            logDesignEvent("[Command] Player queued an Attack command");
        } else {
            lastActionWasHeal = true;
            Command healCmd = () -> handleGuiPlayerHeal();
            invoker.addCommand(healCmd);
            logDesignEvent("[Command] Player queued a Heal command");
        }

        invoker.executeAll();
        updateHpLabels();
        checkGameOver();
        if (!enemy.isAlive() || !player.isAlive()) return;

        // Enemy turn ONLY if last action was NOT heal
        if (!lastActionWasHeal) {
            enemyTurn();
        }
    }

    private void enemyTurn() {
        if (!enemy.isAlive() || !player.isAlive()) return;

        logDesignEvent("[State] Enemy turn begins");
        // Enemy always aggressive
        enemy.setStrategy(new AggressiveAttack());
        enemy.attack(player);
        playAttackAnimation(false);
        updateHpLabels();
        checkGameOver();
        logDesignEvent("[State] Player turn begins again");
    }

    private void onChangeStrategy() {
        if (player == null) return;
        if (player.getStrategy() instanceof AggressiveAttack) {
            player.setStrategy(new DefensiveAttack());
        } else {
            player.setStrategy(new AggressiveAttack());
        }
        updateStrategyLabel();
        logDesignEvent("[Strategy] Player strategy switched to: " + playerStrategyName());
    }

    private void handleGuiPlayerAttack() {
        if (!player.isAlive() || !enemy.isAlive()) return;

        if (playerIsMage) {
            String[] options = {"Staff Attack", "Cast Fireball"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Choose your attack:",
                    "Mage Attack",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice != 1) {
                // Staff attack (default or closed dialog)
                int dmg = randomBetween(6, 12);
                enemy.takeDamage(dmg);
                player.notifyObservers(player.getName()
                        + " strikes the Goblin with staff for " + dmg + " damage!");
                playAttackAnimation(true);
            } else {
                // Fireball
                int cost = 10;
                if (playerMana >= cost) {
                    playerMana -= cost;
                    int dmg = randomBetween(18, 25);
                    enemy.takeDamage(dmg);
                    player.notifyObservers(player.getName()
                            + " casts Fireball for " + dmg + " damage! (Mana: "
                            + playerMana + "/" + playerMaxMana + ")");
                    playAttackAnimation(true);
                } else {
                    player.notifyObservers("Not enough mana to cast Fireball! The spell fizzles and the Goblin punishes you!");
                    // Immediate punishment hit
                    int counter = randomBetween(8, 15);
                    player.takeDamage(counter);
                }
            }
        } else {
            // Warrior: strong physical attacks
            int dmg = randomBetween(12, 20);
            enemy.takeDamage(dmg);
            player.notifyObservers(player.getName()
                    + " swings mightily and hits the Goblin for " + dmg + " damage!");
            playAttackAnimation(true);
        }
    }

    private void handleGuiPlayerHeal() {
        if (!player.isAlive()) return;
        int healAmt = randomBetween(10, 16);
        player.heal(healAmt); // Character.heal already logs via observers
        // No enemy attack this turn (handled by lastActionWasHeal flag)
    }

    private int randomBetween(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    private void updateHpLabels() {
        if (player != null) {
            if (playerIsMage) {
                heroHpLabel.setText(player.getName() + " HP: " + player.getHealth()
                        + " | Mana: " + playerMana + "/" + playerMaxMana);
            } else {
                heroHpLabel.setText(player.getName() + " HP: " + player.getHealth());
            }
        }
        if (enemy != null) {
            goblinHpLabel.setText("Goblin HP: " + enemy.getHealth());
        }
    }

    private void updateStrategyLabel() {
        String name = playerStrategyName();
        strategyLabel.setText("Strategy: [" + name + "]");
        if ("Aggressive".equalsIgnoreCase(name)) {
            actionButton.setText("Attack");
        } else if ("Defensive".equalsIgnoreCase(name)) {
            actionButton.setText("Heal");
        } else {
            actionButton.setText("Action");
        }
    }

    private String playerStrategyName() {
        AttackStrategy s = (player == null) ? null : player.getStrategy();
        return (s == null) ? "None" : s.getName();
    }

    private void checkGameOver() {
        if (player == null || enemy == null) return;

        if (!player.isAlive()) {
            logDesignEvent("[State] Game over – player defeated");
            JOptionPane.showMessageDialog(this,
                    "You were defeated by the Goblin.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else if (!enemy.isAlive()) {
            logDesignEvent("[State] Game over – goblin defeated");
            JOptionPane.showMessageDialog(this,
                    "You defeated the Goblin!",
                    "Victory", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==========================
    // Animation (simple "attack")
    // ==========================

    private void playAttackAnimation(boolean playerAttacking) {
        if (attackTimer != null && attackTimer.isRunning()) {
            attackTimer.stop();
        }

        final JLabel movingLabel = playerAttacking ? heroSpriteLabel : goblinSpriteLabel;
        originalHeroX = movingLabel.getX();
        heroOffset = 0;
        movingForward = true;

        attackTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                heroOffset += movingForward ? 5 : -5;
                movingLabel.setLocation(originalHeroX + heroOffset, movingLabel.getY());

                if (heroOffset >= 40) {
                    movingForward = false;
                } else if (heroOffset <= 0) {
                    movingLabel.setLocation(originalHeroX, movingLabel.getY());
                    attackTimer.stop();
                }
            }
        });
        attackTimer.start();
    }

    // ==========================
    // Logging helper
    // ==========================

    private void logDesignEvent(String msg) {
        if (consoleLogger != null) {
            consoleLogger.onEvent(msg);
        }
        if (guiLogger != null) {
            guiLogger.onEvent(msg);
        }
    }
}
