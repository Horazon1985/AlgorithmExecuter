package algorithmexecuter.model.command;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import algorithmexecuter.CompilerUtils;
import algorithmexecuter.booleanexpression.BooleanConstant;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.exceptions.AlgorithmExecutionException;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.exceptions.constants.AlgorithmExecutionExceptionIds;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.Algorithm;
import algorithmexecuter.booleanexpression.BooleanExpression;
import algorithmexecuter.enums.AssignValueType;
import algorithmexecuter.enums.Operators;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.Signature;
import algorithmexecuter.model.utilclasses.MalString;
import exceptions.EvaluationException;
import java.util.HashSet;
import java.util.Set;

public class AssignValueCommand extends AlgorithmCommand {

    private final Identifier identifierSrc;
    private final Object targetValue;
    private final AssignValueType type;
    private final Signature targetAlgorithmSignature;
    private final Identifier[] targetAlgorithmArguments;
    private Algorithm targetAlgorithm;

    public AssignValueCommand(Identifier identifierSrc, Object targetValue, AssignValueType type) throws AlgorithmCompileException {
        if (!areTypesCompatible(identifierSrc, IdentifierType.identifierTypeOf(targetValue))) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES);
        }
        this.identifierSrc = identifierSrc;
        this.targetValue = targetValue;
        this.type = type;
        this.targetAlgorithmSignature = null;
        this.targetAlgorithmArguments = null;
    }

    public AssignValueCommand(Identifier identifierSrc, Signature targetAlgorithmSignature, Identifier[] targetAlgorithmArguments, AssignValueType type) throws AlgorithmCompileException {
        if (!areTypesCompatible(identifierSrc, targetAlgorithmSignature.getReturnType())) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES);
        }
        this.identifierSrc = identifierSrc;
        this.targetValue = null;
        this.type = type;
        this.targetAlgorithmSignature = targetAlgorithmSignature;
        this.targetAlgorithmArguments = targetAlgorithmArguments;
    }

    private boolean areTypesCompatible(Identifier identifierSrc, IdentifierType targetType) {
        return identifierSrc.getType().isSameOrGeneralTypeOf(targetType);
    }

    public Object getTargetValue() {
        return this.targetValue;
    }

    public Identifier getIdentifierSrc() {
        return this.identifierSrc;
    }

    public Signature getTargetAlgorithmSignature() {
        return targetAlgorithmSignature;
    }

    public Identifier[] getTargetAlgorithmArguments() {
        return targetAlgorithmArguments;
    }

    public Algorithm getTargetAlgorithm() {
        return targetAlgorithm;
    }

    public AssignValueType getType() {
        return type;
    }

    public void setTargetAlgorithm(Algorithm targetAlgorithm) {
        this.targetAlgorithm = targetAlgorithm;
    }

    @Override
    public String toString() {
        String command = "AssignValueCommand[type = " + this.type + ", identifierSrc = " + this.identifierSrc;
        // Typ: MalString.
        if (this.targetValue != null) {
            if (this.targetValue instanceof MalString) {
                String values = "(";
                MalString malString = (MalString) this.targetValue;
                for (int i = 0; i < malString.getStringValues().length; i++) {
                    if (malString.getStringValues()[i] instanceof String) {
                        values += "\"" + malString.getStringValues()[i] + "\"";
                    } else {
                        values += malString.getStringValues()[i].toString();
                    }
                    if (i < malString.getStringValues().length - 1) {
                        values += ", ";
                    }
                }
                values += ")";
                return command + ", stringValues = " + values + "]";
            }
            return command + ", targetValue = " + this.targetValue + "]";
        }
        return command + ", targetAlgorithm = " + this.targetAlgorithmSignature.toString() + "]";
    }

    private Set<String> getVarsFromAlgorithmParameters(Algorithm alg) {
        Set<String> varsInAlgorithmSignature = new HashSet<>();
        AbstractExpression abstrExpr;
        for (Identifier identifier : alg.getInputParameters()) {
            if (identifier.getRuntimeValue() != null && identifier.getRuntimeValue() instanceof AbstractExpression) {
                abstrExpr = (AbstractExpression) identifier.getRuntimeValue();
                varsInAlgorithmSignature.addAll(abstrExpr.getContainedIndeterminates());
            }
        }
        return varsInAlgorithmSignature;
    }

    @Override
    public Identifier execute(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException, EvaluationException {
        if (this.targetValue != null) {
            if (this.targetValue instanceof MalString) {
                MalString malString = (MalString) this.targetValue;
                String resultValue = "";
                for (Object obj : malString.getStringValues()) {
                    if (obj instanceof String) {
                        resultValue += obj;
                    } else if (obj instanceof AbstractExpression) {
                        resultValue += simplifyTargetExpression((AbstractExpression) obj, scopeMemory);
                    }
                }
                this.identifierSrc.setRuntimeValue(new MalString(resultValue));
            } else if (this.targetValue instanceof AbstractExpression) {
                AbstractExpression abstrExpr = (AbstractExpression) this.targetValue;
                Set<String> varsInTargetExpr = abstrExpr.getContainedIndeterminates();
                checkForUnknownIdentifier(scopeMemory, varsInTargetExpr);
                AbstractExpression targetExprSimplified = simplifyTargetExpression(abstrExpr, scopeMemory);
                this.identifierSrc.setRuntimeValue(targetExprSimplified);
            }
        } else {
            this.targetAlgorithm.initInputParameter(this.targetAlgorithmArguments);
            Set<String> varsInTargetExpr = getVarsFromAlgorithmParameters(this.targetAlgorithm);
            checkForUnknownIdentifier(scopeMemory, varsInTargetExpr);
            this.identifierSrc.setValueFromGivenIdentifier(this.targetAlgorithm.execute());
        }
        scopeMemory.addToMemoryInRuntime(this.identifierSrc);
        return null;
    }

    private void checkForUnknownIdentifier(AlgorithmMemory scopeMemory, Set<String> varsInTargetExpr) throws AlgorithmExecutionException {
        for (String var : varsInTargetExpr) {
            if (!scopeMemory.containsIdentifier(var)) {
                throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_UNKNOWN_IDENTIFIER);
            }
        }
    }

    private AbstractExpression simplifyTargetExpression(AbstractExpression abstrExpr, AlgorithmMemory scopeMemory) throws EvaluationException {
        AbstractExpression targetExprSimplified;

        if (abstrExpr instanceof Expression) {
            Expression exprSimplified = (Expression) abstrExpr;
            for (Identifier identifier : scopeMemory.values()) {
                if (identifier.getRuntimeValue() instanceof Expression) {
                    exprSimplified = exprSimplified.replaceVariable(identifier.getName(), (Expression) identifier.getRuntimeValue());
                }
            }
            targetExprSimplified = exprSimplified;
        } else if (abstrExpr instanceof LogicalExpression) {
            LogicalExpression logExprSimplified = (LogicalExpression) abstrExpr;
            for (Identifier identifier : scopeMemory.values()) {
                if (identifier.getRuntimeValue() instanceof LogicalExpression) {
                    logExprSimplified = logExprSimplified.replaceVariable(identifier.getName(), (LogicalExpression) identifier.getRuntimeValue());
                }
            }
            targetExprSimplified = logExprSimplified;
        } else if (abstrExpr instanceof MatrixExpression) {
            MatrixExpression matExprSimplified = (MatrixExpression) abstrExpr;
            for (Identifier identifier : scopeMemory.values()) {
                if (identifier.getRuntimeValue() instanceof Expression) {
                    matExprSimplified = matExprSimplified.replaceVariable(identifier.getName(), (Expression) identifier.getRuntimeValue());
                } else if (identifier.getRuntimeValue() instanceof MatrixExpression) {
                    matExprSimplified = matExprSimplified.replaceMatrixVariable(identifier.getName(), (MatrixExpression) identifier.getRuntimeValue());
                }
            }
            targetExprSimplified = matExprSimplified;
        } else {
            targetExprSimplified = new BooleanConstant(((BooleanExpression) abstrExpr).evaluate(CompilerUtils.extractAbstactExpressionValuesOfIdentifiers(scopeMemory)));
        }

        if (targetExprSimplified instanceof Expression) {
            return ((Expression) targetExprSimplified).simplify();
        } else if (targetExprSimplified instanceof LogicalExpression) {
            return ((LogicalExpression) targetExprSimplified).simplify();
        } else if (targetExprSimplified instanceof MatrixExpression) {
            return ((MatrixExpression) targetExprSimplified).simplify();
        }
        return targetExprSimplified;
    }

    @Override
    public String toCommandString() {
        String commandString = "";
        if (this.type == AssignValueType.NEW) {
            commandString += this.identifierSrc.getType().toString() + " ";
        }
        commandString += this.identifierSrc.getName() + Operators.DEFINE.getValue();
        if (this.targetValue != null) {
            if (this.targetValue instanceof MalString) {
                MalString malString = (MalString) this.targetValue;
                for (int i = 0; i < malString.getStringValues().length; i++) {
                    if (malString.getStringValues()[i] instanceof String) {
                        commandString += "\"" + malString.getStringValues()[i] + "\"";
                    } else {
                        commandString += malString.getStringValues()[i].toString();
                    }
                    if (i < malString.getStringValues().length - 1) {
                        commandString += Operators.CONCAT.getValue();
                    }
                }
            } else {
                commandString += this.targetValue;
            }
        } else {
            commandString += this.targetAlgorithm.getName() + ReservedChars.OPEN_BRACKET.getStringValue();
            for (int i = 0; i < this.targetAlgorithmArguments.length; i++) {
                commandString += this.targetAlgorithmArguments[i].getName();
                if (i < this.targetAlgorithmArguments.length - 1) {
                    commandString += ReservedChars.ARGUMENT_SEPARATOR.getStringValue();
                }
            }
            commandString += ReservedChars.CLOSE_BRACKET.getStringValue();
        }
        return commandString + ReservedChars.LINE_SEPARATOR.getStringValue();
    }

}
