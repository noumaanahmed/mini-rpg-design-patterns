package edu.neu.csye7374;

public class AggressiveAttack implements AttackStrategy {

    @Override
    public void execute(Character self, Character target) {

        if (self.getMana() > 0) {
            // Mage auto chooses fireball OR staff depending on UI
            self.staffAttack(target);
        } 
        else {
            // Warrior or Mage with no mana
            int dmg = 12; // warrior baseline
            target.takeDamage(dmg);
            self.notifyObservers(self.getName() + " strikes powerfully for " + dmg + " damage!");
        }
    }

    @Override
    public String getName() { return "Aggressive"; }
}
