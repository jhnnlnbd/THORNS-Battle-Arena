import java.util.Random;








public abstract class Character {





    private String  name;
    private int     hp;
    private int     maxHp;
    private int     mp;
    private int     maxMp;
    private int     atk;
    private int     def;
    private int     spd;
    private boolean defending;
    private int     level;
    private int     xp;





    protected static final Random RAND = new Random();




    public Character(String name, int hp, int mp,
                     int atk, int def, int spd) {
        this.name      = name;
        this.hp        = hp;
        this.maxHp     = hp;
        this.mp        = mp;
        this.maxMp     = mp;
        this.atk       = atk;
        this.def       = def;
        this.spd       = spd;
        this.defending = false;
        this.level     = 1;
        this.xp        = 0;
    }




    public String  getName()       { return name; }
    public int     getHp()         { return hp; }
    public int     getMaxHp()      { return maxHp; }
    public int     getMp()         { return mp; }
    public int     getMaxMp()      { return maxMp; }
    public int     getAtk()        { return atk; }
    public int     getDef()        { return def; }
    public int     getSpd()        { return spd; }
    public boolean isDefending()   { return defending; }
    public int     getLevel()      { return level; }
    public int     getXp()         { return xp; }



    public void setHp(int value) {

        this.hp = Math.max(0, Math.min(maxHp, value));
    }

    public void setMp(int value) {
        this.mp = Math.max(0, Math.min(maxMp, value));
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }





    public int attack(Character target) {
        int raw      = this.atk - (int)(target.getDef() * 0.6);
        int variance = RAND.nextInt(7) - 3;          // -3 to +3
        int damage   = Math.max(1, raw + variance);

        if (target.isDefending()) {
            damage = Math.max(1, damage / 2);
        }
        target.setHp(target.getHp() - damage);
        return damage;
    }


    public final int heal(int amount) {
        int before = this.hp;
        this.setHp(this.hp + amount);
        return this.hp - before;
    }


    public void regenMp(int amount) {
        this.setMp(this.mp + amount);
    }


    public boolean isAlive() {
        return this.hp > 0;
    }


    public String gainXp(int amount) {
        this.xp += amount;
        if (this.xp >= getXpThreshold()) {
            return levelUp();
        }
        return null;
    }

    public int getXpThreshold() { return level * 100; }

    private String levelUp() {
        this.level++;
        this.xp = 0;
        int hpBoost  = 15;
        int atkBoost = 2;
        int defBoost = 1;
        this.maxHp += hpBoost;
        this.hp     = this.maxHp;
        this.atk   += atkBoost;
        this.def   += defBoost;
        return String.format("Level Up! Now Lv.%d | +%d HP | +%d ATK | +%d DEF",
                level, hpBoost, atkBoost, defBoost);
    }





    public abstract int    useSkill(Character target);
    public abstract int    getSkillCost();
    public abstract String getSkillName();
    public abstract String getSkillDescription();
    public abstract String getClassName();
    public abstract String getSprite();




    @Override
    public String toString() {
        return String.format("[%s Lv.%d | HP:%d/%d | MP:%d/%d | ATK:%d | DEF:%d | SPD:%d]",
                name, level, hp, maxHp, mp, maxMp, atk, def, spd);
    }
}
