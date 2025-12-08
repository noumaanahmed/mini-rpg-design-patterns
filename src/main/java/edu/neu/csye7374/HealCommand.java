package edu.neu.csye7374;

/**
 * Design Pattern: Command (Concrete Command)
 * ------------------------------------------
 * Wraps a call to target.heal(amount).
 */
public class HealCommand implements Command {

    private final Character target;
    private final int amount;

    public HealCommand(Character target, int amount) {
        this.target = target;
        this.amount = amount;
    }

@Override
public void execute() {
    if (target == null) return;

    target.notifyObservers(
        "[Pattern][Command] HealCommand.execute() invoked for "
        + target.getName()
    );

    target.heal(amount);
}

}
