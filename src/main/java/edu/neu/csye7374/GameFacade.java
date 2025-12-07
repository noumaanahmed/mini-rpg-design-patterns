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

    // ---------------------------------------------------------
    // START NEW GAME (FACTORY + BUILDER + STRATEGY + OBSERVER)
    // ---------------------------------------------------------
    public void startNewGame(String name, String type, int difficulty) {
        config.setDifficulty(difficulty);
        log("[Pattern: Singleton] Difficulty stored in GameConfig = " + difficulty);

        // Factory for player
        player = CharacterFactory.createCharacter(type, name);
        log("[Pattern: Factory] Player created via CharacterFactory as '" + type
                + "' named '" + player.getName() + "'.");

        // Builder for enemy
        int goblinHP;
        switch (difficulty) {
            case 1: goblinHP = 50; break;
            case 2: goblinHP = 80; break;
            case 3: goblinHP = 120; break;
            default: goblinHP = 80;
        }

        CharacterBuilder enemyBuilder = new CharacterBuilder()
                .setName("Goblin")
                .setHealth(goblinHP);
        enemy = enemyBuilder.build();
        log("[Pattern: Builder] Enemy 'Goblin' built via CharacterBuilder with HP = " + goblinHP + ".");

        // Observer wiring (Bridge to console / GUI)
        if (observer != null) {
            player.addObserver(observer);
            enemy.addObserver(observer);
            log("[Pattern: Observer] Attached shared GameObserver to player and enemy.");
        }

        // -----------------------------------------------------
        // Strategy + Decorator setup
        //  - Warrior: Aggressive + CriticalStrikeDecorator
        //  - Mage   : Aggressive (no crit; uses explicit staff/fireball API)
        // -----------------------------------------------------
        AttackStrategy baseAggressive = new AggressiveAttack();
        if ("warrior".equalsIgnoreCase(type)) {
            player.setStrategy(new CriticalStrikeDecorator(baseAggressive));
            log("[Pattern: Strategy + Decorator] Warrior uses CriticalStrikeDecorator(AggressiveAttack).");
        } else {
            player.setStrategy(baseAggressive);
            log("[Pattern: Strategy] Mage uses AggressiveAttack (staff/fireball handled explicitly).");
        }

        // Enemy always aggressive
        enemy.setStrategy(new AggressiveAttack());
        log("[Pattern: Strategy] Enemy strategy = AggressiveAttack.");

        state = new PlayerTurnState();

        log("New game started: " + player.getName() + " vs Goblin");
        log("Difficulty: " + difficulty + " | Goblin HP: " + goblinHP);
    }

    // ---------------------------------------------------------
    // TEMPLATE-ISH "BASIC" ACTIONS (domain only)
    // ---------------------------------------------------------
    void basicPlayerAttack() {
        if (player != null && enemy != null && player.isAlive() && enemy.isAlive()) {
            player.attack(enemy);
            afterAction();
        }
    }

    void basicPlayerHeal() {
        if (player != null && player.isAlive()) {
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

    // ---------------------------------------------------------
    // AFTER ANY ACTION (Template-like end-of-turn handling)
    // ---------------------------------------------------------
    private void afterAction() {
        if (isBattleOver()) {
            state = new GameOverState();
            if (!player.isAlive()) {
                log("You were defeated!");
            } else if (!enemy.isAlive()) {
                log("You defeated the Goblin!");
            }
        } else if (state instanceof EnemyTurnState) {
            log("[Pattern: State] EnemyTurnState would auto-attack here (console/alt flows).");
        }
    }

    // ---------------------------------------------------------
    // STATE-DRIVEN ENTRY POINTS (console version)
    // ---------------------------------------------------------
    public void playerAttack() {
        state.playerAttack(this);
    }

    public void playerHeal() {
        state.playerHeal(this);
    }

    // ---------------------------------------------------------
    // SIMPLE QUERY HELPERS
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // 🔹 GUI-FRIENDLY FACADE METHODS (NO UI CODE HERE)
    // ---------------------------------------------------------

    /** Warrior or Mage basic physical attack (sword or staff). */
    public void guiPlayerSwordOrStaffAttack(boolean isMage) {
        if (player == null || enemy == null || !player.isAlive() || !enemy.isAlive()) return;

        if (isMage) {
            log("[Pattern: Facade] Mage staff attack triggered from GUI.");
            log("[Pattern: Observer] Damage + events broadcast via Character.notifyObservers().");
            player.staffAttack(enemy);
        } else {
            // Warrior: use whatever Strategy is currently set (Aggressive+Crit)
            log("[Pattern: Strategy + Decorator] Warrior uses its Strategy (Aggressive+Crit) via player.attack().");
            player.attack(enemy);
        }
        afterAction();
    }

    /** Mage fireball attack (uses Strategy + mana in Character). */
    public void guiPlayerFireballAttack() {
        if (player == null || enemy == null || !player.isAlive() || !enemy.isAlive()) return;

        log("[Pattern: Strategy] Switching to FireballAttack for this action.");
        AttackStrategy prev = player.getStrategy();
        player.setStrategy(new FireballAttack());
        player.attack(enemy); // Character.castFireball handles mana + punishment.
        player.setStrategy(prev);
        afterAction();
    }

    /** Player heal – uses HealCommand to demonstrate Command pattern. */
    public void guiPlayerHeal() {
        if (player == null || !player.isAlive()) return;

        int amount = 12;
        log("[Pattern: Command] HealCommand(" + amount + ") executed via GameFacade.");
        new HealCommand(player, amount).execute();
        afterAction();
    }

    /** Goblin attack – GUI just calls this, logic stays here. */
    public void guiEnemyAttack() {
        if (enemy == null || player == null || !enemy.isAlive() || !player.isAlive()) return;

        log("[Pattern: Strategy] Enemy uses AggressiveAttack via Strategy.");
        enemy.setStrategy(new AggressiveAttack());
        enemy.attack(player);
        afterAction();
    }
}
