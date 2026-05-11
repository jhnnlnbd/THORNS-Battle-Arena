







public class Mage extends Character {


    private int    spellPower;
    private boolean manaShieldUp;

    public Mage(String name) {
        super(name, /*hp*/ 85, /*mp*/ 110, /*atk*/ 14, /*def*/ 7, /*spd*/ 15);
        this.spellPower    = 38;
        this.manaShieldUp  = true;   // starts active
    }

    public int     getSpellPower()   { return spellPower; }
    public boolean isManaShieldUp()  { return manaShieldUp; }



    public int applyManaShield(int incomingDmg) {
        if (!manaShieldUp || getMp() <= 0) return incomingDmg;
        int absorbed = (int)(incomingDmg * 0.4);
        absorbed     = Math.min(absorbed, getMp());
        setMp(getMp() - absorbed);
        if (getMp() == 0) manaShieldUp = false;
        return incomingDmg - absorbed;
    }




    @Override
    public int useSkill(Character target) {
        int variance = RAND.nextInt(11) - 5;
        int damage   = spellPower + variance;
        damage       = Math.max(1, damage);
        target.setHp(target.getHp() - damage);
        return damage;
    }

    @Override public int    getSkillCost()        { return 22; }
    @Override public String getSkillName()        { return "Fireball"; }
    @Override public String getSkillDescription() {
        return "Pure magic burst. ~" + spellPower + " damage, 100% ignores DEF. " +
               (manaShieldUp ? "Mana Shield ACTIVE." : "Mana Shield DOWN.");
    }
    @Override public String getClassName() { return "Mage"; }
    @Override public String getSprite()    { return "✦"; }

    @Override
    public String toString() {
        return super.toString() +
               String.format(" [SpellPow:%d | ManaShield:%s]",
                       spellPower, manaShieldUp ? "ON" : "OFF");
    }
}
