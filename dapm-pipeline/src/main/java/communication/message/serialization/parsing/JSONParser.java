package communication.message.serialization.parsing;

import communication.message.MessageTypeRegistry;
import communication.message.serialization.deserialization.MessageFactory;
import utils.Pair;

import java.util.*;

public class JSONParser {
    public Object parse(String json) {
        if (isJSONArray(json)) { return parseJSONArray(json); }
        else if (isJSONObject(json)) { return parseJSONObject(json); }

        throw new InvalidJSON("A JSON string must either start with an array or an object.");
    }

    List<Object> parseJSONArray(String array) {
        if (!isJSONArray(array)) {
            throw new InvalidJSON("Expected an array but received: " + array);
        }

        List<String> stringItems = commaSplitArray(array);

        List<Object> items = new ArrayList<>();
        for (String item : stringItems) { items.add(parseValue(item)); }
        return items;
    }

    Object parseValue(String item) {
        if (isJSONObject(item)) { return parseJSONObject(item); }
        if (isJSONArray(item)) { return parseJSONArray(item); }
        if (isDAPMMessage(item)) { return MessageFactory.deserialize(item); }
        else { return parseSimpleType(item); }
    }

    private boolean isDAPMMessage(String item) {
        int colonIndex = item.indexOf(':');
        if (colonIndex == -1) { return false; }

        int lastDotBeforeColon = item.lastIndexOf('.', colonIndex);
        if (lastDotBeforeColon == -1) { return false; }

        String potentialSimpleClassName = item.substring(lastDotBeforeColon + 1, colonIndex);
        return MessageTypeRegistry.isSupportedMessageType(potentialSimpleClassName);
    }

    Map<String, Object> parseJSONObject(String object) {
        if (!isJSONObject(object)){
            throw new InvalidJSON("Expected an object but received: " + object);
        }

        List<String> nameValuePairs = commaSplitObject(object);

        Map<String, Object> objectMap = new HashMap<>();
        for (String pair : nameValuePairs) {
            Pair<String, String> nameAndValue = splitAndStripKeyAndValue(pair);
            String name = unwrap(nameAndValue.first(), '\"', '\"');
            Object value = parseValue(nameAndValue.second());
            objectMap.put(name, value);
        }

        return objectMap;
    }

    /** Strips away all leading and trailing whitespace characters. */
    Object parseSimpleType(String item) {
        item = item.strip();
        if (item.isEmpty()) {
            throw new InvalidJSON("string may not be empty [but is allowed to contain empty quotation marks, i.e. '\"\"']");
        }
        if (isString(item))
            { return toString(item); }
        else if (isBoolean(item)) {return Boolean.parseBoolean(item); }
        else if (isInteger(item)) { return Integer.parseInt(item); }
        else if (isDouble(item)) { return Double.parseDouble(item); }
        else if (isNull(item)) { return null; }

        throw new InvalidJSON("Unsupported item type: " + item);
    }

    /** A string is wrapped iff the first non-whitespace character is start and the last non-whitespace character is
     *  end. */
    boolean isWrapped(String str, char start, char end) {
        int first = findNonWhitespaceIndex(str, 0, 1, start);
        if (first == -1) return false; // Start wrapper not found

        int last = findNonWhitespaceIndex(str, str.length() - 1, -1, end);
        return last != -1; // End wrapper found
    }

    String unwrap(String str, char startWrapper, int endWrapper) {
        assert str.length() >= 2 : "unwrap can only be called when it can actually unwrap the string. Received" + str;
        int startIndex = str.indexOf(startWrapper);
        int endIndex = str.lastIndexOf(endWrapper);
        assert startIndex != -1 && endIndex != -1 : "wrapper missing for unwrap; unwrap should only be called when it can actually unwrap the string. Received "+str;
        return str.substring(startIndex+1, endIndex);
    }

    boolean isJSONObject(String str) {
        return isWrapped(str, '{', '}');
    }

    boolean isJSONArray(String str) {
        return isWrapped(str, '[', ']');
    }

    List<String> commaSplitArray(String array) {
        assert isJSONArray(array) : "Expected an array but received: " + array;
        return commaSplit(unwrap(array, '[', ']'));
    }

    List<String> commaSplitObject(String object) {
        assert isJSONObject(object) : "Expected an object but received: " + object;
        return commaSplit(unwrap(object, '{', '}'));
    }

