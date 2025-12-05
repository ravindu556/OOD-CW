package teammate;

import java.util.logging.Logger;

public class Participant {
    private static final Logger logger = AppLogger.getLogger(Participant.class);
    private String id;
    private String name;
    private String email;
    private String preferredGame;
    private int skillLevel;
    private String preferredRole;
    private int personalityScore;
    private String personalityType;


    public Participant(String id, String name, String email, String game,
                       int skillLevel, String role, int score, String type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.preferredGame = game;
        this.skillLevel = skillLevel;
        this.preferredRole = role;
        this.personalityScore = score;
        this.personalityType = type;
        logger.fine("Participant loaded from CSV: " + id + " (" + type + ")");
    }

    public Participant(String id, String name, String email, String game,
                       int skillLevel, String role, int rawTotal5Q) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.preferredGame = game;
        this.skillLevel = skillLevel;
        this.preferredRole = role;
        this.personalityScore = rawTotal5Q * 4;
        this.personalityType = classify(rawTotal5Q * 4);

        logger.info("New participant created: " + name +
                " | Score: " + this.personalityScore +
                " | Type: " + personalityType);
    }

    private String classify(int score) {
        if (score >= 90) return "Leader";
        else if (score >= 70) return "Balanced";
        else return "Thinker";
    }

    public String getId() {
        return id;
    }

    public String getParticipantId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPreferredGame() {
        return preferredGame;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public String getPreferredRole() {
        return preferredRole;
    }

    public int getPersonalityScore() {
        return personalityScore;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    @Override
    public String toString() {
        return String.format("%-6s │ %-18s │ %-10s │ %-12s │ %2d │ %-9s (%3d)",
                id, name, preferredGame, preferredRole, skillLevel, personalityType, personalityScore);
    }

    public String toCSVLine() {
        return id + "," + name + "," + email + "," + preferredGame + "," +
                skillLevel + "," + preferredRole + "," + personalityScore + "," + personalityType;
    }
}

