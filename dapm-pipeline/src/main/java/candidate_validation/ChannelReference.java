package candidate_validation;

import communication.message.Message;

import java.util.Objects;
import java.util.Set;

public class ChannelReference {
    private final ProcessingElementReference publisher;
    private final Set<SubscriberReference> subscribers;

    public ChannelReference(ProcessingElementReference publisher, Set<SubscriberReference> subscribers) {
        this.publisher = publisher;
        this.subscribers = subscribers;
    }

    public ChannelReference(ProcessingElementReference publisher, SubscriberReference singleSubscriber) {
        this.publisher = publisher;
        this.subscribers = Set.of(singleSubscriber);
    }

    public ProcessingElementReference getPublisher() { return publisher; }

    public Set<SubscriberReference> getSubscribers() { return Set.copyOf(subscribers); }

    public Class<? extends Message> output() { return publisher.getOutput(); }

    @Override
    public String toString() {
        return "MC[" + publisher + ", " + subscribers + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ChannelReference otherChannel)) return false;
        return publisher.equals(otherChannel.publisher) && subscribers.equals(otherChannel.subscribers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publisher, subscribers);
    }

}
