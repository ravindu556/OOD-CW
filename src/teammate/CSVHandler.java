package teammate;

import java.io.*;
import java.util.*;

public class CSVHandler {

    private static final String DEFAULT_FILE = "participants_sample.csv";

    public static List<Participant> loadParticipants(String filename) {
        List<Participant> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length < 8) continue;

                String id = data[0].trim();
                String name = data[1].trim();
                String email = data[2].trim();
                String game = data[3].trim();
                int skill = Integer.parseInt(data[4].trim());
                String role = data[5].trim();
                int score = Integer.parseInt(data[6].trim());
                String type = data[7].trim();

                list.add(new Participant(id, name, email, game, skill, role, score, type));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return list;
    }

    public static List<Participant> loadParticipants() {
        return loadParticipants(DEFAULT_FILE);
    }

    public static String generateNextId(List<Participant> participants) {
        int highest = 0;
        for (Participant p : participants) {
            if (p.getId().startsWith("P")) {
                try {
                    int num = Integer.parseInt(p.getId().substring(1));
                    if (num > highest) highest = num;
                } catch (Exception ignored) {
                }
            }
        }
        return String.format("P%03d", highest + 1);
    }


    public static void saveAllParticipants(List<Participant> participants) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DEFAULT_FILE))) {

            writer.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");


            for (Participant p : participants) {
                writer.println(p.toCSVLine());
            }
            System.out.println("All participants saved to " + DEFAULT_FILE);
        } catch (Exception e) {
            System.out.println("Failed to save file: " + e.getMessage());
        }

    }
    public static void saveFormedTeams(List<Team> teams) {
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
            System.out.println("All teams saved to formed_teams.csv");
        } catch (Exception e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }
}