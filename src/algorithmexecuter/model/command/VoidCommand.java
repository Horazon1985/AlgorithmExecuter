package algorithmexecuter.model.command;

import abstractexpressions.expression.classes.Expression;
import algorithmexecuter.AlgorithmCompiler;
import algorithmexecuter.enums.FixedAlgorithmNames;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmExecutionException;
import algorithmexecuter.exceptions.constants.AlgorithmExecutionExceptionIds;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.Algorithm;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.Signature;
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.output.AlgorithmOutputPrinter;
import exceptions.EvaluationException;

public class VoidCommand extends AlgorithmCommand {

    private final String name;
    private final Identifier[] identifiers;

    public VoidCommand(String name, Identifier... identifiers) {
        this.name = name;
        this.identifiers = identifiers;
    }

    public String getName() {
        return name;
    }

    public Identifier[] getIdentifiers() {
        return identifiers;
    }

    @Override
    public String toString() {
        return "VoidCommand[name = " + this.name + ", identifiers = " + identifierArrayToString(this.identifiers) + "]";
    }

    private String identifierArrayToString(Identifier[] identifiers) {
        String result = "(";
        for (int i = 0; i < identifiers.length; i++) {
            result += identifiers[i];
            if (i < identifiers.length - 1) {
                result += ", ";
            }
        }
        return result + ")";
    }

    public Signature getSignature() {
        IdentifierType[] identifierTypes = new IdentifierType[this.identifiers.length];
        for (int i = 0; i < this.identifiers.length; i++) {
            identifierTypes[i] = this.identifiers[i].getType();
        }
        return new Signature(null, this.name, identifierTypes);
    }

    @Override
    public Identifier execute(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException, EvaluationException {
        // Zunächst über alle definierten Algorithmen iterieren.
        for (Algorithm alg : AlgorithmCompiler.ALGORITHMS.getAlgorithmStorage()) {
            if (alg.getSignature().equals(getSignature()) && alg.getReturnType() == null) {
                alg.initInputParameter(this.identifiers);
                alg.execute();
                return null;
            }
        }
        // Nun alle standardmäßig implementierten Void-Methoden ausprobieren.
        // "inc" = ++
        if (this.name.equals(FixedAlgorithmNames.INC.getValue()) && this.identifiers.length == 1 && this.identifiers[0].getType() == IdentifierType.EXPRESSION) {
            inc(this.identifiers[0]);
            scopeMemory.addToMemoryInRuntime(Identifier.createIdentifier(scopeMemory, this.name, IdentifierType.EXPRESSION));
            return null;
        }
        // "dec" = --
        if (this.name.equals(FixedAlgorithmNames.DEC.getValue()) && this.identifiers.length == 1 && this.identifiers[0].getType() == IdentifierType.EXPRESSION) {
            dec(this.identifiers[0]);
            scopeMemory.addToMemoryInRuntime(Identifier.createIdentifier(scopeMemory, this.name, IdentifierType.EXPRESSION));
            return null;
        }
        // "print" = Konsolenausgabe
        if (this.name.equals(FixedAlgorithmNames.PRINT.getValue()) && this.identifiers.length == 1) {
            if (this.identifiers[0].getType() != IdentifierType.STRING) {
                AlgorithmOutputPrinter.printLine(this.identifiers[0].toString());
            } else {
                AlgorithmOutputPrinter.printLine(stringArrayToOutputString(this.identifiers[0].getRuntimeStringValue()));
            }
            return null;
        }
        throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_NO_SUCH_COMMAND);
    }

    private String stringArrayToOutputString(MalString malString) {
        String result = "";
        for (Object obj : malString.getStringValues()) {
            result += obj;
        }
        return result;
    }

    //////////////////////// Liste vordefinierter Void-Befehle ////////////////////////
    public static void inc(Identifier identifier) throws AlgorithmExecutionException {
        if (identifier.getRuntimeValue() != null) {
            try {
                identifier.setRuntimeValue(((Expression) identifier.getRuntimeValue()).add(Expression.ONE).simplify());
                return;
            } catch (EvaluationException e) {
                throw new AlgorithmExecutionException(e.getMessage());
            }
        }
        throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_NULL_POINTER, identifier.getName());
    }

    public static void dec(Identifier identifier) throws AlgorithmExecutionException {
        if (identifier.getRuntimeValue() != null) {
            try {
                identifier.setRuntimeValue(((Expression) identifier.getRuntimeValue()).sub(Expression.ONE).simplify());
                return;
            } catch (EvaluationException e) {
                throw new AlgorithmExecutionException(e.getMessage());
            }
        }
        throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_NULL_POINTER, identifier.getName());
    }

    @Override
    public String toCommandString() {
        String commandString = this.name + ReservedChars.OPEN_BRACKET.getStringValue();
        for (int i = 0; i < this.identifiers.length; i++) {
            commandString += this.identifiers[i].getName();
            if (i < this.identifiers.length - 1) {
                commandString += ReservedChars.ARGUMENT_SEPARATOR.getStringValue() + " ";
            }
        }
        return commandString + ReservedChars.CLOSE_BRACKET.getStringValue() + ReservedChars.LINE_SEPARATOR.getStringValue();
    }

}
