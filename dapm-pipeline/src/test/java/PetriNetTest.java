import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;
import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PetriNetTest {
    @Test
    void validPetriNet() {
        /*
         *             --> p2 -
         *            /        \
         *   p1 --> t1          --> t2 --> p4
         *            \        /
         *             --> p3 -
         * */
        Place p1 = new Place("p1", 0);
        Place p2 = new Place("p2", 0);
        Place p3 = new Place("p3", 0);
        Place p4 = new Place("p4", 0);
        Transition t1 = new Transition("t1");
        Transition t2 = new Transition("t2");
        PlaceToTransitionArc a1 = new PlaceToTransitionArc("a1", p1, t1);
        TransitionToPlaceArc a2_1 = new TransitionToPlaceArc("a2_1", t1, p2);
        TransitionToPlaceArc a2_2 = new TransitionToPlaceArc("a2_2", t1, p3);
        PlaceToTransitionArc a3_1 = new PlaceToTransitionArc("a3_1", p2, t2);
        PlaceToTransitionArc a3_2 = new PlaceToTransitionArc("a3_2", p3, t2);
        TransitionToPlaceArc a4 = new TransitionToPlaceArc("a4", t2, p4);

        Set<Place> places = new HashSet<>(Arrays.asList(p1,p2,p3,p4));
        Set<Transition> transitions = new HashSet<>(Arrays.asList(t1, t2));
        Set<Arc> flowRelation = new HashSet<>(Arrays.asList(a1,a2_1,a2_2, a3_1, a3_2, a4));
        // Note that firing every transition when enabled in this petri net results in 2 tokens in p4 at the end
        // so this is not a workflow net
        PetriNet petriNet = new PetriNet(places, transitions, flowRelation);
    }

    @Test
    void negativeMarking() {
        assertThrows(AssertionError.class, () -> new Place("P1", -1));
    }

    @Test
    void duplicatePlaceID() {
        Place p1 = new Place("P1", 0);
        Place p1_copy = new Place("P1", 1);
        PetriNet pn = new PetriNet();
        pn.addPlace(p1);
        assertThrows(AssertionError.class, () -> pn.addPlace(p1_copy));
    }

    @Test
    void duplicateTransitionID() {
        Transition t1 = new Transition("T1");
        Transition t1_copy = new Transition("T1");
        PetriNet pn = new PetriNet();
        pn.addTransition(t1);
        assertThrows(AssertionError.class, () -> pn.addTransition(t1_copy));
    }

    @Test
    void missingArc() {
        Place p1 = new Place("P1", 0);
        Transition t1 = new Transition("T1"); // not added to petri net below
        PetriNet pn = new PetriNet();
        pn.addPlace(p1);
        PlaceToTransitionArc pta = new PlaceToTransitionArc("P1", p1, t1);
        assertThrows(AssertionError.class, () -> pn.addArc(pta));
    }
}