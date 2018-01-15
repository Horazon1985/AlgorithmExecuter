package algorithmexecuter.exceptions;

public class ParseReturnException extends AlgorithmCompileException {
    
    public ParseReturnException(String message, Object... params) {
        super(message, params);
    }
    
    public ParseReturnException(Integer[] errorLines, String message, Object... params) {
        super(errorLines, message, params);
    }
    
}
