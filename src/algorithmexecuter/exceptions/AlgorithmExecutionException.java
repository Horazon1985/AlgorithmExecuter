package algorithmexecuter.exceptions;

import algorithmexecuter.lang.translator.Translator;

public class AlgorithmExecutionException extends AlgorithmException {

    public AlgorithmExecutionException(String messageId, Object... params) {
        super(Translator.translateOutputMessage(messageId, params));
    }

}
