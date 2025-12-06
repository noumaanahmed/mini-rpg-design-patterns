package edu.neu.csye7374;

public class CharacterFactory {

    public static Character createCharacter(String type, String name) {

        CharacterBuilder builder = new CharacterBuilder()
                .setName(name);

        Character c;

        switch (type.toLowerCase()) {
            case "warrior":
                c = builder.setHealth(120).build();
                // Warriors have no mana
                break;

            case "mage":
                c = builder.setHealth(80).build();
                c.setMana(40);   // Mages start with mana
                break;

            default:
                c = builder.setHealth(100).build();
        }

        return c;
    }
}
