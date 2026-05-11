import java.util.ArrayList;
import java.util.List;


public class BattleEngine {


    private Character hero;
    private Character enemy;

    private int     round;
    private boolean playerTurn;
    private boolean gameOver;
    private int     poisonTurns;
    private List<String> battleLog;




    public static class BattleResult {
        public final String  message;
        public final int     heroDmg;
        public final int     enemyDmg;
        public final int     healAmt;
        public final boolean levelUp;
        public final String  levelUpMsg;
        public final boolean gameOver;
        public final boolean playerWon;
        public final String  specialNote;  // e.g. "CRITICAL HIT!"

        public BattleResult(String msg, int heroDmg, int enemyDmg,
                            int heal, boolean lvlUp, String lvlUpMsg,
                            boolean over, boolean won, String note) {
            this.message    = msg;
            this.heroDmg    = heroDmg;
            this.enemyDmg   = enemyDmg;
            this.healAmt    = heal;
            this.levelUp    = lvlUp;
            this.levelUpMsg = lvlUpMsg;
            this.gameOver   = over;
            this.playerWon  = won;
            this.specialNote = note;
        }
    }


    public BattleEngine(Character hero, Character enemy) {
        this.hero       = hero;
        this.enemy      = enemy;
        this.round      = 1;
        this.playerTurn = true;
        this.gameOver   = false;
        this.poisonTurns = 0;
        this.battleLog  = new ArrayList<>();
        log("=== Battle Start: " + hero.getName() + " vs " + enemy.getName() + " ===");
    }


    public Character getHero()       { return hero; }
    public Character getEnemy()      { return enemy; }
    public int       getRound()      { return round; }
    public boolean   isPlayerTurn()  { return playerTurn; }
    public boolean   isGameOver()    { return gameOver; }
    public List<String> getBattleLog(){ return new ArrayList<>(battleLog); }

    private void log(String msg) { battleLog.add(msg); }


    public BattleResult doAttack() {
        if (!playerTurn || gameOver) return null;


        int dmg = hero.attack(enemy);
        hero.setDefending(false);

        String msg = hero.getName() + " attacks " + enemy.getName() +
                     " for " + dmg + " damage!";
        log("Round " + round + " | PLAYER | " + msg);

        return resolveAfterPlayerAction(msg, dmg, 0, null);
    }


    public BattleResult doSkill() {
        if (!playerTurn || gameOver) return null;
        if (hero.getMp() < hero.getSkillCost()) return null;

        hero.setMp(hero.getMp() - hero.getSkillCost());


        int dmg = hero.useSkill(enemy);
        hero.setDefending(false);

        String specialNote = "";
        int selfHeal = 0;


        if (hero instanceof Paladin) {
            selfHeal    = ((Paladin) hero).getLastSelfHeal();
            specialNote = "+" + selfHeal + " HP restored!";
        }

        if (hero instanceof Rogue) {
            poisonTurns = 3;
            specialNote = "Poison applied! (3 turns)";
        }

        String msg = hero.getName() + " uses " + hero.getSkillName() +
                     " → " + dmg + " damage!";
        log("Round " + round + " | SKILL  | " + msg + " " + specialNote);

        return resolveAfterPlayerAction(msg, dmg, selfHeal, specialNote);
    }


    public BattleResult doHeal() {
        if (!playerTurn || gameOver) return null;
        int healCost = 18;
        if (hero.getMp() < healCost) return null;

        hero.setMp(hero.getMp() - healCost);
        int amount = (int)(hero.getMaxHp() * 0.28);
        int healed = hero.heal(amount);   // inherited from Character; clamped to maxHp
        hero.setDefending(false);

        String msg = hero.getName() + " heals for " + healed + " HP!";
        log("Round " + round + " | HEAL   | " + msg);

        return resolveAfterPlayerAction(msg, 0, healed, null);
    }


    public BattleResult doDefend() {
        if (!playerTurn || gameOver) return null;

        hero.setDefending(true);
        hero.regenMp(10);

        String msg = hero.getName() + " takes a defensive stance! (+10 MP)";
        log("Round " + round + " | DEFEND | " + msg);

        return resolveAfterPlayerAction(msg, 0, 0, "Damage halved this turn!");
    }


