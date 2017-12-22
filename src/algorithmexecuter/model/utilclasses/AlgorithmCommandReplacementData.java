package algorithmexecuter.model.utilclasses;

import algorithmexecuter.model.command.AlgorithmCommand;
import java.util.List;

public class AlgorithmCommandReplacementData {

    private final List<AlgorithmCommand> commands;
    private final String substitutedExpression;

    public AlgorithmCommandReplacementData(List<AlgorithmCommand> commands, String rightSide) {
        this.commands = commands;
        this.substitutedExpression = rightSide;
    }

    public List<AlgorithmCommand> getCommands() {
        return commands;
    }

    public String getSubstitutedExpression() {
        return substitutedExpression;
    }

}