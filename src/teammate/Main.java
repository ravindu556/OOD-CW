package teammate;

import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = AppLogger.getLogger(Main.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("             TEAM MATE SYSTEM");
        System.out.println("==================================================\n");

        while (true) {
            System.out.println("Who are you?");
            System.out.println("1. Participant ");
            System.out.println("2. Organizer");
            System.out.println("3. Exit");
            System.out.print("\nEnter choice (1-3): ");


            int choice = readInt();
            logger.info("User selected main menu option: " + choice);
            if (choice == 1) {
                MenuHandler user = new ParticipantMode();

                user.run();
            } else if (choice == 2) {
                MenuHandler user = new OrganizerMode();

                user.run();


        } else if (choice == 3) {
                logger.info("Application terminated by user.");
                System.out.println("\nThank you for using TeamMate System. Goodbye!\n");
                break;
            } else {
                logger.warning("Invalid menu choice: " + choice);
                System.out.println("Invalid choice. Please enter 1, 2 or 3.\n");
            }
        }
        scanner.close();
    }
        private static int readInt () {
            while (true) {
                try {
                    return Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    logger.warning("Invalid number entered.");
                    System.out.print("Please enter a valid number: ");
                }
            }
        }

    }


