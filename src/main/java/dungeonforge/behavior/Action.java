package dungeonforge.behavior;

import dungeonforge.core.Monster;

public final class Action {

    public enum Type { ATTACK, RANGED_ATTACK, FLEE, HEAL_ALLY, WAIT }

    private final Type type;
    private final Monster target;
    private final String flavor;

    private Action(Type type, Monster target, String flavor) {
        this.type = type;
        this.target = target;
        this.flavor = flavor;
    }

    public static Action attack() {
        return new Action(Type.ATTACK, null, "attacks");
    }

    public static Action rangedAttack() {
        return new Action(Type.RANGED_ATTACK, null, "strikes from range");
    }

    public static Action flee() {
        return new Action(Type.FLEE, null, "flees");
    }

    public static Action healAlly(Monster ally) {
        return new Action(Type.HEAL_ALLY, ally, "mends");
    }

    public static Action wait(String flavor) {
        return new Action(Type.WAIT, null, flavor);
    }

    public Type getType()      { return type; }
    public Monster getTarget() { return target; }
    public String getFlavor()  { return flavor; }
}
