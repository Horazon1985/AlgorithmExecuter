package algorithmexecuter.model.utilclasses.malstring;

import algorithmexecuter.model.AlgorithmMemory;

public class MalStringString extends MalStringSummand {

    private String stringValue;

    public MalStringString(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getVariableName() {
        return stringValue;
    }

    public void setVariableName(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString(){
        return "MalStringString[" + this.stringValue + "]";
    }

    @Override
    public Object getRuntimeValue(AlgorithmMemory scopeMemory) {
        return this.stringValue;
    }
    
}
