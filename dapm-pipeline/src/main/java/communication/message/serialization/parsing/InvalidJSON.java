package communication.message.serialization.parsing;

import exceptions.ParsingException;

public class InvalidJSON extends ParsingException {
    public InvalidJSON(String message) { super(message); }
}