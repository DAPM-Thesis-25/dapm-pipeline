package candidate_validation.parsing;

import exceptions.ParsingException;

public class InvalidCandidate extends ParsingException {
    public InvalidCandidate(String message) {
        super(message);
    }
}
