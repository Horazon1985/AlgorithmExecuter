package algorithmexecuter.output;

import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.exceptions.constants.AlgorithmPrinterExceptionIds;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.Algorithm;
import java.text.SimpleDateFormat;
import java.util.Date;
import algorithmexecuter.lang.translator.Translator;
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.model.utilclasses.malstring.MalStringCharSequence;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

public class AlgorithmOutputPrinter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

    private JTextPane outputArea;

    private static AlgorithmOutputPrinter instance = null;

    public static AlgorithmOutputPrinter getInstance() {
        if (instance == null) {
            instance = new AlgorithmOutputPrinter();
        }
        return instance;

    }

    /**
     * Setzt als Instanz einen Mock mockPrinter. Zugleich wird das Ausgabefeld
     * durch eine neu instanziierte JTextPane gemockt.
     */
    public static void setMockInstance(AlgorithmOutputPrinter mockPrinter) {
        instance = mockPrinter;
        instance.setOutputArea(new JTextPane());
    }

    public void setOutputArea(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    public void clearOutput() {
        if (this.outputArea == null) {
            return;
        }
        this.outputArea.setText("");
    }

    private void print(StyledDocument doc, SimpleAttributeSet keyWord, String line) {
        try {
            doc.insertString(doc.getLength(), withDate(line), keyWord);
        } catch (Exception e) {
        }
    }

    private void println(StyledDocument doc, SimpleAttributeSet keyWord, String line) {
        print(doc, keyWord, line + "\n");
    }

    public void printStartParsingAlgorithms() {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_START_COMPILING_ALGORITHM));
    }

    public void printEndParsingAlgorithms() {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_COMPILING_ALGORITHM_SUCCESSFUL));
    }

    public void printStartExecutingAlgorithms() {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_START_EXECUTING_ALGORITHM));
    }

    public void printLine(String s) {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, s);
    }

    public void printOutput(Algorithm alg, Identifier identifier) {
        if (this.outputArea == null || identifier == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        if (identifier.getType() == IdentifierType.STRING) {
            String printedValue = ((MalStringCharSequence) ((MalString) identifier.getRuntimeValue()).getMalStringSummands()[0]).getStringValue();
            println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_OUTPUT_OF_ALGORITHM, alg.getName(), printedValue));
        } else {
            println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_OUTPUT_OF_ALGORITHM, alg.getName(), identifier.getRuntimeValue()));
        }
    }

    public void printEndExecutingAlgorithms() {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_EXECUTION_OF_ALGORITHM_SUCCESSFUL));
    }

    public void printAbortAlgorithm() {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_EXECUTION_OF_ALGORITHM_ABORTED));
    }

    public void printException(Exception e) {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_EXCEPTION_IN_ALGORITHM_OCCURRED, e.getMessage()));
    }

    public void printUnexpectedException(Exception e) {
        if (this.outputArea == null) {
            return;
        }
        StyledDocument doc = this.outputArea.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        println(doc, keyWord, Translator.translateOutputMessage(AlgorithmPrinterExceptionIds.AP_UNEXPECTED_EXCEPTION_IN_ALGORITHM_OCCURRED, e.getMessage()));
    }

    private static String withDate(String s) {
        return DATE_FORMAT.format(new Date()) + ": " + s;
    }

}
