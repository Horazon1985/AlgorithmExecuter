package algorithmexecuter.model;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import algorithmexecuter.AlgorithmCompiler;
import algorithmexecuter.AlgorithmExecuter;
import algorithmexecuter.CompilerUtils;
import algorithmexecuter.annotations.Execute;
import algorithmexecuter.enums.FixedAlgorithmNames;
import algorithmexecuter.model.command.AlgorithmCommand;
import algorithmexecuter.model.command.IfElseControlStructure;
import algorithmexecuter.model.command.WhileControlStructure;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmExecutionException;
import algorithmexecuter.exceptions.constants.AlgorithmExecutionExceptionIds;
import algorithmexecuter.model.command.DoWhileControlStructure;
import algorithmexecuter.model.command.ForControlStructure;
import algorithmexecuter.model.identifier.Identifier;
import exceptions.EvaluationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Algorithm {

    private final String name;
    private final Identifier[] inputParameters;
    private final IdentifierType returnType;
    private final List<AlgorithmCommand> commands;

    private Algorithm(String name, Identifier[] inputParameters, IdentifierType returnType, List<AlgorithmCommand> commands) {
        this.name = name;
        this.inputParameters = inputParameters;
        this.returnType = returnType;
        this.commands = commands;
    }

    public Algorithm(String name, Identifier[] inputParameters, IdentifierType returnType) {
        this(name, inputParameters, returnType, new ArrayList<AlgorithmCommand>());
    }

    public Signature getSignature() {
        IdentifierType[] identifierTypes = new IdentifierType[this.inputParameters.length];
        for (int i = 0; i < this.inputParameters.length; i++) {
            identifierTypes[i] = this.inputParameters[i].getType();
        }
        return new Signature(this.returnType, this.name, identifierTypes);
    }

    public String getName() {
        return name;
    }

    public Identifier[] getInputParameters() {
        return inputParameters;
    }

    public IdentifierType getReturnType() {
        return returnType;
    }

    public List<AlgorithmCommand> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        String algorithm = "";
        if (this.returnType != null) {
            algorithm += this.returnType + " ";
        }
        algorithm += this.name + "(";
        for (int i = 0; i < this.inputParameters.length; i++) {
            algorithm += this.inputParameters[i].getType() + " " + this.inputParameters[i].getName();
            if (i < this.inputParameters.length - 1) {
                algorithm += ", ";
            }
        }
        algorithm += ") {\n";
        for (AlgorithmCommand c : this.commands) {
            algorithm += c.toString() + "; \n";
        }
        return algorithm + "}";
    }

    public void appendCommand(AlgorithmCommand command) {
        command.setAlgorithm(this);
        this.commands.add(command);
    }

    public void appendCommands(List<AlgorithmCommand> commands) {
        appendCommands(commands, true);
    }

    private void appendCommands(List<AlgorithmCommand> commands, boolean topLevel) {
        for (AlgorithmCommand c : commands) {
            c.setAlgorithm(this);
            if (c.isControlStructure()) {
                if (c.isIfElseControlStructure()) {
                    IfElseControlStructure ifElseCommand = (IfElseControlStructure) c;
                    appendCommands(ifElseCommand.getCommandsIfPart(), false);
                    appendCommands(ifElseCommand.getCommandsElsePart(), false);
                } else if (c.isWhileControlStructure()) {
                    WhileControlStructure whileCommand = (WhileControlStructure) c;
                    appendCommands(whileCommand.getCommands(), false);
                } else if (c.isDoWhileControlStructure()) {
                    DoWhileControlStructure doWhileCommand = (DoWhileControlStructure) c;
                    appendCommands(doWhileCommand.getCommands(), false);
                } else if (c.isForControlStructure()) {
                    ForControlStructure forCommand = (ForControlStructure) c;
                    appendCommands(forCommand.getInitialization(), false);
                    appendCommands(forCommand.getEndLoopCommands(), false);
                    appendCommands(forCommand.getLoopAssignment(), false);
                    appendCommands(forCommand.getCommands(), false);
                }
            }
            if (topLevel) {
                this.commands.add(c);
            }
        }
    }

    public void initInputParameter(Identifier[] identifiers) {
        Object[] values = new Object[identifiers.length];
        for (int i = 0; i < this.inputParameters.length; i++) {
            values[i] = identifiers[i].getRuntimeValue();
        }
        for (int i = 0; i < this.inputParameters.length; i++) {
            this.inputParameters[i].setRuntimeValue(values[i]);
        }
    }

    public Identifier execute() throws AlgorithmExecutionException, EvaluationException {
        // Leeren Algorithmus nur im void-Fall akzeptieren.
        if (this.commands.isEmpty()) {
            if (this.returnType == null) {
                return Identifier.NULL_IDENTIFIER;
            } else if (!isStandardAlgorithm()) {
                throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_RETURN_TYPE_EXPECTED);
            }
        }

        // Prüfung, ob alle Parameter Werte besitzen. Sollte eigentlich stets der Fall sein.
        checkForInputIdentifierWithoutValues();
        
        // Prüfung, ob es sich um einen Standardalgorithmus handelt.
        if (isStandardAlgorithm()) {
            return executeStandardAlgorithm(getInitialAlgorithmMemory());
        }

        return AlgorithmExecuter.executeConnectedBlock(getInitialAlgorithmMemory(), this.commands);
    }

    private void checkForInputIdentifierWithoutValues() throws AlgorithmExecutionException {
        for (int i = 0; i < this.inputParameters.length; i++) {
            if (this.inputParameters[i].getRuntimeValue() == null) {
                throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_ALGORITHM_NOT_ALL_INPUT_PARAMETERS_SET, i, this.getName());
            }
        }
    }

    private boolean isStandardAlgorithm() {
        for (Algorithm alg : AlgorithmCompiler.FIXED_ALGORITHMS) {
            if (alg.getName().equals(this.name)) {
                return true;
            }
        }
        return false;
    }
    
    private Identifier executeStandardAlgorithm(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        Method[] methods = Algorithm.class.getDeclaredMethods();
        Execute annotation;
        for (Method method : methods) {
            annotation = method.getAnnotation(Execute.class);
            if (annotation != null && annotation.algorithmName().getValue().equals(this.name)) {
                try {
                    return (Identifier) method.invoke(this, scopeMemory);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    if (e.getCause() instanceof AlgorithmExecutionException) {
                        throw (AlgorithmExecutionException) e.getCause();
                    }
                }
            }
        }
        throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_NO_SUCH_COMMAND, getSignature());
    }

    @Execute(algorithmName = FixedAlgorithmNames.ENTRY)
    private Identifier executeEntry(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        MatrixExpression matExpr = (MatrixExpression) this.inputParameters[0].getRuntimeValue();
        Expression i = (Expression) this.inputParameters[1].getRuntimeValue();
        Expression j = (Expression) this.inputParameters[2].getRuntimeValue();
        if (!matExpr.isMatrix()) {
            throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_MATRIXEXPRESSION_COULD_NOT_BE_SIMPLIFIED_TO_A_MATRIX, matExpr);
        }
        Matrix m = (Matrix) matExpr;
        if (!i.isIntegerConstant()) {
            throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_INDEX_IS_NOT_AN_INTEGER, i);
        }
        BigInteger indexI = ((Constant) i).getBigIntValue();
        if (indexI.compareTo(BigInteger.ZERO) < 0 || indexI.compareTo(BigInteger.valueOf(m.getColumnNumber() - 1)) > 0) {
            throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_INDEX_OUT_OF_BOUNDS, indexI);
        }
        if (!j.isIntegerConstant()) {
            throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_INDEX_IS_NOT_AN_INTEGER, j);
        }
        BigInteger indexJ = ((Constant) j).getBigIntValue();
        if (indexJ.compareTo(BigInteger.ZERO) < 0 || indexJ.compareTo(BigInteger.valueOf(m.getRowNumber() - 1)) > 0) {
            throw new AlgorithmExecutionException(AlgorithmExecutionExceptionIds.AE_INDEX_OUT_OF_BOUNDS, indexJ);
        }
        Identifier result = Identifier.createIdentifier(CompilerUtils.generateTechnicalIdentifierName(scopeMemory), IdentifierType.EXPRESSION);
        result.setRuntimeValue(m.getEntry(((Constant) i).getBigIntValue().intValue(), ((Constant) j).getBigIntValue().intValue()));
        return result;
    }

    private AlgorithmMemory getInitialAlgorithmMemory() {
        return new AlgorithmMemory(this, this.getInputParameters());
    }

    public String toCommandString() {
        String commandString = "";
        if (this.returnType != null) {
            commandString += this.returnType.getValue() + " ";
        }
        commandString += this.name + ReservedChars.OPEN_BRACKET.getStringValue();
        for (int i = 0; i < this.inputParameters.length; i++) {
            commandString += this.inputParameters[i].getType().getValue() + " " + this.inputParameters[i].getName();
            if (i < this.inputParameters.length - 1) {
                commandString += ReservedChars.ARGUMENT_SEPARATOR.getStringValue() + " ";
            }
        }
        commandString += ReservedChars.CLOSE_BRACKET.getStringValue() + ReservedChars.BEGIN.getStringValue();

        for (AlgorithmCommand command : this.commands) {
            commandString += command.toCommandString();
        }

        return commandString + ReservedChars.END.getStringValue();

    }

}
