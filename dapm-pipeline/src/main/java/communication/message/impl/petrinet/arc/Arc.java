package communication.message.impl.petrinet.arc;

public abstract class Arc {
    protected final String ID;

    public Arc(String ID) {
        assert ID.matches("^[A-Za-z0-9_]+$");
        this.ID = ID;
    }

    public String getID() {return ID;}

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}
