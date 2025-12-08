package edu.neu.csye7374;

public class PlayerTurnState implements GameState {

    @Override
    public void playerAttack(GameFacade game) {
        if (game == null || game.isBattleOver()) {
            return;
        }
            game.log("[Pattern][State] PlayerTurnState handling playerAttack().");


        Character player = game.getPlayer();
        Character enemy  = game.getEnemy();

        if (player == null || enemy == null || !player.isAlive() || !enemy.isAlive()) {
            return;
        }

        // Infer Mage vs Warrior by mana pool
        boolean isMage = player.getMaxMana() > 0;

        // Use the GUI-friendly Facade API (no FX code here)
        game.guiPlayerSwordOrStaffAttack(isMage);

        // Turn transition
        if (game.isBattleOver()) {
            game.setState(new GameOverState());
        } else {
            game.setState(new EnemyTurnState());
        }
    }

    @Override
    public void playerHeal(GameFacade game) {
        if (game == null || game.isBattleOver()) {
            return;
        }
            game.log("[Pattern][State] PlayerTurnState handling playerHeal().");


        Character player = game.getPlayer();
        if (player == null || !player.isAlive()) {
            return;
        }

        // Heal via Facade
        game.guiPlayerHeal();

        // Turn transition
        if (game.isBattleOver()) {
            game.setState(new GameOverState());
        } else {
            game.setState(new EnemyTurnState());
        }
    }

    @Override
    public String getName() {
        return "Player Turn";
    }
}
