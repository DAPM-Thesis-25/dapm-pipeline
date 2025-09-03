package communication.message.serialization.parsing;

import exceptions.ParsingException;

public class InvalidJXES extends ParsingException {
    public InvalidJXES(String message) { super(message); }
}
