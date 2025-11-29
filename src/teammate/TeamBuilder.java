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

}