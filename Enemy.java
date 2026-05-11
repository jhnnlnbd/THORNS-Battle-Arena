// ============================================================
//  Enemy.java — Abstract Enemy Base (extends Character)
//  EnemyGoblin, EnemyOrc, EnemyVampire, EnemyDragon
//
//  Enemies are ALSO Characters — same class hierarchy.
//  Demonstrates: Inheritance tree with multiple layers.
//  BattleEngine treats all enemies as Character references
//  (polymorphic) — same attack() call works for any enemy.
// ============================================================




abstract class Enemy extends Character {

    private String rank;
    private int    xpReward;
    private int    skillMp;

    public Enemy(String name, int hp, int mp, int atk, int def, int spd,
                 String rank, int xpReward, int skillMp) {
        super(name, hp, mp, atk, def, spd);
        this.rank      = rank;
        this.xpReward  = xpReward;
        this.skillMp   = skillMp;
    }

    public String getRank()     { return rank; }
    public int    getXpReward() { return xpReward; }
    public int    getSkillMp()  { return skillMp; }


    public boolean decideUseSkill() {
        return getMp() >= skillMp && Character.RAND.nextDouble() < 0.35;
    }
}






class EnemyGoblin extends Enemy {
    public EnemyGoblin() {
        super("Goblin King", 80, 30, 14, 5, 13, "I", 60, 8);
    }
    @Override
    public int useSkill(Character target) {
        int dmg = getAtk() + 8;
        target.setHp(target.getHp() - dmg);
        return dmg;
    }
    @Override public int    getSkillCost()        { return 8; }
    @Override public String getSkillName()        { return "Stab"; }
    @Override public String getSkillDescription() { return "Vicious stab ignoring most armor."; }
    @Override public String getClassName()        { return "Goblin"; }
    @Override public String getSprite()           { return "👹"; }
}


class EnemyOrc extends Enemy {
    public EnemyOrc() {
        super("Orc Warlord", 115, 45, 19, 11, 9, "II", 100, 14);
    }
    @Override
    public int useSkill(Character target) {

        boolean wasDefending = target.isDefending();
        target.setDefending(false);
        int raw  = getAtk() + 10 + RAND.nextInt(6);
        int dmg  = Math.max(1, raw - (int)(target.getDef() * 0.3));
        target.setHp(target.getHp() - dmg);
        target.setDefending(wasDefending);
        return dmg;
    }
    @Override public int    getSkillCost()        { return 14; }
    @Override public String getSkillName()        { return "Warlord Smash"; }
    @Override public String getSkillDescription() { return "Crushes through defensive stance."; }
    @Override public String getClassName()        { return "Orc"; }
    @Override public String getSprite()           { return "👺"; }
}


class EnemyVampire extends Enemy {
    public EnemyVampire() {
        super("Vampire Lord", 95, 75, 17, 9, 16, "II", 110, 16);
    }
    @Override
    public int useSkill(Character target) {

        int dmg    = getAtk() + 12;
        target.setHp(target.getHp() - dmg);
        int healed = dmg / 2;
        this.heal(healed);
        return dmg;
    }
    public int getLastDrainHeal() { return 0; }

    @Override public int    getSkillCost()        { return 16; }
    @Override public String getSkillName()        { return "Drain Life"; }
    @Override public String getSkillDescription() { return "Deals dmg and steals 50% as HP."; }
    @Override public String getClassName()        { return "Vampire"; }
    @Override public String getSprite()           { return "🧛"; }
}


class EnemyDragon extends Enemy {
    private int breathCharges;

    public EnemyDragon() {
        super("Ancient Dragon", 160, 90, 26, 15, 11, "III", 200, 22);
        this.breathCharges = 2;
    }
    @Override
    public int useSkill(Character target) {
        if (breathCharges <= 0) {

            int claw = getAtk() + 5;
            target.setHp(target.getHp() - claw);
            return claw;
        }

        int base   = 35 + RAND.nextInt(16);   // 35–50
        int damage = Math.max(30, base);
        target.setHp(target.getHp() - damage);
        breathCharges--;
        return damage;
    }
    public int getBreathCharges() { return breathCharges; }
    public void rechargeBreath()  { breathCharges = Math.min(2, breathCharges + 1); }

    @Override public int    getSkillCost()        { return 22; }
    @Override public String getSkillName()        { return "Inferno"; }
    @Override public String getSkillDescription() { return "35-50 fire dmg, ignores DEF. 2 charges."; }
    @Override public String getClassName()        { return "Dragon"; }
    @Override public String getSprite()           { return "🐉"; }
}
