
public class Warrior extends Character {


    private int shieldBonus;
    private int rageMeter;




    public Warrior(String name) {
        super(name,
              /*hp*/  130,
              /*mp*/   50,
              /*atk*/  20,
              /*def*/  14,
              /*spd*/   9);
        this.shieldBonus = 5;
        this.rageMeter   = 0;
    }


    public int getShieldBonus() { return shieldBonus; }
    public int getRageMeter()   { return rageMeter; }


    public void addRage(int dmgTaken) {
        this.rageMeter = Math.min(100, this.rageMeter + dmgTaken / 2);
    }





    @Override
    public int useSkill(Character target) {

        int rageBonus  = (rageMeter / 20) * 5;
        int damage     = 28 + rageBonus;
        target.setHp(target.getHp() - damage);
        rageMeter = 0;
        return damage;
    }

    @Override public int    getSkillCost()        { return 12; }
    @Override public String getSkillName()        { return "Shield Bash"; }
    @Override public String getSkillDescription() {
        return "Ignores enemy DEF. Deals " + (28 + (rageMeter/20)*5) +
               " fixed dmg. Rage resets after use.";
    }
    @Override public String getClassName() { return "Warrior"; }
    @Override public String getSprite()    { return "⚔"; }



    @Override
    public String toString() {
        return super.toString() +
               String.format(" [Shield+%d | Rage:%d%%]", shieldBonus, rageMeter);
    }
}
