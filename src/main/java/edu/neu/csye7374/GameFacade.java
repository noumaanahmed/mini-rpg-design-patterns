package edu.neu.csye7374;

public class GameFacade {

    private Character player;
    private Character enemy;

    private GameState state;
    private GameObserver observer;

    private final GameConfig config = GameConfig.getInstance();

    public GameFacade() {
        this.state = new GameOverState(); // until a game starts
    }

    public void setObserver(GameObserver observer) {
        this.observer = observer;
    }

    void log(String msg) {
        if (observer != null) {
            observer.onEvent(msg);
        }
    }

    /**
     * Starts a new game using the Factory + Builder patterns
     * and wires up strategies and observers.
     */
    public void startNewGame(String name, String type, int difficulty) {
        config.setDifficulty(difficulty);

        // Factory + Builder for player
        player = CharacterFactory.createCharacter(type, name);

        // Builder for enemy with difficulty-based HP
        int goblinHP;
        switch (difficulty) {
            case 1 -> goblinHP = 50;
            case 2 -> goblinHP = 80;
            case 3 -> goblinHP = 120;
            default -> goblinHP = 80;
        }

        CharacterBuilder enemyBuilder = new CharacterBuilder()
                .setName("Goblin")
                .setHealth(goblinHP);
        enemy = enemyBuilder.build();

        // Observer wiring
        if (observer != null) {
            player.addObserver(observer);
            enemy.addObserver(observer);
        }

        // Strategies (Strategy + Decorator)
        AttackStrategy playerBase = new AggressiveAttack();
        player.setStrategy(new CriticalStrikeDecorator(playerBase)); // Decorated strategy
        enemy.setStrategy(new AggressiveAttack());

        state = new PlayerTurnState();

        log("New game started: " + player.getName() + " vs Goblin");
        log("Difficulty: " + difficulty + " | Goblin HP: " + goblinHP);
    }

    // ---------------------------------------------------------------------
    // INTERNAL TEMPLATE-LIKE HELPERS (used by console & UI)
    // ---------------------------------------------------------------------

    void basicPlayerAttack() {
        if (player != null && enemy != null && player.isAlive() && enemy.isAlive()) {
            player.attack(enemy);
            afterAction();
        }
    }

    void basicPlayerHeal() {
        if (player != null && player.isAlive()) {
            // basic heal amount; UI can wrap this instead of inventing logic
            player.heal(10);
            afterAction();
        }
    }

    void basicEnemyAttack() {
        if (enemy != null && player != null && enemy.isAlive() && player.isAlive()) {
            log("Enemy turn:");
            enemy.attack(player);
            afterAction();
        }
    }

    // called after any action
    private void afterAction() {
        if (isBattleOver()) {
            state = new GameOverState();
            if (!player.isAlive()) {
                log("You were defeated!");
            } else if (!enemy.isAlive()) {
                log("You defeated the Goblin!");
            }
        }
        // NOTE:
        // For the console demo, you can still drive a State machine.
        // For the JavaFX UI, we will explicitly decide when enemy attacks,
        // so we do NOT automatically trigger enemy turns here.
    }

    // ---------------------------------------------------------------------
    // ORIGINAL STATE-BASED ENTRY POINTS (console-oriented)
    // ---------------------------------------------------------------------

    public void playerAttack() {
        state.playerAttack(this);
    }

    public void playerHeal() {
        state.playerHeal(this);
    }

    // ---------------------------------------------------------------------
    // UI-FRIENDLY METHODS FOR JavaFX (no rules in GUI)
    // ---------------------------------------------------------------------

    /**
     * Basic player attack for UI:
     * uses the current Strategy (Aggressive + CriticalStrike).
     */
    public void uiPlayerBasicAttack() {
        basicPlayerAttack();
    }

    /**
     * Fireball attack for Mage:
     * temporarily swaps strategy to FireballAttack, then restores.
     * All mana and punishment logic lives inside Character + FireballAttack.
     */
    public void uiPlayerFireballAttack() {
        if (player == null || enemy == null || !player.isAlive() || !enemy.isAlive()) {
            return;
        }
        AttackStrategy previous = player.getStrategy();
        try {
            player.setStrategy(new FireballAttack());
            basicPlayerAttack();
        } finally {
            player.setStrategy(previous);
        }
    }

    /**
     * Heal action for UI:
     * calls basicPlayerHeal so healing is decided in domain layer.
     */
    public void uiPlayerHeal() {
        basicPlayerHeal();
    }

    // ---------------------------------------------------------------------
    // QUERY METHODS
    // ---------------------------------------------------------------------

    public boolean isBattleOver() {
        return player == null || enemy == null || !player.isAlive() || !enemy.isAlive();
    }

    public Character getPlayer() {
        return player;
    }

    public Character getEnemy() {
        return enemy;
    }

    public GameState getState() {
        return state;
    }

    void setState(GameState state) {
        this.state = state;
    }
}
