package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
// TODO: understand simple source and maybe distinguish instead by making a GeneratingSource (a source from which data is generated in the source).
public abstract class SimpleSource<O extends Message> extends Source<O> {

    private volatile boolean isRunning;
    private ThreadPoolExecutor executor;

    public SimpleSource(Configuration configuration) { super(configuration); }

    @Override
    public boolean start() {
        try {
            isRunning = true;
            if(executor == null || executor.isShutdown()) {
                executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
            }
            executor.submit(() -> {
                while(isRunning) {
                    O output = process();
                    publish(output);
                }
            });
            return isRunning;
        } catch (Exception e) {
            throw new RuntimeException("Exception in SimpleSource", e);
        }
    }

    protected abstract O process();

    @Override
    public boolean terminate() {
        isRunning = false;
        assert executor != null;
        executor.shutdown();
        executor = null;
        return super.terminate();
    }
}
