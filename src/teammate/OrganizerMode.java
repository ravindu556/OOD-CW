package teammate;

import java.util.*;

public class OrganizerMode {

    private static final Scanner sc = new Scanner(System.in);
    private static List<Participant> participants;
    private static List<Team> formedTeams = new ArrayList<>();
    private static int teamSize = 5;

    public static void run() {
        participants = CSVHandler.loadParticipants();
        System.out.println("\nLoaded " + participants.size() + " participants from participants.csv\n");

        while (true) {
            showMenu();

            int choice = readInt();

            switch (choice) {
                case 1 -> viewAllParticipants();
                case 2 -> setTeamSize();
                case 3 -> formBalancedTeams();
                case 4 -> viewFormedTeams();
                case 5 -> saveTeamsToCSV();
                case 6 -> {
                    System.out.println("\nReturning to main menu...\n");
                    return;
                }
                default -> System.out.println("Invalid option. Please choose 1–6.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("==================================================");
        System.out.println("           ORGANIZER MODE                         ");
        System.out.println("==================================================");
        System.out.println("1. View All Participants");
        System.out.println("2. Set Team Size (current: " + teamSize + ")");
        System.out.println("3. Form Balanced Teams");
        System.out.println("4. View Formed Teams");
        System.out.println("5. Save Teams to formed_teams.csv");
        System.out.println("6. Return to Main Menu");
        System.out.print("\nEnter your choice (1-6): ");
    }

}