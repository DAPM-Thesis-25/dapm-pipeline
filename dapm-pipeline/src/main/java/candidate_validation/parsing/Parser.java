package candidate_validation.parsing;

public interface Parser<O> {
    O deserialize(String str);
}
