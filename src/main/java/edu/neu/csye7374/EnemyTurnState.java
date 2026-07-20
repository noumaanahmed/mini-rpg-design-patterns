package edu.neu.csye7374;

public class EnemyTurnState implements GameState {

    @Override
    public void playerAttack(GameFacade game) {
        if (game != null) {
                    game.log("[Pattern][State] EnemyTurnState rejects playerAttack(): not your turn.");

            game.log("It is not your turn.");
        }
    }

    @Override
    public void playerHeal(GameFacade game) {
        if (game != null) {
                    game.log("[Pattern][State] EnemyTurnState rejects playerHeal(): not your turn.");

            game.log("It is not your turn.");
        }
    }

    /**
     * Enemy action for the turn.
     * This is meant for a console / non-FX driver.
     * In the JavaFX GUI, FXBattleController + GameEngine call guiEnemyAttack()
     * directly and handle animations.
     */
    public void performEnemyAction(GameFacade game) {
        if (game == null || game.isBattleOver()) {
            return;
        }

        Character enemy = game.getEnemy();
        Character player = game.getPlayer();

        if (enemy == null || player == null || !enemy.isAlive() || !player.isAlive()) {
            return;
        }

                game.log("[Pattern][State] EnemyTurnState rejects playerHeal(): not your turn.");


        game.guiEnemyAttack();

        if (game.isBattleOver()) {
            game.setState(new GameOverState());
        } else {
            game.setState(new PlayerTurnState());
        }
    }

    @Override
    public String getName() {
        return "Enemy Turn";
    }
}
