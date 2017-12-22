package algorithmexecuter;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.interfaces.IdentifierValidator;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
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

    public static final String GEN_VAR = "#";

    private CompilerUtils() {
    }

    /**
     * Transportklasse für Algorithmensignaturen. name gibt den Namen des
     * Algorithmus an und parameters die konkreten Parameterwerte.
     */
    public static class AlgorithmParseData {

        String name;
        String[] parameters;

        public String getName() {
            return name;
        }

        public String[] getParameters() {
            return parameters;
        }

        public AlgorithmParseData(String name, String[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }

    }

    public static String preprocessAlgorithm(String input) {
        String outputFormatted = input;
        outputFormatted = removeLeadingWhitespaces(outputFormatted);
        outputFormatted = removeEndingWhitespaces(outputFormatted);
        outputFormatted = replaceAllRepeatedly(outputFormatted, " ", "\t", "\n");
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

    public static Signature getSignature(IdentifierType returnType, String algName, Identifier[] parameters) {
        IdentifierType[] types = new IdentifierType[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getType();
        }
        return new Signature(returnType, algName, types);
    }

    public static AlgorithmParseData getAlgorithmParseData(String input) throws AlgorithmCompileException {
        String[] algNameAndParams = CompilerUtils.getAlgorithmNameAndParameters(input);
        String algName = algNameAndParams[0];
        String[] params = CompilerUtils.getParameters(algNameAndParams[1]);
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
    private static String[] getAlgorithmNameAndParameters(String input) throws AlgorithmCompileException {

        // Leerzeichen beseitigen
        input = CompilerUtils.removeLeadingWhitespaces(input);

        String[] result = new String[2];
        int i = input.indexOf(ReservedChars.OPEN_BRACKET.getValue());
        if (i == -1) {
            // Um zu verhindern, dass es eine IndexOutOfBoundsException gibt.
            i = 0;
        }
        result[0] = input.substring(0, i);

        // Wenn der Algorithmusname leer ist -> Fehler.
        if (result[0].length() == 0) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_ALGORITHM_HAS_NO_NAME);
        }

        // Wenn result[0].length() > input.length() - 2 -> Fehler (der Befehl besitzt NICHT die Form command(...), insbesondere fehlt ")" am Ende).
        if (result[0].length() > input.length() - 2) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        if (input.charAt(input.length() - 1) != ReservedChars.CLOSE_BRACKET.getValue()) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        result[1] = input.substring(result[0].length() + 1, input.length() - 1);

        return result;

    }

    /**
     * Input: String input, in der NUR die Parameter (getrennt durch ein Komma)
     * stehen. Beispiel input = "expression x, expression y". Parameter sind
     * dann {expression x, expression y}. Nach einem eingelesenen Komma, welches
     * NICHT von runden Klammern umgeben ist, werden die Parameter getrennt.
     *
     * @throws AlgorithmCompileException
     */
    private static String[] getParameters(String input) throws AlgorithmCompileException {

        // Falls Parameterstring leer ist -> Fertig
        if (input.isEmpty()) {
            return new String[0];
        }

        ArrayList<String> resultParameters = new ArrayList<>();
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
                    throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_IDENTIFIER_EXPECTED);
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == input.length() - 1) {
                if (startPositionOfCurrentParameter == input.length()) {
                    throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_IDENTIFIER_EXPECTED);
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, input.length()));
            }

        }

        if (bracketCounter != 0 || squareBracketCounter != 0) {
            if (bracketCounter > 0) {
                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
            }
            if (bracketCounter < 0) {
                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.OPEN_BRACKET.getValue());
            }
            if (squareBracketCounter > 0) {
                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_SQUARE_BRACKET.getValue());
            }
            if (squareBracketCounter < 0) {
                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.OPEN_SQUARE_BRACKET.getValue());
            }
        }

        String[] resultParametersAsArray = new String[resultParameters.size()];
        for (int i = 0; i < resultParameters.size(); i++) {
            resultParametersAsArray[i] = resultParameters.get(i);
        }

        return resultParametersAsArray;

    }

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
     * Prüft, ob die Signaturen in signatures eine Signatur des Hauptalgorithmus
     * enthalten.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfMainAlgorithmSignatureExists(AlgorithmSignatureStorage signatures) throws AlgorithmCompileException {
        for (Signature sgn : signatures.getAlgorithmSignatureStorage()) {
            if (sgn.getName().equals(FixedAlgorithmNames.MAIN.getValue())) {
                return;
            }
        }
        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_DOES_NOT_EXIST);
    }

    /**
     * Prüft, ob algorithms den Hauptalgorithmus enthalten.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkIfMainAlgorithmExists(AlgorithmStorage algorithms) throws AlgorithmCompileException {
        for (Algorithm alg : algorithms.getAlgorithmStorage()) {
            if (alg.getName().equals(FixedAlgorithmNames.MAIN.getValue())) {
                return;
            }
        }
        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MAIN_ALGORITHM_DOES_NOT_EXIST);
    }

    /**
     * Prüft, ob alle Bezeichner mit Namen im Set vars auch deklariert wurden.
     *
     * @throws ParseAssignValueException
     */
    public static void checkIfAllIdentifiersAreDefined(Set<String> vars, AlgorithmMemory memory) throws ParseAssignValueException {
        for (String var : vars) {
            if (!memory.containsKey(var)) {
                throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, var);
            }
        }
    }

    /**
     * Prüft, ob die alle Bezeichner mit Namen im Set vars vom Typ type sind.
     *
     * @throws ParseAssignValueException
     */
    public static void checkIfIdentifiersAreOfCorrectType(IdentifierType type, Set<String> vars, AlgorithmMemory memory) throws ParseAssignValueException {
        for (String var : vars) {
            if (memory.get(var).getType() != type) {
                throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES, memory.get(var).getType(), type);
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
    public static String[] splitByKomma(String input) throws AlgorithmCompileException {
        List<String> linesAsList = new ArrayList<>();
        int wavedBracketCounter = 0;
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        int beginBlockPosition = 0;
        int endBlockPosition = -1;
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
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END);
        }
        if (bracketCounter > 0) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET);
        }
        if (squareBracketCounter > 0) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_SQUARE_BRACKET);
        }

        return linesAsList.toArray(new String[linesAsList.size()]);
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
     * Prüft, ob die Liste der Befehle commands nur einfache return enthält.
     *
     * @throws AlgorithmCompileException
     */
    public static void checkForOnlySimpleReturns(List<AlgorithmCommand> commands) throws AlgorithmCompileException {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).isReturnCommand() && ((ReturnCommand) commands.get(i)).getIdentifier() != null) {
                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_VOID_ALGORITHM_MUST_CONTAIN_ONLY_SIMPLE_RETURNS);
            }
            if (commands.get(i).isControlStructure()) {
                for (List<AlgorithmCommand> commandsInBlock : ((ControlStructure) commands.get(i)).getCommandBlocks()) {
                    checkForOnlySimpleReturns(commandsInBlock);
                }
            }
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
            if (commands.get(i).isReturnCommand() && ((ReturnCommand) commands.get(i)).getIdentifier() != null) {
                returnIdentifier = ((ReturnCommand) commands.get(i)).getIdentifier();
                if (returnIdentifier == null) {
                    throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_WRONG_RETURN_TYPE);
                }
                if (!returnType.isSameOrGeneralTypeOf(returnIdentifier.getType())) {
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
    public static void checkForUnreachableCodeInBlock(List<AlgorithmCommand> commands, Algorithm alg) throws AlgorithmCompileException {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).isReturnCommand() && i < commands.size() - 1) {
                throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_UNREACHABLE_CODE, alg);
            }
            if (commands.get(i).isControlStructure()) {
                for (List<AlgorithmCommand> commandsInBlock : ((ControlStructure) commands.get(i)).getCommandBlocks()) {
                    checkForUnreachableCodeInBlock(commandsInBlock, alg);
                }
                if (commands.get(i).isIfElseControlStructure()) {
                    if (doBothPartsContainReturnStatementInIfElseBlock((IfElseControlStructure) commands.get(i)) && i < commands.size() - 1) {
                        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_UNREACHABLE_CODE, alg);
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
            }
            if (command.isReturnCommand()) {
                
                
                
                
                
                
                
                
                
                
                
            }
            // Ermittlung aller Bezeichner, welche bereits deklariert und initialisiert wurden.
            if (command.isDeclareIDentifierCommand()) {
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
        }
        return new HashSet<>();
    }

    private static Set<String> getUsedIdentifierNamesInCaseOfAbstractExpression(AbstractExpression targetValue) {
        if (targetValue instanceof Expression) {
            return ((Expression) targetValue).getContainedVars();
        }
        if (targetValue instanceof MatrixExpression) {
            Set<String> allVars = ((MatrixExpression) targetValue).getContainedExpressionVars();
            Set<String> matrixVars = ((MatrixExpression) targetValue).getContainedMatrixVars();
            allVars.addAll(matrixVars);
            return allVars;
        }
        if (targetValue instanceof BooleanExpression) {

        }
        return new HashSet<>();
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
    public static Object parseParameterAgaingstType(String input, IdentifierValidator validator, AlgorithmMemory scopeMemory, IdentifierType type) throws AlgorithmCompileException, ExpressionException, BooleanExpressionException {
        switch (type) {
            case EXPRESSION:
                Expression expr = Expression.build(input, validator);
                // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                checkIfAllIdentifiersAreDefined(expr.getContainedVars(), scopeMemory);
                checkIfIdentifiersAreOfCorrectType(type, expr.getContainedVars(), scopeMemory);
                return expr;
            case BOOLEAN_EXPRESSION:
                BooleanExpression boolExpr = BooleanExpression.build(input, validator, extractTypesFromMemory(scopeMemory));
                // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                CompilerUtils.checkIfAllIdentifiersAreDefined(boolExpr.getContainedVars(), scopeMemory);
                CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, boolExpr.getContainedExpressionVars(), scopeMemory);
                CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.BOOLEAN_EXPRESSION, boolExpr.getContainedBooleanVars(scopeMemory), scopeMemory);
                CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.MATRIX_EXPRESSION, boolExpr.getContainedMatrixVars(), scopeMemory);
                return boolExpr;
            case MATRIX_EXPRESSION:
                MatrixExpression matExpr = MatrixExpression.build(input, validator, validator);
                // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                CompilerUtils.checkIfAllIdentifiersAreDefined(matExpr.getContainedVars(), scopeMemory);
                CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, matExpr.getContainedExpressionVars(), scopeMemory);
                CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.MATRIX_EXPRESSION, matExpr.getContainedMatrixVars(), scopeMemory);
                return matExpr;
            default:
                // Fall: String. 
                return getMalString(input, scopeMemory);
        }
    }

    public static ParameterData parseParameterWithoutType(String input, IdentifierValidator validator, AlgorithmMemory scopeMemory) throws AlgorithmCompileException {
        try {
            // Prüfung, ob der Parameter ein Ausdruck ist.
            Expression expr = Expression.build(input, validator);
            return new ParameterData(expr);
        } catch (ExpressionException eExpr) {
            // Prüfung, ob der Parameter ein boolscher Ausdruck ist.
            Map<String, IdentifierType> typeMap = CompilerUtils.extractTypesFromMemory(scopeMemory);
            try {
                BooleanExpression boolExpr = BooleanExpression.build(input, validator, typeMap);
                return new ParameterData(boolExpr);
            } catch (BooleanExpressionException eBoolExpr) {
                try {
                    // Prüfung, ob der Parameter ein Matrizenausdruck ist.
                    MatrixExpression matExpr = MatrixExpression.build(input, validator, validator);
                    return new ParameterData(matExpr);
                } catch (ExpressionException eMatExpr) {
                    // Prüfung, ob der Parameter ein String ist.
                    try {
                        MalString malString = CompilerUtils.getMalString(input, scopeMemory);
                        return new ParameterData(malString);
                    } catch (AlgorithmCompileException e) {
                        throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, input);
                    }
                }
            }
        }
    }

    ///////////////////// Methoden für die Zerlegung eines Strings ///////////////////////
    public static MalString getMalString(String input, AlgorithmMemory scopeMemory) throws AlgorithmCompileException {
        List<String> stringValuesAsStrings = decomposeByConcat(input);
        List<MalStringSummand> malStringSummands = new ArrayList<>();
        for (String s : stringValuesAsStrings) {
            if (isValidString(s)) {
                malStringSummands.add(new MalStringCharSequence(s.substring(1, s.length() - 1)));
            } else if (scopeMemory.containsIdentifier(s) && scopeMemory.get(s).getType().equals(IdentifierType.STRING)) {
                malStringSummands.add(new MalStringVariable(scopeMemory.get(s).getName()));
            } else {
                // Öffnende Klammer am Anfang und schließende Klammer am Ende beseitigen.
                boolean stringWasSurroundedByBracket = false;
                while (s.startsWith(ReservedChars.OPEN_BRACKET.getStringValue()) && s.endsWith(ReservedChars.CLOSE_BRACKET.getStringValue())) {
                    s = s.substring(1, s.length() - 1);
                    stringWasSurroundedByBracket = true;
                }

                AbstractExpression abstrExpr = null;
                try {
                    AlgorithmCompiler.VALIDATOR.setKnownVariables(extractClassesOfAbstractExpressionIdentifiersFromMemory(scopeMemory));
                    abstrExpr = Expression.build(s, AlgorithmCompiler.VALIDATOR);
                    AlgorithmCompiler.VALIDATOR.unsetKnownVariables();
                    // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                    CompilerUtils.checkIfAllIdentifiersAreDefined(abstrExpr.getContainedVars(), scopeMemory);
                    CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, abstrExpr.getContainedVars(), scopeMemory);
                    malStringSummands.add(new MalStringAbstractExpression(abstrExpr));
                    continue;
                } catch (ExpressionException e) {
                    AlgorithmCompiler.VALIDATOR.unsetKnownVariables();
                }
                try {
                    abstrExpr = BooleanExpression.build(s, AlgorithmCompiler.VALIDATOR, extractTypesFromMemory(scopeMemory));
                    // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                    CompilerUtils.checkIfAllIdentifiersAreDefined(abstrExpr.getContainedVars(), scopeMemory);
                    CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, ((BooleanExpression) abstrExpr).getContainedExpressionVars(), scopeMemory);
                    CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.BOOLEAN_EXPRESSION, ((BooleanExpression) abstrExpr).getContainedBooleanVars(scopeMemory), scopeMemory);
                    CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.MATRIX_EXPRESSION, ((BooleanExpression) abstrExpr).getContainedMatrixVars(), scopeMemory);
                    malStringSummands.add(new MalStringAbstractExpression(abstrExpr));
                    continue;
                } catch (BooleanExpressionException e) {
                }
                try {
                    AlgorithmCompiler.VALIDATOR.setKnownVariables(extractClassesOfAbstractExpressionIdentifiersFromMemory(scopeMemory));
                    abstrExpr = MatrixExpression.build(s, AlgorithmCompiler.VALIDATOR, AlgorithmCompiler.VALIDATOR);
                    AlgorithmCompiler.VALIDATOR.unsetKnownVariables();
                    // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                    CompilerUtils.checkIfAllIdentifiersAreDefined(abstrExpr.getContainedVars(), scopeMemory);
                    CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, ((MatrixExpression) abstrExpr).getContainedExpressionVars(), scopeMemory);
                    CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.MATRIX_EXPRESSION, ((MatrixExpression) abstrExpr).getContainedMatrixVars(), scopeMemory);
                    malStringSummands.add(new MalStringAbstractExpression(abstrExpr));
                    continue;
                } catch (ExpressionException e) {
                    AlgorithmCompiler.VALIDATOR.unsetKnownVariables();
                }
                if (abstrExpr == null) {
                    // Letzte Möglichkeit: s war von Klammern umgeben und innen steht ein zusammengesetzter String.
                    if (stringWasSurroundedByBracket) {
                        List<String> substrings = decomposeByConcat(s);
                        if (substrings.size() > 1) {
                            MalString subMalString = getMalString(s, scopeMemory);
                            for (MalStringSummand obj : subMalString.getMalStringSummands()) {
                                malStringSummands.add(obj);
                            }
                        }
                    } else {
                        throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_NOT_A_VALID_STRING, s);
                    }
                }
            }
        }

        return new MalString(malStringSummands.toArray(new MalStringSummand[malStringSummands.size()]));
    }

    public static MalString getMalString(String input, Map<String, IdentifierType> typesMap) throws AlgorithmCompileException {
        // Hier wird ein "Hilfsspeicher" erschaffen, damit die getMalString-Methode 
        // mit der Signatur getMalString(String,AlgorithmMemory) anwendbar ist.
        AlgorithmMemory memory = new AlgorithmMemory(null);
        for (String varName : typesMap.keySet()) {
            memory.put(varName, Identifier.createIdentifier(varName, typesMap.get(varName)));
        }
        return getMalString(input, memory);
    }

    private static List<String> decomposeByConcat(String input) throws AlgorithmCompileException {
        List<String> stringValues = new ArrayList<>();

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
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET);
        }
        if (endBlockPosition != input.length()) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, input.substring(endBlockPosition));
        }

        return stringValues;
    }

    private static boolean isValidString(String input) {
        return input.startsWith(ReservedChars.STRING_DELIMITER.getStringValue())
                && input.endsWith(ReservedChars.STRING_DELIMITER.getStringValue())
                && input.replaceAll(ReservedChars.STRING_DELIMITER.getStringValue(), "").length() == input.length() - 2;
    }

}
