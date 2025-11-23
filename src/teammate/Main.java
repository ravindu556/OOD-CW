package teammate;

import java.util.Scanner;



public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("             TEAM MATE SYSTEM");
        System.out.println("==================================================\n");

        while (true) {
            System.out.println("Who are you?");
            System.out.println("1. Participant (complete personality survey only)");
            System.out.println("2. Organizer (manage teams and form groups)");
            System.out.println("3. Exit");
            System.out.print("\nEnter choice (1-3): ");


            int choice = readInt();
            if (choice == 1) {

                 ParticipantMode.run();
            } else if (choice == 2) {
                //System.out.println("\nOrganizer mode not ready yet – coming next!");
                OrganizerMode.run();
            } else if (choice == 3) {
                System.out.println("\nThank you for using TeamMate System. Goodbye!\n");
                break;
            } else {
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
                    System.out.print("Please enter a valid number: ");
                }
            }
        }

    }



