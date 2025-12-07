package edu.neu.csye7374;

import java.util.ArrayList;
import java.util.List;

public class Character {

    private static final int FIREBALL_COST = 10;

    private String name;
    private int health;
    private int maxHealth;

    // MAGE-ONLY ATTRIBUTE
    private int mana = 0;
    private int maxMana = 0;

    private AttackStrategy strategy;
    private List<GameObserver> observers = new ArrayList<>();

    public Character(String name, int health) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
    }

    // -------------------------
    // MANA SUPPORT
    // -------------------------
    public void setMana(int mana) {
        this.mana = mana;
        this.maxMana = mana;
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void reduceMana(int amount) {
        mana -= amount;
        if (mana < 0) mana = 0;
        notifyObservers(name + " used " + amount + " mana. (Mana: " + mana + ")");
    }

    public boolean hasMana(int required) {
        return mana >= required;
    }

    public boolean canCastFireball() {
        return hasMana(FIREBALL_COST);
    }

    // -------------------------
    // OBSERVER
    // -------------------------
    public void addObserver(GameObserver obs) { observers.add(obs); }

    public void notifyObservers(String msg) {
        for (GameObserver obs : observers) obs.onEvent(msg);
    }

    // -------------------------
    // GETTERS / SETTERS
    // -------------------------
    public String getName() { return name; }

    public int getHealth() { return health; }

    public AttackStrategy getStrategy() { return strategy; }

    public void setStrategy(AttackStrategy strategy) { this.strategy = strategy; }

    // -------------------------
    // COMBAT
    // -------------------------
    public void takeDamage(int dmg) {
        if (health <= 0) return;
        health -= dmg;
        if (health < 0) health = 0;
        notifyObservers(name + " took " + dmg + " damage! (HP: " + health + ")");
    }

    public void heal(int amount) {
        if (health <= 0) return;
        health += amount;
        if (health > maxHealth) health = maxHealth;
        notifyObservers(name + " healed " + amount + " HP! (HP: " + health + ")");
    }

    public void attack(Character target) {
        if (health <= 0) {
            notifyObservers(name + " is defeated and cannot attack!");
            return;
        }
        if (strategy == null) {
            notifyObservers(name + " has no attack strategy set!");
            return;
        }
        if (target == null || !target.isAlive()) {
            notifyObservers(name + " tried to attack, but the target is already defeated!");
            return;
        }
        strategy.execute(this, target);
    }

    public boolean isAlive() { return health > 0; }

    // -------------------------
    // CUSTOM MAGE ATTACKS
    // -------------------------
    public void staffAttack(Character target) {
        int dmg = 5; // weak
        target.takeDamage(dmg);
        notifyObservers(name + " strikes with their staff for " + dmg + " damage!");
    }

    public void castFireball(Character target) {
        if (!canCastFireball()) {
            notifyObservers(name + " has NO MANA and leaves themselves open!");
            // Immediate punishment
            target.attack(this);
            return;
        }

        reduceMana(FIREBALL_COST);
        int dmg = 18; // strong spell
        target.takeDamage(dmg);
        notifyObservers(name + " casts FIREBALL for " + dmg + " damage!");
    }
}
