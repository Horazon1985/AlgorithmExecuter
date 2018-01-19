package algorithmexecuter;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.interfaces.IdentifierValidator;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.TypeMatrixFunction;
import abstractexpressions.matrixexpression.classes.TypeMatrixOperator;
import algorithmexecuter.booleanexpression.BooleanExpression;
import algorithmexecuter.enums.FixedAlgorithmNames;
import algorithmexecuter.model.command.AlgorithmCommand;
import algorithmexecuter.model.command.ControlStructure;
import algorithmexecuter.model.command.IfElseControlStructure;
import algorithmexecuter.model.command.ReturnCommand;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.enums.Operators;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.exceptions.BooleanExpressionException;
import algorithmexecuter.exceptions.ParseAssignValueException;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.Algorithm;
import algorithmexecuter.model.AlgorithmSignatureStorage;
import algorithmexecuter.model.AlgorithmStorage;
import algorithmexecuter.model.Signature;
import algorithmexecuter.model.command.AssignValueCommand;
import algorithmexecuter.model.command.DeclareIdentifierCommand;
import algorithmexecuter.model.utilclasses.EditorCodeString;
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.model.utilclasses.malstring.MalStringVariable;
import algorithmexecuter.model.utilclasses.ParameterData;
import algorithmexecuter.model.utilclasses.malstring.MalStringAbstractExpression;
import algorithmexecuter.model.utilclasses.malstring.MalStringCharSequence;
import algorithmexecuter.model.utilclasses.malstring.MalStringSummand;
import exceptions.ExpressionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompilerUtils {

    private static final String SIGN_TAB = "\t";
    private static final String SIGN_NEXT_LINE = "\n";

    public static final String GEN_VAR = "#";

    private CompilerUtils() {
    }

    /**
     * Transportklasse für Algorithmensignaturen. name gibt den Namen des
     * Algorithmus an und parameters die konkreten Parameterwerte.
     */
    public static class AlgorithmParseData {

        EditorCodeString name;
        EditorCodeString[] parameters;

        public EditorCodeString getName() {
            return name;
        }

        public EditorCodeString[] getParameters() {
            return parameters;
        }

        public AlgorithmParseData(EditorCodeString name, EditorCodeString[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }

    }

    /**
     * Gibt den Parameter input vorformatiert zurück, damit er für einen
     * Kompilierungsprozess geeignet ist.
     */
    public static EditorCodeString preprocessAlgorithm(EditorCodeString input) {
        EditorCodeString outputFormatted = input;
        outputFormatted = removeLeadingWhitespaces(outputFormatted);
        outputFormatted = removeEndingWhitespaces(outputFormatted);
        outputFormatted = replaceAllRepeatedly(outputFormatted, " ", SIGN_TAB, SIGN_NEXT_LINE);
        outputFormatted = replaceAllRepeatedly(outputFormatted, " ", "  ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, ",", ", ", " ,");
        outputFormatted = replaceAllRepeatedly(outputFormatted, ";", "; ", " ;");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "=", " =", "= ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "{", " {", "{ ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "}", " }", "} ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "(", " (", "( ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, ")", " )", ") ");
        return outputFormatted;
    }

    /**
     * Gibt den Parameter input vorformatiert zurück, damit er für einen
     * Kompilierungsprozess geeignet ist.
     */
    public static String preprocessAlgorithm(String input) {
        String outputFormatted = input;
        outputFormatted = removeLeadingWhitespaces(outputFormatted);
        outputFormatted = removeEndingWhitespaces(outputFormatted);
        outputFormatted = replaceAllRepeatedly(outputFormatted, " ", SIGN_TAB, SIGN_NEXT_LINE);
        outputFormatted = replaceAllRepeatedly(outputFormatted, " ", "  ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, ",", ", ", " ,");
        outputFormatted = replaceAllRepeatedly(outputFormatted, ";", "; ", " ;");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "=", " =", "= ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "\\{", " \\{", "\\{ ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "\\}", " \\}", "\\} ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "\\(", " \\(", "\\( ");
        outputFormatted = replaceAllRepeatedly(outputFormatted, "\\)", " \\)", "\\) ");
        return outputFormatted;
    }

    private static EditorCodeString removeLeadingWhitespaces(EditorCodeString input) {
        while (input.startsWith(" ")) {
            input = input.substring(1);
        }
        while (input.endsWith(" ")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    private static EditorCodeString removeEndingWhitespaces(EditorCodeString input) {
        while (input.endsWith(" ")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    private static EditorCodeString replaceAllRepeatedly(EditorCodeString input, String replaceBy, String... toReplace) {
        EditorCodeString result = input;
        for (String s : toReplace) {
            result = replaceRepeatedly(result, s, replaceBy);
        }
        return result;
    }

    private static EditorCodeString replaceRepeatedly(EditorCodeString input, String toReplace, String replaceBy) {
        EditorCodeString result = input;
        do {
            input = result;
            result = result.replaceAll(toReplace, replaceBy);
        } while (!result.equals(input));
        return result;
    }

    private static String removeLeadingWhitespaces(String input) {
        while (input.startsWith(" ")) {
            input = input.substring(1);
        }
        while (input.endsWith(" ")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    private static String removeEndingWhitespaces(String input) {
        while (input.endsWith(" ")) {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }

    private static String replaceAllRepeatedly(String input, String replaceBy, String... toReplace) {
        String result = input;
        for (String s : toReplace) {
            result = replaceRepeatedly(result, s, replaceBy);
        }
        return result;
    }

    private static String replaceRepeatedly(String input, String toReplace, String replaceBy) {
        String result = input;
        do {
            input = result;
            result = result.replaceAll(toReplace, replaceBy);
        } while (!result.equals(input));
        return result;
    }

    public static Integer[] getErrorLines(EditorCodeString lines) {
        if (lines.getValue().isEmpty()) {
            return new Integer[0];
        }
        int numberOfLines = lines.getLineNumbers()[lines.getLineNumbers().length - 1] - lines.getLineNumbers()[0] + 1;
        Integer[] errorLines = new Integer[numberOfLines];
        for (int i = 0; i < numberOfLines; i++) {
            errorLines[i] = lines.getLineNumbers()[0] + 1;
        }
        return errorLines;
    }

    /**
     * Gibt die Signatur eines Algorithmus zurück, welche durch die
     * Eingabeparameter vollständig festgelegt ist.
     */
    public static Signature getSignature(IdentifierType returnType, String algName, Identifier[] parameters) {
        IdentifierType[] types = new IdentifierType[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getType();
        }
        return new Signature(returnType, algName, types);
    }

    /**
     * Gibt ein AlgorithmParseData-Objekt zurück, falls input einen (formalen)
     * Algorithmusaufruf darstellt, also einen String der Form
     * 'algorithmusname(param1, ..., paramN)'.
     */
    public static AlgorithmParseData getAlgorithmParseData(EditorCodeString input) throws AlgorithmCompileException {
        EditorCodeString[] algNameAndParams = CompilerUtils.getAlgorithmNameAndParameters(input);
        EditorCodeString algName = algNameAndParams[0];
        EditorCodeString[] params = CompilerUtils.getParameters(algNameAndParams[1]);
        return new AlgorithmParseData(algName, params);
    }

    /**
     * Der Algorithmusname und die Parameter in der Befehlsklammer werden
     * ausgelesen und zurückgegeben.<br>
     * BEISPIEL: input = alg(expression x, expression y). Zurückgegeben wird ein
     * array der Länge zwei: im 0. Eintrag steht der String "alg", im 1. der
     * String "expression x, expression y".
     *
     * @throws AlgorithmCompileException
     */
    private static EditorCodeString[] getAlgorithmNameAndParameters(EditorCodeString input) throws AlgorithmCompileException {

        // Leerzeichen beseitigen
        input = CompilerUtils.removeLeadingWhitespaces(input);

        EditorCodeString[] algNameAndParameters = new EditorCodeString[2];
        int i = input.indexOf(ReservedChars.OPEN_BRACKET.getValue());
        if (i == -1) {
            // Um zu verhindern, dass es eine IndexOutOfBoundsException gibt.
            i = 0;
        }
        algNameAndParameters[0] = input.substring(0, i);

        // Wenn der Algorithmusname leer ist -> Fehler.
        if (algNameAndParameters[0].length() == 0) {
            throw new AlgorithmCompileException(input.firstChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_ALGORITHM_HAS_NO_NAME);
        }

        // Wenn algNameAndParameters[0].length() > input.length() - 2 -> Fehler (Algorithmenaufruf besitzt NICHT die Form 'algName(...)', insbesondere fehlt ")" am Ende).
        if (algNameAndParameters[0].length() > input.length() - 2) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        if (input.charAt(input.length() - 1) != ReservedChars.CLOSE_BRACKET.getValue()) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        algNameAndParameters[1] = input.substring(algNameAndParameters[0].length() + 1, input.length() - 1);

        return algNameAndParameters;

    }

    /**
     * Input: String input, in der NUR die Parameter (getrennt durch ein Komma)
     * stehen. Beispiel input = "expression x, expression y". Parameter sind
     * dann {expression x, expression y}. Nach einem eingelesenen Komma, welches
     * NICHT von runden Klammern umgeben ist, werden die Parameter getrennt.
     *
     * @throws AlgorithmCompileException
     */
    private static EditorCodeString[] getParameters(EditorCodeString input) throws AlgorithmCompileException {

        // Falls Parameterstring leer ist -> Fertig
        if (input.isEmpty()) {
            return new EditorCodeString[0];
        }

        ArrayList<EditorCodeString> resultParameters = new ArrayList<>();
        int startPositionOfCurrentParameter = 0;

        /*
         Differenz zwischen der Anzahl der öffnenden und der der schließenden
         Klammern (bracketCounter == 0 am Ende -> alles ok).
         */
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        char currentChar;
        // Jetzt werden die einzelnen Parameter ausgelesen
        for (int i = 0; i < input.length(); i++) {

            currentChar = input.charAt(i);
            if (currentChar == ReservedChars.OPEN_BRACKET.getValue()) {
                bracketCounter++;
            } else if (currentChar == ReservedChars.CLOSE_BRACKET.getValue()) {
                bracketCounter--;
            } else if (currentChar == ReservedChars.OPEN_SQUARE_BRACKET.getValue()) {
                squareBracketCounter++;
            } else if (currentChar == ReservedChars.CLOSE_SQUARE_BRACKET.getValue()) {
                squareBracketCounter--;
            }
            if (bracketCounter == 0 && squareBracketCounter == 0 && currentChar == ReservedChars.ARGUMENT_SEPARATOR.getValue()) {
                if (input.substring(startPositionOfCurrentParameter, i).isEmpty()) {
                    throw new AlgorithmCompileException(input.lineNumberAt(i), AlgorithmCompileExceptionIds.AC_IDENTIFIER_EXPECTED);
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == input.length() - 1) {
                if (startPositionOfCurrentParameter == input.length()) {
                    throw new AlgorithmCompileException(input.lineNumberAt(i), AlgorithmCompileExceptionIds.AC_IDENTIFIER_EXPECTED);
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, input.length()));
            }

            if (bracketCounter < 0) {
                throw new AlgorithmCompileException(input.lineNumberAt(i), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.OPEN_BRACKET.getValue());
            }
            if (squareBracketCounter < 0) {
                throw new AlgorithmCompileException(input.lineNumberAt(i), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.OPEN_SQUARE_BRACKET.getValue());
            }
        }

        if (bracketCounter > 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }
        if (squareBracketCounter > 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_SQUARE_BRACKET.getValue());
        }

        EditorCodeString[] resultParametersAsArray = new EditorCodeString[resultParameters.size()];
        for (int i = 0; i < resultParameters.size(); i++) {
            resultParametersAsArray[i] = resultParameters.get(i);
        }

        return resultParametersAsArray;

    }

    /**
     * Falls input einen Algorithmus als String darstellt, so wird der
     * Rückgabetyp zurückgegeben, falls dieser gültig ist. Ansonsten wird null
     * zurückgegeben.
     */
    public static IdentifierType getReturnTypeFromAlgorithmDeclaration(String input) {
        IdentifierType returnType = null;
        for (IdentifierType type : IdentifierType.values()) {
            if (input.startsWith(type.toString())) {
                returnType = type;
                break;
            }
        }
        return returnType;
    }

    /**
     * Prüft, ob algName ein gültiger Algorithmusname ist.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfAlgorithmNameIsValid(EditorCodeString algName) throws AlgorithmCompileException {
        for (TypeFunction type : TypeFunction.values()) {
            if (algName.getValue().equals(type.name())) {
                throw new AlgorithmCompileException(algName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INVALID_ALGORITHM_NAME, algName.getValue());
            }
        }
        for (TypeMatrixFunction type : TypeMatrixFunction.values()) {
            if (algName.getValue().equals(type.name())) {
                throw new AlgorithmCompileException(algName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INVALID_ALGORITHM_NAME, algName.getValue());
            }
        }
        for (TypeOperator type : TypeOperator.values()) {
            if (algName.getValue().equals(type.name())) {
                throw new AlgorithmCompileException(algName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INVALID_ALGORITHM_NAME, algName.getValue());
            }
        }
        for (TypeMatrixOperator type : TypeMatrixOperator.values()) {
            if (algName.getValue().equals(type.name())) {
                throw new AlgorithmCompileException(algName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INVALID_ALGORITHM_NAME, algName.getValue());
            }
        }
        int asciiValue;
        for (int i = 0; i < algName.length(); i++) {
            asciiValue = (int) algName.charAt(i);
            if (!isNumber(asciiValue) && !isSmallLetter(asciiValue) && !isCapitalLetter(asciiValue) && !isUnderscore(asciiValue)) {
                throw new AlgorithmCompileException(algName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INVALID_ALGORITHM_NAME, algName.getValue());
            }
        }
    }

    private static boolean isNumber(int asciiValue) {
        return asciiValue >= 48 && asciiValue <= 57;
    }

    private static boolean isSmallLetter(int asciiValue) {
        return asciiValue >= 97 && asciiValue <= 122;
    }

    private static boolean isCapitalLetter(int asciiValue) {
        return asciiValue >= 65 && asciiValue <= 90;
    }

    private static boolean isUnderscore(int asciiValue) {
        return asciiValue == 95;
    }

    /**
     * Prüft, ob die Signaturen in signatures eine Signatur des Hauptalgorithmus
     * enthalten.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfMainAlgorithmSignatureExists(EditorCodeString inputAlgorithmFile, AlgorithmSignatureStorage signatures) throws AlgorithmCompileException {
        for (Signature sgn : signatures.getAlgorithmSignatureStorage()) {
            if (sgn.getName().equals(FixedAlgorithmNames.MAIN.getValue())) {
                return;
            }
        }
        throw new AlgorithmCompileException(inputAlgorithmFile.getLineNumbers(), AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_DOES_NOT_EXIST);
    }

    /**
     * Prüft, ob algorithms den Hauptalgorithmus enthalten.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfMainAlgorithmExists(EditorCodeString inputAlgorithmFile, AlgorithmStorage algorithms) throws AlgorithmCompileException {
        for (Algorithm alg : algorithms.getAlgorithmStorage()) {
            if (alg.getName().equals(FixedAlgorithmNames.MAIN.getValue())) {
                return;
            }
        }
        throw new AlgorithmCompileException(inputAlgorithmFile.getLineNumbers(), AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_DOES_NOT_EXIST);
    }

    /**
     * Prüft, ob alle Bezeichner mit Namen im Set vars auch deklariert wurden.
     *
     * @throws ParseAssignValueException
     */
    public static void checkIfAllIdentifiersAreDefined(EditorCodeString paramToCheck, Set<String> vars, AlgorithmMemory memory) throws ParseAssignValueException {
        for (String var : vars) {
            if (!memory.containsKey(var)) {
                throw new ParseAssignValueException(paramToCheck.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, var);
            }
        }
    }

    public static Signature getMainAlgorithmSignature(AlgorithmSignatureStorage signatures) throws AlgorithmCompileException {
        for (Signature sgn : signatures.getAlgorithmSignatureStorage()) {
            if (sgn.getName().equals(FixedAlgorithmNames.MAIN.getValue())) {
                return sgn;
            }
        }
        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_DOES_NOT_EXIST);
    }

    public static Algorithm getMainAlgorithm(AlgorithmStorage algorithms) throws AlgorithmCompileException {
        for (Algorithm alg : algorithms.getAlgorithmStorage()) {
            if (alg.getName().equals(FixedAlgorithmNames.MAIN.getValue())) {
                return alg;
            }
        }
        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_DOES_NOT_EXIST);
    }

    /**
     * Teilt den String input gemäß dem Trennungszeichen ',' auf, sofern dieses
     * außerhalb jeglicher Klammern vorkommt. Es wird ein Fehler geworfen, wenn
     * eine öffnende Klammer keine entsprechende schließende Klammer besitzt,
     * oder umgekehrt.
     *
     * @throws AlgorithmCompileException
     */
    public static EditorCodeString[] splitByKomma(EditorCodeString input) throws AlgorithmCompileException {
        List<EditorCodeString> linesAsList = new ArrayList<>();
        int wavedBracketCounter = 0;
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        int beginBlockPosition = 0;
        int endBlockPosition;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ReservedChars.BEGIN.getValue()) {
                wavedBracketCounter++;
            } else if (input.charAt(i) == ReservedChars.END.getValue()) {
                wavedBracketCounter--;
            } else if (input.charAt(i) == ReservedChars.OPEN_BRACKET.getValue()) {
                bracketCounter++;
            } else if (input.charAt(i) == ReservedChars.CLOSE_BRACKET.getValue()) {
                bracketCounter--;
            } else if (input.charAt(i) == ReservedChars.OPEN_SQUARE_BRACKET.getValue()) {
                squareBracketCounter++;
            } else if (input.charAt(i) == ReservedChars.CLOSE_SQUARE_BRACKET.getValue()) {
                squareBracketCounter--;
            }
            if (wavedBracketCounter == 0 && squareBracketCounter == 0 && (input.charAt(i) == ReservedChars.ARGUMENT_SEPARATOR.getValue() || i == input.length() - 1)) {
                if (input.charAt(i) == ReservedChars.ARGUMENT_SEPARATOR.getValue()) {
                    endBlockPosition = i;
                    linesAsList.add(input.substring(beginBlockPosition, endBlockPosition));
                    beginBlockPosition = i + 1;
                } else {
                    linesAsList.add(input.substring(beginBlockPosition, input.length()));
                }
            }
        }
        if (wavedBracketCounter > 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }
        if (bracketCounter > 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }
        if (squareBracketCounter > 0) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_SQUARE_BRACKET.getValue());
        }

        return linesAsList.toArray(new EditorCodeString[linesAsList.size()]);
    }

    /**
     * Prüft, ob die Signatur des Hauptalgorithmus keinen Parameter enthält.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfMainAlgorithmSignatureContainsNoParameters(Signature mainAlgSignature) throws AlgorithmCompileException {
        if (mainAlgSignature.getName().equals(FixedAlgorithmNames.MAIN.getValue()) && mainAlgSignature.getParameterTypes().length != 0) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_NOT_ALLOWED_TO_CONTAIN_PARAMETERS);
        }
    }

    /**
     * Prüft, ob der Hauptalgorithmus keinen Parameter enthält.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfMainAlgorithmContainsNoParameters(Algorithm mainAlg) throws AlgorithmCompileException {
        if (mainAlg.getName().equals(FixedAlgorithmNames.MAIN.getValue()) && mainAlg.getInputParameters().length != 0) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_NOT_ALLOWED_TO_CONTAIN_PARAMETERS);
        }
    }

    /**
     * Prüft, ob die Liste der Befehle commands stets Rückgabebefehle enthalten.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkForContainingReturnCommand(List<AlgorithmCommand> commands, IdentifierType returnType) throws AlgorithmCompileException {
        if (returnType == null) {
            return;
        }
        if (commands.isEmpty()) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MISSING_RETURN_STATEMENT);
        }
        AlgorithmCommand lastCommand = commands.get(commands.size() - 1);
        if (!lastCommand.isReturnCommand()) {
            /* 
            Nur bei If-Else-Kontrollstrukturen müssen beide Blöcke einen Return-Befehl 
            am Ende haben. In allen anderen Fällen wird ein Fehler geworfen.
             */
            if (!lastCommand.isIfElseControlStructure()) {
                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MISSING_RETURN_STATEMENT);
            }
            List<AlgorithmCommand> commandsIfPart = ((IfElseControlStructure) lastCommand).getCommandsIfPart();
            List<AlgorithmCommand> commandsElsePart = ((IfElseControlStructure) lastCommand).getCommandsElsePart();
            checkForContainingReturnCommand(commandsIfPart, returnType);
            checkForContainingReturnCommand(commandsElsePart, returnType);
        }
    }

    /**
     * Prüft, ob die Liste der Befehle commands stets Rückgabebefehle vom
     * geforderten Typ (oder Untertyp) type enthält.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkForCorrectReturnType(List<AlgorithmCommand> commands, IdentifierType returnType) throws AlgorithmCompileException {
        Identifier returnIdentifier;
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).isReturnCommand()) {
                returnIdentifier = ((ReturnCommand) commands.get(i)).getIdentifier();
                if (returnType == null && returnIdentifier != Identifier.NULL_IDENTIFIER
                        || returnType != null && returnIdentifier == Identifier.NULL_IDENTIFIER) {
                    throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_WRONG_RETURN_TYPE);
                }
                if (returnType != null && !returnType.isSameOrSuperTypeOf(returnIdentifier.getType())) {
                    throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_WRONG_RETURN_TYPE);
                }
            }
            if (commands.get(i).isControlStructure()) {
                for (List<AlgorithmCommand> commandsInBlock : ((ControlStructure) commands.get(i)).getCommandBlocks()) {
                    checkForCorrectReturnType(commandsInBlock, returnType);
                }
            }
        }
    }

    /**
     * Prüft, ob der Codeblock commands im Algorithmus alg nicht erreichbaren
     * Code enthält.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkForUnreachableCodeInBlock(EditorCodeString line, List<AlgorithmCommand> commands, Algorithm alg) throws AlgorithmCompileException {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).isReturnCommand() && i < commands.size() - 1) {
                throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_UNREACHABLE_CODE, alg.getName());
            }
            if (commands.get(i).isControlStructure()) {
                for (List<AlgorithmCommand> commandsInBlock : ((ControlStructure) commands.get(i)).getCommandBlocks()) {
                    checkForUnreachableCodeInBlock(line, commandsInBlock, alg);
                }
                if (commands.get(i).isIfElseControlStructure()) {
                    if (doBothPartsContainReturnStatementInIfElseBlock((IfElseControlStructure) commands.get(i)) && i < commands.size() - 1) {
                        throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_UNREACHABLE_CODE, alg.getName());
                    }
                }
            }
        }
    }

    private static boolean doBothPartsContainReturnStatementInIfElseBlock(IfElseControlStructure ifElseBlock) throws AlgorithmCompileException {
        boolean ifPartContainsReturnStatement = false;
        boolean elsePartContainsReturnStatement = false;
        for (AlgorithmCommand c : ifElseBlock.getCommandsIfPart()) {
            if (c.isReturnCommand()) {
                ifPartContainsReturnStatement = true;
                break;
            }
        }
        for (AlgorithmCommand c : ifElseBlock.getCommandsElsePart()) {
            if (c.isReturnCommand()) {
                elsePartContainsReturnStatement = true;
                break;
            }
        }
        return ifPartContainsReturnStatement && elsePartContainsReturnStatement;
    }

    /**
     * Prüft, ob im Algorithmus alg Identifier verwendet werden, welche
     * möglicherweise nicht initialisiert wurden.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfAllUsedIdentifiersAreInitialized(List<AlgorithmCommand> commands, Algorithm alg) throws AlgorithmCompileException {
        // Map mit Namen von Bezeichnern, die bereits deklariert wurden. 
        // Der boolsche Wert zu jedem Key gibt an, ob der entsprechende Bezeichner bereits initialisiert wurde.
        Map<String, Boolean> declaredIdentifiers = new HashMap<>();

        for (AlgorithmCommand command : commands) {

            // Prüfung, ob die rechte Seite einer Zuweisung nur bereits definierte Bezeichner verwendet.
            if (command.isAssignValueCommand()) {
                Object targetValue = ((AssignValueCommand) command).getTargetValue();
                Identifier[] algorithmParameters = ((AssignValueCommand) command).getTargetAlgorithmArguments();
                if (targetValue != null) {
                    // Fall: Wertzuordnung.
                    Set<String> usedIdentifier = getUsedIdentifierNames(targetValue);
                    for (String identifierName : usedIdentifier) {
                        if (declaredIdentifiers.get(identifierName) != null && !declaredIdentifiers.get(identifierName)) {
                            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_IDENTIFIER_MAYBE_NOT_INITIALIZED, identifierName);
                        }
                    }
                } else if (algorithmParameters != null) {
                    // Fall: Algorithmusaufruf.
                    for (Identifier param : algorithmParameters) {
                        Set<String> usedIdentifier = getUsedIdentifierNames(param);
                        for (String identifierName : usedIdentifier) {
                            if (declaredIdentifiers.get(identifierName) != null && !declaredIdentifiers.get(identifierName)) {
                                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_IDENTIFIER_MAYBE_NOT_INITIALIZED, identifierName);
                            }
                        }
                    }
                }
            } else if (command.isReturnCommand()) {
                Identifier identifier = ((ReturnCommand) command).getIdentifier();
                if (identifier != null) {
                    String identifierName = identifier.getName();
                    if (declaredIdentifiers.get(identifierName) != null && !declaredIdentifiers.get(identifierName)) {
                        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_IDENTIFIER_MAYBE_NOT_INITIALIZED, identifierName);
                    }
                }
            } else if (command.isDeclareIDentifierCommand()) {
                declaredIdentifiers.put(((DeclareIdentifierCommand) command).getIdentifierSrc().getName(), false);
            } else if (command.isAssignValueCommand()
                    && declaredIdentifiers.keySet().contains(((AssignValueCommand) command).getIdentifierSrc().getName())) {
                declaredIdentifiers.put(((AssignValueCommand) command).getIdentifierSrc().getName(), true);
            } else if (command.isIfElseControlStructure()) {
                Set<String> assignedIdentifiers = getIdentifiersWhichValueIsAssigned((IfElseControlStructure) command);
                for (String identifier : assignedIdentifiers) {
                    if (declaredIdentifiers.keySet().contains(identifier)) {
                        declaredIdentifiers.put(identifier, true);
                    }
                }
            }

        }

    }

    private static Set<String> getUsedIdentifierNames(Object targetValue) {
        if (targetValue instanceof AbstractExpression) {
            return getUsedIdentifierNamesInCaseOfAbstractExpression((AbstractExpression) targetValue);
        }
        if (targetValue instanceof MalString) {
            Set<String> allUsedIdentifierNames = new HashSet<>();
            for (MalStringSummand summand : ((MalString) targetValue).getMalStringSummands()) {
                if (summand instanceof MalStringVariable) {
                    allUsedIdentifierNames.add(((MalStringVariable) summand).getVariableName());
                } else if (summand instanceof MalStringAbstractExpression) {
                    AbstractExpression abstrExpr = ((MalStringAbstractExpression) summand).getAbstractExpression();
                    allUsedIdentifierNames.addAll(getUsedIdentifierNamesInCaseOfAbstractExpression(abstrExpr));
                }
            }
            return allUsedIdentifierNames;
        }
        return new HashSet<>();
    }

    private static Set<String> getUsedIdentifierNamesInCaseOfAbstractExpression(AbstractExpression targetValue) {
        return targetValue.getContainedVars();
    }

    private static Set<String> getIdentifiersWhichValueIsAssigned(IfElseControlStructure ifElseControlStructure) {
        Set<String> assignedIdentifiers = new HashSet<>();
        Set<String> assignedIdentifiersInIfPart = getIdentifiersInCommandBlockWhichValueIsAssigned(ifElseControlStructure.getCommandsIfPart());
        Set<String> assignedIdentifiersInElsePart = getIdentifiersInCommandBlockWhichValueIsAssigned(ifElseControlStructure.getCommandsElsePart());
        for (String identifier : assignedIdentifiersInIfPart) {
            if (assignedIdentifiersInElsePart.contains(identifier)) {
                assignedIdentifiers.add(identifier);
            }
        }
        return assignedIdentifiers;
    }

    private static Set<String> getIdentifiersInCommandBlockWhichValueIsAssigned(List<AlgorithmCommand> commands) {
        Set<String> assignedIdentifiers = new HashSet<>();
        for (AlgorithmCommand command : commands) {
            if (command.isAssignValueCommand()) {
                assignedIdentifiers.add(((AssignValueCommand) command).getIdentifierSrc().getName());
            } else if (command.isIfElseControlStructure()) {
                assignedIdentifiers.addAll(getIdentifiersWhichValueIsAssigned((IfElseControlStructure) command));
            }
        }
        return assignedIdentifiers;
    }

    public static Map<String, IdentifierType> extractTypesFromMemory(AlgorithmMemory memory) {
        Map<String, IdentifierType> valuesMap = new HashMap<>();
        for (String identifierName : memory.keySet()) {
            valuesMap.put(identifierName, memory.get(identifierName).getType());
        }
        return valuesMap;
    }

    public static Map<String, Class<? extends AbstractExpression>> extractClassesOfAbstractExpressionIdentifiersFromMemory(AlgorithmMemory memory) {
        Map<String, Class<? extends AbstractExpression>> classesMap = new HashMap<>();
        for (String identifierName : memory.keySet()) {
            if (memory.get(identifierName).getType() != null) {
                switch (memory.get(identifierName).getType()) {
                    case EXPRESSION:
                        classesMap.put(identifierName, Expression.class);
                        break;
                    case BOOLEAN_EXPRESSION:
                        classesMap.put(identifierName, BooleanExpression.class);
                        break;
                    case MATRIX_EXPRESSION:
                        classesMap.put(identifierName, MatrixExpression.class);
                        break;
                    default:
                        break;
                }
            }
        }
        return classesMap;
    }

    /**
     * Gibt einen Namen für einen technischen Bezeichner zurück. Der
     * zurückgegebene Name ist "#i" mit dem kleinsten i &ge; 1, welcher in
     * scopeMemory noch nict vorkommt.
     */
    public static String generateTechnicalIdentifierName(AlgorithmMemory scopeMemory) {
        int i = 1;
        while (scopeMemory.containsKey(GEN_VAR + i)) {
            i++;
        }
        return GEN_VAR + i;
    }

    /**
     * Gibt zurück, ob name der Name eines technischen Bezeichners ist.
     */
    public static boolean isTechnicalIdentifierName(String name) {
        if (!name.startsWith(GEN_VAR)) {
            return false;
        }
        try {
            Integer.parseInt(name.substring(GEN_VAR.length()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    ///////////////////// Parsing-HilfsMethoden /////////////////////
    public static Object parseParameterAgaingstType(EditorCodeString param, IdentifierValidator validator, AlgorithmMemory scopeMemory, IdentifierType type) throws AlgorithmCompileException, ExpressionException, BooleanExpressionException {
        switch (type) {
            case EXPRESSION:
                Expression expr = buildExpressionWithScopeMemory(param, validator, scopeMemory);
                // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                checkIfAllIdentifiersAreDefined(param, expr.getContainedVars(), scopeMemory);
                return expr;
            case BOOLEAN_EXPRESSION:
                BooleanExpression boolExpr = buildBooleanExpressionWithScopeMemory(param, validator, scopeMemory);
                // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                CompilerUtils.checkIfAllIdentifiersAreDefined(param, boolExpr.getContainedVars(), scopeMemory);
                return boolExpr;
            case MATRIX_EXPRESSION:
                MatrixExpression matExpr = buildMatrixExpressionWithScopeMemory(param, validator, scopeMemory);
                // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                CompilerUtils.checkIfAllIdentifiersAreDefined(param, matExpr.getContainedVars(), scopeMemory);
                return matExpr;
            default:
                // Fall: String. 
                return getMalString(param, scopeMemory);
        }
    }

    public static ParameterData parseParameterWithoutType(EditorCodeString param, IdentifierValidator validator, AlgorithmMemory scopeMemory) throws AlgorithmCompileException {
        try {
            // Prüfung, ob der Parameter ein Ausdruck ist.
            Expression expr = buildExpressionWithScopeMemory(param, validator, scopeMemory);
            return new ParameterData(expr);
        } catch (ExpressionException eExpr) {
            // Prüfung, ob der Parameter ein boolscher Ausdruck ist.
            try {
                BooleanExpression boolExpr = buildBooleanExpressionWithScopeMemory(param, validator, scopeMemory);
                return new ParameterData(boolExpr);
            } catch (BooleanExpressionException eBoolExpr) {
                try {
                    // Prüfung, ob der Parameter ein Matrizenausdruck ist.
                    MatrixExpression matExpr = buildMatrixExpressionWithScopeMemory(param, validator, scopeMemory);
                    return new ParameterData(matExpr);
                } catch (ExpressionException eMatExpr) {
                    // Prüfung, ob der Parameter ein String ist.
                    try {
                        MalString malString = CompilerUtils.getMalString(param, scopeMemory);
                        return new ParameterData(malString);
                    } catch (AlgorithmCompileException e) {
                        throw new ParseAssignValueException(param.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, param);
                    }
                }
            }
        }
    }

    ///////////////////// Methoden für die Zerlegung eines Strings ///////////////////////
    public static MalString getMalString(EditorCodeString input, AlgorithmMemory scopeMemory) throws AlgorithmCompileException {
        List<EditorCodeString> stringValuesAsStrings = decomposeByConcat(input);
        List<MalStringSummand> malStringSummands = new ArrayList<>();
        for (EditorCodeString s : stringValuesAsStrings) {
            if (isValidString(s)) {
                malStringSummands.add(new MalStringCharSequence(s.substring(1, s.length() - 1).getValue()));
            } else if (scopeMemory.containsIdentifier(s.getValue()) && scopeMemory.get(s.getValue()).getType().equals(IdentifierType.STRING)) {
                malStringSummands.add(new MalStringVariable(scopeMemory.get(s.getValue()).getName()));
            } else {
                // Öffnende Klammer am Anfang und schließende Klammer am Ende beseitigen.
                boolean stringWasSurroundedByBracket = false;
                while (s.startsWith(ReservedChars.OPEN_BRACKET.getStringValue()) && s.endsWith(ReservedChars.CLOSE_BRACKET.getStringValue())) {
                    s = s.substring(1, s.length() - 1);
                    stringWasSurroundedByBracket = true;
                }

                AbstractExpression abstrExpr = null;
                try {
                    abstrExpr = buildExpressionWithScopeMemory(s, AlgorithmCompiler.VALIDATOR, scopeMemory);
                    // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                    CompilerUtils.checkIfAllIdentifiersAreDefined(s, abstrExpr.getContainedVars(), scopeMemory);
                    malStringSummands.add(new MalStringAbstractExpression(abstrExpr));
                    continue;
                } catch (ExpressionException e) {
                }
                try {
                    abstrExpr = buildBooleanExpressionWithScopeMemory(s, AlgorithmCompiler.VALIDATOR, scopeMemory);
                    // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                    CompilerUtils.checkIfAllIdentifiersAreDefined(s, abstrExpr.getContainedVars(), scopeMemory);
                    malStringSummands.add(new MalStringAbstractExpression(abstrExpr));
                    continue;
                } catch (BooleanExpressionException e) {
                }
                try {
                    abstrExpr = buildMatrixExpressionWithScopeMemory(s, AlgorithmCompiler.VALIDATOR, scopeMemory);
                    // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                    CompilerUtils.checkIfAllIdentifiersAreDefined(s, abstrExpr.getContainedVars(), scopeMemory);
                    malStringSummands.add(new MalStringAbstractExpression(abstrExpr));
                    continue;
                } catch (ExpressionException e) {
                }
                if (abstrExpr == null) {
                    // Letzte Möglichkeit: s war von Klammern umgeben und innen steht ein zusammengesetzter String.
                    if (stringWasSurroundedByBracket) {
                        List<EditorCodeString> substrings = decomposeByConcat(s);
                        if (substrings.size() > 1) {
                            MalString subMalString = getMalString(s, scopeMemory);
                            for (MalStringSummand obj : subMalString.getMalStringSummands()) {
                                malStringSummands.add(obj);
                            }
                        }
                    } else {
                        throw new ParseAssignValueException(s.getLineNumbers(), AlgorithmCompileExceptionIds.AC_NOT_A_VALID_STRING, s);
                    }
                }
            }
        }

        return new MalString(malStringSummands.toArray(new MalStringSummand[malStringSummands.size()]));
    }

    public static MalString getMalString(EditorCodeString input, Map<String, IdentifierType> typesMap) throws AlgorithmCompileException {
        // Hier wird ein "Hilfsspeicher" erschaffen, damit die Methode getMalString()
        // mit der Signatur getMalString(String,AlgorithmMemory) anwendbar ist.
        AlgorithmMemory memory = new AlgorithmMemory(null);
        for (String varName : typesMap.keySet()) {
            memory.put(varName, Identifier.createIdentifier(varName, typesMap.get(varName)));
        }
        return getMalString(input, memory);
    }

    private static List<EditorCodeString> decomposeByConcat(EditorCodeString input) throws AlgorithmCompileException {
        List<EditorCodeString> stringValues = new ArrayList<>();

        int bracketCounter = 0;
        int beginBlockPosition = 0;
        int endBlockPosition = -1;
        boolean withinString = false;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ReservedChars.STRING_DELIMITER.getValue()) {
                withinString = !withinString;
            }
            if (input.charAt(i) == ReservedChars.OPEN_BRACKET.getValue() && !withinString) {
                bracketCounter++;
            } else if (input.charAt(i) == ReservedChars.CLOSE_BRACKET.getValue() && !withinString) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                if (Operators.CONCAT.getValue().equals(String.valueOf(input.charAt(i))) && !withinString) {
                    endBlockPosition = i;
                    stringValues.add(input.substring(beginBlockPosition, endBlockPosition));
                    beginBlockPosition = i + 1;
                } else if (i == input.length() - 1) {
                    endBlockPosition = input.length();
                    stringValues.add(input.substring(beginBlockPosition, endBlockPosition));
                    beginBlockPosition = i + 1;
                }
            }
        }
        if (bracketCounter > 0) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }
        if (endBlockPosition != input.length()) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, input.substring(endBlockPosition));
        }

        return stringValues;
    }

    private static boolean isValidString(EditorCodeString input) {
        return input.startsWith(ReservedChars.STRING_DELIMITER.getStringValue())
                && input.endsWith(ReservedChars.STRING_DELIMITER.getStringValue())
                && input.replaceAll(ReservedChars.STRING_DELIMITER.getStringValue(), "").length() == input.length() - 2;
    }

    ////////////////////////////////// Parsen von abstrakten Ausdrücken unter Zuhilfenahme bereits bekannter Bezeichner im Speicher //////////////////////////////////
    public static Expression buildExpressionWithScopeMemory(EditorCodeString input, IdentifierValidator validator, AlgorithmMemory scopeMemory) throws ExpressionException {
        validator.setKnownVariables(extractClassesOfAbstractExpressionIdentifiersFromMemory(scopeMemory));
        try {
            return Expression.build(input.getValue(), validator);
        } finally {
            validator.unsetKnownVariables();
        }
    }

    public static BooleanExpression buildBooleanExpressionWithScopeMemory(EditorCodeString input, IdentifierValidator validator, AlgorithmMemory scopeMemory) throws BooleanExpressionException {
        validator.setKnownVariables(extractClassesOfAbstractExpressionIdentifiersFromMemory(scopeMemory));
        try {
            return BooleanExpression.build(input, validator, extractTypesFromMemory(scopeMemory));
        } finally {
            validator.unsetKnownVariables();
        }
    }

    public static MatrixExpression buildMatrixExpressionWithScopeMemory(EditorCodeString input, IdentifierValidator validator, AlgorithmMemory scopeMemory) throws ExpressionException {
        validator.setKnownVariables(extractClassesOfAbstractExpressionIdentifiersFromMemory(scopeMemory));
        try {
            return MatrixExpression.build(input.getValue(), validator, validator);
        } finally {
            validator.unsetKnownVariables();
        }
    }

}
