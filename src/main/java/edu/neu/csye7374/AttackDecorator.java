package edu.neu.csye7374;

/**
 * Decorator base class for AttackStrategy implementations.
 */
public abstract class AttackDecorator implements AttackStrategy {

    protected final AttackStrategy inner;

    protected AttackDecorator(AttackStrategy inner) {
        if (inner == null) {
            throw new IllegalArgumentException("Inner strategy cannot be null.");
        }
        this.inner = inner;
    }

    @Override
    public String getName() {
        return inner.getName();
    }
}
