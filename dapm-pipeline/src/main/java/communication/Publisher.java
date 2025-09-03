package communication;

public interface Publisher<T> {
    void publish(T message);
}

