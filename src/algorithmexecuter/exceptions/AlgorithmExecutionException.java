package algorithmexecuter.exceptions;

import algorithmexecuter.lang.translator.Translator;

public class AlgorithmExecutionException extends AlgorithmException {

    private AlgorithmExecutionException(boolean messageIsClearText, String message) {
        super(message);
    }
    
    public AlgorithmExecutionException(String messageId, Object... params) {
        super(Translator.translateOutputMessage(messageId, params));
    }

    public AlgorithmExecutionException(Integer[] errorLines, String messageId, Object... params) {
        super(errorLines, Translator.translateOutputMessage(messageId, params));
    }
    
    public static AlgorithmExecutionException createAlgorithmExecutionExceptionWithMessage(String message) {
        return new AlgorithmExecutionException(true, message);
    }
     
}
