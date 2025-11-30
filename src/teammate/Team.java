
package teammate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Team {
    private final int teamNumber;
    private final List<Participant> members = new ArrayList<>();

    public Team(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public void addMember(Participant p) {
        members.add(p);
    }

    public List<Participant> getMembers() {
        return members;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    // NEW: Beautiful one-line summary
    public String getSummary() {
        if (members.isEmpty()) return "TEAM " + teamNumber + " | Empty";

        double avgSkill = members.stream()
                .mapToInt(Participant::getSkillLevel)
                .average().orElse(0.0);

        Map<String, Long> personalityCount = members.stream()
                .collect(Collectors.groupingBy(
                        Participant::getPersonalityType,
                        Collectors.counting()
                ));

        String leader  = "Leader".equals(personalityCount.keySet().iterator().next()) ?
                personalityCount.getOrDefault("Leader", 0L) + " Leader" :
                personalityCount.getOrDefault("Leader", 0L) + " Leader";

        String thinker = personalityCount.getOrDefault("Thinker", 0L) + " Thinker" + (personalityCount.getOrDefault("Thinker", 0L) > 1 ? "s" : "");
        String balanced = personalityCount.getOrDefault("Balanced", 0L) + " Balanced";

        String personality = leader + ", " + balanced + ", " + thinker;
        if (personality.startsWith("0")) personality = personality.replaceFirst("0 [^,]+, ", "");

        return String.format("TEAM %-2d │ Avg Skill: %4.1f │ Personality: %s",
                teamNumber, avgSkill, personality.trim());
    }
    // Inside Team.java:
    public double getAverageSkill() {
        if (members.isEmpty()) return 0.0;
        return members.stream()
                .mapToInt(Participant::getSkillLevel)
                .average().orElse(0.0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append(getSummary()).append("\n");
        sb.append("--------------------------------------------------------------------------------\n");
        for (Participant p : members) {
            sb.append(String.format(" • %-20s | %-12s | %-12s | Skill: %2d | Score: %3d | %s%n",
                    p.getName(), p.getPreferredGame(), p.getPreferredRole(),
                    p.getSkillLevel(), p.getPersonalityScore(), p.getPersonalityType()));
        }
        sb.append("\n");
        return sb.toString();
    }
}