package algorithmexecuter.booleanexpression;

import algorithmexecuter.model.AlgorithmMemory;
import java.util.Set;

public class BooleanVariable extends BooleanExpression {

    private final String name;

    public BooleanVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean contains(String var) {
        return this.name.equals(var);
    }

    @Override
    public void addContainedVars(Set<String> vars) {
        vars.add(this.name);
    }

    @Override
    public void addContainedIndeterminates(Set<String> vars) {
        vars.add(this.name);
    }

    @Override
    public boolean evaluate(AlgorithmMemory scopeMemory) {
        if (scopeMemory.containsKey(this.name) && scopeMemory.get(this.name).getRuntimeValue() instanceof BooleanConstant) {
            return ((BooleanConstant) scopeMemory.get(this.name).getRuntimeValue()).getValue();
        }
        return false;
    }

    @Override
    public void addContainedIdentifier(Set<String> vars) {
        vars.add(this.name);
    }
    
    @Override
    public String toString() {
        return this.name;
    }

}
