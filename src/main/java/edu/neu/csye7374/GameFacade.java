package edu.neu.csye7374;

import edu.neu.csye7374.engine.GameEngine;

public class GameFacade {

    private Character player;
    private Character enemy;

    private GameState state;
    private GameObserver observer;

    // Engine that handles all combat logic
    private GameEngine engine = new GameEngine(this);

    private final GameConfig config = GameConfig.getInstance();

    public GameFacade() {
        this.state = new GameOverState(); // Until game starts
    }

    public void setObserver(GameObserver observer) {
        this.observer = observer;
    }

    void log(String msg) {
        if (observer != null) {
            observer.onEvent(msg);
        }
    }

    public GameEngine getEngine() {
        return engine;
    }

    // --------------------------------------------------------------------
    // START NEW GAME (Factory + Builder + Strategy + Observer)
    // --------------------------------------------------------------------
    public void startNewGame(String name, String type, int difficulty) {

        // Reset engine for new battle
        engine = new GameEngine(this);

        config.setDifficulty(difficulty);
        log("[Pattern][Singleton] Difficulty stored: " + difficulty);

        // --- Player via Factory ---
        player = CharacterFactory.createCharacter(type, name);
        log("[Pattern][Factory] Created Player: " + type + " named " + name);

        // --- Enemy HP by difficulty ---
        int goblinHP = switch (difficulty) {
            case 1 -> 50;
            case 2 -> 80;
            case 3 -> 120;
            default -> 80;
        };

        // --- Enemy via Builder ---
        enemy = new CharacterBuilder()
                .setName("Goblin")
                .setHealth(goblinHP)
                .build();

        log("[Pattern][Builder] Enemy created with HP " + goblinHP);

        // --- Observer wiring ---
        if (observer != null) {
            player.addObserver(observer);
            enemy.addObserver(observer);
            log("[Pattern][Observer] Attached shared observer.");
        }

        // ---------------------------------------------------------
        // Strategy + Decorator setup
        // ---------------------------------------------------------
        AttackStrategy aggressive = new AggressiveAttack();

        if ("warrior".equalsIgnoreCase(type)) {
            player.setStrategy(new CriticalStrikeDecorator(aggressive));
            log("[Pattern][Strategy + Decorator] Warrior uses CriticalStrike(Aggressive).");
        } else {
            player.setStrategy(aggressive);
            log("[Pattern][Strategy] Mage uses AggressiveAttack.");
        }

        enemy.setStrategy(new AggressiveAttack());
        log("[Pattern][Strategy] Goblin uses AggressiveAttack.");

        state = new PlayerTurnState();

        log("[Pattern][State] Game Started: " + player.getName() + " vs Goblin (HP: " + goblinHP + ")");
    }

 // --------------------------------------------------------------------
// ACTIONS (called by GameEngine AFTER resolving logic)
// --------------------------------------------------------------------
public int guiPlayerSwordOrStaffAttack(boolean isMage) {
    if (!validCombat()) return 0;

    // 🔹 Facade pattern log
    log("[Pattern][Facade] Player triggered BASIC_ATTACK via GameFacade.");

    int damage = engine.processPlayerAttack(isMage);
    afterAction();
    return Math.max(damage, 0);
}

public int guiPlayerFireballAttack() {
    if (!validCombat()) return 0;

    // 🔹 Facade pattern log
    log("[Pattern][Facade] Player triggered FIREBALL via GameFacade.");

    int dmg = engine.processPlayerFireball();
    afterAction();
    return Math.max(dmg, 0);
}

public int guiPlayerHeal() {
    if (!player.isAlive()) return 0;

    // 🔹 Facade pattern log
    log("[Pattern][Facade] Player triggered HEAL via GameFacade.");

    int healed = engine.processPlayerHeal();
    afterAction();
    return healed;
}

public int guiEnemyAttack() {
    if (!validCombat()) return 0;

    // 🔹 Facade pattern log
    log("[Pattern][Facade] Enemy turn processed via GameFacade.");

    int dmg = engine.processEnemyAttack();
    afterAction();
    return Math.max(dmg, 0);
}

    // --------------------------------------------------------------------
    // INTERNAL HELPERS
    // --------------------------------------------------------------------
    private boolean validCombat() {
        return player != null && enemy != null &&
               player.isAlive() && enemy.isAlive();
    }

    private void afterAction() {
        if (isBattleOver()) {
            state = new GameOverState();
            if (!player.isAlive()) {
                log("⚠ You were defeated.");
            } else if (!enemy.isAlive()) {
                log("🏆 You defeated the Goblin!");
            }
        }
    }

    // --------------------------------------------------------------------
    // CONSOLE STATE INTERFACE SUPPORT
    // --------------------------------------------------------------------
    public void playerAttack() {
        state.playerAttack(this);
    }

    public void playerHeal() {
        state.playerHeal(this);
    }

    public boolean isBattleOver() {
        return player == null || enemy == null ||
                !player.isAlive() || !enemy.isAlive();
    }

    public Character getPlayer() { return player; }
    public Character getEnemy() { return enemy; }

public GameState getState() { return state; }

void setState(GameState newState) {
    if (newState == null) return;
    String oldName = (state == null) ? "null" : state.getName();
    String newName = newState.getName();
    this.state = newState;
    log("[Pattern][State] Transition: " + oldName + " -> " + newName);
}

}
