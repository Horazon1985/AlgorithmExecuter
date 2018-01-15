package algorithmexecuter.exceptions;

public class AlgorithmException extends Exception {

    private Integer[] errorLines;
    
    public AlgorithmException() {
    }
    
    public AlgorithmException(String message) {
        super(message);
    }
    
    public AlgorithmException(Integer[] errorLines, String message) {
        super(message);
        this.errorLines = errorLines;
    }

    public Integer[] getErrorLines() {
        return errorLines;
    }
    
}
