package edu.neu.csye7374;

import java.util.Scanner;

import edu.neu.csye7374.javafx.FXLauncher;

/**
 * 
 * @author Yash Zaveri
 * 
 */

public class Driver {
    public static void main(String[] args) {

        // Add your code in between these two print statements
        System.out.println("============ Main Execution Start ===================\n");
        Scanner sc = new Scanner(System.in);
        System.out.println("1. Console RPG");
        System.out.println("2. JavaFX RPG");
        System.out.print("Enter choice: ");

        int choice = Integer.parseInt(sc.nextLine().trim());

        if (choice == 2) {
            FXLauncher.main(new String[] {});
        } else {
            Demo.gameRun();
        }

        System.out.println("\n============ Main Execution End =====================");
    }

}
