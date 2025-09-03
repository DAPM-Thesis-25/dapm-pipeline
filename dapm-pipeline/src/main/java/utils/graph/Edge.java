package utils.graph;

import java.util.Objects;

public class Edge<T> {
    private final T from;
    private final T to;

    public Edge(T from, T to) {
        this.from = from;
        this.to = to;
    }

    public T getFrom() { return from; }
    public T getTo() { return to; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?> edge = (Edge<?>) o;
        return Objects.equals(from, edge.from) && Objects.equals(to, edge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
