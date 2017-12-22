package algorithmexecuter.model.utilclasses.malstring;

import algorithmexecuter.model.AlgorithmMemory;

public class MalStringCharSequence extends MalStringSummand {

    private String stringValue;

    public MalStringCharSequence(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString(){
        return "MalStringCharSequence[" + this.stringValue + "]";
    }

    @Override
    public Object getRuntimeValue(AlgorithmMemory scopeMemory) {
        return this.stringValue;
    }
    
}
