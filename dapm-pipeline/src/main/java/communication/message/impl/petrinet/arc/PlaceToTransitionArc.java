package communication.message.impl.petrinet.arc;

import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;

public class PlaceToTransitionArc extends Arc {
    private final Place source;
    private final Transition target;

    public PlaceToTransitionArc(String ID, Place source, Transition target) {
        super(ID);
        this.source = source;
        this.target = target;
    }

    public Place getSource() { return source; }

    public Transition getTarget() { return target; }

    @Override
    public String toString() {
        return String.format("a(%s, %s)", source.toString(), target.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof PlaceToTransitionArc otherArc)) return false;
        return ID.equals(otherArc.ID) && source.equals(otherArc.source) && target.equals(otherArc.target);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(ID, source, target); }
}
