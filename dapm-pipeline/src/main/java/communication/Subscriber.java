package communication;

public interface Subscriber<I> {
    void observe(I input);
}
