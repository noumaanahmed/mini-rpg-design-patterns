package edu.neu.csye7374;

/**
 * Design Pattern: Composite (for Observers)
 * -----------------------------------------
 * Allows multiple GameObserver instances (e.g., ConsoleLogger + FxLogObserver)
 * to be treated as a single observer from the domain's perspective.
 */
public class CompositeObserver implements GameObserver {

    private final GameObserver[] observers;

    public CompositeObserver(GameObserver... observers) {
        this.observers = observers;
    }

    @Override
    public void onEvent(String message) {
        if (observers == null) return;
        for (GameObserver obs : observers) {
            if (obs != null) {
                obs.onEvent(message);
            }
        }
    }
}
