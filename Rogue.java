




public class Rogue extends Character {

    private double critRate;
    private int    poisonStacks;

    public Rogue(String name) {
        super(name, /*hp*/ 95, /*mp*/ 70, /*atk*/ 24, /*def*/ 9, /*spd*/ 22);
        this.critRate     = 0.40;
        this.poisonStacks = 0;
    }

    public double getCritRate()     { return critRate; }
    public int    getPoisonStacks() { return poisonStacks; }




    @Override
    public int useSkill(Character target) {

        int hit1 = Math.max(1, getAtk() - 2 + RAND.nextInt(5));




        boolean crit  = RAND.nextDouble() < critRate;
        int     hit2  = crit
                      ? (int)(getAtk() * 1.5)
                      : getAtk();

        int total = hit1 + hit2;
        target.setHp(target.getHp() - total);

        poisonStacks = 2;
        return total;
    }

    public boolean isCrit() {
        return RAND.nextDouble() < critRate;
    }


    private boolean lastCrit = false;
    public boolean wasLastCrit() { return lastCrit; }

    @Override public int    getSkillCost()        { return 18; }
    @Override public String getSkillName()        { return "Backstab"; }
    @Override public String getSkillDescription() {
        return String.format("Double strike: ATK×2. %.0f%% crit chance (1.5x). Applies 2 poison stacks.",
                critRate * 100);
    }
    @Override public String getClassName() { return "Rogue"; }
    @Override public String getSprite()    { return "◆"; }

    @Override
    public String toString() {
        return super.toString() +
               String.format(" [CritRate:%.0f%% | Poison:%d]", critRate * 100, poisonStacks);
    }
}
