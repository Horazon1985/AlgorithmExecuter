package algorithmexecuter.booleanexpression;

import algorithmexecuter.enums.Operators;
import algorithmexecuter.model.AlgorithmMemory;
import java.util.Map;
import java.util.Set;

public class BooleanNegation extends BooleanExpression {

    private final BooleanExpression argument;

    public BooleanNegation(BooleanExpression argument) {
        this.argument = argument;
    }

    @Override
    public boolean contains(String var) {
        return this.argument.contains(var);
    }

    @Override
    public void addContainedVars(Set<String> vars) {
        this.argument.addContainedVars(vars);
    }

    @Override
    public void addContainedIndeterminates(Set<String> vars) {
        this.argument.addContainedIndeterminates(vars);
    }

    @Override
    public boolean evaluate(AlgorithmMemory scopeMemory) {
        return !this.argument.evaluate(scopeMemory);
    }

    @Override
    public void addContainedIdentifier(Set<String> vars) {
        this.argument.addContainedIdentifier(vars);
    }

    @Override
    public String toString() {
        if (this.argument instanceof BooleanBinaryOperation || this.argument instanceof BooleanBinaryOperation) {
            return Operators.NOT.getValue() + "(" + this.argument.toString() + ")";
        }
        return Operators.NOT.getValue() + this.argument.toString();
    }

}
