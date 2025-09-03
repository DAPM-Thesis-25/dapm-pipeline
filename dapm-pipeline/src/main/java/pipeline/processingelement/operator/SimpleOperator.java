package pipeline.processingelement.operator;

import communication.message.Message;
import pipeline.processingelement.Configuration;

public abstract class SimpleOperator<O extends Message> extends Operator<O, O> {

    public SimpleOperator(Configuration configuration) { super(configuration); }

    @Override
    protected O convertAlgorithmOutput(O algorithmOutput) { return algorithmOutput; }

    @Override
    protected boolean publishCondition(O algorithmOutput) { return true; }

}