    /** @param contents An unwrapped array/container.
     *  @return The strings between outermost commas of the contents input. The strings will be stripped of whitespace, \n, \t,
     *  and \r in both ends. Outermost commas are commas that are not inside a string, object, or array inside the current object/array. */
    private List<String> commaSplit(String contents) {
        List<String> commaSeparatedStrings = new ArrayList<>();
        if (contents.isEmpty()) { return commaSeparatedStrings; }
        // since contents can be nested [they can contain lists/containers/quotes], we must only split at the current level
        int openedCurly = 0;
        int openedSquare = 0;
        boolean inQuote = false;
        int currentStart = 0;

        for (int i = 0; i < contents.length(); i++) {
            char ch = contents.charAt(i);

            if (ch == '"') {
                if (isOuterQuote(contents, i)) { inQuote = !inQuote; }
            } else if (!inQuote) {
                if (ch == ',' && openedCurly == 0 && openedSquare == 0) {
                    commaSeparatedStrings.add(contents.substring(currentStart, i));
                    currentStart = i+1;
                }
                else if (ch == '{') { openedCurly++; }
                else if (ch == '}') { openedCurly--; }
                else if (ch == '[') { openedSquare++; }
                else if (ch == ']') { openedSquare--; }
            }
        }
        // remember to add the last
        commaSeparatedStrings.add(contents.substring(currentStart));
        return commaSeparatedStrings;
    }


    Pair<String, String> splitAndStripKeyAndValue(String property) {
        // property is always of the form key:value () note that key is a String and so can value be. They can contain
        // ':', so we can't simply match the first/last colon in property.
        int colonIndex = getPropertyStringColonIndex(property);
        String key = property.substring(0, colonIndex).strip();
        String value = property.substring(colonIndex+1).strip();
        return new Pair<>(key, value);
    }

    /** Given a single property's string, returns the index of the colon separating the name from the value. */
    private int getPropertyStringColonIndex(String property) {
        boolean inQuote = false;
        for (int i = 0; i < property.length(); i++) {
            char c = property.charAt(i);
            if (c == '"') {
                if (isOuterQuote(property, i)) { inQuote = !inQuote; }
            } else if (c == ':' && !inQuote)
                { return i; }
        }
        throw new InvalidJSON("Property without outer colon provided: " + property);
    }

    private boolean isOuterQuote(String property, int index) {
        if (property.charAt(index) != '"')
            { return false; }
        int backslashCount = 0;
        while (--index >= 0 && property.charAt(index) == '\\') { backslashCount++; }
        return backslashCount % 2 == 0;
    }

    /** @param str The string to be searched.
     *  @param start The index at which to start the search.
     *  @param step The step with which to (iteratively) search. step=1 is moving forward, and step=-1 is moving backward.
     *  @param target The looked for index
     *  @return returns the index in str that matches the first occurrence of target. If the search finds a
     *          non-whitespace character from start with step size 'step' before finding target, or if no target char
     *          is found, then it returns -1. */
    private int findNonWhitespaceIndex(String str, int start, int step, char target) {
        for (int i = start; i >= 0 && i < str.length(); i += step) {
            char ch = str.charAt(i);
            if (ch == target) return i;
            if (!Character.isWhitespace(ch)) return -1;
        }
        return -1;
    }

    boolean isString(String value) {
        return isWrapped(value, '"', '"');
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private boolean isNull(String value) {
        return value.equals("null");
    }

    private Object toString(String item) {
        return unwrap(item, '"', '"').replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\b", "\b")
                .replace("\\f", "\f");
    }

    public static String toJSONString(Object jsonObject) {
        if (jsonObject instanceof Map) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) jsonObject).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(toJSONString(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        } else if (jsonObject instanceof List) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (Object item : (List<?>) jsonObject) {
                if (!first) sb.append(",");
                sb.append(toJSONString(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        else if (jsonObject instanceof String str) { return "\"" + unescapeString(str) + "\"";}
        else if (jsonObject == null) { return "null"; }
        else { return jsonObject.toString(); }
    }

    private static String unescapeString(String original) {
        if (original == null)
        { return null; }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '/': sb.append("\\/"); break;
                default:
                    if (c < ' ') {
                        // formatting magic: control characters (e.g. carriage return or backspace) are handled correctly
                        sb.append(String.format("\\u%04x", (int) c));
                    } else { sb.append(c); }
            }
        }

        return sb.toString();
    }

}
