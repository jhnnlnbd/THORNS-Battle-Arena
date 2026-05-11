




public class Paladin extends Character {

    private int holyPower;
    private int selfHealAmount;
    private int divineShieldCooldown;

    public Paladin(String name) {
        super(name, /*hp*/ 115, /*mp*/ 85, /*atk*/ 16, /*def*/ 16, /*spd*/ 8);
        this.holyPower            = 22;
        this.selfHealAmount       = 18;
        this.divineShieldCooldown = 0;
    }

    public int getHolyPower()            { return holyPower; }
    public int getSelfHealAmount()       { return selfHealAmount; }
    public int getDivineShieldCooldown() { return divineShieldCooldown; }


    public void tickCooldown() {
        if (divineShieldCooldown > 0) divineShieldCooldown--;
    }







    @Override
    public int useSkill(Character target) {

        target.setHp(target.getHp() - holyPower);



        int healed = this.heal(selfHealAmount);
        return holyPower;
    }

    /** Bonus heal accessor so BattleEngine/GUI can display it */
    public int getLastSelfHeal() { return selfHealAmount; }

    @Override public int    getSkillCost()        { return 28; }
    @Override public String getSkillName()        { return "Holy Strike"; }
    @Override public String getSkillDescription() {
        return String.format("Deals %d holy dmg. Heals self %d HP. Divine light empowers both.",
                holyPower, selfHealAmount);
    }
    @Override public String getClassName() { return "Paladin"; }
    @Override public String getSprite()    { return "✤"; }

    @Override
    public String toString() {
        return super.toString() +
               String.format(" [Holy:%d | SelfHeal:%d]", holyPower, selfHealAmount);
    }
}
