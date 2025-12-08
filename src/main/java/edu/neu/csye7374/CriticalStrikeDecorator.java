package edu.neu.csye7374;

import java.util.Random;

public class CriticalStrikeDecorator extends AttackDecorator {

    private final Random rand = new Random();

    public CriticalStrikeDecorator(AttackStrategy inner) {
        super(inner);
    }

    @Override
    public void execute(Character self, Character target) {

        if (self == null || target == null || !self.isAlive() || !target.isAlive()) {
            return;
        }

        // 1️⃣ Perform base attack first
        int before = target.getHealth();
        inner.execute(self, target);
        int baseDamage = before - target.getHealth();

        // If the target took 0 damage, skip crit check
        if (baseDamage <= 0) return;

        // 2️⃣ Roll for critical hit (30% default)
if (rand.nextInt(100) < 30) {
    int extra = Math.max(2, (int) (baseDamage * 0.5)); // 50% bonus
    target.takeDamage(extra);

    // 🔹 Design-pattern log (Decorator wrapping Strategy)
    self.notifyObservers(
            "[Pattern][Decorator][Strategy] 💥 Critical strike! Extra "
                    + extra + " damage!"
    );
}

    }

    @Override
    public String getName() {
        return inner.getName() + "+Crit";
    }
}
