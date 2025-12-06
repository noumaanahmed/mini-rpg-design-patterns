package edu.neu.csye7374;

public class FireballAttack implements AttackStrategy {

    @Override
    public void execute(Character self, Character target) {
        self.castFireball(target);
    }

    @Override
    public String getName() { return "Fireball"; }
}
