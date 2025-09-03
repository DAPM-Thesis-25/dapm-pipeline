package communication.message.impl.petrinet;

public class Transition {
    private final String ID;

    public Transition(String ID) {
        assert ID.matches("^[A-Za-z0-9_]+$");
        this.ID = ID;
    }

    public String getID() { return ID; }

    @Override
    public String toString() { return String.format("t(%s)", ID); }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Transition otherTransition)) return false;
        return ID.equals(otherTransition.ID);
    }

    @Override
    public int hashCode() { return ID.hashCode(); }

}
