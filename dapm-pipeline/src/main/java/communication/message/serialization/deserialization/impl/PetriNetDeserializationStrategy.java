package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;
import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import communication.message.serialization.deserialization.DeserializationStrategy;
import utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PetriNetDeserializationStrategy implements DeserializationStrategy {

    @Override
    public Message deserialize(String payload) {
        Set<Place> places = extractPlaces(payload);
        Set<Transition> transitions = extractTransitions(payload);
        Set<Arc> flowRelation = extractFlowRelation(payload, places, transitions);
        return new PetriNet(places, transitions, flowRelation);
    }

    /** Extracts the places from a PNML string. If a place does not have an initialMarking set, its marking will be
     * set to 0. */
    private static Set<Place> extractPlaces(String pnml) {
        Set<Place> places = new HashSet<>();
        String start = "<place";
        String end = "</place>";
        String initialMarkingText = "<initialMarking>";
        int i = 0;
        int markingEnd;
        while (i < pnml.length()) {
            i = pnml.indexOf(start, i);
            if (i == -1) { break; } // String.indexOf() returns -1 if no matches are found
            // extract ID
            Pair<String, Integer> IDAndEnd = getNextQuoteContent(pnml, i+start.length());
            String ID = IDAndEnd.first();
            i = IDAndEnd.second();

            // extract marking if there is one
            int initialMarkingIndex = pnml.indexOf(initialMarkingText, i);
            int endIndex = pnml.indexOf(end, i);
            assert endIndex != -1 : "'<place' is not closed by '</place>'";
            int marking = 0;
            // a marking is found within the current place's bounds
            if (initialMarkingIndex != -1 && initialMarkingIndex < endIndex) {
                String text = "<text>";
                int markingStart = pnml.indexOf(text, initialMarkingIndex+initialMarkingText.length());
                markingStart = markingStart + text.length();
                markingEnd = pnml.indexOf('<', markingStart+1);
                //substring inside <text> </text> with whitespace removed
                marking = Integer.parseInt(
                        pnml.substring(markingStart, markingEnd).replaceAll("\\s",""));
            }
            places.add(new Place(ID, marking));
            i = endIndex+1;
        }
        return places;
    }

    private static Set<Transition> extractTransitions(String pnml) {
        Set<Transition> transitions = new HashSet<>();
        String start = "<transition";
        int i = 0;
        while (i < pnml.length()) {
            i = pnml.indexOf(start, i);
            if (i == -1) { break; }

            Pair<String, Integer> IDAndEnd = getNextQuoteContent(pnml, i+start.length());
            String ID = IDAndEnd.first();
            i = IDAndEnd.second();

            transitions.add(new Transition(ID));
        }
        return transitions;
    }

    private static Set<Arc> extractFlowRelation(String pnml, Set<Place> places, Set<Transition> transitions) {
        Set<Arc> flowRelation = new HashSet<>();
        // Making hashmaps serves two purposes: 1) it makes it easier to determine whether source/target of the pnml arc
        // is a place/transitions, and 2) it makes the Arc map to the provided place/transition objects (efficiently).
        Map<String, Place> placeMap = new HashMap<>();
        Map<String, Transition> transitionMap = new HashMap<>();
        for (Place place : places) { placeMap.put(place.getID(), place); }
        for (Transition transition : transitions) { transitionMap.put(transition.getID(), transition); }

        String start = "<arc";
        int i = 0;
        while (i < pnml.length()) {
            i = pnml.indexOf(start, i);
            if (i == -1) { break; }

            Pair<String, Integer> IDAndEnd = getNextQuoteContent(pnml, i+start.length());
            String ID = IDAndEnd.first();
            Pair<String, Integer> sourceAndEnd = getNextQuoteContent(pnml, IDAndEnd.second());
            String source = sourceAndEnd.first();
            Pair<String, Integer> targetAndEnd = getNextQuoteContent(pnml, sourceAndEnd.second());
            String target = targetAndEnd.first();
            i = targetAndEnd.second();

            if (placeMap.containsKey(source) && transitionMap.containsKey(target))
            { flowRelation.add(new PlaceToTransitionArc(ID, placeMap.get(source), transitionMap.get(target))); }
            else if (transitionMap.containsKey(source) && placeMap.containsKey(target))
            { flowRelation.add(new TransitionToPlaceArc(ID, transitionMap.get(source), placeMap.get(target)));
            } else
            { throw new IllegalStateException("Arc did not match a pair of Place & Transition in the PNML."); }

        }
        return flowRelation;
    }

    /** @return The next quote content [String] and the index right after the end quote's index, i.e. if i_e is the end
     * quote index, the returned integer is (i_e+1). */
    private static Pair<String, Integer> getNextQuoteContent(String str, int startIndex) {
        int startQuoteIndex = str.indexOf('\"', startIndex);
        int endQuoteIndex = str.indexOf('\"', startQuoteIndex+1);
        assert startQuoteIndex != -1 && endQuoteIndex != -1 && (startQuoteIndex+1) < endQuoteIndex
                : "must have \" pair and non-empty content";

        String content = str.substring(startQuoteIndex+1, endQuoteIndex);
        return new Pair<>(content, endQuoteIndex+1);
    }
}
