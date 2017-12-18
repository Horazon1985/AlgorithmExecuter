package algorithmexecuter.model.utilclasses;

import algorithmexecuter.enums.IdentifierType;

public class ParameterData {

    private IdentifierType type;
    private Object value;

    public IdentifierType getType() {
        return type;
    }

    public void setType(IdentifierType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ParameterData(Object value) {
        this.type = IdentifierType.identifierTypeOf(value);
        this.value = value;
    }

    @Override
    public String toString() {
        return "Parameter[type = " + this.type + ", value = " + this.value + "]";
    }

}
