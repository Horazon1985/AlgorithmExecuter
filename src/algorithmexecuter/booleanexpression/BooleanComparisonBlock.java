package algorithmexecuter.booleanexpression;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import algorithmexecuter.enums.ComparingOperators;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.model.utilclasses.malstring.MalStringAbstractExpression;
import algorithmexecuter.model.utilclasses.malstring.MalStringCharSequence;
import algorithmexecuter.model.utilclasses.malstring.MalStringSummand;
import algorithmexecuter.model.utilclasses.malstring.MalStringVariable;
import exceptions.EvaluationException;
import java.util.Set;

public class BooleanComparisonBlock extends BooleanExpression {

    private final Object left;
    private final Object right;
    private final ComparingOperators comparingOperator;

    public BooleanComparisonBlock(Object left, Object right, ComparingOperators comparingOperator) {
        this.left = left;
        this.right = right;
        this.comparingOperator = comparingOperator;
    }

    @Override
    public boolean contains(String var) {
        boolean containsLeft = contains(this.left, var);
        boolean containsRight = contains(this.right, var);
        return containsLeft || containsRight;
    }

    private boolean contains(Object comparisonPart, String var) {
        if (comparisonPart instanceof AbstractExpression) {
            return ((AbstractExpression) comparisonPart).contains(var);
        }
        return ((MalString) comparisonPart).contains(var);
    }

    @Override
    public void addContainedVars(Set<String> vars) {
        if (this.left instanceof AbstractExpression) {
            if (this.left instanceof MatrixExpression) {
                // Im Falle von Matrizen sind noch Matrixvariablen möglich.
                ((MatrixExpression) this.left).addContainedVars(vars);
            }
            ((AbstractExpression) this.left).addContainedVars(vars);
        } else {
            ((MalString) this.left).addContainedVars(vars);
        }
        if (this.right instanceof AbstractExpression) {
            if (this.right instanceof MatrixExpression) {
                // Im Falle von Matrizen sind noch Matrixvariablen möglich.
                ((MatrixExpression) this.right).addContainedVars(vars);
            }
            ((AbstractExpression) this.right).addContainedVars(vars);
        } else {
            ((MalString) this.right).addContainedVars(vars);
        }
    }

    @Override
    public void addContainedIndeterminates(Set<String> vars) {
        if (this.left instanceof AbstractExpression) {
            ((AbstractExpression) this.left).addContainedIndeterminates(vars);
        } else {
            ((MalString) this.left).addContainedVars(vars);
        }
        if (this.right instanceof AbstractExpression) {
            ((AbstractExpression) this.right).addContainedIndeterminates(vars);
        } else {
            ((MalString) this.right).addContainedVars(vars);
        }
    }

    @Override
    public boolean evaluate(AlgorithmMemory scopeMemory) {
        if (isComparisonOfExpressions()) {
            /* 
            Wenn arithmetische Fehler auftreten, dann werden diese nicht geworfen, 
            sondern der Vergleich liefert stets 'false'
             */
            try {
                Expression exprLeft = (Expression) replaceVariablesInExpressionByIdentifierValues((Expression) this.left, scopeMemory);
                Expression exprRight = (Expression) replaceVariablesInExpressionByIdentifierValues((Expression) this.right, scopeMemory);
                double valueLeft = exprLeft.evaluate();
                double valueRight = exprRight.evaluate();
                switch (this.comparingOperator) {
                    case EQUALS:
                        if (exprLeft.equivalent(exprRight)) {
                            return true;
                        } else if (exprLeft.getContainedIndeterminates().isEmpty() && exprRight.getContainedIndeterminates().isEmpty()) {
                            return valueLeft == valueRight;
                        }
                        break;
                    case NOT_EQUALS:
                        return !new BooleanComparisonBlock(exprLeft, exprRight, ComparingOperators.EQUALS).evaluate(scopeMemory);
                    case GREATER:
                        if (exprLeft.getContainedIndeterminates().isEmpty() && exprRight.getContainedIndeterminates().isEmpty()) {
                            return valueLeft > valueRight;
                        }
                        break;
                    case GREATER_OR_EQUALS:
                        if (exprLeft.getContainedIndeterminates().isEmpty() && exprRight.getContainedIndeterminates().isEmpty()) {
                            return valueLeft >= valueRight;
                        }
                        break;
                    case SMALLER:
                        if (exprLeft.getContainedIndeterminates().isEmpty() && exprRight.getContainedIndeterminates().isEmpty()) {
                            return valueLeft < valueRight;
                        }
                        break;
                    case SMALLER_OR_EQUALS:
                        if (exprLeft.getContainedIndeterminates().isEmpty() && exprRight.getContainedIndeterminates().isEmpty()) {
                            return valueLeft <= valueRight;
                        }
                        break;
                }
            } catch (EvaluationException e) {
            }
            return false;
        } else if (isComparisonOfLogicalExpressions()) {
            /* 
            Wenn arithmetische Fehler auftreten, dann werden diese nicht geworfen, 
            sondern der Vergleich liefert stets 'false'
             */
            boolean logValueLeft = ((LogicalExpression) this.left).evaluate();
            boolean logValueRight = ((LogicalExpression) this.left).evaluate();
            switch (this.comparingOperator) {
                case EQUALS:
                    return logValueLeft == logValueRight;
                case NOT_EQUALS:
                    return logValueLeft != logValueRight;
            }
            return false;
        } else if (isComparisonOfMatrixExpressions()) {
            /* 
            Wenn arithmetische Fehler auftreten, dann werden diese nicht geworfen, 
            sondern der Vergleich liefert stets 'false'
             */
            try {
                MatrixExpression matValueLeft = ((MatrixExpression) this.left).evaluate();
                MatrixExpression matValueRight = ((MatrixExpression) this.left).evaluate();
                switch (this.comparingOperator) {
                    case EQUALS:
                        return matValueLeft.equivalent(matValueRight);
                    case NOT_EQUALS:
                        return !matValueLeft.equivalent(matValueRight);
                }
            } catch (EvaluationException e) {
            }
            return false;
        } else if (isComparisonOfBooleanExpressions()) {
            boolean boolValueLeft = ((BooleanExpression) this.left).evaluate(scopeMemory);
            boolean boolValueRight = ((BooleanExpression) this.right).evaluate(scopeMemory);
            switch (this.comparingOperator) {
                case EQUALS:
                    return boolValueLeft == boolValueRight;
                case NOT_EQUALS:
                    return boolValueLeft != boolValueRight;
            }
        } else if (isComparisonOfStrings()) {
            String leftStringEvaluated = replaceVariablesInMalStringByIdentifierValuesAndGetStringValue((MalString) this.left, scopeMemory);
            String rightStringEvaluated = replaceVariablesInMalStringByIdentifierValuesAndGetStringValue((MalString) this.right, scopeMemory);
            switch (this.comparingOperator) {
                case EQUALS:
                    return leftStringEvaluated.equals(rightStringEvaluated);
                case NOT_EQUALS:
                    return !leftStringEvaluated.equals(rightStringEvaluated);
            }
        }

        return false;
    }

