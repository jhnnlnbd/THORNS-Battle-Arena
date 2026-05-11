import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔═══════════════════════════════════╗");
        System.out.println("║     ⚔  THORNS BATTLE ARENA  ⚔     ║");
        System.out.println("║   Java Console RPG  •  BSIT 1     ║");
        System.out.println("╚═══════════════════════════════════╝\n");


        System.out.println("Select your class:");
        System.out.println("  [1] Warrior  — ATK:20 DEF:14  Shield Bash");
        System.out.println("  [2] Mage     — ATK:12 DEF: 7  Fireball");
        System.out.println("  [3] Rogue    — ATK:24 DEF: 9  Backstab");
        System.out.println("  [4] Paladin  — ATK:16 DEF:16  Holy Strike");
        System.out.print("\nChoice (1-4): ");

        int pick = readInt(sc, 1, 4);


        Character hero;
        switch (pick) {
            case 1:  hero = new Warrior("Hero");  break;
            case 2:  hero = new Mage("Zara");     break;
            case 3:  hero = new Rogue("Shadow");  break;
            default: hero = new Paladin("Light"); break;
        }
        System.out.println("\n▶ You chose: " + hero.getClassName() + " — " + hero);


        System.out.println("\nSelect enemy:");
        System.out.println("  [1] Goblin King  (Rank I  — easy)");
        System.out.println("  [2] Orc Warlord  (Rank II — medium)");
        System.out.println("  [3] Vampire Lord (Rank II — tricky)");
        System.out.println("  [4] Ancient Dragon(Rank III— hard)");
        System.out.println("  [5] Random");
        System.out.print("Choice (1-5): ");
        int epick = readInt(sc, 1, 5);
        Enemy enemy = (epick == 5)
                ? BattleEngine.createRandomEnemy()
                : BattleEngine.createEnemyByIndex(epick - 1);
        System.out.println("▶ Enemy: " + enemy.getClassName() + " — " + enemy);


        BattleEngine engine = new BattleEngine(hero, enemy);
        System.out.println("\n══════════════  BATTLE START  ══════════════\n");



        while (!engine.isGameOver()) {

            System.out.printf("\n[ Round %d ]%n", engine.getRound());
            System.out.printf("  %s: HP %d/%d  MP %d/%d%n",
                    hero.getName(), hero.getHp(), hero.getMaxHp(),
                    hero.getMp(), hero.getMaxMp());
            System.out.printf("  %s: HP %d/%d%n",
                    enemy.getName(), enemy.getHp(), enemy.getMaxHp());

            System.out.println("\nYour action:");
            System.out.println("  [1] Attack");
            System.out.printf ("  [2] %s  (%d MP)%n",
                    hero.getSkillName(), hero.getSkillCost());
            System.out.println("  [3] Heal      (18 MP)");
            System.out.println("  [4] Defend    (+10 MP)");
            System.out.print("Choice: ");

            int action = readInt(sc, 1, 4);
            BattleEngine.BattleResult result;

            switch (action) {
                case 1:  result = engine.doAttack(); break;
                case 2:  result = engine.doSkill();  break;
                case 3:  result = engine.doHeal();   break;
                default: result = engine.doDefend();
            }


            if (result == null) {
                System.out.println("  ✘ Not enough MP!");
                continue;
            }


            for (String line : result.message.split("\n")) {
                if (!line.trim().isEmpty()) System.out.println("  " + line);
            }
            if (result.specialNote != null && !result.specialNote.isEmpty()) {
                System.out.println("  ▶ " + result.specialNote);
            }
            if (result.levelUp) {
                System.out.println("\n  ⭐ " + result.levelUpMsg);
            }
        }


        System.out.println("\n══════════════  BATTLE END  ══════════════");

        if (hero.isAlive()) {
            System.out.println("★  VICTORY!  " + hero.getName() +
                    " defeated " + enemy.getName() + "!");
        } else {
            System.out.println("✗  DEFEATED. Better luck next time!");
        }
        System.out.println(hero);
        sc.close();
    }


    private static int readInt(Scanner sc, int min, int max) {
        try {
            int val = sc.nextInt();
            if (val < min || val > max) throw new IllegalArgumentException();
            return val;
        } catch (Exception e) {
            sc.nextLine();
            System.out.print("  ✘ Enter a number " + min + "–" + max + ": ");
            return readInt(sc, min, max);
        }
    }
}
