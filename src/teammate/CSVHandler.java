package teammate;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class CSVHandler {

    private static final Logger logger = AppLogger.getLogger(CSVHandler.class);
    private static final String DEFAULT_FILE = "participants_sample.csv";

    public static List<Participant> loadParticipants(String filePath) {

        logger.info("Attempting to load participants from file: " + filePath);

        List<Participant> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line = br.readLine(); // Skip header
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",", -1);
                if (data.length < 8) {
                    logger.warning("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    String id = data[0].trim();
                    String name = data[1].trim();
                    String email = data[2].trim();
                    String game = data[3].trim();
                    int skill = Integer.parseInt(data[4].trim());
                    String role = data[5].trim();
                    int score = Integer.parseInt(data[6].trim());
                    String type = data[7].trim();

                    list.add(new Participant(id, name, email, game, skill, role, score, type));
                    count++;

                } catch (Exception ex) {
                    logger.severe("Failed to parse row: " + line + " | Reason: " + ex.getMessage());
                }
            }

            logger.info("CSV load complete — " + count + " participants successfully loaded.");

        } catch (FileNotFoundException e) {
            logger.severe("File not found: " + filePath);
        } catch (IOException e) {
            logger.severe("Error reading file: " + e.getMessage());
        }

        return list;
    }

    public static List<Participant> loadParticipants() {
        logger.info("Loading default file: " + DEFAULT_FILE);
        return loadParticipants(DEFAULT_FILE);
    }

    public static String generateNextId(List<Participant> participants) {
        logger.fine("Generating next participant ID...");
        int highest = 0;

        for (Participant p : participants) {
            if (p.getId().startsWith("P")) {
                try {
                    int num = Integer.parseInt(p.getId().substring(1));
                    if (num > highest) highest = num;
                } catch (Exception ignored) { }
            }
        }

        String newId = String.format("P%03d", highest + 1);
        logger.info("Generated new participant ID: " + newId);
        return newId;
    }

    public static void saveAllParticipants(List<Participant> participants) {
        logger.info("Saving " + participants.size() + " participants to file: " + DEFAULT_FILE);

        try (PrintWriter writer = new PrintWriter(new FileWriter(DEFAULT_FILE))) {

            writer.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");

            for (Participant p : participants) {
                writer.println(p.toCSVLine());
            }

            logger.info("Successfully saved participants to " + DEFAULT_FILE);

        } catch (Exception e) {
            logger.severe("Failed to save file: " + e.getMessage());
        }
    }

    public static void saveFormedTeams(List<Team> teams) {
        logger.info("Saving formed teams to formed_teams.csv");
        try (PrintWriter w = new PrintWriter("formed_teams.csv")) {

            w.println("TeamNumber,MemberID,Name,Email,Game,Role,Skill,Score,PersonalityType");

            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    w.println(team.getTeamNumber() + "," +
                            p.getId() + "," +
                            p.getName() + "," +
                            p.getEmail() + "," +
                            p.getPreferredGame() + "," +
                            p.getPreferredRole() + "," +
                            p.getSkillLevel() + "," +
                            p.getPersonalityScore() + "," +
                            p.getPersonalityType());
                }
            }

            logger.info("Successfully saved formed teams to CSV.");

        } catch (Exception e) {
            logger.severe("Save failed: " + e.getMessage());
        }
    }
}
