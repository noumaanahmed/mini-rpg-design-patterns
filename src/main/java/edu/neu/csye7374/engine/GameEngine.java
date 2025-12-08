package edu.neu.csye7374.engine;

import edu.neu.csye7374.Character;
import edu.neu.csye7374.GameFacade;

import java.util.Random;

/**
 * GameEngine
 * ----------
 * Pure combat coordinator for one battle.
 *
 * ❌ Does NOT implement its own combat formulas.
 * ✅ Delegates all damage / healing to the domain layer:
 *      - Character.attack(...) → Strategy / Decorator
 *      - Character.castFireball(...)
 *      - Character.performHeal()
 *
 * The JavaFX GUI talks ONLY to this engine (and GameFacade)
 * and never manipulates HP / mana directly.
 */
public class GameEngine {

    // High-level actions that the GUI can trigger
    public enum PlayerAction {
        BASIC_ATTACK,
        FIREBALL,
        HEAL
    }

    private final GameFacade game;

    // Cached references (kept in sync with the facade)
    private Character player;
    private Character enemy;

    // For any light randomness that is *visual only* (if needed later)
    @SuppressWarnings("unused")
    private final Random rng = new Random();

    public GameEngine(GameFacade game) {
        this.game = game;
        syncCharacters();
    }

    /**
     * Refresh references from the facade.
     * Call this if GameFacade ever swaps player / enemy.
     */
    public void syncCharacters() {
        this.player = game.getPlayer();
        this.enemy  = game.getEnemy();
    }

    // =========================================================
    // CORE ACTION API (used by JavaFX GUI)
    // =========================================================

    public ActionResult handlePlayerAction(PlayerAction action) {
        syncCharacters();
        if (!combatAlive()) {
            return new ActionResult(
                    ActionResult.ActionType.MISS,
                    0,
                    false,
                    "[Engine] Action ignored: combat already over."
            );
        }

        return switch (action) {
            case BASIC_ATTACK -> doPlayerBasicAttack();
            case FIREBALL     -> doPlayerFireball();
            case HEAL         -> doPlayerHeal();
        };
    }

    public ActionResult handleEnemyAttack() {
        syncCharacters();
        if (!combatAlive()) {
            return new ActionResult(
                    ActionResult.ActionType.MISS,
                    0,
                    false,
                    "[Engine] Enemy cannot act, combat is over."
            );
        }
        return doEnemyAttack();
    }

    // =========================================================
    // LEGACY PROCESS METHODS (used by GameFacade's console mode)
    // =========================================================

    /**
     * For GameFacade: returns damage done by a basic attack.
     * The {@code isMage} flag is kept only for compatibility; the
     * Strategy / Decorator on the Character actually decides how the
     * attack behaves.
     */
    public int processPlayerAttack(boolean isMage) {
        ActionResult result = handlePlayerAction(PlayerAction.BASIC_ATTACK);
        return Math.max(result.getValue(), 0);
    }

    public int processPlayerFireball() {
        ActionResult result = handlePlayerAction(PlayerAction.FIREBALL);
        return Math.max(result.getValue(), 0);
    }

    public int processPlayerHeal() {
        ActionResult result = handlePlayerAction(PlayerAction.HEAL);
        return Math.max(result.getValue(), 0);
    }

    public int processEnemyAttack() {
        ActionResult result = handleEnemyAttack();
        return Math.max(result.getValue(), 0);
    }

    // =========================================================
    // INTERNAL ACTION IMPLEMENTATIONS
    // =========================================================

    /**
     * BASIC ATTACK
     * ------------
     * Delegates to Strategy / Decorator via Character.attack().
     */
    private ActionResult doPlayerBasicAttack() {
        if (!combatAlive()) {
            return new ActionResult(
                    ActionResult.ActionType.MISS, 0, false,
                    "[Engine] Player basic attack ignored; combat over."
            );
        }

        int before = enemy.getHealth();

        // === DESIGN PATTERNS HAPPEN HERE ===
        // Strategy + Decorator chain decides the actual damage.
        player.attack(enemy);

        int after = enemy.getHealth();
        int damage = Math.max(0, before - after);

        boolean critical = false;
        if (damage > 0 && player.getStrategy() != null) {
            // Heuristic: if using CriticalStrikeDecorator then for GUI
            // purposes we treat hits as "critical" so you still get the
            // crit flash. (Real critical logic lives in the decorator.)
            String stratName = player.getStrategy().getName();
            critical = stratName != null && stratName.contains("Crit");
        }

        String msg = damage > 0
                ? player.getName() + " attacks for " + damage + " damage."
                : player.getName() + " attacks but deals no damage.";

        ActionResult.ActionType type = ActionResult.ActionType.ATTACK;
        if (!enemy.isAlive()) {
            msg += " The Goblin is defeated!";
            type = ActionResult.ActionType.ENEMY_DEATH;
        }

        return new ActionResult(type, damage, critical, msg);
    }

