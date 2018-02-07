package algorithmexecuter.model.command;

import abstractexpressions.expression.classes.Expression;
import algorithmexecuter.AlgorithmBuilder;
import algorithmexecuter.annotations.Execute;
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
import algorithmexecuter.model.utilclasses.malstring.MalStringCharSequence;
import algorithmexecuter.output.AlgorithmOutputPrinter;
import exceptions.EvaluationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        for (Algorithm alg : AlgorithmBuilder.ALGORITHMS.getAlgorithmStorage()) {
            if (alg.getSignature().equals(getSignature()) && alg.getReturnType() == null) {
                alg.initInputParameter(this.identifiers);
                alg.execute();
                return null;
            }
        }
        // Nun alle standardmäßig implementierten Void-Methoden ausprobieren.
        return executeFixedVoidCommand(scopeMemory);
    }

    private Identifier executeFixedVoidCommand(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        Method[] methods = VoidCommand.class.getDeclaredMethods();
        Execute annotation;
        for (Method method : methods) {
            annotation = method.getAnnotation(Execute.class);
            if (annotation != null && annotation.algorithmName().getValue().equals(this.name)) {
                try {
                    method.invoke(this, scopeMemory);
                    return null;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    if (e.getCause() instanceof AlgorithmExecutionException) {
                        throw (AlgorithmExecutionException) e.getCause();
                    }
                }
            }
        }
        throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_NO_SUCH_COMMAND, this.getSignature());
    }

    @Execute(algorithmName = FixedAlgorithmNames.INC)
    private void executeInc(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        inc(this.identifiers[0]);
    }

    @Execute(algorithmName = FixedAlgorithmNames.DEC)
    private void executeDec(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        dec(this.identifiers[0]);
    }

    @Execute(algorithmName = FixedAlgorithmNames.PRINT)
    private void executePrint(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        if (this.identifiers[0].getType() != IdentifierType.STRING) {
            AlgorithmOutputPrinter.getInstance().printLine(this.identifiers[0].getRuntimeValue().toString());
        } else {
            AlgorithmOutputPrinter.getInstance().printLine(((MalStringCharSequence) ((MalString) this.identifiers[0].getRuntimeValue()).getMalStringSummands()[0]).getStringValue());
        }
    }

    @Execute(algorithmName = FixedAlgorithmNames.ENTRY)
    private void executeEntry(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        // Nichts tun. Der Rückgabewert wird ohnehin nicht berücksichtigt.
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
