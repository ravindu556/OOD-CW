package teammate;

import java.util.*;
import java.util.concurrent.*;
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

    private static void viewAllParticipants() {
        if (participants.isEmpty()) {
            System.out.println("\nNo participants found.\n");
        } else {
            System.out.println("\n=== ALL PARTICIPANTS (" + participants.size() + ") ===\n");
            for (Participant p : participants) {
                System.out.printf("P%03d | %-20s | %-10s | %-10s | Skill: %2d | Score: %3d | %s%n",
                        Integer.parseInt(p.getId().substring(1)),
                        p.getName().length() > 20 ? p.getName().substring(0, 17) + "..." : p.getName(),
                        p.getPreferredGame(),
                        p.getPreferredRole(),
                        p.getSkillLevel(),
                        p.getPersonalityScore(),
                        p.getPersonalityType());
            }
        }
        pause();
    }

    private static void setTeamSize() {
        System.out.print("\nEnter team size (3–6 recommended): ");
        int size = readInt();
        if (size >= 2 && size <= 10) {
            teamSize = size;
            System.out.println("Team size updated to " + teamSize + "\n");
        } else {
            System.out.println("Invalid size. Using default team size of 5.\n");
            teamSize = 5;
        }
    }

//    private static void formBalancedTeams() {
//        if (participants.size() < teamSize) {
//            System.out.println("Not enough participants for teams of size " + teamSize + "!\n");
//            pause();
//            return;
//        }
//
//        System.out.println("\nForming balanced teams using multi-threading algorithm...\n");
//        formedTeams = TeamBuilder.buildTeams(new ArrayList<>(participants), teamSize);
//        System.out.println("Successfully formed " + formedTeams.size() + " balanced team(s)!\n");
//        pause();
//    }

    private static void formBalancedTeams() {
        if (participants.size() < teamSize) {
            System.out.println("Not enough participants for teams of size " + teamSize + "!\n");
            pause();
            return;
        }

        System.out.println("\nForming balanced teams using multi-threading algorithm...\n");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Team>> future = executor.submit(() ->
                TeamBuilder.buildTeams(new ArrayList<>(participants), teamSize)
        );

        try {
            formedTeams = future.get(10, TimeUnit.SECONDS);
            System.out.println("Successfully formed " + formedTeams.size() + " balanced team(s)!\n");
        } catch (TimeoutException e) {
            System.out.println("Team formation timed out!\n");
        } catch (Exception e) {
            System.out.println("Error during team formation: " + e.getMessage() + "\n");
        } finally {
            executor.shutdown();
        }

        pause();
    }

    private static void viewFormedTeams() {
        if (formedTeams.isEmpty()) {
            System.out.println("\nNo teams have been formed yet. Please select option 3 first.\n");
        } else {
            System.out.println(TeamBuilder.displayTeams());
        }
        pause();
    }

    private static void saveTeamsToCSV() {
        if (formedTeams.isEmpty()) {
            System.out.println("\nNo teams to save!\n");
            return;
        }
        CSVHandler.saveFormedTeams(formedTeams);
        pause();
    }


    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private static void pause() {
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
}