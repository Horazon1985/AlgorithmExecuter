package algorithmexecuter.model.utilclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EditorCodeString {

    private String value;

    private final Integer[] lineNumbers;

    public EditorCodeString(String value) {
        this.value = "";
        List<Integer> lineNumberList = new ArrayList<>();
        int line = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.substring(i, i + 1).equals("\n")) {
                line++;
            } else {
                this.value += value.charAt(i);
                lineNumberList.add(line);
            }
        }
        this.lineNumbers = lineNumberList.toArray(new Integer[lineNumberList.size()]);
    }

    private EditorCodeString(String value, Integer[] lineNumbers) {
        this.value = value;
        this.lineNumbers = lineNumbers;
    }

    public  EditorCodeString(EditorCodeString code) {
        this.value = code.value;
        this.lineNumbers = code.lineNumbers;
    }
    
    /**
     * Gibt einen EditorCodeString zurück, dessen Stringwert der gegebene String
     * value ist und dessen Zeilennummern lineNumbers den konstanten Wert
     * lineNumber besitzen.
     */
    public static EditorCodeString createEditorCodeStringWithGivenLineNumber(String value, int lineNumber) {
        Integer[] constantLineNumbers = new Integer[value.length()];
        for (int i = 0; i < constantLineNumbers.length; i++) {
            constantLineNumbers[i] = lineNumber;
        }
        return new EditorCodeString(value, constantLineNumbers);
    }

    public String getValue() {
        return value;
    }

    public Integer[] getLineNumbers() {
        return lineNumbers;
    }

    public EditorCodeString substring(int i) {
        return substring(i, this.value.length());
    }

    public EditorCodeString substring(int i, int j) {
        String substring = this.value.substring(i, j);
        Integer[] lineNumbersOfSubstring = new Integer[substring.length()];
        for (int k = i; k < j; k++) {
            lineNumbersOfSubstring[k - i] = this.lineNumbers[k];
        }
        return new EditorCodeString(substring, lineNumbersOfSubstring);
    }

    public char charAt(int i) {
        return this.value.charAt(i);
    }

    public EditorCodeString firstChar() {
        if (this.isEmpty()) {
            return new EditorCodeString("", new Integer[0]);
        }
        return this.substring(0, 1);
    }
    
    public EditorCodeString lastChar() {
        if (this.isEmpty()) {
            return new EditorCodeString("", new Integer[0]);
        }
        return this.substring(this.length() -1);
    }

    public int lineNumberAt(int i) {
        return this.lineNumbers[i];
    }

    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    public boolean startsWith(String startString) {
        return this.value.startsWith(startString);
    }

    public boolean endsWith(String startString) {
        return this.value.endsWith(startString);
    }

    public int length() {
        return this.value.length();
    }

    public int indexOf(char c) {
        return this.value.indexOf(c);
    }

    public int indexOf(char c, int i) {
        return this.value.indexOf(c, i);
    }

    public int indexOf(String substring) {
        return this.value.indexOf(substring);
    }

    public int indexOf(String substring, int i) {
        return this.value.indexOf(substring, i);
    }

    public boolean contains(CharSequence substring) {
        return this.value.contains(substring);
    }

    public EditorCodeString replaceAll(String substring, String replacement) {
        EditorCodeString resultString = this;
        int indexOfOccurance = this.value.indexOf(substring);
        while (indexOfOccurance >= 0 && resultString.indexOf(substring, indexOfOccurance) >= 0) {
            resultString = resultString.replaceFirst(substring, replacement, indexOfOccurance);
            indexOfOccurance =  resultString.indexOf(substring, indexOfOccurance + replacement.length());
        }
        return resultString;
    }

    public EditorCodeString replaceFirst(String substring, String replacement) {
        return EditorCodeString.this.replaceFirst(substring, replacement, 0);
    }

    public EditorCodeString replaceFirst(String substring, String replacement, int indexOfFirstOccurance) {
        if (!this.value.contains(substring)) {
            return this;
        }
        int firstIndex = this.value.indexOf(substring, indexOfFirstOccurance);
        String newValue = this.value.substring(0, firstIndex) + replacement + this.value.substring(firstIndex + substring.length());
        Integer[] newLineNumbers = new Integer[newValue.length()];
        int lineNumberOfReplacement;
        if (firstIndex == 0) {
            lineNumberOfReplacement = 0;
        } else {
            lineNumberOfReplacement = this.lineNumbers[firstIndex - 1];
        }
        for (int i = 0; i < firstIndex; i++) {
            newLineNumbers[i] = this.lineNumbers[i];
        }
        for (int i = firstIndex; i < firstIndex + replacement.length(); i++) {
            newLineNumbers[i] = lineNumberOfReplacement;
        }
        for (int i = firstIndex + replacement.length(); i < newValue.length(); i++) {
            newLineNumbers[i] = this.lineNumbers[i - replacement.length() + substring.length()];
        }
        return new EditorCodeString(newValue, newLineNumbers);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.value);
        hash = 47 * hash + Arrays.deepHashCode(this.lineNumbers);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EditorCodeString other = (EditorCodeString) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Arrays.deepEquals(this.lineNumbers, other.lineNumbers)) {
            return false;
        }
        return true;
    }

    public boolean equalsInStringValue(EditorCodeString string) {
        return Objects.equals(this.value, string.value);
    }

    @Override
    public String toString() {
        String stringRepresentation = "(" + this.value + ";[";
        for (int i = 0; i < this.lineNumbers.length; i++) {
            stringRepresentation += this.lineNumbers[i];
            if (i < this.lineNumbers.length - 1) {
                stringRepresentation += ", ";
            }
        }
        return stringRepresentation + "])";
    }

}
