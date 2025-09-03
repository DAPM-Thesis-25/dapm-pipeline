package communication.message.impl.petrinet.arc;

import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;

public class TransitionToPlaceArc extends Arc {
    private final Transition source;
    private final Place target;

    public TransitionToPlaceArc(String ID, Transition source, Place target) {
        super(ID);
        this.source = source;
        this.target = target;
    }

    public Transition getSource() { return source; }
    public Place getTarget() { return target; }

    @Override
    public String toString() {
        return String.format("a(%s, %s)", source.toString(), target.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof TransitionToPlaceArc otherArc)) return false;
        return ID.equals(otherArc.ID) && source.equals(otherArc.source) && target.equals(otherArc.target);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(ID, source, target); }
}
