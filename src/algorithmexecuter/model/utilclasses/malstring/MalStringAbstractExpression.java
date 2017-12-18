package algorithmexecuter.model.utilclasses.malstring;

import abstractexpressions.interfaces.AbstractExpression;
import algorithmexecuter.model.AlgorithmMemory;

public class MalStringAbstractExpression extends MalStringSummand {

    private AbstractExpression abstrExpr;

    public MalStringAbstractExpression(AbstractExpression abstrExpr) {
        this.abstrExpr = abstrExpr;
    }

    public AbstractExpression getVariableName() {
        return abstrExpr;
    }

    public void setVariableName(AbstractExpression abstrExpr) {
        this.abstrExpr = abstrExpr;
    }

    @Override
    public String toString(){
        return "MalStringAbstractExpression[" + this.abstrExpr + "]";
    }

    @Override
    public Object getRuntimeValue(AlgorithmMemory scopeMemory) {
        return this.abstrExpr;
    }
    
}
