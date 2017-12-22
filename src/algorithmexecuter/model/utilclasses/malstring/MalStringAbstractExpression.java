package algorithmexecuter.model.utilclasses.malstring;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import algorithmexecuter.booleanexpression.BooleanExpression;
import algorithmexecuter.model.AlgorithmMemory;
import java.util.Set;

public class MalStringAbstractExpression extends MalStringSummand {

    private AbstractExpression abstrExpr;

    public MalStringAbstractExpression(AbstractExpression abstrExpr) {
        this.abstrExpr = abstrExpr;
    }

    public AbstractExpression getAbstractExpression() {
        return abstrExpr;
    }

    public void setAbstractExpression(AbstractExpression abstrExpr) {
        this.abstrExpr = abstrExpr;
    }

    @Override
    public String toString(){
        return "MalStringAbstractExpression[" + this.abstrExpr + "]";
    }

    @Override
    public Object getRuntimeValue(AlgorithmMemory scopeMemory) {
        return replaceVariablesInMemory(scopeMemory);
    }
    
    private AbstractExpression replaceVariablesInMemory(AlgorithmMemory scopeMemory) {
        AbstractExpression resultExpression = this.abstrExpr;
        
        if (this.abstrExpr instanceof Expression) {
            Set<String> vars = resultExpression.getContainedVars();
            for (String var : vars) {
                if (scopeMemory.containsIdentifier(var)) {
                    resultExpression = ((Expression) resultExpression).replaceVariable(var, (Expression) scopeMemory.get(var).getRuntimeValue());
                }
            }
        } else if (this.abstrExpr instanceof MatrixExpression) {
            Set<String> exprVars = ((MatrixExpression) resultExpression).getContainedExpressionVars();
            Set<String> matrixVars = ((MatrixExpression) resultExpression).getContainedMatrixVars();
            for (String var : exprVars) {
                if (scopeMemory.containsIdentifier(var)) {
                    resultExpression = ((MatrixExpression) resultExpression).replaceVariable(var, (Expression) scopeMemory.get(var).getRuntimeValue());
                }
            }
            for (String var : matrixVars) {
                if (scopeMemory.containsIdentifier(var)) {
                    resultExpression = ((MatrixExpression) resultExpression).replaceMatrixVariable(var, (MatrixExpression) scopeMemory.get(var).getRuntimeValue());
                }
            }
        } else if (this.abstrExpr instanceof BooleanExpression) {
            Set<String> exprVars = ((BooleanExpression) resultExpression).getContainedExpressionVars();
            Set<String> matrixVars = ((BooleanExpression) resultExpression).getContainedMatrixVars();
            Set<String> booleanVars = ((BooleanExpression) resultExpression).getContainedBooleanVars(scopeMemory);




            
            
            
        }
        
        return resultExpression;
    }
    
}