    private boolean isComparisonOfExpressions() {
        return this.left instanceof Expression && this.right instanceof Expression && this.comparingOperator != null;
    }

    private boolean isComparisonOfLogicalExpressions() {
        return this.left instanceof LogicalExpression && this.right instanceof LogicalExpression && this.comparingOperator != null;
    }

    private boolean isComparisonOfMatrixExpressions() {
        return this.left instanceof MatrixExpression && this.right instanceof MatrixExpression && this.comparingOperator != null;
    }

    private boolean isComparisonOfBooleanExpressions() {
        return this.left instanceof BooleanExpression && this.right instanceof BooleanExpression && this.comparingOperator != null;
    }

    private boolean isComparisonOfStrings() {
        return this.left instanceof MalString && this.right instanceof MalString && this.comparingOperator != null;
    }

    @Override
    public void addContainedIdentifier(Set<String> vars) {
        addContainedVars(vars);
    }

    @Override
    public String toString() {
        return this.left.toString() + this.comparingOperator.getValue() + this.right.toString();
    }

    private static AbstractExpression replaceVariablesInExpressionByIdentifierValues(Expression expr, AlgorithmMemory scopeMemory) {
        Set<String> vars = expr.getContainedVars();
        for (String var : vars) {
            if (scopeMemory.containsIdentifier(var) && scopeMemory.get(var).getRuntimeValue() instanceof Expression) {
                expr = expr.replaceVariable(var, (Expression) scopeMemory.get(var).getRuntimeValue());
            }
        }
        return expr;
    }

    private static String replaceVariablesInMalStringByIdentifierValuesAndGetStringValue(MalString malString, AlgorithmMemory scopeMemory) {
        String resultString = "";
        for (MalStringSummand summand : malString.getMalStringSummands()) {
            resultString += getRuntimeSummandStringValue(summand, scopeMemory);
        }
        return resultString;
    }

    private static String getRuntimeSummandStringValue(MalStringSummand summand, AlgorithmMemory scopeMemory) {
        if (summand instanceof MalStringCharSequence) {
            return ((MalStringCharSequence) summand).getStringValue();
        }
        if (summand instanceof MalStringVariable) {
            Object variableValue = scopeMemory.get(((MalStringVariable) summand).getVariableName()).getRuntimeValue();
            if (variableValue == null) {
                return "null";
            }
            if (variableValue instanceof AbstractExpression) {
                return variableValue.toString();
            }
            // Fall: variableValue ist ein MalString. Zur Laufzeit hat es dann immer genau einen Summanden, welcher eine einfache Zeichenkette ist.
            return ((MalStringCharSequence) ((MalString) variableValue).getMalStringSummands()[0]).getStringValue();
        }
        if (summand instanceof MalStringAbstractExpression) {
            if (((MalStringAbstractExpression) summand).getAbstractExpression() == null) {
                return "null";
            }
            return ((MalStringAbstractExpression) summand).getAbstractExpression().toString();
        }
        return "";
    }

}
