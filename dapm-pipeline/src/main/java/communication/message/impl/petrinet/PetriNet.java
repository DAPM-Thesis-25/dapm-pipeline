package communication.message.impl.petrinet;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.impl.petrinet.arc.Arc;
import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.PetriNetDeserializationStrategy;

import java.util.HashSet;
import java.util.Set;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = PetriNetDeserializationStrategy.class)
public class PetriNet extends Message {
    private final Set<Place> places;
    private final Set<Transition> transitions;
    private final Set<Arc> flowRelation;

    public PetriNet() {
        places = new HashSet<>();
        transitions = new HashSet<>();
        flowRelation = new HashSet<>();
    }

    public PetriNet(Set<Place> places, Set<Transition> transitions, Set<Arc> flowRelation) {
        // add each component individually to assert invariants
        this();
        assert this.places.isEmpty() && this.transitions.isEmpty() && this.flowRelation.isEmpty() : "Empty constructor no longer compatible";
        assert !places.contains(null) && !transitions.contains(null) && !flowRelation.contains(null);

        for (Place p : places) {addPlace(p);}
        for (Transition t : transitions) {addTransition(t);}
        for (Arc a : flowRelation) {addArc(a);}
    }

    public Set<Place> getPlaces() { return places; }
    public Set<Transition> getTransitions() { return transitions; }
    public Set<Arc> getFlowRelation() { return flowRelation; }

    public void addPlace(Place p) {
        assert !places.contains(p) : "Place with the same id already exists";
        places.add(p);
    }
    public void addTransition(Transition t) {
        assert !transitions.contains(t) : "Transition with the same id already exists";
        transitions.add(t);
    }

    public void addArc(Arc a) {
        assert !flowRelation.contains(a) : "Arc with the same id already exists";
        if (a instanceof PlaceToTransitionArc pta) {
            assert places.contains(pta.getSource()) && transitions.contains(pta.getTarget()) : "An arc must be between an existing place and transition";
        } else if (a instanceof TransitionToPlaceArc tpa) {
            assert transitions.contains(tpa.getSource()) : "An arc must be between an existing transition and place";
        } else {
            throw new IllegalCallerException("The given arc is not supported");
        }
        flowRelation.add(a);
    }

    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return "PetriNet{\t" + "places=" + places + ",\n\ttransitions=" + transitions + ",\n\tflowRelation=" + flowRelation + "\n}";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof PetriNet otherPetriNet)) return false;
        return places.equals(otherPetriNet.places)
                && transitions.equals(otherPetriNet.transitions)
                && flowRelation.equals(otherPetriNet.flowRelation);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(places, transitions, flowRelation); }


}