package exceptions;

public class PipelineExecutionException extends RuntimeException {
    public PipelineExecutionException(String message) {
        super(message);
    }
    public PipelineExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
