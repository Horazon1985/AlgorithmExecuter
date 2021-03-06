package algorithmexecuter.model;

import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.model.identifier.Identifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlgorithmMemory {

    private Algorithm algorithm;
    private final Map<String, Identifier> memory;

    public AlgorithmMemory(Algorithm alg) {
        this.algorithm = alg;
        this.memory = new HashMap<>();
    }

    public AlgorithmMemory(Algorithm alg, List<Identifier> identifiers) {
        this(alg);
        for (Identifier identifier : identifiers) {
            this.memory.put(identifier.getName(), identifier);
        }
    }

    public AlgorithmMemory(Algorithm alg, Identifier[] identifiers) {
        this(alg);
        for (Identifier identifier : identifiers) {
            this.memory.put(identifier.getName(), identifier);
        }
    }

    public Algorithm getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(Algorithm alg) {
        this.algorithm = alg;
    }
    
    public Identifier get(String name) {
        return this.memory.get(name);
    }

    public void put(String name, Identifier identifier) {
        this.memory.put(name, identifier);
    }
    
    public boolean containsKey(String name) {
        return this.memory.containsKey(name);
    }

    public Set<String> keySet() {
        return this.memory.keySet();
    }

    public Collection<Identifier> values() {
        return this.memory.values();
    }

    @Override
    public String toString() {
        String memoryString;
        if (this.algorithm == null) {
            memoryString = "AlgorithmMemory[Algorithm = null";
        } else {
            memoryString = "AlgorithmMemory[Algorithm = " + this.algorithm.getName();
        }
        if (!this.memory.keySet().isEmpty()) {
            memoryString += ", ";
        }
        for (String identifierName : this.memory.keySet()) {
            memoryString += identifierName + ": " + this.memory.get(identifierName) + ", ";
        }
        if (!this.memory.keySet().isEmpty()) {
            return memoryString.substring(0, memoryString.length() - 2) + "]";
        }
        return memoryString + "]";
    }

    public boolean containsIdentifier(String identifierName) {
        return this.memory.get(identifierName) != null;
    }

    public void clearMemory() {
        this.memory.clear();
    }
    
    public int getSize() {
        return this.memory.size();
    }

    public AlgorithmMemory copyMemory() {
        AlgorithmMemory copyOfMemory = new AlgorithmMemory(this.algorithm);
        for (String identifierName : this.memory.keySet()) {
            copyOfMemory.put(identifierName, this.memory.get(identifierName));
        }
        return copyOfMemory;
    }

    public void addToMemoryInCompileTime(Integer[] errorLines, Identifier identifier) throws AlgorithmCompileException {
        if (this.memory.get(identifier.getName()) != null) {
            // Identifier existiert bereits!
            throw new AlgorithmCompileException(errorLines, AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, identifier.getName());
        }
        this.memory.put(identifier.getName(), identifier);
    }

    public void addToMemoryInRuntime(Identifier identifier) {
        /*
        Während der Laufzeit kann es zu keinen Namensclashs kommen,
        da der Algorithmus zuvor bereits kompiliert wurde.
         */
        this.memory.put(identifier.getName(), identifier);
    }

    public void removeFromMemory(Identifier identifier) {
        this.memory.remove(identifier.getName());
    }

}
