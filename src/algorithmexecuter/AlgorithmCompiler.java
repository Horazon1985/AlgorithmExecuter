package algorithmexecuter;

import abstractexpressions.interfaces.IdentifierValidator;
import algorithmexecuter.enums.FixedAlgorithmNames;
import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.model.command.AlgorithmCommand;
import algorithmexecuter.model.command.AssignValueCommand;
import algorithmexecuter.model.command.ControlStructure;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.enums.Keyword;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.Algorithm;
import algorithmexecuter.model.AlgorithmSignatureStorage;
import algorithmexecuter.model.AlgorithmStorage;
import algorithmexecuter.model.Signature;
import algorithmexecuter.model.command.ForControlStructure;
import algorithmexecuter.model.utilclasses.EditorCodeString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AlgorithmCompiler {

    public static final Algorithm[] FIXED_ALGORITHMS;

    public static final IdentifierValidator VALIDATOR = new IdentifierValidatorImpl();

    public static final AlgorithmStorage ALGORITHMS = new AlgorithmStorage();

    protected static final AlgorithmSignatureStorage ALGORITHM_SIGNATURES = new AlgorithmSignatureStorage();

    private static final List<EditorCodeString> ALGORITHM_CODES = new ArrayList<>();

    static {
        // 1. Standardalgorithmen definieren.
        List<Algorithm> fixedAlgorithms = new ArrayList<>();
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.INC.getValue(), new Identifier[]{Identifier.createIdentifier("a", IdentifierType.EXPRESSION)}, null));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.DEC.getValue(), new Identifier[]{Identifier.createIdentifier("a", IdentifierType.EXPRESSION)}, null));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.PRINT.getValue(), new Identifier[]{Identifier.createIdentifier("a", IdentifierType.EXPRESSION)}, null));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.PRINT.getValue(), new Identifier[]{Identifier.createIdentifier("a", IdentifierType.BOOLEAN_EXPRESSION)}, null));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.PRINT.getValue(), new Identifier[]{Identifier.createIdentifier("a", IdentifierType.MATRIX_EXPRESSION)}, null));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.PRINT.getValue(), new Identifier[]{Identifier.createIdentifier("a", IdentifierType.STRING)}, null));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.ENTRY.getValue(),
                new Identifier[]{Identifier.createIdentifier("a", IdentifierType.MATRIX_EXPRESSION),
                    Identifier.createIdentifier("i", IdentifierType.EXPRESSION),
                    Identifier.createIdentifier("j", IdentifierType.EXPRESSION)
                }, IdentifierType.EXPRESSION));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.APPROX.getValue(),
                new Identifier[]{Identifier.createIdentifier("a", IdentifierType.EXPRESSION)}, IdentifierType.EXPRESSION));
        fixedAlgorithms.add(new Algorithm(FixedAlgorithmNames.APPROX.getValue(),
                new Identifier[]{Identifier.createIdentifier("a", IdentifierType.MATRIX_EXPRESSION)}, IdentifierType.MATRIX_EXPRESSION));
        FIXED_ALGORITHMS = fixedAlgorithms.toArray(new Algorithm[fixedAlgorithms.size()]);

        // 2. Standardalgorithmen zu den "bekannten" Algorithmen hinzufügen
        ALGORITHMS.addAll(fixedAlgorithms);

        // 3. Signaturen von Standardalgorithmen zu den "bekannten" Signaturen hinzufügen.
        fixedAlgorithms.forEach((alg) -> {
            ALGORITHM_SIGNATURES.add(alg.getSignature());
        });
    }

    private static void initStorages() {
        ALGORITHMS.clearAlgorithmStorage();
        for (Algorithm alg : FIXED_ALGORITHMS) {
            ALGORITHMS.add(alg);
        }
        ALGORITHM_SIGNATURES.clearAlgorithmSignatureStorage();
        for (Algorithm alg : FIXED_ALGORITHMS) {
            ALGORITHM_SIGNATURES.add(alg.getSignature());
        }
        ALGORITHM_CODES.clear();
    }

    private static void removeStandardAlgorithmsFromStorage() {
        for (Algorithm alg : FIXED_ALGORITHMS) {
            ALGORITHMS.remove(alg);
            ALGORITHM_SIGNATURES.remove(alg.getSignature());
        }
    }

    private static void parseAlgorithmSignatures(EditorCodeString inputAlgorithmFile) throws AlgorithmCompileException {
        if (inputAlgorithmFile.isEmpty()) {
            return;
        }

        int bracketCounter = 0;
        boolean beginPassed = false;
        int lastEndOfAlgorithm = -1;

        for (int i = 0; i < inputAlgorithmFile.length(); i++) {
            if (inputAlgorithmFile.charAt(i) == ReservedChars.BEGIN.getValue()) {
                bracketCounter++;
                beginPassed = true;
            } else if (inputAlgorithmFile.charAt(i) == ReservedChars.END.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0 && beginPassed) {
                ALGORITHM_SIGNATURES.add(parseAlgorithmSignature(inputAlgorithmFile.substring(lastEndOfAlgorithm + 1, i + 1)));
                beginPassed = false;
                lastEndOfAlgorithm = i;
            }
            if (bracketCounter < 0) {
                throw new AlgorithmCompileException(inputAlgorithmFile.substring(i, i + 1).getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.BEGIN.getValue());
            }
        }

        if (bracketCounter > 0) {
            throw new AlgorithmCompileException(inputAlgorithmFile.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }

        // Prüfung, ob die Signatur des Main-Algorithmus existiert.
        CompilerUtils.checkIfMainAlgorithmSignatureExists(inputAlgorithmFile, ALGORITHM_SIGNATURES);
        // Prüfung, ob die Signatur ein Main-Algorithmus parameterlos ist.
        CompilerUtils.checkIfMainAlgorithmSignatureContainsNoParameters(CompilerUtils.getMainAlgorithmSignature(ALGORITHM_SIGNATURES));

    }

    private static Signature parseAlgorithmSignature(EditorCodeString input) throws AlgorithmCompileException {

        int indexBeginParameters = input.indexOf(ReservedChars.OPEN_BRACKET.getValue());
        if (indexBeginParameters < 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_FILE_MUST_CONTAIN_A_BEGIN);
        }
        if (input.indexOf(ReservedChars.CLOSE_BRACKET.getValue()) < 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_FILE_MUST_CONTAIN_AN_END);
        }
        if (indexBeginParameters > input.indexOf(ReservedChars.CLOSE_BRACKET.getValue())) {
            throw new AlgorithmCompileException(input.lineNumberAt(input.indexOf(ReservedChars.CLOSE_BRACKET.getValue())), AlgorithmCompileExceptionIds.AC_END_BEFORE_BEGIN);
        }

        // Rückgabewert ermitteln (führende Leerzeichen existieren nicht).
        IdentifierType returnType = CompilerUtils.getReturnTypeFromAlgorithmDeclaration(input.getValue());
        // Signatur ermitteln.
        if (returnType != null) {
            input = input.substring(returnType.toString().length());
        }
        EditorCodeString candidateForSignature = input.substring(0, input.indexOf(ReservedChars.BEGIN.getValue()));
        CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(candidateForSignature);
        EditorCodeString algName = algParseData.getName();

        // Prüfung, ob der Algorithmenname gültig ist.
        CompilerUtils.checkIfAlgorithmNameIsValid(algName);

        EditorCodeString[] parametersAsStrings = algParseData.getParameters();

        Identifier[] parameters = getIdentifiersFromParameterStrings(parametersAsStrings, new AlgorithmMemory(null));

        // Prüfung, ob Algorithmusparameter nicht doppelt vorkommen.
        checkForTwiceOccurringParameters(parameters, candidateForSignature);

        Signature signature = CompilerUtils.getSignature(returnType, algName.getValue(), parameters);

        // Falls ein Algorithmus mit derselben Signatur bereits vorhanden ist, Fehler werfen.
        if (containsAlgorithmWithSameSignature(signature)) {
            throw new AlgorithmCompileException(algName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_ALGORITHM_ALREADY_EXISTS, signature);
        }

        return signature;

    }

    public static void parseAlgorithmFile(String inputAlgorithmFile) throws AlgorithmCompileException {
        initStorages();

        EditorCodeString editorCodeInput = new EditorCodeString(inputAlgorithmFile);

        if (editorCodeInput.isEmpty()) {
            return;
        }

        // Vorformatierung.
        editorCodeInput = CompilerUtils.preprocessAlgorithm(editorCodeInput);

        /* 
        Sämtliche Signaturen ermitteln, damit alle vorhandenen Algorithmennamen 
        bekannt sind, auch wenn diese Compilerfehler enthalten.
         */
        parseAlgorithmSignatures(editorCodeInput);

        int bracketCounter = 0;
        boolean beginPassed = false;
        int lastEndOfAlgorithm = -1;

        EditorCodeString singleAlgorithmCode;
        for (int i = 0; i < editorCodeInput.length(); i++) {
            if (editorCodeInput.charAt(i) == ReservedChars.BEGIN.getValue()) {
                bracketCounter++;
                beginPassed = true;
            } else if (editorCodeInput.charAt(i) == ReservedChars.END.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0 && beginPassed || i == editorCodeInput.length() - 1) {
                singleAlgorithmCode = editorCodeInput.substring(lastEndOfAlgorithm + 1, i + 1);
                ALGORITHMS.add(parseAlgorithm(singleAlgorithmCode));
                ALGORITHM_CODES.add(singleAlgorithmCode);
                beginPassed = false;
                lastEndOfAlgorithm = i;
            }
            if (bracketCounter < 0) {
                throw new AlgorithmCompileException(editorCodeInput.substring(i, i + 1).getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.BEGIN.getValue());
            }
        }

        if (bracketCounter > 0) {
            throw new AlgorithmCompileException(editorCodeInput.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }

        // Prüfung, ob ein Main-Algorithmus existiert.
        checkIfMainAlgorithmExists(editorCodeInput);
        // Prüfung, ob ein Main-Algorithmus parameterlos ist.
        checkIfMainAlgorithmContainsNoParameters(CompilerUtils.getMainAlgorithm(ALGORITHMS));
        // Bei Bezeichnerzuordnungen Algorithmensignaturen durch Algorithmenreferenzen ersetzen.
        replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands();

        // Zum Schluss: Standardalgorithmen wieder aus dem Storage entfernen.
        removeStandardAlgorithmsFromStorage();
    }

    private static Algorithm parseAlgorithm(EditorCodeString input) throws AlgorithmCompileException {

        int indexBeginParameters = input.indexOf(ReservedChars.OPEN_BRACKET.getValue());
        if (indexBeginParameters < 0) {
            throw new AlgorithmCompileException(input.firstChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_FILE_MUST_CONTAIN_A_BEGIN);
        }
        if (input.indexOf(ReservedChars.CLOSE_BRACKET.getValue()) < 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_FILE_MUST_CONTAIN_AN_END);
        }
        if (indexBeginParameters > input.indexOf(ReservedChars.CLOSE_BRACKET.getValue())) {
            int indexOfCloseBracket = input.indexOf(ReservedChars.CLOSE_BRACKET.getValue());
            throw new AlgorithmCompileException(input.substring(indexOfCloseBracket, indexOfCloseBracket + 1).getLineNumbers(), AlgorithmCompileExceptionIds.AC_END_BEFORE_BEGIN);
        }

        // Rückgabewert ermitteln (führende Leerzeichen existieren nicht).
        IdentifierType returnType = CompilerUtils.getReturnTypeFromAlgorithmDeclaration(input.getValue());
        // Signatur ermitteln.
        if (returnType != null) {
            input = input.substring(returnType.toString().length());
        }
        EditorCodeString candidateForSignature = input.substring(0, input.indexOf(ReservedChars.BEGIN.getValue()));
        CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(candidateForSignature);
        EditorCodeString algName = algParseData.getName();
        EditorCodeString[] parametersAsStrings = algParseData.getParameters();

        AlgorithmMemory memory = new AlgorithmMemory(null);

        Identifier[] parameters = getIdentifiersFromParameterStrings(parametersAsStrings, memory);

        Signature signature = CompilerUtils.getSignature(returnType, algName.getValue(), parameters);

        // Falls ein Algorithmus mit derselben Signatur bereits vorhanden ist, Fehler werfen.
        if (containsAlgorithmWithSameSignature(signature)) {
            throw new AlgorithmCompileException(algName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_ALGORITHM_ALREADY_EXISTS, signature);
        }

        // Algorithmusparameter zum Variablenpool hinzufügen.
        addParametersToMemoryInCompileTime(candidateForSignature, parameters, memory);

        Algorithm alg = new Algorithm(algName.getValue(), parameters, returnType);
        memory.setAlgorithm(alg);

        int indexEndParameters = input.indexOf(ReservedChars.CLOSE_BRACKET.getValue());

        /* 
        Algorithmusnamen und Parameter inkl. Klammern beseitigen. Es bleibt nur 
        noch ein String der Form "{...;...;...}" übrig.
         */
        input = input.substring(indexEndParameters + 1, input.length());
        // input muss mit "{" beginnen und auf "}" enden.
        if (!input.startsWith(String.valueOf(ReservedChars.BEGIN.getValue()))) {
            throw new AlgorithmCompileException(input.firstChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_ALGORITHM_MUST_START_WITH_BEGIN);
        }
        if (!input.endsWith(String.valueOf(ReservedChars.END.getValue()))) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_ALGORITHM_MUST_END_WITH_END);
        }
        // Öffnende {-Klammer und schließende }-Klammer am Anfang und am Ende beseitigen.
        input = input.substring(1, input.length() - 1);

        input = putSeparatorAfterBlockEnding(input);

        if (!input.isEmpty()) {
            // Alle Zeilen innerhalb des Algorithmus kompilieren.
            List<AlgorithmCommand> commands = AlgorithmCommandCompiler.parseConnectedBlockWithoutKeywords(input, memory, alg);
            // Allen Befehlen den aktuellen Algorithmus alg zuordnen.
            alg.appendCommands(commands);
        }

        // Plausibilitätschecks.
        checkAlgorithmForPlausibility(alg);

        return alg;
    }

    private static void replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands() {
        for (Algorithm alg : ALGORITHMS.getAlgorithmStorage()) {
            replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands(alg.getCommands());
        }
    }

    private static void checkForTwiceOccurringParameters(Identifier[] parameter, EditorCodeString candidateForSignature) throws AlgorithmCompileException {
        for (int i = 0; i < parameter.length; i++) {
            for (int j = i + 1; j < parameter.length; j++) {
                if (parameter[i].getName().equals(parameter[j].getName())) {
                    throw new AlgorithmCompileException(candidateForSignature.getLineNumbers(), AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, parameter[i].getName());
                }
            }
        }
    }

    private static Identifier[] getIdentifiersFromParameterStrings(EditorCodeString[] parameterStrings, AlgorithmMemory memory) throws AlgorithmCompileException {
        Identifier[] resultIdentifiers = new Identifier[parameterStrings.length];
        IdentifierType parameterType;
        EditorCodeString parameterName;
        for (int i = 0; i < parameterStrings.length; i++) {

            parameterType = null;
            for (IdentifierType type : IdentifierType.values()) {
                if (parameterStrings[i].startsWith(type.toString() + " ")) {
                    parameterType = type;
                    break;
                }
            }
            if (parameterType == null) {
                throw new AlgorithmCompileException(parameterStrings[i].getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, parameterStrings[i]);
            }
            parameterName = parameterStrings[i].substring((parameterType.toString() + " ").length());

            // Validierung des Parameternamen.
            if (!VALIDATOR.isValidIdentifier(parameterName.getValue())) {
                throw new AlgorithmCompileException(parameterStrings[i].getLineNumbers(), AlgorithmCompileExceptionIds.AC_ILLEGAL_CHARACTER, parameterName);
            }
            // Prüfung auf doppelte Deklaration.
            if (memory.containsIdentifier(parameterName.getValue())) {
                throw new AlgorithmCompileException(parameterStrings[i].getLineNumbers(), AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, parameterName);
            }
            resultIdentifiers[i] = Identifier.createIdentifier(parameterName.getValue(), parameterType);

        }
        return resultIdentifiers;
    }

    private static void addParametersToMemoryInCompileTime(EditorCodeString candidateForSignature, Identifier[] parameters, AlgorithmMemory memory) throws AlgorithmCompileException {
        for (Identifier parameter : parameters) {
            memory.addToMemoryInCompileTime(candidateForSignature.getLineNumbers(), parameter);
        }
    }

    private static boolean containsAlgorithmWithSameSignature(Signature signature) {
        Signature algSignature;
        for (Algorithm alg : ALGORITHMS.getAlgorithmStorage()) {
            algSignature = CompilerUtils.getSignature(alg.getReturnType(), alg.getName(), alg.getInputParameters());
            if (algSignature.getName().equals(signature.getName())
                    && Arrays.deepEquals(algSignature.getParameterTypes(), signature.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    private static EditorCodeString putSeparatorAfterBlockEnding(EditorCodeString input) {
        EditorCodeString inputWithSeparators = input.replaceAll("}", "};");
        // Ausnahme: if (...) {...} else {...}: Semikolon zwischen dem if- und dem else-Block entfernen.
        inputWithSeparators = inputWithSeparators.replaceAll("}" + String.valueOf(ReservedChars.LINE_SEPARATOR.getValue()) + Keyword.ELSE.getValue(), "}" + Keyword.ELSE.getValue());
        // Ausnahme: do {...} while (...): Semikolon zwischen dem do-Block und dem while entfernen.
        inputWithSeparators = inputWithSeparators.replaceAll("}" + String.valueOf(ReservedChars.LINE_SEPARATOR.getValue()) + Keyword.WHILE.getValue(), "}" + Keyword.WHILE.getValue());
        return inputWithSeparators;
    }

    private static void checkAlgorithmForPlausibility(Algorithm alg) throws AlgorithmCompileException {
        // Prüfung, ob es bei Void-Algorithmen keine zurückgegebenen Objekte gibt.
//        checkIfVoidAlgorithmContainsOnlyAtMostSimpleReturns(alg);
        // Prüfung, ob es bei Algorithmen mit Rückgabewerten immer Rückgaben mit korrektem Typ gibt.
        checkIfNonVoidAlgorithmContainsAlwaysReturnsWithCorrectReturnType(alg);
        // Prüfung, ob alle eingeführten Bezeichner auch initialisiert wurden.
        checkIfAllIdentifierAreInitialized(alg);
    }

    private static void checkIfMainAlgorithmExists(EditorCodeString editorCodeInput) throws AlgorithmCompileException {
        CompilerUtils.checkIfMainAlgorithmExists(editorCodeInput, ALGORITHMS);
    }

    private static void checkIfMainAlgorithmContainsNoParameters(Algorithm alg) throws AlgorithmCompileException {
        CompilerUtils.checkIfMainAlgorithmContainsNoParameters(alg);
    }

//    private static void checkIfVoidAlgorithmContainsOnlyAtMostSimpleReturns(Algorithm alg) throws AlgorithmCompileException {
//        if (alg.getReturnType() == null) {
//            CompilerUtils.checkForOnlySimpleReturns(alg.getCommands());
//        }
//    }

    private static void checkIfNonVoidAlgorithmContainsAlwaysReturnsWithCorrectReturnType(Algorithm alg) throws AlgorithmCompileException {
        // Prüfung, ob Wertrückgabe immer erfolgt.
        CompilerUtils.checkForContainingReturnCommand(alg.getCommands(), alg.getReturnType());
        // Prüfung auf korrekten Rückgabewert.
        CompilerUtils.checkForCorrectReturnType(alg.getCommands(), alg.getReturnType());
    }

    private static void checkIfAllIdentifierAreInitialized(Algorithm alg) throws AlgorithmCompileException {
        CompilerUtils.checkIfAllUsedIdentifiersAreInitialized(alg.getCommands(), alg);
    }

    private static void replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands(List<AlgorithmCommand> commands) {
        AssignValueCommand assignValueCommand;
        for (AlgorithmCommand command : commands) {
            if (command.isAssignValueCommand() && ((AssignValueCommand) command).getTargetAlgorithmSignature() != null) {
                assignValueCommand = (AssignValueCommand) command;
                Signature signature = assignValueCommand.getTargetAlgorithmSignature();
                Algorithm calledAlg = null;
                for (Algorithm alg : ALGORITHMS.getAlgorithmStorage()) {
                    if (alg.getSignature().equals(signature)) {
                        calledAlg = alg;
                        break;
                    }
                }
                if (calledAlg == null) {
                    for (Algorithm alg : FIXED_ALGORITHMS) {
                        if (alg.getSignature().equals(signature)) {
                            calledAlg = alg;
                            break;
                        }
                    }
                }
                // Ab hier ist calledAlg != null (dies wurde durch andere, vorherige Prüfungen sichergestellt).
                assignValueCommand.setTargetAlgorithm(calledAlg);
            } else if (command.isControlStructure()) {
                // Analoges bei allen Unterblöcken durchführen.
                for (List<AlgorithmCommand> commandBlock : ((ControlStructure) command).getCommandBlocks()) {
                    replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands(commandBlock);
                }
                if (command.isForControlStructure()) {
                    replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands(((ForControlStructure) command).getInitialization());
                    replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands(((ForControlStructure) command).getEndLoopCommands());
                    replaceAlgorithmSignaturesByAlgorithmReferencesInAssignValueCommands(((ForControlStructure) command).getLoopAssignment());
                }
            }
        }
    }

}
