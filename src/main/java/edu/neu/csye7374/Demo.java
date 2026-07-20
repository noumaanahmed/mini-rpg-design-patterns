package edu.neu.csye7374;

import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 * Demo entry point for the console version.
 *
 * Design Pattern: Template Method (overall game loop)
 * ---------------------------------------------------
 * The battle loop follows a fixed algorithm with pluggable details:
 *  1. Show menu and stats
 *  2. Read player choice
 *  3. Queue commands in CommandInvoker
 *  4. Execute commands
 *  5. (Optional) Enemy turn
 *  6. Check win/lose and repeat
 *
 * NEW:
 *  - Warrior vs Mage differentiation
 *  - Mage has mana and can choose Staff Attack or Fireball
 *  - If Mage tries Fireball with no mana, they get punished
 *  - Heal action does NOT trigger an enemy attack that turn
 */
public class Demo {

    private static final Random RAND = new Random();

    public static void gameRun() {
        Scanner sc = new Scanner(System.in);

        System.out.println();
        System.out.println("===========================================");
        System.out.println("           MINI RPG CONSOLE GAME           ");
        System.out.println("===========================================\n");

        // --- Singleton Pattern in use ---
        System.out.println("[Pattern] Using Singleton (GameConfig) for difficulty.");
        GameConfig config = GameConfig.getInstance();

        int diff = readIntInRange(sc,
                "Enter difficulty (1-Easy, 2-Normal, 3-Hard): ",
                1, 3);
        config.setDifficulty(diff);

        // Goblin HP scaling based on difficulty
        int goblinHP;
        switch (diff) {
            case 1:
                goblinHP = 50;
                break;
            case 2:
                goblinHP = 80;
                break;
            case 3:
                goblinHP = 120;
                break;
            default:
                goblinHP = 80;
                break;
        }
        if (goblinHP < 1) goblinHP = 1;

        System.out.println();
        System.out.println("-------------------------------------------");
        System.out.println("Difficulty set to: " + diff + "  |  Goblin HP = " + goblinHP);
        System.out.println("-------------------------------------------\n");

        // --- Observer / Bridge Pattern ---
        System.out.println("[Pattern] Using Observer/Bridge for logging (ConsoleLogger, GUI adapter).");
        ConsoleLogger logger = new ConsoleLogger("GameLogger");

        // --- Factory + Builder Pattern for Player & Enemy ---
        System.out.println("[Pattern] Using Factory (CharacterFactory) + Builder (CharacterBuilder) for characters.\n");

        System.out.println("Choose your class:");
        System.out.println("  1. Warrior (strong physical attacks)");
        System.out.println("  2. Mage    (weaker staff, strong fireball with mana)");
        int classChoice = readIntInRange(sc, "Enter choice: ", 1, 2);
        boolean isMage = (classChoice == 2);

        System.out.print("\nEnter your character name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            name = "Hero";
        }

        Character player = (classChoice == 1)
                ? CharacterFactory.createCharacter("warrior", name)
                : CharacterFactory.createCharacter("mage", name);
        player.addObserver(logger);

        CharacterBuilder enemyBuilder = new CharacterBuilder()
                .setName("Goblin")
                .setHealth(goblinHP);
        Character enemy = enemyBuilder.build();
        enemy.addObserver(logger);

        // Mage mana pool is tracked locally, not in Character
        final int[] playerMana = new int[1];
        final int maxMana;
        if (isMage) {
            playerMana[0] = 40; // starting mana
            maxMana = 40;
            System.out.println("[Info] As a Mage, you start with " + maxMana + " mana.");
        } else {
            playerMana[0] = 0;
            maxMana = 0;
        }

        // --- Strategy Pattern ---
        System.out.println();
        System.out.println("[Pattern] Using Strategy for mode: Aggressive (Attack) vs Defensive (Heal).");
        int strat = readIntInRange(sc,
                "\nSelect starting mode:\n"
                        + "  1. Aggressive (Action = Attack)\n"
                        + "  2. Defensive (Action = Heal)\n"
                        + "Enter choice: ",
                1, 2);

        if (strat == 1) {
            player.setStrategy(new AggressiveAttack());
        } else {
            player.setStrategy(new DefensiveAttack());
        }
        // Enemy always aggressive
        enemy.setStrategy(new AggressiveAttack());

        // --- Command Pattern ---
        System.out.println("[Pattern] Using Command + CommandInvoker to queue actions.\n");
        CommandInvoker invoker = new CommandInvoker();

        boolean playing = true;
        boolean lastActionWasHeal = false;

        System.out.println();
System.out.println("[Pattern][Template] Starting main battle loop (Demo.gameRun).");



        while (playing) {
            // ===== Menu & Status =====
            System.out.println();
            System.out.println("===========================================");
            System.out.println("                 BATTLE MENU               ");
            System.out.println("===========================================");
            System.out.println("Current Strategy : [" + playerStrategyName(player) + "]");
            System.out.println("Player HP        : " + player.getHealth());
            if (isMage) {
                System.out.println("Player Mana      : " + playerMana[0] + "/" + maxMana);
            }
            System.out.println("Goblin HP        : " + enemy.getHealth());
            System.out.println("-------------------------------------------");

            boolean isAggressive = player.getStrategy() instanceof AggressiveAttack;
            if (isAggressive) {
                System.out.println("  1. Attack");
            } else {
                System.out.println("  1. Heal");
            }
            System.out.println("  2. Change Strategy (Aggressive / Defensive)");
            System.out.println("  3. Quit Game");

            int action = readIntInRange(sc, "\nChoose an option: ", 1, 3);
            System.out.println();

            switch (action) {
                case 1:
                    invoker = new CommandInvoker();
                    if (isAggressive) {
                        lastActionWasHeal = false;
                        // Command encapsulates the full attack decision (staff vs fireball etc.)
                        Command attackCmd = () -> handlePlayerAttack(sc, player, enemy, isMage, playerMana, maxMana);
                        invoker.addCommand(attackCmd);
                    } else {
                        lastActionWasHeal = true;
                        Command healCmd = () -> handlePlayerHeal(player);
                        invoker.addCommand(healCmd);
                    }
                    break;

                case 2:
                    int s = readIntInRange(sc,
                            "\nSelect new strategy mode:\n"
                                    + "  1. Aggressive (Action = Attack)\n"
                                    + "  2. Defensive (Action = Heal)\n"
                                    + "Enter choice: ",
                            1, 2);
                    if (s == 1) {
                        player.setStrategy(new AggressiveAttack());
                    } else {
                        player.setStrategy(new DefensiveAttack());
                    }
                    System.out.println("\n[Info] Strategy mode changed successfully.\n");
                    continue;

                case 3:
                    System.out.println("[Info] Exiting game...\n");
                    playing = false;
                    continue;
            }

            // Execute all queued commands this turn
            invoker.executeAll();

            // Enemy turn happens ONLY if last action was NOT heal
            if (playing && !lastActionWasHeal && enemy.isAlive() && player.isAlive()) {
                enemyTurn(enemy, player);
            }

            // Check results
            if (!player.isAlive()) {
                System.out.println();
                System.out.println("===========================================");
                System.out.println("               YOU WERE DEFEATED           ");
                System.out.println("===========================================\n");
                playing = false;
            } else if (!enemy.isAlive()) {
                System.out.println();
                System.out.println("===========================================");
                System.out.println("             YOU DEFEATED GOBLIN           ");
                System.out.println("===========================================\n");
                playing = false;
            }
        }

        System.out.println("=== GAME OVER ===\n");
        sc.close();
    }

