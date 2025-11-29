package teammate;

import java.util.*;


public class TeamBuilder {

    // Static list to store all formed teams
    private static List<Team> formedTeams = new ArrayList<>();


    public static List<Team> buildTeams(List<Participant> all, int teamSize) {
        // Clear any previously formed teams
        formedTeams.clear();

        // Validate input parameters
        if (all == null || all.isEmpty() || teamSize < 1) {
            return formedTeams;
        }

        // Create working copy and shuffle for randomization
        List<Participant> players = new ArrayList<>(all);
        Collections.shuffle(players);

        // ========== PHASE 0: CATEGORIZE BY PERSONALITY TYPE ==========
        List<Participant> leaders  = new ArrayList<>();
        List<Participant> thinkers = new ArrayList<>();
        List<Participant> balanced = new ArrayList<>();

        // Separate participants into personality categories
        for (Participant p : players) {
            String type = p.getPersonalityType();
            if ("Leader".equals(type)) {
                leaders.add(p);
            } else if ("Thinker".equals(type)) {
                thinkers.add(p);
            } else {
                balanced.add(p);
            }
        }


        int totalPlayers = players.size();
        int fullTeams    = totalPlayers / teamSize;
        int remainder    = totalPlayers % teamSize;
        int totalTeams   = fullTeams + (remainder > 0 ? 1 : 0);

        // Create empty team objects (numbered 1, 2, 3, ...)
        for (int i = 1; i <= totalTeams; i++) {
            formedTeams.add(new Team(i));
        }


        leaders.sort((a, b) -> Integer.compare(b.getSkillLevel(), a.getSkillLevel()));
        thinkers.sort((a, b) -> Integer.compare(b.getSkillLevel(), a.getSkillLevel()));
        balanced.sort((a, b) -> Integer.compare(b.getSkillLevel(), a.getSkillLevel()));

        // ========== PHASE 1: MANDATORY LEADER DISTRIBUTION ==========
        // Guarantee: Every team gets at least 1 Leader
        int leaderIdx = 0;
        for (int t = 0; t < totalTeams && leaderIdx < leaders.size(); t++) {
            formedTeams.get(t).addMember(leaders.get(leaderIdx++));
        }

        // ========== PHASE 2: MANDATORY THINKER DISTRIBUTION ==========
        // Guarantee: Every team gets at least 1 Thinker
        int thinkerIdx = 0;
        for (int t = 0; t < totalTeams && thinkerIdx < thinkers.size(); t++) {
            formedTeams.get(t).addMember(thinkers.get(thinkerIdx++));
        }

        // ========== PHASE 3: COLLECT REMAINING PLAYERS ==========
        List<Participant> remaining = new ArrayList<>();

        // Add unassigned leaders (teams can have 2 leaders for balance)
        while (leaderIdx < leaders.size()) {
            remaining.add(leaders.get(leaderIdx++));
        }

        // Add unassigned thinkers (teams can have up to 2 thinkers)
        while (thinkerIdx < thinkers.size()) {
            remaining.add(thinkers.get(thinkerIdx++));
        }

        // Add all balanced personality participants
        remaining.addAll(balanced);

        // Sort remaining by skill (highest first) for balanced distribution
        remaining.sort((a, b) -> Integer.compare(b.getSkillLevel(), a.getSkillLevel()));

        // ========== PHASE 4: SKILL-BALANCED DISTRIBUTION ==========
        // CORE ALGORITHM: Always add to team with LOWEST current average skill
        // This continuously equalizes team skill levels throughout distribution
        for (Participant p : remaining) {
            boolean placed = false;

            // Sort teams by current average skill (lowest first)
            // This ensures we always try to balance to the weakest team first
            List<Team> sortedTeams = new ArrayList<>(formedTeams);
            sortedTeams.sort(Comparator.comparingDouble(TeamBuilder::getTeamAvgSkill));

            // TRY 1: Place with strict constraints
            for (Team team : sortedTeams) {
                int teamIdx = formedTeams.indexOf(team);
                // Calculate target size (last team might be smaller)
                int targetSize = (teamIdx == totalTeams - 1 && remainder > 0)
                        ? remainder
                        : teamSize;

                // Check if team has space and participant meets all constraints
                if (team.getMembers().size() < targetSize) {
                    if (canAddToTeam(team, p)) {
                        team.addMember(p);
                        placed = true;
                        break;
                    }
                }
            }

            // TRY 2: Fallback with relaxed thinker constraint
            // If strict rules block placement, allow 3rd thinker if absolutely needed
            if (!placed) {
                for (Team team : sortedTeams) {
                    int teamIdx = formedTeams.indexOf(team);
                    int targetSize = (teamIdx == totalTeams - 1 && remainder > 0)
                            ? remainder
                            : teamSize;

                    if (team.getMembers().size() < targetSize) {
                        if (canAddRelaxed(team, p)) {
                            team.addMember(p);
                            placed = true;
                            break;
                        }
                    }
                }
            }

            // TRY 3: Last resort - add to any incomplete team
            // Ensures no participant is left unassigned
            if (!placed) {
                for (int i = 0; i < totalTeams; i++) {
                    Team team = formedTeams.get(i);
                    int targetSize = (i == totalTeams - 1 && remainder > 0)
                            ? remainder
                            : teamSize;
                    if (team.getMembers().size() < targetSize) {
                        team.addMember(p);
                        break;
                    }
                }
            }
        }

    }
}