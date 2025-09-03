package serialization.message;

import communication.message.Message;
import communication.message.impl.Alignment;
import communication.message.impl.Metrics;
import communication.message.impl.time.Date;
import communication.message.impl.Trace;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;
import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import communication.message.impl.time.UTCTime;
import communication.message.serialization.MessageSerializer;
import communication.message.serialization.deserialization.MessageFactory;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MessageDeserializerTest {

    public Message getMessage(Class<? extends Message> type, String path) throws IOException {
        String contents = Files.readString(Paths.get(path));
        String expected = type.getName() + ":" + contents;
        return MessageFactory.deserialize(expected);
    }

    @Test
    void PNMLInverse() {
        MessageSerializer serializer = new MessageSerializer();
        PetriNet pn = getPetriNetExample();
        String pnml = serializer.visit(pn);
        Message pn_2 = MessageFactory.deserialize(pnml);

        assertEquals(pn, pn_2);
    }

    @Test
    void petriNetExample() throws IOException {
        Message output = getMessage(PetriNet.class,
                "src/test/resources/serialization/message/petrinet/kindler_article.xml");

        PetriNet expected = new PetriNet();
        Place p1 = new Place("p1", 3);
        expected.addPlace(p1);
        Transition t1 = new Transition("t1");
        expected.addTransition(t1);
        expected.addArc(new PlaceToTransitionArc("a1", p1, t1));

        assertEquals(expected, output);
    }

    @Test
    void eventJXESInverse() {
        // make event
        String caseID = "id1";
        String activity = "a1";
        String timestamp = "15:07Z";
        Attribute<Integer> attr1 = new Attribute<>("int", 5);
        Attribute<List<Double>> attr2 = new Attribute<>("listAttr", Arrays.asList(5.0, 0.0));
        Set<Attribute<?>> extraAttrs = new HashSet<>(Arrays.asList(attr1, attr2));
        Event event = new Event(caseID, activity, timestamp, extraAttrs);

        MessageSerializer serializer = new MessageSerializer();
        String JXES = serializer.visit(event);
        Message event_2 = MessageFactory.deserialize(JXES);

        assertEquals(event, event_2);

    }

    @Test
    void articleEvent() throws IOException {
        System.out.println("The following messages come from MessageDeserializerTest.articleEvent().\n" +
                "They illustrate that the JXES deserialization currently does not use certain attributes.");
        Message output = getMessage(Event.class,
                "src/test/resources/serialization/message/event/example.json");

        Set<Attribute<?>> nonEssentialAttributes = new HashSet<>();
        // global attributes
        nonEssentialAttributes.add(new Attribute<>("Key 1", 1));
        nonEssentialAttributes.add(new Attribute<>("Key 2", 2));

        // (local) trace and event attributes
        nonEssentialAttributes.add(new Attribute<>("string", "hi"));
        nonEssentialAttributes.add(new Attribute<>("int", 1));
        nonEssentialAttributes.add(new Attribute<>("float", 1.0));
        nonEssentialAttributes.add(new Attribute<>("boolean", true));

        List<Map<String, Object>> listValue = new ArrayList<>();
        listValue.add(new HashMap<>() {{put("key", 1);}});
        listValue.add(new HashMap<>() {{put("key", 2);}});
        listValue.add(new HashMap<>() {{put("new key", "new value"); put("la di lay lo", 6969); }});
        nonEssentialAttributes.add(new Attribute<>("list", listValue));

        Map<String, Object> containerValue = new HashMap<>();
        containerValue.put("key", 1);
        containerValue.put("new key", "new value");
        nonEssentialAttributes.add(new Attribute<>("container", containerValue));

        Map<String, Object> nestedAttributeValue = new HashMap<>();
        nestedAttributeValue.put("value", 1);
        nestedAttributeValue.put("nested-attrs", new HashMap<>());
        nonEssentialAttributes.add(new Attribute<>("nested-attribute", nestedAttributeValue));

        nonEssentialAttributes.add(new Attribute<>("org:resource", "Resource A"));

        Event expected = new Event("case ID", "Activity 1","2013-10-21T14:23:06.419Z", nonEssentialAttributes);
        assertEquals(expected, output);
    }

    @Test
    void wikipediaEventInverse() throws IOException {
        Event expected = new Event(
                "File:THIRTEENTH ANNUAL REUNION & BANQUET (held by) UNION COLLEGE \nALUMNI ASSOCIATION OF NEW YORK (at) \"SAVOY, THE, NEW YORK, NY\" (HOTEL;) (NYPL Hades-275181-4000011701).jpg",
                "edit",
                "1746088230",
                new HashSet<>()
        );

        MessageSerializer serializer = new MessageSerializer();
        String JXES = serializer.visit(expected);
        Message output = MessageFactory.deserialize(JXES);
        assertEquals(expected, output);
    }

    @Test
    void wikipediaEventInverseWithAttribute() throws IOException {
        Set<Attribute<?>> nonEssentialAttributes = new HashSet<>();
        nonEssentialAttributes.add(new Attribute<>("UTC time", new UTCTime()));
        Event expected = new Event("case", "act", "timestamp", nonEssentialAttributes);

        MessageSerializer serializer = new MessageSerializer();
        String JXES = serializer.visit(expected);
        Message output = MessageFactory.deserialize(JXES);
        assertEquals(expected, output);
    }

    @Test
    void arabicCharsEvent() throws IOException {
        Event expected = new Event("تصنيف:تجمع سكان",
                "categorize",
                "1746088411",
                new HashSet<>());

        Event output = (Event) getMessage(Event.class, "src/test/resources/serialization/JSONParser/arabic_chars.json");

        assertEquals(expected, output);
    }

    @Test
    void traceSingleInverse() {
        // create trace
        Event e1 = new Event("c1", "a1", "t1", new HashSet<>());
        List<Event> events = new ArrayList<>(List.of(e1));
        Trace singletonTrace = new Trace(events);

        // serialize and deserialize
        MessageSerializer serializer = new MessageSerializer();
        String serialization = serializer.visit(singletonTrace);
        Message singletonTrace_2 = MessageFactory.deserialize(serialization);

        assertEquals(singletonTrace, singletonTrace_2);
    }

    @Test
    void traceMultipleInverse() {
        // create trace
        Event e1 = new Event("c1", "a1", "t1", new HashSet<>());
        Event e2 = new Event("c1", "a2", "t2", new HashSet<>());
        Event e3 = new Event("c1", "a3", "t3", new HashSet<>());
        List<Event> events = new ArrayList<>(List.of(e1, e2, e3));
        Trace multipleTrace = new Trace(events);

        // serialize and deserialize
        MessageSerializer serializer = new MessageSerializer();
        String serialization = serializer.visit(multipleTrace);
        Message multipleTrace_2 = MessageFactory.deserialize(serialization);

        assertEquals(multipleTrace, multipleTrace_2);
    }

    @Test
    void alignmentExample() throws IOException {
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Trace logTrace = new Trace(new ArrayList<>(List.of(el1, el2)));
        Event em1 = new Event("00", "A1", "00", new HashSet<>());
        Event em2 = new Event("00", "A3", "00", new HashSet<>());
        Trace modelTrace = new Trace(new ArrayList<>(List.of(em1, em2)));
        Alignment expected = new Alignment(logTrace, modelTrace);

        String JXESPathString = "src/test/resources/serialization/message/alignment/alignment.json";
        String JXESContents = Files.readString(Paths.get(JXESPathString));
        String serialization = Alignment.class.getName() + ":" + JXESContents;
        Message output = MessageFactory.deserialize(serialization);
        assertEquals(expected, output);

    }

    @Test
    void alignmentInverse() {
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Trace logTrace = new Trace(new ArrayList<>(List.of(el1, el2)));
        Event em1 = new Event("00", "A1", "00", new HashSet<>());
        Event em2 = new Event("00", "A3", "00", new HashSet<>());
        Trace modelTrace = new Trace(new ArrayList<>(List.of(em1, em2)));
        Alignment alignment = new Alignment(logTrace, modelTrace);

        MessageSerializer serializer = new MessageSerializer();
        String serialization = serializer.visit(alignment);
        Message alignment_2 = MessageFactory.deserialize(serialization);
        assertEquals(alignment, alignment_2);
    }

    @Test
    void alignmentTraceSwapInverse() {
        // Serialization and deserialization is currently dependent on ordering of traces in a JXES string. Therefore,
        // they are only valid if alignments are given in the correct order
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Trace logTrace = new Trace(new ArrayList<>(List.of(el1, el2)));
        Event em1 = new Event("00", "A1", "00", new HashSet<>());
        Event em2 = new Event("00", "A3", "00", new HashSet<>());
        Trace modelTrace = new Trace(new ArrayList<>(List.of(em1, em2)));
        Alignment alignment_1 = new Alignment(logTrace, modelTrace);
        Alignment alignment_2 = new Alignment(modelTrace, logTrace);

        MessageSerializer serializer = new MessageSerializer();
        String output_1 = serializer.visit(alignment_1);
        String output_2 = serializer.visit(alignment_2);
        assertNotEquals(MessageFactory.deserialize(output_1),
                MessageFactory.deserialize(output_2));
    }

    @Test
    void DateInverse() {
        Date expected = new Date();
        MessageSerializer serializer = new MessageSerializer();
        String timeStr = serializer.visit(expected);
        Message output = MessageFactory.deserialize(timeStr);
        assertEquals(expected, output);
    }

    @Test
    void MetricInverse() {
        Metrics expected = new Metrics(1.0, 2.1221, 3.0, 4., 5.010101, 6.1234, 7., .8, 9., 10.);
        MessageSerializer serializer = new MessageSerializer();
        String metricsStr = serializer.visit(expected);
        Message output = MessageFactory.deserialize(metricsStr);
        assertEquals(expected, output);
    }


    public PetriNet getPetriNetExample() {
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
        return new PetriNet(places, transitions, flowRelation);
    }

}