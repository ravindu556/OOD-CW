package teammate;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class OrganizerMode implements MenuHandler {


    private static final Logger logger = AppLogger.getLogger(OrganizerMode.class);

    private  final Scanner sc = new Scanner(System.in);
    private  List<Participant> participants = new ArrayList<>();
    private  List<Team> formedTeams = new ArrayList<>();
    private  int teamSize = 5;

    @Override
    public  void run() {
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

    @Override
    public  void showMenu() {
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

    private  void loadParticipantsFromCSV() {

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


    private  void viewAllParticipants() {
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


    private  void setTeamSize() {
        logger.info("User is changing team size.");

        while (true) {
            System.out.print("\nEnter team size  or press Enter for default (5): ");
            String input = sc.nextLine().trim();


            if (input.isEmpty()) {
                teamSize = 5;
                logger.info("No input entered. Default team size applied: 5");
                System.out.println("\n Default team size applied: 5\n");
                break;
            }
            try {
                int size = Integer.parseInt(input);

                if (size >3 && size <= 10) {
                    teamSize = size;
                    logger.info("Team size updated to: " + teamSize);
                    System.out.println("\n Team size successfully updated to: " + teamSize + "\n");
                    break;
                } else {
                    logger.warning("Invalid team size entered: " + size);
                    System.out.println("\n Invalid range!  Try again.\n");
                }

            } catch (NumberFormatException e) {
                logger.warning("Invalid non-numeric input: " + input);
                System.out.println("\n Invalid input! Please enter a NUMBER.\n");
            }
        }
    }
    private  void formBalancedTeams() {
        logger.info("Attempting to form balanced teams using threading...");

        if (participants.isEmpty()) {
            System.out.println("No participants loaded!\n");
            pause();
            return;
        }

        if (participants.size() < teamSize) {
            System.out.println("Not enough participants! Need at least " + teamSize + "\n");
            pause();
            return;
        }

        System.out.println("Starting team formation with " + participants.size() + " participants...");
        System.out.print("Press Enter to continue or type 'cancel' to abort: ");
        if (sc.nextLine().trim().equalsIgnoreCase("cancel")) {
            System.out.println("Cancelled.\n");
            pause();
            return;
        }

        long start = System.currentTimeMillis();

        TeamBuilder builder = new TeamBuilder(new ArrayList<>(participants), teamSize);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Team>> future = executor.submit(() -> {



            // This entire call, which includes internal threading, runs in the background
            return builder.buildTeams();
        });

        try {
            // Main thread waits for the result (Blocking call, but keeps the menu loop clean)
            formedTeams.clear();
            formedTeams.addAll(future.get(60, TimeUnit.SECONDS));

            long time = System.currentTimeMillis() - start;
            logger.info("Teams successfully formed. Total teams: " + formedTeams.size());
            System.out.printf("\nTEAM FORMATION COMPLETE in %.2f seconds!\n", time / 1000.0);
            System.out.println("Successfully formed " + formedTeams.size() + " balanced teams!\n");

            List<Participant> leftover = builder.getUnassignedParticipants();

            if (!leftover.isEmpty()) {
                System.out.println("\n===============================================");
                System.out.println("   PARTICIPANTS NOT ASSIGNED TO ANY TEAM");
                System.out.println("===============================================\n");

                for (Participant p : leftover) {
                    System.out.printf("ID: %s | %-20s | Skill: %2d | %s%n",
                            p.getId(), p.getName(),
                            p.getSkillLevel(),
                            p.getPersonalityType());
                }

                System.out.println("\nReason:");
                System.out.println("• Not enough remaining participants OR");
                System.out.println("• Team constraints (Leader/Thinker/Role/Game) prevented valid team assignment\n");
            }

        } catch (TimeoutException e) {
            logger.severe("Team formation timed out.");
            System.out.println("\nERROR: Team formation timed out after 60 seconds.\n");
        } catch (Exception e) {
            logger.severe("Error during team formation: " + e.getMessage());
            e.printStackTrace();
            System.out.println("\nERROR during team formation.\n");
        } finally {
            // Must shut down the executor service
            executor.shutdown();
        }

        pause();
    }

private  void viewFormedTeams() {
    logger.info("User requested to view formed teams.");

    if (formedTeams.isEmpty()) {
        logger.warning("No teams formed yet.");
        System.out.println("\nNo teams formed yet. Please form teams first!\n");
        pause();
        return;
    }

    System.out.println("\n" + "-".repeat(80));
    System.out.println("                FORMED TEAMS (" + formedTeams.size() + " teams)");
    System.out.println("-".repeat(80));

    Collections.sort(formedTeams, Comparator.comparingInt(Team::getTeamNumber));


    for (Team team : formedTeams) {
        System.out.println(team);
    }

    System.out.println("-".repeat(80) + "\n");
    pause();
}

    private  void saveTeamsToCSV() {
        if (formedTeams.isEmpty()) {
            logger.warning("Attempted to save teams before any were formed.");
            System.out.println("\nNo teams to save!\n");
            return;
        }

        logger.info("Saving formed teams to CSV...");
        System.out.println("\nSaving formed teams to CSV!\n");
        CSVHandler.saveFormedTeams(formedTeams);
        pause();
    }


    private  int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                logger.warning("Invalid number entered by user.");
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private  void pause() {
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
}

