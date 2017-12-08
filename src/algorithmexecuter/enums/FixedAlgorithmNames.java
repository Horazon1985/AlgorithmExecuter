package algorithmexecuter.enums;

import algorithmexecuter.model.Signature;
import java.util.HashMap;
import java.util.Map;

public enum FixedAlgorithmNames {

    MAIN("main"),
    INC("inc"),
    DEC("dec"),
    PRINT("print"),
    ENTRY("entry");

    private static final Map<FixedAlgorithmNames, Signature> signatures = new HashMap<>();

    private final String value;

    static {
        signatures.put(INC, new Signature(IdentifierType.EXPRESSION, INC.value, new IdentifierType[]{IdentifierType.EXPRESSION}));
        signatures.put(DEC, new Signature(IdentifierType.EXPRESSION, DEC.value, new IdentifierType[]{IdentifierType.EXPRESSION}));
        signatures.put(PRINT, new Signature(null, PRINT.value, new IdentifierType[]{IdentifierType.EXPRESSION}));
        signatures.put(PRINT, new Signature(null, PRINT.value, new IdentifierType[]{IdentifierType.BOOLEAN_EXPRESSION}));
        signatures.put(PRINT, new Signature(null, PRINT.value, new IdentifierType[]{IdentifierType.MATRIX_EXPRESSION}));
        signatures.put(PRINT, new Signature(null, PRINT.value, new IdentifierType[]{IdentifierType.STRING}));
        signatures.put(ENTRY, new Signature(IdentifierType.EXPRESSION, ENTRY.value, new IdentifierType[]{IdentifierType.MATRIX_EXPRESSION, IdentifierType.EXPRESSION, IdentifierType.EXPRESSION}));
    }

    FixedAlgorithmNames(String name) {
        this.value = name;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
