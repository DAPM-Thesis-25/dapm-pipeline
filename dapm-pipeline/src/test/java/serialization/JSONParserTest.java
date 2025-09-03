package serialization;

import communication.message.serialization.parsing.InvalidJSON;
import communication.message.serialization.parsing.JSONParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONParserTest {

    public Object getJSONMap(String path) throws IOException {
        String contents = Files.readString(Paths.get(path));
        return (new JSONParser()).parse(contents);
    }

    @Test
    public void validJSON() throws IOException {
        Object output = getJSONMap("src/test/resources/serialization/example.json");

        Map<String, Object> expected = new HashMap<>();
        expected.put("string", "text");
        expected.put("number", 42);
        expected.put("float", 4.2);
        expected.put("booleanTrue", true);
        expected.put("booleanFalse", false);
        expected.put("nullValue", null);
        expected.put("emptyObject", new HashMap<>());
        expected.put("emptyArray", new ArrayList<>());

        // array
        Map<String, Object> arrayObject = new HashMap<>();
        arrayObject.put("nested", "object");
        List<Object> array = new ArrayList<>(List.of(1, "two", true, arrayObject));
        array.add(null);
        expected.put("array", array);

        // doubly nested object
        List<Object> innerInnerArray = new ArrayList<>(List.of(false, 0, "end"));
        Map<String, Object> innerInnerObject = new HashMap<>();
        innerInnerObject.put("deep", innerInnerArray);
        Map<String, Object> innerObject = new HashMap<>();
        innerObject.put("nestedKey", innerInnerObject);
        expected.put("object", innerObject);

        assertEquals(output, expected);
    }

    @Test
    public void noObject() {
        assertThrows(InvalidJSON.class, () -> getJSONMap(
                "src/test/resources/serialization/JSONParser/no_object.json"
        ));
    }

    @Test
    public void emptyItem() {
        assertThrows(InvalidJSON.class, () -> getJSONMap(
                "src/test/resources/serialization/JSONParser/empty_array_item.json"));
    }

    @Test
    public void trickyQuotesAndColons() throws IOException{
        JSONParser parser = new JSONParser();
        Map<String, String> objectMap = (Map<String, String>) getJSONMap(
                "src/test/resources/serialization/JSONParser/tricky_quotes_and_colons.json");

    }

}
