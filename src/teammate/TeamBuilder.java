
package teammate;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TeamBuilder {

    private static final Logger logger = Logger.getLogger(TeamBuilder.class.getName());

    private final List<Participant> participants;
    private final List<Team> formedTeams;
    private final int teamSize;


    private final Object participantLock = new Object();
    private final Object teamLock = new Object();


    public TeamBuilder(List<Participant> allParticipants, int teamSize) {
        // Create thread-safe synchronized copy
        this.participants = Collections.synchronizedList(new ArrayList<>(allParticipants));
        this.formedTeams = Collections.synchronizedList(new ArrayList<>());
        this.teamSize = teamSize;
    }


    public List<Team> buildTeams() {
        if (participants == null || participants.isEmpty()) {
            System.out.println("\n  No participants available to form teams!");
            logger.warning("No participants available to form teams.");
            return new ArrayList<>();
        }

        if (teamSize < 3 || teamSize > 10) {
            System.out.println("\n  Team size must be between 3 and 10!");
            logger.warning("Team size must be between 3 and 10.");
            return new ArrayList<>();
        }

        System.out.println("\n" + "-".repeat(60));
        System.out.println("STARTING CONCURRENT TEAM FORMATION");
        System.out.println("-".repeat(60));
        System.out.println("Total participants: " + participants.size());
        System.out.println("Team size: " + teamSize);
        logger.info("Starting concurrent team formation...");
        logger.info("Total participants: " + participants.size());
        logger.info("Team size: " + teamSize);

        // Calculate teams and validate resources
        int totalTeamsNeeded = participants.size() / teamSize;
        if (totalTeamsNeeded == 0) {
            System.out.println("\n⚠  Not enough participants to form even one team!");
            System.out.println("   Need at least " + teamSize + " participants.");
            logger.warning("Not enough participants to form even one team.");
            return new ArrayList<>();
        }

        // Check personality distribution
        Map<String, Long> personalityCount = countPersonalities();
        long leaders = personalityCount.getOrDefault("Leader", 0L);
        long thinkers = personalityCount.getOrDefault("Thinker", 0L);

        System.out.println("\nPersonality Distribution:");
        System.out.println("  Leaders:  " + leaders);
        System.out.println("  Thinkers: " + thinkers);
        System.out.println("  Balanced: " + personalityCount.getOrDefault("Balanced", 0L));

        logger.info("Personality Distribution -> Leaders: " + leaders + ", Thinkers: " + thinkers);
        if (leaders < 1 || thinkers < 1) {
            System.out.println("\n⚠  Cannot form teams: Need at least 1 Leader AND 1 Thinker per team.");
            logger.warning("Cannot form teams: Minimum 1 Leader and 1 Thinker required.");
            return new ArrayList<>();
        }

        // Limit teams by available leaders
        int maxTeamsByLeaders = (int) leaders;
        totalTeamsNeeded = Math.min(totalTeamsNeeded, maxTeamsByLeaders);
        logger.info("Teams to form: " + totalTeamsNeeded);
        System.out.println("\nTeams to form: " + totalTeamsNeeded);
        System.out.println("-".repeat(60));

        // Calculate optimal thread count
        int numOfThreads = Math.min(totalTeamsNeeded, Runtime.getRuntime().availableProcessors());
        System.out.println("\n Using " + numOfThreads + " threads for parallel processing\n");

        // Create thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
        List<Future<Team>> futures = new ArrayList<>();


        for (int i = 0; i < totalTeamsNeeded; i++) {
            final int teamNumber = i + 1;
            Future<Team> future = executorService.submit(new Callable<Team>() {
                @Override
                public Team call() {
                    return formSingleTeam(teamNumber);
                }
            });
            futures.add(future);
        }

        // Collect results from all threads
        int successCount = 0;
        int failCount = 0;

        for (Future<Team> future : futures) {
            try {
                Team team = future.get(); // Wait for thread to complete
                if (team != null && team.getMembers().size() == teamSize) {
                    synchronized (teamLock) {
                        formedTeams.add(team);
                    }
                    successCount++;
//
                } else {
                    failCount++;
//
                }
            } catch (InterruptedException e) {
                System.err.println("  Thread was interrupted: " + e.getMessage());
                logger.severe("Error during team formation: " + e.getMessage());
                Thread.currentThread().interrupt();
                failCount++;
            } catch (ExecutionException e) {
                System.err.println("  Error during team formation: " + e.getMessage());
                e.printStackTrace();
                failCount++;
            }
        }

        // Shutdown thread pool gracefully
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                System.out.println("  Forced shutdown of thread pool!");


            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEAM FORMATION COMPLETED");
        System.out.println("-".repeat(60));
        System.out.println(" Successfully formed: " + successCount + " teams");
        if (failCount > 0) {
            System.out.println(" Failed: " + failCount + " teams");
        }

        // Optimization phase
        if (!formedTeams.isEmpty()) {
            System.out.println("\n Starting optimization phase...");
            optimizeBalance();
        }
        // Print final statistics
        if (!formedTeams.isEmpty()) {
            printFinalStatistics();
        }

        return new ArrayList<>(formedTeams);
    }


    private Team formSingleTeam(int teamNumber) {
        Team team = new Team(teamNumber);
        List<Participant> selectedMembers = new ArrayList<>();

        try {
            synchronized (participantLock) {
                // Check if enough participants remain
                if (participants.size() < teamSize) {

                    return null;
                }

                // Select exactly 1 Leader
                Participant leader = selectByPersonality("Leader", selectedMembers);
                if (leader == null) {

                    return null;
                }
                selectedMembers.add(leader);

                // Select 1-2 Thinkers
                int thinkersNeeded = (teamSize >= 5) ? 2 : 1;
                int thinkersAdded = 0;

                for (int i = 0; i < thinkersNeeded; i++) {
                    Participant thinker = selectBestMatch(selectedMembers, "Thinker");
                    if (thinker != null) {
                        selectedMembers.add(thinker);
                        thinkersAdded++;
                    } else {
                        if (thinkersAdded == 0) {

                            return null;
                        }
                        break;
                    }
                }

                //  Fill remaining slots with Balanced or any available
                int remainingSlots = teamSize - selectedMembers.size();
                for (int i = 0; i < remainingSlots; i++) {
                    Participant balanced = selectBestMatch(selectedMembers, "Balanced");
                    if (balanced == null) {
                        balanced = selectBestMatch(selectedMembers, null); // Any type
                    }

                    if (balanced != null) {
                        selectedMembers.add(balanced);
                    } else {
                        return null;
                    }
                }
                // Validate team constraints
                if (!validateTeam(selectedMembers, teamNumber)) {

                    return null;
                }
                //  Remove selected members from available pool
                participants.removeAll(selectedMembers);
            }

            //  Add members to team (outside synchronized block for better performance)
            for (Participant p : selectedMembers) {
                team.addMember(p);
            }

            return team;

        } catch (Exception e) {
            System.err.println("⚠  Exception in Team " + teamNumber + ": " + e.getMessage());
            logger.severe("Exception in Team " + teamNumber + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Participant selectBestMatch(List<Participant> currentTeam, String preferredPersonality) {
        List<Participant> candidates = new ArrayList<>(participants);
        candidates.removeAll(currentTeam);

        if (preferredPersonality != null) {
            candidates = candidates.stream()
                    .filter(p -> p.getPersonalityType().equalsIgnoreCase(preferredPersonality))
                    .collect(Collectors.toList());
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // Shuffle for randomness and fairness
        Collections.shuffle(candidates);

        // Find first candidate that meets all requirements
        for (Participant candidate : candidates) {
            if (meetsRequirements(candidate, currentTeam)) {
                return candidate;
            }
        }

        return null;
    }


    private Participant selectByPersonality(String personalityType, List<Participant> selected) {
        List<Participant> candidates = participants.stream()
                .filter(p -> p.getPersonalityType().equalsIgnoreCase(personalityType))
                .filter(p -> !selected.contains(p))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return null;
        }

        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    private boolean meetsRequirements(Participant candidate, List<Participant> currentTeam) {

        long sameGameCount = currentTeam.stream()
                .filter(p -> p.getPreferredGame().equalsIgnoreCase(candidate.getPreferredGame()))
                .count();
        if (sameGameCount >= 2) {
            return false;
        }

        if (teamSize >= 4) {
            int remainingSlots = teamSize - currentTeam.size() - 1;
            Set<String> currentRoles = currentTeam.stream()
                    .map(Participant::getPreferredRole)
                    .collect(Collectors.toSet());

            if (remainingSlots <= 2 && currentRoles.size() < 3) {
                if (currentRoles.contains(candidate.getPreferredRole()) && remainingSlots == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validateTeam(List<Participant> team, int teamNumber) {
        if (team.size() != teamSize) {

            return false;
        }


        Set<String> uniqueIds = team.stream()
                .map(Participant::getParticipantId)
                .collect(Collectors.toSet());
        if (uniqueIds.size() != team.size()) {

            return false;
        }


        long leaders = team.stream()
                .filter(p -> p.getPersonalityType().equalsIgnoreCase("Leader"))
                .count();
        if (leaders != 1) {

            return false;
        }


        long thinkers = team.stream()
                .filter(p -> p.getPersonalityType().equalsIgnoreCase("Thinker"))
                .count();
        if (thinkers < 1 || thinkers > 2) {

            return false;
        }

        if (teamSize >= 4) {
            long uniqueRoles = team.stream()
                    .map(Participant::getPreferredRole)
                    .distinct()
                    .count();
            if (uniqueRoles < 3) {

                return false;
            }
        }


        Map<String, Long> gameCounts = team.stream()
                .collect(Collectors.groupingBy(Participant::getPreferredGame, Collectors.counting()));

        for (Map.Entry<String, Long> entry : gameCounts.entrySet()) {
            if (entry.getValue() > 2) {

                return false;
            }
        }

        return true;
    }


    private void optimizeBalance() {
        if (formedTeams.size() < 2) {
            System.out.println("  Optimization skipped (need at least 2 teams)");
            return;
        }

        System.out.println("\n Optimizing teams for fair skill distribution...");

        boolean improved = true;
        int iterations = 0;
        int maxIterations = 150;

        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;

            formedTeams.sort(Comparator.comparingDouble(this::getTeamAvgSkill));

            Team weakest = formedTeams.get(0);
            Team strongest = formedTeams.get(formedTeams.size() - 1);

            double beforeRange = getSkillRange();

            List<Participant> weakestCandidates = weakest.getMembers().stream()
                    .filter(p -> !p.getPersonalityType().equalsIgnoreCase("Leader"))
                    .sorted(Comparator.comparingInt(Participant::getSkillLevel))
                    .collect(Collectors.toList());

            List<Participant> strongestCandidates = strongest.getMembers().stream()
                    .filter(p -> !p.getPersonalityType().equalsIgnoreCase("Leader"))
                    .sorted((a, b) -> b.getSkillLevel() - a.getSkillLevel())
                    .collect(Collectors.toList());

            boolean swapMade = false;

            for (Participant weak : weakestCandidates) {
                for (Participant strong : strongestCandidates) {


                    weakest.getMembers().remove(weak);
                    strongest.getMembers().remove(strong);
                    weakest.getMembers().add(strong);
                    strongest.getMembers().add(weak);

                    double afterRange = getSkillRange();


                    if (afterRange < beforeRange &&
                            isTeamValid(weakest) && isTeamValid(strongest)) {

                        improved = true;
                        swapMade = true;
                        break;
                    }


                    weakest.getMembers().remove(strong);
                    strongest.getMembers().remove(weak);

                    weakest.getMembers().add(weak);
                    strongest.getMembers().add(strong);
                }
                if (swapMade) break;
            }
        }

        System.out.println("\n Final Skill Range: " + String.format("%.2f", getSkillRange()));
    }


    private boolean isTeamValid(Team team) {
        List<Participant> members = team.getMembers();

        long leaderCount = members.stream()
                .filter(m -> m.getPersonalityType().equals("Leader"))
                .count();
        if (leaderCount != 1) return false;

        long thinkerCount = members.stream()
                .filter(m -> m.getPersonalityType().equals("Thinker"))
                .count();
        if (thinkerCount < 1 || thinkerCount > 2) return false;

        Map<String, Long> gameCounts = members.stream()
                .collect(Collectors.groupingBy(Participant::getPreferredGame, Collectors.counting()));
        for (Long count : gameCounts.values()) {
            if (count > 2) return false;
        }

        if (teamSize >= 4) {
            long uniqueRoles = members.stream()
                    .map(Participant::getPreferredRole)
                    .distinct()
                    .count();
            if (uniqueRoles < 3) return false;
        }

        return true;
    }


    private double getSkillRange() {
        if (formedTeams.isEmpty()) return 0;
        double min = formedTeams.stream()
                .mapToDouble(this::getTeamAvgSkill)
                .min()
                .orElse(0);
        double max = formedTeams.stream()
                .mapToDouble(this::getTeamAvgSkill)
                .max()
                .orElse(0);
        return max - min;
    }


    private double getTeamAvgSkill(Team team) {
        if (team.getMembers().isEmpty()) return 0.0;
        return team.getMembers().stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0);
    }


    private Map<String, Long> countPersonalities() {
        return participants.stream()
                .collect(Collectors.groupingBy(Participant::getPersonalityType, Collectors.counting()));
    }


    private void printFinalStatistics() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("FINAL TEAM STATISTICS");
        System.out.println("-".repeat(60));

        double minAvg = formedTeams.stream()
                .mapToDouble(this::getTeamAvgSkill)
                .min()
                .orElse(0);
        double maxAvg = formedTeams.stream()
                .mapToDouble(this::getTeamAvgSkill)
                .max()
                .orElse(0);
        double overallAvg = formedTeams.stream()
                .mapToDouble(this::getTeamAvgSkill)
                .average()
                .orElse(0);
        double range = maxAvg - minAvg;

        System.out.printf("  Lowest Team Avg:   %.2f%n", minAvg);
        System.out.printf("  Highest Team Avg:  %.2f%n", maxAvg);
        System.out.printf("  Overall Average:   %.2f%n", overallAvg);
        System.out.printf("  Skill Range:       %.2f ", range);

        if (range <= 1.0) {
            System.out.println("( EXCELLENT - Highly Balanced!)");
        } else if (range <= 2.0) {
            System.out.println("( GOOD - Well Balanced)");
        } else if (range <= 3.0) {
            System.out.println("(  FAIR - Moderately Balanced)");
        } else {
            System.out.println("( NEEDS IMPROVEMENT)");
        }

        System.out.println("-".repeat(60));
    }


    public List<Team> getFormedTeams() {
        return new ArrayList<>(formedTeams);
    }
    public List<Participant> getUnassignedParticipants() {
        return new ArrayList<>(participants); // These are leftover
    }

  }

