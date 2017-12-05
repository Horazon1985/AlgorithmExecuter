package algorithmexecuter.model.identifier;

import abstractexpressions.interfaces.AbstractExpression;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.utilclasses.MalString;
import java.util.Objects;

public class Identifier {

    private final IdentifierType type;
    private final String name;
    private Object runtimeValue;

    private Identifier(IdentifierType type, String name) {
        this.type = type;
        this.name = name;
    }

    public IdentifierType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object getRuntimeValue() {
        return runtimeValue;
    }

    public void setRuntimeValue(Object value) {
        this.runtimeValue = value;
    }

    public void setValueFromGivenIdentifier(Identifier identifier) {
        this.runtimeValue = identifier.runtimeValue;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.runtimeValue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identifier other = (Identifier) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.runtimeValue, other.runtimeValue)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Identifier[type = " + this.type + ", name = " + this.name
                + ", runtimeValue = " + this.runtimeValue + "]";
    }

    public static Identifier createIdentifier(String identifierName, IdentifierType type) {
        return new Identifier(type, identifierName);
    }

    public static Identifier createIdentifier(AlgorithmMemory scopeMemory, String identifierName, IdentifierType type) {
        if (scopeMemory.containsIdentifier(identifierName)) {
            return scopeMemory.getMemory().get(identifierName);
        }
        return new Identifier(type, identifierName);
    }
    
}
