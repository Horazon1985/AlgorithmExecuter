package algorithmexecuter;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.matrixexpression.classes.Matrix;
import algorithmexecuter.enums.IdentifierType;
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
    
    ////////////////////////////////// Typecast-Methoden //////////////////////////////////////
    /**
     * Castet das Objekt obj OHNE verhierige Prüfung zum Typ supertype.
     */
    public static Object castToSameOrSuperType(Object obj, IdentifierType supertype) {
        // Ausdrücke werden immer zu 1x1-Matrizen gecastet.
        if (IdentifierType.identifierTypeOf(obj) == IdentifierType.EXPRESSION && supertype == IdentifierType.MATRIX_EXPRESSION) {
            return new Matrix((Expression) obj);
        }
        return obj;
    }
    
}
