package teammate;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class OrganizerMode {

    private static final Logger logger = AppLogger.getLogger(OrganizerMode.class);

    private static final Scanner sc = new Scanner(System.in);
    private static List<Participant> participants = new ArrayList<>();
    private static List<Team> formedTeams = new ArrayList<>();
    private static int teamSize = 5;

    public static void run() {

        logger.info("Organizer Mode started.");

        while (true) {
            showMenu();
            int choice = readInt();

            logger.info("User selected menu option: " + choice);

            switch (choice) {
                case 1 -> loadParticipantsFromCSV();
                case 2 -> viewAllParticipants();
                case 3 -> setTeamSize();
                case 4 -> formBalancedTeams();
                case 5 -> viewFormedTeams();
                case 6 -> saveTeamsToCSV();
                case 7 -> {
                    logger.info("User exited Organizer Mode.");
                    System.out.println("\nReturning to main menu...\n");
                    return;
                }
                default -> {
                    logger.warning("Invalid menu selection: " + choice);
                    System.out.println("Invalid option. Please choose 1–7.");
                }
            }
        }
    }

    private static void showMenu() {
        System.out.println("==================================================");
        System.out.println("           ORGANIZER MODE                         ");
        System.out.println("==================================================");
        System.out.println("1. Load Participants from CSV " );
        System.out.println("2. View All Participants");
        System.out.println("3. Set Team Size (current: " + teamSize + ")");
        System.out.println("4. Form Balanced Teams");
        System.out.println("5. View Formed Teams");
        System.out.println("6. Save Teams to formed_teams.csv");
        System.out.println("7. Return to Main Menu");
        System.out.print("\nEnter your choice (1-7): ");
    }

    private static void loadParticipantsFromCSV() {

        logger.info("Attempting to load participants from CSV...");

        System.out.println(" • Press Enter → load default file: participants_sample.csv");
        System.out.println(" • Type filename ");
        System.out.println(" • Enter full path ");
        System.out.print("\nEnter file path or press Enter for default: ");
        String input = sc.nextLine().trim();
        String filePath = input.isEmpty() ? "participants_sample.csv" : input;

        File file = new File(filePath);

        if (!file.exists()) {
            logger.severe("CSV NOT FOUND: " + file.getAbsolutePath());
            System.out.println("\nERROR: File not found!");
            pause();
            return;
        }

        logger.info("CSV found. Loading file: " + filePath);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Participant>> future = executor.submit(() ->
                CSVHandler.loadParticipants(filePath)
        );

        try {
            participants = future.get(15, TimeUnit.SECONDS);
            logger.info("Successfully loaded " + participants.size() + " participants.");
            System.out.println("SUCCESS!");
            System.out.println("   Loaded " + participants.size() + " participants");
        } catch (TimeoutException e) {
            logger.severe("CSV loading timed out.");
            System.out.println("ERROR: Loading took too long.\n");
        } catch (Exception e) {
            logger.severe("Error loading CSV: " + e.getMessage());
            System.out.println("ERROR during loading.\n");
        } finally {
            executor.shutdown();
        }

        pause();
    }


    private static void viewAllParticipants() {
        logger.info("User chose to view all participants.");

        if (participants.isEmpty()) {
            logger.warning("No participants to display.");
            System.out.println("\nNo participants found.\n");
        } else {
            logger.info("Displaying " + participants.size() + " participants.");
            System.out.println("\n=== ALL PARTICIPANTS (" + participants.size() + ") ===\n");
            for (Participant p : participants) {
                System.out.printf("P%03d | %-20s | %-10s | %-10s | Skill: %2d | Score: %3d | %s%n",
                        Integer.parseInt(p.getId().substring(1)),
                        p.getName(),
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
        logger.info("User is changing team size.");

        System.out.print("\nEnter team size (3–6 recommended): ");
        int size = readInt();

        if (size >= 2 && size <= 10) {
            teamSize = size;
            logger.info("Team size updated to: " + teamSize);
        } else {
            logger.warning("Invalid team size entered: " + size + ". Reset to default.");
            teamSize = 5;
        }
    }


    private static void formBalancedTeams() {

        logger.info("Attempting to form balanced teams using threading...");

        if (participants.size() < teamSize) {
            logger.warning("Not enough participants to form a team. Required: " + teamSize +
                    ", Available: " + participants.size());
            System.out.println("Not enough participants!\n");
            pause();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Team>> future = executor.submit(() ->
                TeamBuilder.buildTeams(new ArrayList<>(participants), teamSize)
        );

        try {
            formedTeams = future.get(10, TimeUnit.SECONDS);
            logger.info("Teams successfully formed. Total teams: " + formedTeams.size());
            System.out.println("Teams formed successfully!\n");
        } catch (TimeoutException e) {
            logger.severe("Team formation timed out.");
        } catch (Exception e) {
            logger.severe("Error during team formation: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        pause();
    }


    private static void viewFormedTeams() {
        logger.info("User requested to view formed teams.");

        if (formedTeams.isEmpty()) {
            logger.warning("No teams formed yet.");
            System.out.println("\nNo teams yet.\n");
        } else {
            logger.info("Displaying " + formedTeams.size() + " teams.");
            System.out.println(TeamBuilder.displayTeams());
        }
        pause();
    }


    private static void saveTeamsToCSV() {
        if (formedTeams.isEmpty()) {
            logger.warning("Attempted to save teams before any were formed.");
            System.out.println("\nNo teams to save!\n");
            return;
        }

        logger.info("Saving formed teams to CSV...");
        CSVHandler.saveFormedTeams(formedTeams);
        pause();
    }


    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                logger.warning("Invalid number entered by user.");
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private static void pause() {
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
}
