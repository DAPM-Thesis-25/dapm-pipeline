package communication.message.impl.petrinet;

public class Place {
    private final String ID;
    private int marking;

    public Place(String ID, int marking) {
        assert marking >= 0 : "A place must have a non-negative number of tokens [marking]";
        assert ID.matches("^[A-Za-z0-9_]+$");
        this.ID = ID;
        this.marking = marking;
    }

    public String getID() {return this.ID;}
    public int getMarking() {return this.marking;}

    @Override
    public String toString() {return String.format("p(%s, %d)", ID, marking);}

    @Override
    public int hashCode() {return ID.hashCode();}

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Place otherPlace)) return false;
        return ID.equals(otherPlace.ID);
    }

}
