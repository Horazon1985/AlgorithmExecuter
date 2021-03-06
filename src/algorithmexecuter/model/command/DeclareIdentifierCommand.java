package algorithmexecuter.model.command;

import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmExecutionException;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.AlgorithmMemory;

public class DeclareIdentifierCommand extends AlgorithmCommand {

    private final Identifier identifierSrc;

    public DeclareIdentifierCommand(Identifier identifierSrc) {
        this.identifierSrc = identifierSrc;
    }

    public Identifier getIdentifierSrc() {
        return this.identifierSrc;
    }

    @Override
    public String toString() {
        return "DeclareIdentifierCommand[identifierSrc = " + this.identifierSrc + "]";
    }

    @Override
    public Identifier execute(AlgorithmMemory scopeMemory) throws AlgorithmExecutionException {
        scopeMemory.addToMemoryInRuntime(this.identifierSrc);
        return null;
    }

    @Override
    public String toCommandString() {
        return this.identifierSrc.getType().toString() + " " + this.identifierSrc.getName() + ReservedChars.LINE_SEPARATOR.getStringValue();
    }
    
}
