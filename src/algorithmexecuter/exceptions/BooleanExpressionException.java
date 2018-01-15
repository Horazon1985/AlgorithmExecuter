package algorithmexecuter.exceptions;

import algorithmexecuter.lang.translator.Translator;

public class BooleanExpressionException extends AlgorithmException {
    
    public BooleanExpressionException(String message, Object... params) {
        super(Translator.translateOutputMessage(message, params));
    }
    
    public BooleanExpressionException(Integer[] errorLines, String message, Object... params) {
        super(errorLines, Translator.translateOutputMessage(message, params));
    }
    
}
