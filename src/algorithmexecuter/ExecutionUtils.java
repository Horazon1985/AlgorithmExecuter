package algorithmexecuter;

import algorithmexecuter.model.AlgorithmMemory;

public final class ExecutionUtils {
    
    private ExecutionUtils() {
    }
    
    public static void updateMemoryBeforeBlockExecution(AlgorithmMemory memoryBeforBlockExecution, AlgorithmMemory scopeMemory) {
        for (String identifierName : memoryBeforBlockExecution.keySet()) {
            if (scopeMemory.keySet().contains(identifierName)) {
                memoryBeforBlockExecution.put(identifierName, scopeMemory.get(identifierName));
            }
        }
    }
    
    
}
