package edu.neu.csye7374;

import java.util.Random;

/**
 * Design Pattern: Strategy (Concrete Strategy)
 * -------------------------------------------
 * Defensive behavior: instead of attacking, the character heals itself.
 */
public class DefensiveAttack implements AttackStrategy {

    private final Random rand = new Random();

@Override
public void execute(Character attacker, Character target) {
    if (attacker == null || target == null || !attacker.isAlive()) {
        return;
    }
    int dmg = attacker.staffAttack(target) - 2;
    if (dmg < 0) dmg = 0;

    attacker.notifyObservers(
        "[Pattern][Strategy] " + attacker.getName()
        + " attacks defensively (" + dmg + " dmg)."
    );

    if (dmg > 0) {
        target.takeDamage(dmg);
    }
}


    @Override
    public String getName() {
        return "Defensive";
    }
}
