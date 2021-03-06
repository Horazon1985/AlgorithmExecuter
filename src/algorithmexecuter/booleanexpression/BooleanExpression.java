package algorithmexecuter.booleanexpression;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.interfaces.IdentifierValidator;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixVariable;
import algorithmexecuter.CompilerUtils;
import algorithmexecuter.enums.ComparingOperators;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.enums.Keyword;
import algorithmexecuter.enums.Operators;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.exceptions.BooleanExpressionException;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.utilclasses.EditorCodeString;
import algorithmexecuter.model.utilclasses.MalString;
import exceptions.ExpressionException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BooleanExpression implements AbstractExpression {

    public abstract boolean evaluate(AlgorithmMemory scopeMemory);

    @Override
    public Set<String> getContainedVars() {
        Set<String> vars = new HashSet<>();
        addContainedVars(vars);
        return vars;
    }

    @Override
    public Set<String> getContainedIndeterminates() {
        Set<String> vars = new HashSet<>();
        addContainedIndeterminates(vars);
        return vars;
    }

    public Set<String> getContainedExpressionVars() {
        Set<String> allVars = getContainedVars();
        Set<String> exprVars = new HashSet<>();
        for (String var : allVars) {
            if (Variable.doesVariableAlreadyExist(var)) {
                exprVars.add(var);
            }
        }
        return exprVars;
    }

    public Set<String> getContainedExpressionIndeterminates() {
        Set<String> allVars = getContainedIndeterminates();
        Set<String> exprVars = new HashSet<>();
        for (String var : allVars) {
            if (Variable.doesVariableAlreadyExist(var)) {
                exprVars.add(var);
            }
        }
        return exprVars;
    }

    public Set<String> getContainedBooleanVars(AlgorithmMemory scopeMemory) {
        Set<String> allVars = getContainedVars();
        Set<String> boolVars = new HashSet<>();
        for (String var : allVars) {
            if (scopeMemory.get(var) != null && scopeMemory.get(var).getType().equals(IdentifierType.BOOLEAN_EXPRESSION)) {
                boolVars.add(var);
            }
        }
        return boolVars;
    }

    public Set<String> getContainedBooleanIndeterminates(AlgorithmMemory scopeMemory) {
        Set<String> allVars = getContainedIndeterminates();
        Set<String> boolVars = new HashSet<>();
        for (String var : allVars) {
            if (scopeMemory.get(var) != null && scopeMemory.get(var).getType().equals(IdentifierType.BOOLEAN_EXPRESSION)) {
                boolVars.add(var);
            }
        }
        return boolVars;
    }

    public Set<String> getContainedMatrixVars() {
        Set<String> allVars = getContainedVars();
        Set<String> exprVars = new HashSet<>();
        for (String var : allVars) {
            if (MatrixVariable.doesMatrixVariableAlreadyExist(var)) {
                exprVars.add(var);
            }
        }
        return exprVars;
    }

    public Set<String> getContainedMatrixIndeterminates() {
        Set<String> allVars = getContainedIndeterminates();
        Set<String> exprVars = new HashSet<>();
        for (String var : allVars) {
            if (MatrixVariable.doesMatrixVariableAlreadyExist(var)) {
                exprVars.add(var);
            }
        }
        return exprVars;
    }

    public static BooleanExpression build(EditorCodeString input, IdentifierValidator validator,
            Map<String, IdentifierType> typesMap) throws BooleanExpressionException {

        /*
         Prioritäten: |: 0, &: 1, !: 2, ~: 3, Vergleiche, Boolsche Konstante oder Variable: 4.
         */
        int priority = 4;
        int breakpoint = -1;
        int bracketCounter = 0;
        int inputLength = input.length();
        EditorCodeString currentEnding;

        if (input.getValue().equals("")) {
            throw new BooleanExpressionException(AlgorithmCompileExceptionIds.AC_BOOLEAN_EXPRESSION_EMPTY_OR_INCOMPLETE);
        }

        for (int i = 1; i <= inputLength - 1; i++) {
            currentEnding = input.substring(0, inputLength - i + 1);

            // Öffnende und schließende Klammern zählen.
            if (currentEnding.endsWith(ReservedChars.OPEN_BRACKET.getStringValue()) && bracketCounter == 0) {
                throw new BooleanExpressionException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
            }

            if (currentEnding.endsWith(ReservedChars.CLOSE_BRACKET.getStringValue())) {
                bracketCounter++;
            }
            if (currentEnding.endsWith(ReservedChars.OPEN_BRACKET.getStringValue())) {
                bracketCounter--;
            }

            if (bracketCounter != 0) {
                continue;
            }
            // Aufteilungspunkt finden; zunächst wird nach |, &, !, Vergleichsoperator gesucht 
            // breakpoint gibt den Index in formula an, wo die Formel aufgespalten werden soll.
            if (currentEnding.endsWith(Operators.OR.getValue()) && priority > 1) {
                priority = 0;
                breakpoint = inputLength - i;
            } else if (currentEnding.endsWith(Operators.AND.getValue()) && priority > 2) {
                priority = 1;
                breakpoint = inputLength - i;
            } else if (currentEnding.endsWith(Operators.NOT.getValue()) && priority > 3) {
                priority = 2;
                breakpoint = inputLength - i;
            } else if ((currentEnding.endsWith(ComparingOperators.EQUALS.getValue())
                    || currentEnding.endsWith(ComparingOperators.NOT_EQUALS.getValue())
                    || currentEnding.endsWith(ComparingOperators.GREATER.getValue())
                    || currentEnding.endsWith(ComparingOperators.GREATER_OR_EQUALS.getValue())
                    || currentEnding.endsWith(ComparingOperators.SMALLER.getValue())
                    || currentEnding.endsWith(ComparingOperators.SMALLER_OR_EQUALS.getValue()))
                    && priority > 0) {
                priority = 3;
                breakpoint = inputLength - i;
            }
        }

        if (bracketCounter > 0) {
            throw new BooleanExpressionException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.OPEN_BRACKET.getValue());
        }

        // Aufteilung, falls eine Elementaroperation (|, &, !) vorliegt
        if (priority < 2) {
            EditorCodeString inputLeft = input.substring(0, breakpoint);
            EditorCodeString inputRight = input.substring(breakpoint + 1, inputLength);

            if (inputLeft.getValue().equals("") && priority != 1) {
                throw new BooleanExpressionException(AlgorithmCompileExceptionIds.AC_LEFT_SIDE_OF_BOOLEAN_BINARY_EXPRESSION_IS_EMPTY);
            }
            if (inputRight.getValue().equals("")) {
                throw new BooleanExpressionException(AlgorithmCompileExceptionIds.AC_RIGHT_SIDE_OF_BOOLEAN_BINARY_EXPRESSION_IS_EMPTY);
            }

            switch (priority) {
                case 0:
                    return new BooleanBinaryOperation(build(inputLeft, validator, typesMap),
                            build(inputRight, validator, typesMap), BooleanBinaryOperationType.OR);
                case 1:
                    return new BooleanBinaryOperation(build(inputLeft, validator, typesMap),
                            build(inputRight, validator, typesMap), BooleanBinaryOperationType.AND);
            }
        }

        if (priority == 2 && breakpoint == 0) {
            /*
             Falls eine Negation vorliegt, dann muss breakpoint == 0 sein.
             Falls formula von der Form !xyz... ist, dann soll xyz... gelesen
             werden und dann die entsprechende Negation zurückgegeben
             werden.
             */
            EditorCodeString inputLeft = input.substring(1, inputLength);
            return new BooleanNegation(build(inputLeft, validator, typesMap));
        }

        if (priority >= 3) {
            // WICHTIG: Verglichen wird bei (Matrizen-) Ausdrücken stets mit der Methode equivalent().
            ComparingOperators comparisonType = null;
            if (containsOperatorExactlyOneTime(input, ComparingOperators.EQUALS)) {
                comparisonType = ComparingOperators.EQUALS;
            } else if (containsOperatorExactlyOneTime(input, ComparingOperators.NOT_EQUALS)) {
                comparisonType = ComparingOperators.NOT_EQUALS;
            } else if (containsOperatorExactlyOneTime(input, ComparingOperators.GREATER_OR_EQUALS)) {
                comparisonType = ComparingOperators.GREATER_OR_EQUALS;
            } else if (containsOperatorExactlyOneTime(input, ComparingOperators.GREATER)) {
                comparisonType = ComparingOperators.GREATER;
            } else if (containsOperatorExactlyOneTime(input, ComparingOperators.SMALLER_OR_EQUALS)) {
                comparisonType = ComparingOperators.SMALLER_OR_EQUALS;
            } else if (containsOperatorExactlyOneTime(input, ComparingOperators.SMALLER)) {
                comparisonType = ComparingOperators.SMALLER;
            }

            if (comparisonType != null) {
                // Es kommt genau ein Vergleichsoperator in input vor.
                EditorCodeString leftPart = input.substring(0, input.indexOf(comparisonType.getValue()));
                EditorCodeString rightPart = input.substring(input.indexOf(comparisonType.getValue()) + comparisonType.getValue().length());
                /* 
                Es wird nach folgenden Regeln eine Instanz von BooleanComparisonBlock gebildet: 
                (1) Alle Operatoren machen bei gewöhnlichen Ausdrücken Sinn. 
                (2) Bei boolschen Ausdrücken, Matrizenausdrücken und Strings machen nur "==" und "!=" Sinn.
                 */
                Object left = parseMalExpression(leftPart, validator, typesMap);
                Object right = parseMalExpression(rightPart, validator, typesMap);
                if (left != null && right != null) {
                    if (left instanceof Expression && right instanceof Expression) {
                        return new BooleanComparisonBlock(left, right, comparisonType);
                    }
                    if (left instanceof BooleanExpression && right instanceof BooleanExpression && (comparisonType == ComparingOperators.EQUALS || comparisonType == ComparingOperators.NOT_EQUALS)) {
                        return new BooleanComparisonBlock(left, right, comparisonType);
                    }
                    if (left instanceof MatrixExpression && right instanceof MatrixExpression && (comparisonType == ComparingOperators.EQUALS || comparisonType == ComparingOperators.NOT_EQUALS)) {
                        return new BooleanComparisonBlock(left, right, comparisonType);
                    }
                    if (left instanceof MalString && right instanceof MalString && (comparisonType == ComparingOperators.EQUALS || comparisonType == ComparingOperators.NOT_EQUALS)) {
                        return new BooleanComparisonBlock(left, right, comparisonType);
                    }
                    throw new BooleanExpressionException(AlgorithmCompileExceptionIds.AC_BOOLEAN_OPERATOR_NOT_APPLICABLE, left, right);
                }
            }
        }

        // Falls kein binärer Operator und die Formel die Form (...) hat -> Klammern beseitigen
        if (priority == 4 && input.substring(0, 1).getValue().equals(ReservedChars.OPEN_BRACKET.getStringValue())
                && input.substring(inputLength - 1, inputLength).getValue().equals(ReservedChars.CLOSE_BRACKET.getStringValue())) {
            return build(input.substring(1, inputLength - 1), validator, typesMap);
        }

        // Falls der Ausdruck eine logische Konstante ist (false, true)
        if (priority == 4) {
            if (input.getValue().equals(Keyword.FALSE.getValue())) {
                return new BooleanConstant(false);
            }
            if (input.getValue().equals(Keyword.TRUE.getValue())) {
                return new BooleanConstant(true);
            }
        }

        // Falls der Ausdruck eine Variable ist
        if (priority == 4) {
            if (validator.isValidIdentifier(input.getValue()) && typesMap.get(input.getValue()) == IdentifierType.BOOLEAN_EXPRESSION) {
                return new BooleanVariable(input.getValue());
            }
        }

        throw new BooleanExpressionException(AlgorithmCompileExceptionIds.AC_BOOLEAN_EXPRESSION_CANNOT_BE_INTERPRETED, input.getValue());
    }

    private static boolean containsOperatorExactlyOneTime(EditorCodeString input, ComparingOperators op) {
        return input.contains(op.getValue()) && input.length() - input.replaceAll(op.getValue(), "").length() == op.getValue().length();
    }

    /**
     * Versucht, den Parameter input zu parsen. Die Typen, gegen die geparst
     * wird, sind: Expression, BooleanExpression, MatrixExpression, MalString.
     */
    private static Object parseMalExpression(EditorCodeString input, IdentifierValidator validator, Map<String, IdentifierType> typesMap) {
        // In valuesMap werden nur Variablen aufgenommen.
        AbstractExpression parsedInput;
        try {
            parsedInput = Expression.build(input.getValue(), validator);
            if (doesValuesMapContainAllVarsOfCorrectType(parsedInput, typesMap)) {
                return parsedInput;
            }
        } catch (ExpressionException e) {
        }
        try {
            /*
            Hier darf build() rekursiv angewendet werden, da input hier eine kleinere Länge
            besitzt, als der input im vorherigen Aufruf.
             */
            parsedInput = MatrixExpression.build(input.getValue(), validator, validator);
            if (doesValuesMapContainAllVarsOfCorrectType(parsedInput, typesMap)) {
                return parsedInput;
            }
        } catch (ExpressionException e) {
        }
        try {
            parsedInput = BooleanExpression.build(input, validator, typesMap);
            if (doesValuesMapContainAllVarsOfCorrectType(parsedInput, typesMap)) {
                return parsedInput;
            }
        } catch (BooleanExpressionException e) {
        }

        try {
            return CompilerUtils.getMalString(input, typesMap);
        } catch (AlgorithmCompileException e) {
        }

        return null;
    }
    
    private static boolean doesValuesMapContainAllVarsOfCorrectType(AbstractExpression abstrExpr, Map<String, IdentifierType> typesMap) {
        Set<String> vars = abstrExpr.getContainedVars();
        IdentifierType type = IdentifierType.identifierTypeOf(abstrExpr);
        for (String var : vars) {
            if (!typesMap.containsKey(var)) {
                return false;
            }
            if (!type.isSameOrSuperTypeOf(typesMap.get(var))) {
                return false;
            }
        }
        return true;
    }

    public abstract void addContainedIdentifier(Set<String> vars);

    public boolean isEquiv() {
        return this instanceof BooleanBinaryOperation && ((BooleanBinaryOperation) this).getType().equals(BooleanBinaryOperationType.EQUIVALENCE);
    }

    public boolean isOr() {
        return this instanceof BooleanBinaryOperation && ((BooleanBinaryOperation) this).getType().equals(BooleanBinaryOperationType.OR);
    }

    public boolean isAnd() {
        return this instanceof BooleanBinaryOperation && ((BooleanBinaryOperation) this).getType().equals(BooleanBinaryOperationType.AND);
    }

    public boolean isBuildingBlock() {
        return this instanceof BooleanComparisonBlock;
    }

    public BooleanExpression not() {
        return new BooleanNegation(this);
    }

    public BooleanExpression equiv(BooleanExpression boolExpr) {
        return new BooleanBinaryOperation(this, boolExpr, BooleanBinaryOperationType.EQUIVALENCE);
    }

    public BooleanExpression or(BooleanExpression boolExpr) {
        return new BooleanBinaryOperation(this, boolExpr, BooleanBinaryOperationType.OR);
    }

    public BooleanExpression and(BooleanExpression boolExpr) {
        return new BooleanBinaryOperation(this, boolExpr, BooleanBinaryOperationType.AND);
    }

}
