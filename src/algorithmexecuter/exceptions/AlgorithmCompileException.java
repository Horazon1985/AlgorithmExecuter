package algorithmexecuter.exceptions;

import algorithmexecuter.lang.translator.Translator;

public class AlgorithmCompileException extends AlgorithmException {

    public AlgorithmCompileException(Integer[] errorLines, Exception e) {
        super(errorLines, e.getMessage());
    }
    
    public AlgorithmCompileException(AlgorithmCompileException e) {
        super(e.getErrorLines(), e.getMessage());
    }
    
    public AlgorithmCompileException(String messageId, Object... params) {
        super(Translator.translateOutputMessage(messageId, params));
    }
    
    public AlgorithmCompileException(Integer[] errorLines, String messageId, Object... params) {
        super(errorLines, Translator.translateOutputMessage(messageId, params));
    }
    
    public AlgorithmCompileException(Integer errorLine, String messageId, Object... params) {
        super(new Integer[]{errorLine}, Translator.translateOutputMessage(messageId, params));
    }
    
}
