package algorithmexecuter.model.utilclasses;

import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import algorithmexecuter.model.utilclasses.malstring.MalStringAbstractExpression;
import algorithmexecuter.model.utilclasses.malstring.MalStringCharSequence;
import algorithmexecuter.model.utilclasses.malstring.MalStringSummand;
import algorithmexecuter.model.utilclasses.malstring.MalStringVariable;
import java.util.Set;

public class MalString {

    private MalStringSummand[] summands;

    public MalString(MalStringSummand[] stringValues) {
        this.summands = stringValues;
    }

    public MalString(String s) {
        this.summands = new MalStringSummand[]{new MalStringCharSequence(s)};
    }

    public MalStringSummand[] getMalStringSummands() {
        return summands;
    }

    public void setMalStringSummands(MalStringSummand[] summands) {
        this.summands = summands;
    }

    public boolean contains(String varName) {
        for (MalStringSummand summand : this.summands) {
            if (summand instanceof MalStringVariable && ((MalStringVariable) summand).getVariableName().equals(varName)) {
                return true;
            }
        }
        return false;
    }

    public void addContainedVars(Set<String> vars) {
        for (MalStringSummand summand : this.summands) {
            if (summand instanceof MalStringVariable) {
                vars.add(((MalStringVariable) summand).getVariableName());
            } else if (summand instanceof MalStringAbstractExpression) {
                AbstractExpression abstrExpr = ((MalStringAbstractExpression) summand).getAbstractExpression();
                if (abstrExpr instanceof MatrixExpression) {
                    vars.addAll(((MatrixExpression) abstrExpr).getContainedMatrixVars());
                }
                vars.addAll(abstrExpr.getContainedVars());
            }
        }
    }

    @Override
    public String toString() {
        String result = "(";
        for (int i = 0; i < this.summands.length; i++) {
            result += this.summands[i];
            if (i < this.summands.length - 1) {
                result += ", ";
            }
        }
        return result + ")";
    }

}