    // ======================
    // Player action helpers
    // ======================

    private static void handlePlayerAttack(Scanner sc,
                                           Character player,
                                           Character enemy,
                                           boolean isMage,
                                           int[] playerMana,
                                           int maxMana) {
        if (!player.isAlive() || !enemy.isAlive()) return;

        if (isMage) {
            System.out.println("Choose your attack:");
            System.out.println("  1. Staff Attack (low damage, no mana cost)");
            System.out.println("  2. Cast Fireball (high damage, costs mana)");
            int choice = readIntInRange(sc, "Enter choice: ", 1, 2);

            if (choice == 1) {
                int dmg = randomBetween(6, 12);
                enemy.takeDamage(dmg);
                player.notifyObservers(player.getName()
                        + " strikes the Goblin with staff for " + dmg + " damage!");
            } else {
                final int cost = 10;
                if (playerMana[0] >= cost) {
                    playerMana[0] -= cost;
                    int dmg = randomBetween(18, 25);
                    enemy.takeDamage(dmg);
                    player.notifyObservers(player.getName()
                            + " casts Fireball for " + dmg + " damage! (Mana: "
                            + playerMana[0] + "/" + maxMana + ")");
                } else {
                    player.notifyObservers("Not enough mana to cast Fireball! The spell fizzles and the Goblin punishes you!");
                    int counter = randomBetween(8, 15);
                    player.takeDamage(counter);
                }
            }
        } else {
            // Warrior: strong physical attacks only
            int dmg = randomBetween(12, 20);
            enemy.takeDamage(dmg);
            player.notifyObservers(player.getName()
                    + " swings mightily and hits the Goblin for " + dmg + " damage!");
        }
    }

    private static void handlePlayerHeal(Character player) {
        if (!player.isAlive()) return;
        int healAmt = randomBetween(10, 16);
        player.heal(healAmt);
        // Character.heal already notifies observers
    }

    private static void enemyTurn(Character enemy, Character player) {
        System.out.println();
        System.out.println("--------------- Enemy Turn ---------------");
        enemy.attack(player); // Uses AggressiveAttack strategy
    }

    private static int randomBetween(int min, int max) {
        return RAND.nextInt((max - min) + 1) + min;
    }

    private static String playerStrategyName(Character player) {
        AttackStrategy s = player.getStrategy();
        return (s == null) ? "None" : s.getName();
    }

    // --- Helper methods for safe input ---

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = sc.nextInt();
                sc.nextLine(); // consume newline
                return value;
            } catch (InputMismatchException e) {
                System.out.println("[Warning] Invalid input. Please enter a number.");
                sc.nextLine(); // clear invalid input
            }
        }
    }

    private static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            int value = readInt(sc, prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("[Warning] Please enter a number between "
                    + min + " and " + max + ".");
        }
    }
}
