import java.io.*;
import java.util.ArrayList;
import java.util.List;












public class GameStats implements Serializable {





    private static final long serialVersionUID = 1001L;


    private static final String SAVE_PATH = "battle_stats.dat";


    private int totalWins;
    private int totalLosses;
    private int totalRounds;
    private int bestRound;
    private int highestDmgDealt;
    private List<MatchRecord> history;




    public static class MatchRecord implements Serializable {
        private static final long serialVersionUID = 1002L;

        public final String heroClass;
        public final String enemyName;
        public final boolean won;
        public final int rounds;
        public final int damageDealt;
        public final int damageTaken;
        public final String timestamp;


        public MatchRecord(String heroClass, String enemyName,
                           boolean won, int rounds,
                           int dmgDealt, int dmgTaken) {
            this.heroClass   = heroClass;
            this.enemyName   = enemyName;
            this.won         = won;
            this.rounds      = rounds;
            this.damageDealt = dmgDealt;
            this.damageTaken = dmgTaken;

            this.timestamp   = formatTime(System.currentTimeMillis());
        }

        private String formatTime(long ms) {
            java.util.Date d = new java.util.Date(ms);
            return String.format("%tH:%tM:%tS", d, d, d);
        }


        @Override
        public String toString() {
            return String.format("[%s] %s vs %s — %s in %d round%s | DMG: %d | Taken: %d",
                    timestamp, heroClass, enemyName,
                    won ? "WIN" : "LOSS", rounds,
                    rounds == 1 ? "" : "s",
                    damageDealt, damageTaken);
        }
    }


    public GameStats() {
        this.totalWins       = 0;
        this.totalLosses     = 0;
        this.totalRounds     = 0;
        this.bestRound       = Integer.MAX_VALUE;
        this.highestDmgDealt = 0;
        this.history         = new ArrayList<>();
    }


    public int          getTotalWins()       { return totalWins; }
    public int          getTotalLosses()     { return totalLosses; }
    public int          getTotalMatches()    { return totalWins + totalLosses; }
    public int          getTotalRounds()     { return totalRounds; }
    public int          getHighestDmgDealt() { return highestDmgDealt; }
    public List<MatchRecord> getHistory()    { return new ArrayList<>(history); }

    public String getBestRoundDisplay() {
        return bestRound == Integer.MAX_VALUE ? "—" : String.valueOf(bestRound);
    }

    public double getWinRate() {
        int total = getTotalMatches();
        return total == 0 ? 0.0 : (double) totalWins / total * 100.0;
    }

    public double getAvgRoundsPerMatch() {
        int total = getTotalMatches();
        return total == 0 ? 0.0 : (double) totalRounds / total;
    }


    public void recordMatch(String heroClass, String enemyName,
                             boolean won, int rounds,
                             int dmgDealt, int dmgTaken) {
        if (won) totalWins++;
        else     totalLosses++;

        totalRounds += rounds;


        if (won && rounds < bestRound) bestRound = rounds;


        if (dmgDealt > highestDmgDealt) highestDmgDealt = dmgDealt;


        history.add(0, new MatchRecord(heroClass, enemyName, won, rounds, dmgDealt, dmgTaken));
        if (history.size() > 20) history.remove(history.size() - 1);
    }




    public void save() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(SAVE_PATH))) {
            oos.writeObject(this);   // writes this GameStats instance to file
        } catch (IOException e) {
            System.err.println("Could not save stats: " + e.getMessage());
        }
    }




    public static GameStats load() {
        File f = new File(SAVE_PATH);
        if (!f.exists()) return new GameStats();   // first run
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(f))) {

            return (GameStats) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load stats (starting fresh): " + e.getMessage());
            return new GameStats();
        }
    }


    public void reset() {
        totalWins       = 0;
        totalLosses     = 0;
        totalRounds     = 0;
        bestRound       = Integer.MAX_VALUE;
        highestDmgDealt = 0;
        history.clear();
        save();
    }

    @Override
    public String toString() {
        return String.format("GameStats[W:%d L:%d WinRate:%.1f%% AvgRounds:%.1f BestRound:%s]",
                totalWins, totalLosses, getWinRate(),
                getAvgRoundsPerMatch(), getBestRoundDisplay());
    }
}
