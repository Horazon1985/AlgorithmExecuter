package algorithmexecuter.model.utilclasses;

import algorithmexecuter.model.Signature;

public class AlgorithmCallData {

    private final Signature signature;
    private final ParameterData[] parameterValues;

    public AlgorithmCallData(Signature signature, ParameterData[] parameterValues) {
        this.signature = signature;
        this.parameterValues = parameterValues;
    }

    public Signature getSignature() {
        return signature;
    }

    public ParameterData[] getParameterValues() {
        return parameterValues;
    }

}
