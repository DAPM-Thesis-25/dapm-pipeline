package candidate_validation.parsing;

import exceptions.ParsingException;

public class JsonSchemaMismatch extends ParsingException {
    public JsonSchemaMismatch(String message) { super(message); }
}
