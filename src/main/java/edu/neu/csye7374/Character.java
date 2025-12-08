package edu.neu.csye7374;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Character {

    // ================================================================
    // BALANCE CONSTANTS (easy tuning)
    // ================================================================
    private static final Random RNG = new Random();

    private static final int STAFF_MIN = 4;
    private static final int STAFF_MAX = 7;

    private static final int FIREBALL_MIN = 14;
    private static final int FIREBALL_MAX = 20;

    private static final int HEAL_MIN = 8;
    private static final int HEAL_MAX = 16;

    private static final int FIREBALL_COST = 10;

    // ================================================================
    // FIELDS
    // ================================================================
    private String name;

    private int health;
    private int maxHealth;

    private int mana = 0;
    private int maxMana = 0;

    private AttackStrategy strategy;
    private final List<GameObserver> observers = new ArrayList<>();

    // ================================================================
    // CONSTRUCTOR
    // ================================================================
    public Character(String name, int health) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
    }

    // ================================================================
    // MANA MANAGEMENT
    // ================================================================
    public void setMana(int mana) {
        this.mana = mana;
        this.maxMana = mana;
    }

    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }

    public boolean hasMana(int required) { return mana >= required; }
    public boolean canCastFireball() { return hasMana(FIREBALL_COST); }

    public void reduceMana(int amount) {
        mana -= amount;
        if (mana < 0) mana = 0;

        notifyObservers(name + " used " + amount + " mana. (Mana: " + mana + ")");
    }

    // ================================================================
    // OBSERVER SUPPORT
    // ================================================================
    public void addObserver(GameObserver obs) { observers.add(obs); }

    public void notifyObservers(String msg) {
        for (GameObserver obs : observers) {
            obs.onEvent(msg);
        }
    }

    // ================================================================
    // BASIC GETTERS
    // ================================================================
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public AttackStrategy getStrategy() { return strategy; }
    public void setStrategy(AttackStrategy strategy) { this.strategy = strategy; }

    public boolean isAlive() { return health > 0; }

    // ================================================================
    // RANDOM HELPERS
    // ================================================================
    private int randomBetween(int low, int high) {
        return RNG.nextInt(high - low + 1) + low;
    }

    // ================================================================
    // DAMAGE + HEAL CORE LOGIC
    // ================================================================
    public void takeDamage(int dmg) {
        if (health <= 0) return;

        health -= dmg;
        if (health < 0) health = 0;

        notifyObservers(name + " took " + dmg + " damage! (HP: " + health + ")");
    }

    public void heal(int amount) {
        if (!isAlive()) return;

        health += amount;
        if (health > maxHealth) health = maxHealth;

        notifyObservers(name + " healed " + amount + " HP! (HP: " + health + ")");
    }

    private int applyDamage(Character target, int dmg, String actionMsg) {
        if (!target.isAlive()) {
            notifyObservers(target.getName() + " is already defeated.");
            return 0;
        }

        target.takeDamage(dmg);
        notifyObservers(actionMsg + " for " + dmg + " damage.");
        return dmg;
    }

    private int applyHeal(int amount) {
        int before = health;
        heal(amount);
        int healed = health - before;

        notifyObservers(name + " restores " + healed + " HP!");
        return healed;
    }

    // ================================================================
    // ATTACK ROUTING
    // ================================================================
    public void attack(Character target) {
        if (!isAlive()) {
            notifyObservers(name + " is defeated and cannot attack!");
            return;
        }

        if (strategy == null) {
            notifyObservers(name + " has no attack strategy set!");
            return;
        }

        strategy.execute(this, target);
    }

    // ================================================================
    // MAGE ACTIONS (randomized)
    // ================================================================
    public int staffAttack(Character target) {
        int dmg = randomBetween(STAFF_MIN, STAFF_MAX);
        return applyDamage(target, dmg, name + " swings their staff");
    }

    public int castFireball(Character target) {

        if (!canCastFireball()) {
            notifyObservers(name + " has NO MANA and leaves themselves open!");
            // Punishment attack (enemy attacks mage)
            target.attack(this);
            return 0;
        }

        reduceMana(FIREBALL_COST);

        int dmg = randomBetween(FIREBALL_MIN, FIREBALL_MAX);
        return applyDamage(target, dmg, name + " casts FIREBALL");
    }

    // ================================================================
    // HEAL (GUI calls this → GameEngine → GameFacade)
    // ================================================================
    public int performHeal() {
        int amount = randomBetween(HEAL_MIN, HEAL_MAX);
        return applyHeal(amount);
    }

    // ================================================================
// DIRECT HP SETTERS (required by GameEngine)
// ================================================================
public void setHealth(int hp) {
    this.health = hp;
    if (this.health < 0) this.health = 0;
    if (this.health > this.maxHealth) this.health = this.maxHealth;
}

public void setMaxHealth(int hp) {
    this.maxHealth = hp;
    if (this.maxHealth < 1) this.maxHealth = 1;
    if (this.health > this.maxHealth) this.health = this.maxHealth;
}

}
