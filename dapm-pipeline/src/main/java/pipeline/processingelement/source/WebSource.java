package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.Configuration;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public abstract class WebSource<O extends Message> extends Source<O> {
    private Disposable subscription;

    public WebSource(Configuration configuration) { super(configuration); }

    @Override
    public boolean start() {
        try {
            subscription = process().subscribe(this::publish);
            return !subscription.isDisposed();
        } catch (Exception e) {
            throw new RuntimeException("Exception in WebSource", e);
        }
    }

    protected abstract Flux<O> process();

    @Override
    public boolean terminate() {
        assert subscription != null;
        subscription.dispose();
        subscription = null;
        return super.terminate();
    }
}
