package edu.neu.csye7374;

public class GameOverState implements GameState {

    @Override
    public void playerAttack(GameFacade game) {
        if (game != null) {
                    game.log("[Pattern][State] GameOverState ignores playerAttack(); battle already over.");

            game.log("The battle is already over.");
        }
    }

    @Override
    public void playerHeal(GameFacade game) {
        if (game != null) {
                    game.log("[Pattern][State] GameOverState ignores playerHeal(); battle already over.");

            game.log("The battle is already over.");
        }
    }

    @Override
    public String getName() {
        return "Game Over";
    }
}
