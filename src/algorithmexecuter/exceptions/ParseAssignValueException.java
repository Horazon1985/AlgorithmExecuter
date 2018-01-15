package algorithmexecuter.exceptions;

public class ParseAssignValueException extends AlgorithmCompileException {
    
    public ParseAssignValueException(AlgorithmCompileException e) {
        super(e);
    }
    
    public ParseAssignValueException(String message, Object... params) {
        super(message, params);
    }
    
    public ParseAssignValueException(Integer[] errorLines, String message, Object... params) {
        super(errorLines, message, params);
    }
    
}
