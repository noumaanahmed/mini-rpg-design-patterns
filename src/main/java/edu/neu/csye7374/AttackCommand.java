package edu.neu.csye7374;

public class AttackCommand implements Command {

	private final Character attacker;
	private final Character target;

	public AttackCommand(Character attacker, Character target) {
		this.attacker = attacker;
		this.target = target;
	}

@Override
public void execute() {
    if (attacker == null || target == null) return;

    attacker.notifyObservers(
        "[Pattern][Command] AttackCommand.execute() invoked for "
        + attacker.getName()
    );

    attacker.attack(target);   // Strategy/Decorator runs inside this
}

}
