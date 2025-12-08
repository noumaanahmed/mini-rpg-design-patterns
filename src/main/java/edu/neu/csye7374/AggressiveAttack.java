package edu.neu.csye7374;

public class AggressiveAttack implements AttackStrategy {

    @Override
    public void execute(Character self, Character target) {

        if (self == null || target == null || !self.isAlive() || !target.isAlive()) {
            return;
        }

        // Basic physical attack for either Warrior or Mage
        // Random damage is determined inside Character
        int dmg = self.staffAttack(target);  // Staff & Sword now share same base logic

        self.notifyObservers("[Pattern][Strategy] " + self.getName()
                        + " attacked aggressively (" + dmg + " dmg).");
    }
    @Override
    public String getName() {
        return "Aggressive";
    }
}
