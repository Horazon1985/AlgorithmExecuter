package algorithmexecuter.model.command;

import algorithmexecuter.AlgorithmExecuter;
import algorithmexecuter.CompilerUtils;
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
import java.util.Map;

public class WhileControlStructure extends ControlStructure {

    private final BooleanExpression condition;
    private final List<AlgorithmCommand> commands;

    public WhileControlStructure(BooleanExpression condition, List<AlgorithmCommand> commands) {
        this.condition = condition;
        this.commands = commands;
        this.commandBlocks = (List<AlgorithmCommand>[]) Array.newInstance(new ArrayList<>().getClass(), 1);
        this.commandBlocks[0] = commands;
    }

    public BooleanExpression getCondition() {
        return condition;
    }

    public List<AlgorithmCommand> getCommands() {
        return commands;
    }

    @Override
    public Identifier execute(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException, EvaluationException {
        Identifier result = null;
        while (this.condition.evaluate(scopeMemory)) {
            try {
                result = AlgorithmExecuter.executeConnectedBlock(scopeMemory, this.commands);
                if (result != null) {
                    return result;
                }
            } catch (AlgorithmBreakException e) {
                return null;
            } catch (AlgorithmContinueException e) {
            }
        }
        return result;
    }

    @Override
    public String toString() {
        String whileCommandString = "while (" + this.condition.toString() + ") {";
        for (AlgorithmCommand c : this.commands) {
            whileCommandString += c.toString() + "; \n";
        }
        return whileCommandString + "}";
    }

    @Override
    public String toCommandString() {
        String commandString = Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getStringValue() + this.condition.toString()
                + ReservedChars.CLOSE_BRACKET.getStringValue() + ReservedChars.BEGIN.getStringValue();

        for (AlgorithmCommand command : this.commands) {
            commandString += command.toCommandString();
        }
        return commandString + ReservedChars.END.getStringValue();
    }
    
}
