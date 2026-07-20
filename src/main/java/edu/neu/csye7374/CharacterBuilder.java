package edu.neu.csye7374;

/**
 * Design Pattern: Builder
 * -----------------------
 * Fluent API for constructing Character objects.
 */
public class CharacterBuilder {

    private String name = "Unnamed";
    private int health = 100;
    private Integer mana = null; // optional: set only for mages

    public CharacterBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CharacterBuilder setHealth(int health) {
        this.health = health;
        return this;
    }

    public CharacterBuilder setMana(int mana) {
        this.mana = mana;
        return this;
    }

    public Character build() {
        Character c = new Character(name, health);
        if (mana != null) {
            c.setMana(mana);
        }
        return c;
    }
}
