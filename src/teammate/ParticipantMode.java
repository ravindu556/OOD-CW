package teammate;

import java.util.Scanner;
import java.util.List;
import java.util.logging.Logger;

public class ParticipantMode implements MenuHandler {

    private static final Logger logger = AppLogger.getLogger(ParticipantMode.class);
    private  final Scanner sc = new Scanner(System.in);

    @Override
    public void showMenu() {
        System.out.println("\n===== PARTICIPANT MENU =====");
        System.out.println("→ This mode automatically starts the survey.");
        System.out.println("(Press Enter to continue)");
    }
    @Override
    public  void run() {
        logger.info("Participant survey started.");
        System.out.println("\n=== Personality & Preference Survey ===\n");

        String name = readName();
        String email = readEmail();

        System.out.println("\nRate each statement (1 = Strongly Disagree, 5 = Strongly Agree):");
        int q1 = askQuestion("1. I enjoy taking the lead in group situations");
        int q2 = askQuestion("2. I prefer analyzing situations before acting");
        int q3 = askQuestion("3. I work well in team environments");
        int q4 = askQuestion("4. I stay calm under pressure");
        int q5 = askQuestion("5. I like making quick decisions");

        int rawTotal = q1 + q2 + q3 + q4 + q5;
        logger.info("Survey raw personality score: " + rawTotal);

        System.out.println("\nChoose your preferred game:");
        System.out.println("1. Chess     2. FIFA     3. CS:GO     4. DOTA 2     5. Valorant     6. Basketball");
        System.out.print("Enter number (1-6): ");
        String[] games = {"", "Chess", "FIFA", "CS:GO", "DOTA 2", "Valorant", "Basketball"};
        int gameChoice = readIntInRange(1, 6);
        String game = games[gameChoice];

        logger.info("Game selected: " + game);

        System.out.println("\nChoose your preferred role:");
        System.out.println("1. Strategist   2. Attacker   3. Defender   4. Supporter   5. Coordinator");
        System.out.print("Enter number (1-5): ");
        String[] roles = {"", "Strategist", "Attacker", "Defender", "Supporter", "Coordinator"};
        int roleChoice = readIntInRange(1, 5);
        String role = roles[roleChoice];

        logger.info("Role selected: " + role);

        System.out.print("\nRate your skill level in " + game + " (1 = Beginner, 10 = Pro): ");
        int skill = readIntInRange(1, 10);
        logger.info("Skill level selected: " + skill);

        List<Participant> all = CSVHandler.loadParticipants();
        String newId = CSVHandler.generateNextId(all);


        Participant newParticipant = new Participant(newId, name, email, game, skill, role, rawTotal);

        all.add(newParticipant);
        CSVHandler.saveAllParticipants(all);

        logger.info("Participant saved: " + newId + " (" + name + ")");

        System.out.println("\nSUCCESS!");
        System.out.println("You have been added as: " + newId);
        System.out.println("Personality Score: " + (rawTotal * 4) + " → " + newParticipant.getPersonalityType());
        System.out.println("Your data has been saved to participants.csv\n");
        System.out.println("Press Enter to return to main menu...");
        sc.nextLine();
    }

    private  int askQuestion(String question) {
        System.out.print(question + ": ");
        return readIntInRange(1, 5);
    }

    private  int readIntInRange(int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.print("Please enter a number between " + min + " and " + max + ": ");
            } catch (NumberFormatException e) {
                logger.warning("Invalid numeric input from user.");
                System.out.print("Invalid input. Enter a number between " + min + " and " + max + ": ");
            }
        }
    }



    private  String readName() {
        while (true) {
            System.out.print("Enter your full name (letters and spaces only): ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Name cannot be empty!");
                continue;
            }
            if (input.matches("[a-zA-Z\\s]+")) {
                return input;
            }
            logger.warning("Invalid name entry: " + input);
            System.out.println("Invalid name! Use only letters and spaces.");
        }
    }

    private  String readEmail() {
        while (true) {
            System.out.print("Enter your email (must contain @ and . ): ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Email cannot be empty!");
                continue;
            }
            if (input.contains("@") && input.contains(".") ) {
                logger.info("Email entered: " + input);
                return input;
            }
            logger.warning("Invalid email entry: " + input);
            System.out.println("Invalid email! Must contain @ and a dot )");
        }
    }
}






