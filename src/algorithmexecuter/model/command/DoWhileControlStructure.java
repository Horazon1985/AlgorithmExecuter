package algorithmexecuter.model.command;

import algorithmexecuter.AlgorithmExecuter;
import algorithmexecuter.booleanexpression.BooleanExpression;
import algorithmexecuter.enums.Keyword;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmBreakException;
import algorithmexecuter.exceptions.AlgorithmContinueException;
import algorithmexecuter.exceptions.AlgorithmExecutionException;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.identifier.Identifier;
import exceptions.EvaluationException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DoWhileControlStructure extends ControlStructure {

    private final List<AlgorithmCommand> commands;

    private final BooleanExpression condition;

    public DoWhileControlStructure(List<AlgorithmCommand> commands, BooleanExpression condition) {
        this.commands = commands;
        this.condition = condition;
        this.commandBlocks = (List<AlgorithmCommand>[]) Array.newInstance(new ArrayList<>().getClass(), 1);
        this.commandBlocks[0] = commands;
    }

    public List<AlgorithmCommand> getCommands() {
        return commands;
    }

    public BooleanExpression getCondition() {
        return condition;
    }

    @Override
    public Identifier execute(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException, EvaluationException {
        Identifier result = null;
        do {
            try {
                result = AlgorithmExecuter.executeConnectedBlock(scopeMemory, this.commands);
                if (result != null) {
                    return result;
                }
            } catch (AlgorithmBreakException e) {
                return null;
            } catch (AlgorithmContinueException e) {
            }
            // Identifierwerte aktualisieren.
        } while (this.condition.evaluate(scopeMemory));
        return result;
    }

    @Override
    public String toString() {
        String doWhileCommandString = "do (";
        doWhileCommandString = this.commands.stream().map((c) -> c.toString() + "; \n").reduce(doWhileCommandString, String::concat);
        doWhileCommandString += "} while (" + this.condition.toString() + ")";
        return doWhileCommandString;
    }

    @Override
    public String toCommandString() {
        String commandString = Keyword.DO.getValue() + ReservedChars.BEGIN.getStringValue();
        for (AlgorithmCommand command : this.commands) {
            commandString += command.toCommandString();
        }
        return commandString + ReservedChars.END.getStringValue() + Keyword.WHILE.getValue() 
                + ReservedChars.OPEN_BRACKET.getStringValue() + this.condition.toString()
                + ReservedChars.CLOSE_BRACKET.getStringValue() + ReservedChars.LINE_SEPARATOR.getStringValue();
    }

}