    /**
     * FIREBALL
     * --------
     * Delegates to the mage-specific ability on Character.
     * All mana checks & punishment behaviour live inside Character.
     */
    private ActionResult doPlayerFireball() {
        if (!combatAlive()) {
            return new ActionResult(
                    ActionResult.ActionType.MISS, 0, false,
                    "[Engine] Fireball ignored; combat over."
            );
        }

        // Non-mage characters simply cannot cast Fireball.
        if (player.getMaxMana() <= 0) {
            return new ActionResult(
                    ActionResult.ActionType.MISS, 0, false,
                    player.getName() + " cannot cast Fireball."
            );
        }

        int before = enemy.getHealth();
        int dmg = player.castFireball(enemy);   // uses Character + Observer

        int after = enemy.getHealth();
        int applied = Math.max(0, before - after);

        // If the mage had no mana, castFireball() will have logged the
        // punishment attack via Observer. From the GUI’s perspective,
        // this is simply a "miss".
        if (applied <= 0) {
            return new ActionResult(
                    ActionResult.ActionType.MISS,
                    0,
                    false,
                    player.getName() + " fails to land a Fireball."
            );
        }

        String msg = player.getName() + " casts Fireball for " + applied + " damage.";
        ActionResult.ActionType type = ActionResult.ActionType.FIREBALL;

        if (!enemy.isAlive()) {
            msg += " The Goblin is incinerated!";
            type = ActionResult.ActionType.ENEMY_DEATH;
        }

        return new ActionResult(type, applied, false, msg);
    }

    /**
     * HEAL
     * ----
     * Delegates to Character.performHeal().
     */
    private ActionResult doPlayerHeal() {
        if (!player.isAlive()) {
            return new ActionResult(
                    ActionResult.ActionType.MISS, 0, false,
                    player.getName() + " cannot heal while defeated."
            );
        }

        int healed = player.performHeal(); // Character + Observer

        String msg = player.getName() + " heals for " + healed + " HP.";

        return new ActionResult(
                ActionResult.ActionType.HEAL,
                healed,
                false,
                msg
        );
    }

    /**
     * ENEMY ATTACK
     * ------------
     * Delegates to the Goblin's Strategy (AggressiveAttack).
     */
    private ActionResult doEnemyAttack() {
        if (!combatAlive()) {
            return new ActionResult(
                    ActionResult.ActionType.MISS, 0, false,
                    "[Engine] Enemy attack ignored; combat over."
            );
        }

        int before = player.getHealth();

        // === DESIGN PATTERNS AGAIN ===
        enemy.attack(player);  // Strategy executes here

        int after = player.getHealth();
        int damage = Math.max(0, before - after);

        String msg = damage > 0
                ? "Goblin attacks for " + damage + " damage."
                : "Goblin attacks but fails to harm " + player.getName() + ".";

        ActionResult.ActionType type = ActionResult.ActionType.ENEMY_ATTACK;
        if (!player.isAlive()) {
            msg += " " + player.getName() + " is defeated.";
            type = ActionResult.ActionType.PLAYER_DEATH;
        }

        return new ActionResult(type, damage, false, msg);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean combatAlive() {
        return player != null && enemy != null &&
               player.isAlive() && enemy.isAlive();
    }

    // =========================================================
    // ACCESSORS (for GUI / facade)
    // =========================================================

    public Character getPlayer() {
        return player;
    }

    public Character getEnemy() {
        return enemy;
    }

    public boolean isPlayerMage() {
        return player != null && player.getMaxMana() > 0;
    }

    public boolean isBattleOver() {
        return player == null || enemy == null ||
               !player.isAlive() || !enemy.isAlive();
    }

    public GameFacade getGameFacade() {
        return game;
    }
}
