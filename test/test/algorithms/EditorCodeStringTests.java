package test.algorithms;

import algorithmexecuter.model.utilclasses.EditorCodeString;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EditorCodeStringTests {
    
    @Test
    public void replaceTest() {
        EditorCodeString editorCodeString = new EditorCodeString("T\neststr\ning");
        
        EditorCodeString editorCodeStringReplaced = editorCodeString.replaceFirst("st", "ABCD", 0);
        EditorCodeString resultString = new EditorCodeString("T\neABCDstr\ning");
        assertTrue(resultString.equals(editorCodeStringReplaced));
        
        editorCodeStringReplaced = editorCodeString.replaceAll("st", "ABCD");
        resultString = new EditorCodeString("T\neABCDABCDr\ning");
        assertTrue(resultString.equals(editorCodeStringReplaced));

        editorCodeStringReplaced = editorCodeString.replaceAll("xy", "ABCD");
        resultString = new EditorCodeString("T\neststr\ning");
        assertTrue(resultString.equals(editorCodeStringReplaced));
    }
    
}
