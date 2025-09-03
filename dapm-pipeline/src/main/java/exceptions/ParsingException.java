package exceptions;

// abstract because the name is so non-descriptive
public abstract class ParsingException extends RuntimeException {
    public ParsingException(String message) {
        super(message);
    }
}
