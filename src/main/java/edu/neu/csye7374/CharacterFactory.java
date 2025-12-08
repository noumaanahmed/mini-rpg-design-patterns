package edu.neu.csye7374;

public class CharacterFactory {

    public static Character createCharacter(String type, String name) {

        CharacterBuilder builder = new CharacterBuilder().setName(name);
        Character c;

        switch (type.toLowerCase()) {

            case "warrior":
                c = builder.setHealth(120).build();
                c.setMana(0);   // Warriors never use mana
                break;

            case "mage":
                c = builder.setHealth(80).build();
                c.setMana(40);  // Starting mana pool
                break;

            default:
                // Flexible default character
                c = builder.setHealth(100).build();
                c.setMana(0);
                break;
        }

        return c;
    }
}
