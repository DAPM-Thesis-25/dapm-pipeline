package serialization.message;

import communication.message.Message;
import communication.message.impl.Alignment;
import communication.message.impl.Trace;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;
import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import communication.message.serialization.MessageSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageSerializerTest {

    public String getExpectedSerialization(Message instance, String path) throws IOException {
        String expectedPayload = Files.readString(Paths.get(path));
        return instance.getName() + ':' + expectedPayload;
    }

    @Test
    public void event() {
        Attribute<Integer> intAttr = new Attribute<>("int", 5);
        Attribute<String> stringAttr = new Attribute<>("string", "str '\":({ ing");
        Attribute<Double> doubleAttr = new Attribute<>("double", 5.0);
        Attribute<Boolean> booleanAttr = new Attribute<>("boolean", true);
        Set<Attribute<?>> extraAttributes = new HashSet<>(Arrays.asList(intAttr, stringAttr, doubleAttr, booleanAttr));
        Event event = new Event("caseID", "activity", "timestamp", extraAttributes);

        String expected = Event.class.getName() + ":{\"traces\": [{\"attrs\": {\"concept:name\": \"caseID\"}, \"events\": [{\"concept:name\": \"activity\", \"date\": \"timestamp\", \"boolean\": true, \"string\": \"str '\\\":({ ing\", \"double\": 5.0, \"int\": 5}]}]}\n";
        MessageSerializer serializer = new MessageSerializer();
        String output = serializer.visit(event);
        assertEquals(expected.replaceAll("\\s+", ""), output.replaceAll("\\s+", ""));
    }

    @Test
    void arabic_chars() {
        Event event = new Event("تصنيف:تجمع سكان",
                "categorize",
                "1746088411",
                new HashSet<>());

        String expected = Event.class.getName() + ":{\"traces\": [{\"attrs\": {\"concept:name\": \"تصنيف:تجمع سكان\"}, \"events\": [{\"concept:name\": \"categorize\", \"date\": \"1746088411\"}]}]}\n";
        MessageSerializer serializer = new MessageSerializer();
        String output = serializer.visit(event);
        assertEquals(expected.replaceAll("\\s+", ""), output.replaceAll("\\s+", ""));
    }

    @Test
    void petriNet() throws IOException {
        PetriNet petriNet = getExamplePetriNet();
        MessageSerializer serializer = new MessageSerializer();
        String output = serializer.visit(petriNet);

        String expected = getExpectedSerialization(petriNet,
                "src/test/resources/serialization/message/petrinet/example.xml");

        assertEquals(output.replaceAll("\\s+", ""),
                expected.replaceAll("\\s+", ""));
    }

    @Test
    void traceEmpty() {
        // this test is here in case traces are allowed to be non-empty in the future
        Trace trace = new Trace(new ArrayList<>());
        MessageSerializer serializer = new MessageSerializer();
        assertThrows(AssertionError.class, () -> serializer.visit(trace));
    }

    @Test
    void traceSingle() throws IOException {
        Event e1 = new Event("c1", "a1", "t1", new HashSet<>());
        List<Event> events = new ArrayList<>(List.of(e1));
        Trace singletonTrace = new Trace(events);
        MessageSerializer serializer = new MessageSerializer();
        String output = serializer.visit(singletonTrace);

        String expected = getExpectedSerialization(singletonTrace,
                "src/test/resources/serialization/message/trace/single.json");

        assertEquals(output.replaceAll("\\s+", ""), expected.replaceAll("\\s+", ""));
    }

    @Test
    void traceMultiple() throws IOException {
        String caseID = "c1";
        Event e1 = new Event(caseID, "a1", "t1", new HashSet<>());
        Event e2 = new Event(caseID, "a2", "t2", new HashSet<>());
        Event e3 = new Event(caseID, "a3", "t3", new HashSet<>());
        List<Event> events = new ArrayList<>(List.of(e1, e2, e3));
        Trace multipleTrace = new Trace(events);
        MessageSerializer serializer = new MessageSerializer();
        String output = serializer.visit(multipleTrace);

        String expected = getExpectedSerialization(multipleTrace,
                "src/test/resources/serialization/message/trace/multiple.json");

        assertEquals(output.replaceAll("\\s+", ""), expected.replaceAll("\\s+", ""));

    }

    @Test
    void multipleCaseIDTrace(){
        // trace serialization assumes all event case IDs in a trace are the same. If this is not the case, update trace serialization
        Event e1 = new Event("c1", "a1", "t1", new HashSet<>());
        Event e2 = new Event("c2", "a2", "t2", new HashSet<>());
        List<Event> events = new ArrayList<>(List.of(e1, e2));
        assertThrows(AssertionError.class, () -> new Trace(events));
    }

    @Test
    void alignment() throws IOException {
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Trace logTrace = new Trace(new ArrayList<>(List.of(el1, el2)));
        Event em1 = new Event("00", "A1", "00", new HashSet<>());
        Event em2 = new Event("00", "A3", "00", new HashSet<>());
        Trace modelTrace = new Trace(new ArrayList<>(List.of(em1, em2)));
        Alignment alignment = new Alignment(logTrace, modelTrace);
        MessageSerializer serializer = new MessageSerializer();
        String output = serializer.visit(alignment);

        String expected = getExpectedSerialization(alignment,
                "src/test/resources/serialization//message/alignment/alignment.json");

        assertEquals(output.replaceAll("\\s+", ""), expected.replaceAll("\\s+", ""));
    }

    public PetriNet getExamplePetriNet(){
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
        return new PetriNet(places, transitions, flowRelation);
    }


}