    private BattleResult resolveAfterPlayerAction(String heroMsg,
                                                   int heroDmg,
                                                   int heroHeal,
                                                   String note) {

        if (!enemy.isAlive()) {
            gameOver = true;
            String xpMsg = checkLevelUp();
            log("=== " + hero.getName() + " WINS in Round " + round + "! ===");
            return new BattleResult(heroMsg, heroDmg, 0, heroHeal,
                    xpMsg != null, xpMsg, true, true, note);
        }


        BattleResult enemyResult = doEnemyTurn();


        int poisonDmg = 0;
        if (poisonTurns > 0) {
            poisonDmg = 3;
            enemy.setHp(enemy.getHp() - poisonDmg);
            poisonTurns--;
            log("Poison ticks! Enemy takes " + poisonDmg + " damage. (" + poisonTurns + " turns left)");
        }

        if (!enemy.isAlive()) {
            gameOver = true;
            String xpMsg = checkLevelUp();
            return new BattleResult(heroMsg + " | Poison kills " + enemy.getName() + "!",
                    heroDmg, enemyResult == null ? 0 : enemyResult.enemyDmg,
                    heroHeal, xpMsg != null, xpMsg, true, true, note);
        }

        if (!hero.isAlive()) {
            gameOver = true;
            log("=== " + hero.getName() + " DEFEATED in Round " + round + "! ===");
            return new BattleResult(
                    heroMsg + "\n" + (enemyResult == null ? "" : enemyResult.message),
                    heroDmg, enemyResult == null ? 0 : enemyResult.enemyDmg,
                    heroHeal, false, null, true, false, note);
        }


        round++;
        playerTurn = true;
        hero.setDefending(false);


        if (hero instanceof Paladin) ((Paladin) hero).tickCooldown();
        if (enemy instanceof EnemyDragon) ((EnemyDragon) enemy).rechargeBreath();
        enemy.regenMp(6);

        String combined = heroMsg + "\n" +
                (enemyResult == null ? "" : enemyResult.message) +
                (poisonDmg > 0 ? "\n☠ Poison: -" + poisonDmg + " to " + enemy.getName() : "");

        return new BattleResult(combined, heroDmg,
                enemyResult == null ? 0 : enemyResult.enemyDmg,
                heroHeal, false, null, false, false, note);
    }


    private BattleResult doEnemyTurn() {
        playerTurn = false;



        Enemy e = (Enemy) enemy;



        boolean useSkill = e.decideUseSkill();

        int dmg;
        String msg;
        int mpCost = e.getSkillMp();

        if (useSkill) {
            e.setMp(e.getMp() - mpCost);


            dmg = enemy.useSkill(hero);


            String specialNote = (enemy instanceof EnemyVampire)
                    ? " (drained " + dmg/2 + " HP!)" : "";
            msg = enemy.getName() + " uses " + enemy.getSkillName() +
                  "! " + dmg + " damage!" + specialNote;
        } else {
            dmg = enemy.attack(hero);
            msg = enemy.getName() + " attacks for " + dmg + " damage!";
        }


        if (hero instanceof Mage) {


        }


        if (hero instanceof Warrior) {
            ((Warrior) hero).addRage(dmg);
        }

        log("Round " + round + " | ENEMY  | " + msg);

        return new BattleResult(msg, 0, dmg, 0, false, null, false, false, null);
    }


    private String checkLevelUp() {
        Enemy e      = (Enemy) enemy;
        String lvlUp = hero.gainXp(e.getXpReward());
        if (lvlUp != null) log("LEVEL UP! " + lvlUp);
        return lvlUp;
    }




    public static Enemy createRandomEnemy() {

        int roll = Character.RAND.nextInt(4);
        switch (roll) {
            case 0: return new EnemyGoblin();
            case 1: return new EnemyOrc();
            case 2: return new EnemyVampire();
            default: return new EnemyDragon();
        }
    }

    public static Enemy createEnemyByIndex(int idx) {
        switch (idx) {
            case 0: return new EnemyGoblin();
            case 1: return new EnemyOrc();
            case 2: return new EnemyVampire();
            default: return new EnemyDragon();
        }
    }
}
