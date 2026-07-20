package edu.neu.csye7374;

public interface GameState {

    /**
     * Handle a player attack in the current state.
     * For the JavaFX GUI, this is mainly used by a console / text driver,
     * since FXBattleController talks directly to GameEngine + GameFacade.
     */
    void playerAttack(GameFacade game);

    /**
     * Handle a player heal action in the current state.
     */
    void playerHeal(GameFacade game);

    /**
     * Name of the state (for logging / debugging).
     */
    String getName();
}
