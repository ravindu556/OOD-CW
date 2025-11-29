
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

}