package edu.neu.csye7374.engine;

/**
 * Encapsulates the result of a single combat action.
 * - type: what kind of action occurred
 * - value: damage or healing amount (non-negative)
 * - critical: whether the hit was critical
 * - logMessage: optional domain log text (GUI can ignore or use it)
 */
public class ActionResult {

    public enum ActionType {
        ATTACK,
        FIREBALL,
        HEAL,
        ENEMY_ATTACK,
        PLAYER_DEATH,
        ENEMY_DEATH,
        MISS
    }

    private final ActionType type;
    private final int value;          // damage or heal amount
    private final boolean critical;
    private final String logMessage;  // domain-generated text (optional for GUI)

    public ActionResult(ActionType type, int value, boolean critical, String logMessage) {
        this.type = type;
        this.value = Math.max(0, value);
        this.critical = critical;
        this.logMessage = logMessage;
    }

    public ActionType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public boolean isCritical() {
        return critical;
    }

    public String getLogMessage() {
        return logMessage;
    }
}
