package algorithmexecuter.exceptions;

public class ParseControlStructureException extends AlgorithmCompileException {
    
    public ParseControlStructureException(Integer[] errorLines, Exception e) {
        super(errorLines, e);
    }
    
    public ParseControlStructureException(String message, Object... params) {
        super(message, params);
    }
    
    public ParseControlStructureException(Integer[] errorLines, String message, Object... params) {
        super(errorLines, message, params);
    }
    
}
