package algorithmexecuter.model.utilclasses.malstring;

import algorithmexecuter.model.AlgorithmMemory;

public class MalStringVariable extends MalStringSummand {

    private String variableName;

    public MalStringVariable(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String toString(){
        return "MalStringVariable[" + this.variableName + "]";
    }

    @Override
    public Object getRuntimeValue(AlgorithmMemory scopeMemory) {
        return scopeMemory.get(this.variableName).getRuntimeValue();
    }
    
}
