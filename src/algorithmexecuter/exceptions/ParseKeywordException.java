package algorithmexecuter.exceptions;

public class ParseKeywordException extends AlgorithmCompileException {
    
    public ParseKeywordException(String message, Object... params) {
        super(message, params);
    }
    
    public ParseKeywordException(Integer[] errorLines, String message, Object... params) {
        super(errorLines, message, params);
    }
    
}
