package algorithmexecuter;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixVariable;
import static algorithmexecuter.AlgorithmBuilder.VALIDATOR;
import algorithmexecuter.model.command.AlgorithmCommand;
import algorithmexecuter.model.command.AssignValueCommand;
import algorithmexecuter.model.command.DeclareIdentifierCommand;
import algorithmexecuter.model.command.IfElseControlStructure;
import algorithmexecuter.model.command.ReturnCommand;
import algorithmexecuter.model.command.WhileControlStructure;
import algorithmexecuter.booleanexpression.BooleanExpression;
import algorithmexecuter.booleanexpression.BooleanVariable;
import algorithmexecuter.enums.AssignValueType;
import algorithmexecuter.enums.ComparingOperators;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.enums.Keyword;
import algorithmexecuter.enums.Operators;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.exceptions.BooleanExpressionException;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.exceptions.NotDesiredCommandException;
import algorithmexecuter.exceptions.ParseAssignValueException;
import algorithmexecuter.exceptions.ParseControlStructureException;
import algorithmexecuter.exceptions.ParseKeywordException;
import algorithmexecuter.exceptions.ParseReturnException;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.AlgorithmMemory;
import algorithmexecuter.model.Algorithm;
import algorithmexecuter.model.Signature;
import algorithmexecuter.model.command.DoWhileControlStructure;
import algorithmexecuter.model.command.ForControlStructure;
import algorithmexecuter.model.command.KeywordCommand;
import algorithmexecuter.model.command.VoidCommand;
import algorithmexecuter.model.utilclasses.AlgorithmCallData;
import algorithmexecuter.model.utilclasses.AlgorithmCommandReplacementData;
import algorithmexecuter.model.utilclasses.EditorCodeString;
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.model.utilclasses.ParameterData;
import exceptions.ExpressionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AlgorithmLineCompiler {

    /**
     * Gibt eine Liste von Befehlen zurück, welche aus der gegebenen Zeile
     * generíert werden. Aus einer Zeile können auch mehrere Befehle generiert
     * werden.<br>
     * <b>BEISPIEL:</b> Der Algorithmus "computeggt(expression a, expression b)"
     * existiert bereits. Dann werden aus "expression x = computeggt(15,25) *
     * exp(4)" etwa die beiden folgenden Befehle generiert: <br>
     * expression #1 = computeggt(15,25)<br>
     * expression x = #1 * exp(4)<br>
     *
     * @throws AlgorithmCompileException
     */
    private static List<AlgorithmCommand> parseLine(EditorCodeString line, AlgorithmMemory memory, Algorithm alg, boolean keywordsAllowed) throws AlgorithmCompileException {
        try {
            return parseAssignValueCommand(line, memory);
        } catch (ParseAssignValueException e) {
            throw e;
        } catch (NotDesiredCommandException e) {
        }

        try {
            return parseDeclareIdentifierCommand(line, memory);
        } catch (ParseAssignValueException e) {
            throw e;
        } catch (NotDesiredCommandException e) {
        }

        try {
            return parseControlStructure(line, memory, alg, keywordsAllowed);
        } catch (ParseControlStructureException e) {
            throw e;
        } catch (NotDesiredCommandException e) {
        }

        try {
            return parseKeywordCommand(line, keywordsAllowed);
        } catch (ParseKeywordException e) {
            throw e;
        } catch (NotDesiredCommandException e) {
        }

        try {
            return parseReturnCommand(line, memory, alg);
        } catch (ParseReturnException e) {
            throw e;
        } catch (NotDesiredCommandException e) {
        }

        try {
            return parseVoidCommand(line, memory);
        } catch (NotDesiredCommandException e) {
        }

        throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_COMMAND_COUND_NOT_BE_PARSED, line.getValue());
    }

    private static List<AlgorithmCommand> parseDeclareIdentifierCommand(EditorCodeString line, AlgorithmMemory scopeMemory) throws AlgorithmCompileException, NotDesiredCommandException {
        // Typ ermitteln. Dieser ist != null, wenn es sich definitiv um eine Identifierdeklaration handelt.
        IdentifierType type = getTypeIfIsValidDeclareIdentifierCommand(line);
        if (type == null) {
            throw new NotDesiredCommandException();
        }

        String identifierName = line.substring(type.toString().length() + 1).getValue();
        // Prüfung, ob dieser Bezeichner gültigen Namen besitzt.
        if (!VALIDATOR.isValidIdentifier(identifierName)) {
            throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_ILLEGAL_CHARACTER, identifierName);
        }
        // Prüfung, ob dieser Identifier bereits existiert.
        if (scopeMemory.containsIdentifier(identifierName)) {
            throw new ParseAssignValueException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, identifierName);
        }
        Identifier identifier = Identifier.createIdentifier(scopeMemory, identifierName, type);
        scopeMemory.put(identifierName, identifier);
        return Collections.singletonList((AlgorithmCommand) new DeclareIdentifierCommand(identifier));
    }

    private static IdentifierType getTypeIfIsValidDeclareIdentifierCommand(EditorCodeString line) {
        for (IdentifierType type : IdentifierType.values()) {
            if (line.startsWith(type.toString() + " ")) {
                return type;
            }
        }
        return null;
    }

    private static List<AlgorithmCommand> parseAssignValueCommand(EditorCodeString line, AlgorithmMemory scopeMemory) throws AlgorithmCompileException, NotDesiredCommandException {
        // Ermittlung der Stelle des Zuweisungsoperators "=", falls vorhanden. 
        int defineCharPosition = getPositionOfDefineCharIfIsAssignValueCommandIfValid(line);
        if (defineCharPosition < 0) {
            throw new NotDesiredCommandException();
        }

        EditorCodeString leftSide = line.substring(0, defineCharPosition);
        EditorCodeString rightSide = line.substring(defineCharPosition + 1);
        // Linke Seite behandeln.
        IdentifierType type = null;
        for (IdentifierType t : IdentifierType.values()) {
            if (leftSide.startsWith(t.toString() + " ")) {
                type = t;
                break;
            }
        }

        String identifierNameValue;
        AssignValueType assignValueType;
        if (type != null) {

            EditorCodeString identifierName = leftSide.substring((type.toString() + " ").length());
            identifierNameValue = identifierName.getValue();
            assignValueType = AssignValueType.NEW;
            // Prüfung, ob dieser Bezeichner gültigen Namen besitzt.
            if (!VALIDATOR.isValidIdentifier(identifierNameValue)) {
                throw new ParseAssignValueException(identifierName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_ILLEGAL_CHARACTER, identifierNameValue);
            }
            // Prüfung, ob dieser Bezeichner bereits existiert.
            if (scopeMemory.containsIdentifier(identifierNameValue)) {
                throw new ParseAssignValueException(identifierName.getLineNumbers(), AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, identifierNameValue);
            }
        } else {
            identifierNameValue = leftSide.getValue();
            assignValueType = AssignValueType.CHANGE;
            // Prüfung, ob dieser Identifier bereits existiert.
            if (!scopeMemory.containsIdentifier(identifierNameValue)) {
                throw new ParseAssignValueException(leftSide.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, identifierNameValue);
            }
            type = scopeMemory.get(identifierNameValue).getType();
        }

        // Rechte Seite behandeln.
        Identifier identifier = Identifier.createIdentifier(scopeMemory, identifierNameValue, type);
        if (rightSide.isEmpty()) {
            throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_IDENTIFIER_VALUE_EXPECTED, identifierNameValue);
        }
        if (type != null) {

            // Klammern links und rechts um den Rückgabewert entfernen.
            while (rightSide.startsWith(ReservedChars.OPEN_BRACKET.getStringValue()) && rightSide.endsWith(ReservedChars.CLOSE_BRACKET.getStringValue())) {
                rightSide = rightSide.substring(1, rightSide.length() - 1);
            }

            AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(rightSide, scopeMemory);
            List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
            EditorCodeString rightSideReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

            if (type != IdentifierType.STRING) {
                try {
                    AbstractExpression expr = (AbstractExpression) CompilerUtils.parseParameterAgaingstType(rightSideReplaced, VALIDATOR, scopeMemory, type);
                    scopeMemory.put(identifierNameValue, identifier);
                    commands.add(new AssignValueCommand(identifier, expr, assignValueType, rightSide.getLineNumbers()));
                    return commands;
                } catch (BooleanExpressionException | ExpressionException e) {
                    // 1. Versuch: Es ist eine Zuweisung, aber vom falschen Typ. Dann entsprechende Meldung über Inkompatibilität ausgeben.
                    ParameterData parsedParameter = null;
                    try {
                        parsedParameter = CompilerUtils.parseParameterWithoutType(rightSideReplaced, VALIDATOR, scopeMemory);
                    } catch (AlgorithmCompileException ex) {
                        // Nichts tun.
                    }
                    if (parsedParameter != null && parsedParameter.getType() != type) {
                        throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES, parsedParameter.getType().getValue(), type.getValue());
                    }

                    // 2. Versuch: Unterscheidung von zwei Fällen: die rechte Seite hat die formale Form "f(a_1, ..., a_n)". Dann wird versucht, 
                    // einen Algorithmusaufruf daraus zu kompilieren. Ansonsten wird der Fehler geworfen, dass das Symbol unbekannt ist.
                    boolean hasAlgorithmCallStructure = false;
                    try {
                        CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(rightSideReplaced);
                        if (doesAlgorithmWithGivenNameExists(algParseData.getName().getValue())) {
                            hasAlgorithmCallStructure = true;
                        }
                    } catch (AlgorithmCompileException ex) {
                        throw new AlgorithmCompileException(rightSideReplaced.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, rightSideReplaced.getValue());
                    }
                    if (hasAlgorithmCallStructure) {
                        return parseAlgorithmCall(scopeMemory, commands, rightSideReplaced, type, identifier, assignValueType);
                    } else {
                        throw new AlgorithmCompileException(rightSideReplaced.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, rightSideReplaced.getValue());
                    }
                }
            } else {
                try {
                    MalString malString = CompilerUtils.getMalString(rightSideReplaced, scopeMemory);
                    scopeMemory.put(identifierNameValue, identifier);
                    commands.add(new AssignValueCommand(identifier, malString, assignValueType, rightSide.getLineNumbers()));
                    return commands;
                } catch (AlgorithmCompileException e) {
                    // Inkompatibilität von Typen ist hier nicht möglich, da jeder abstrakte Ausdruck zugleich als String interpretiert werden kann.
                    // Unterscheidung von zwei Fällen: die rechte Seite hat die formale Form "f(a_1, ..., a_n)". Dann wird versucht, 
                    // einen Algorithmusaufruf daraus zu kompilieren. Ansonsten wird der Fehler geworfen, dass das Symbol unbekannt ist.
                    boolean hasAlgorithmCallStructure = false;
                    try {
                        CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(rightSideReplaced);
                        if (doesAlgorithmWithGivenNameExists(algParseData.getName().getValue())) {
                            hasAlgorithmCallStructure = true;
                        }
                    } catch (AlgorithmCompileException ex) {
                        throw new AlgorithmCompileException(rightSideReplaced.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, rightSideReplaced.getValue());
                    }
                    if (hasAlgorithmCallStructure) {
                        return parseAlgorithmCall(scopeMemory, commands, rightSideReplaced, type, identifier, assignValueType);
                    } else {
                        throw new AlgorithmCompileException(rightSideReplaced.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, rightSideReplaced.getValue());
                    }
                }
            }
        }

        throw new NotDesiredCommandException();

    }

    private static boolean doesAlgorithmWithGivenNameExists(String algName) {
        for (Signature signature : AlgorithmBuilder.ALGORITHM_SIGNATURES.getAlgorithmSignatureStorage()) {
            if (signature.getName().equals(algName)) {
                return true;
            }
        }
        return false;
    }

    private static int getPositionOfDefineCharIfIsAssignValueCommandIfValid(EditorCodeString line) {
        int wavyBracketCounter = 0, bracketCounter = 0, squareBracketCounter = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.BEGIN.getValue()) {
                wavyBracketCounter++;
            } else if (line.charAt(i) == ReservedChars.END.getValue()) {
                wavyBracketCounter--;
            } else if (line.charAt(i) == ReservedChars.OPEN_BRACKET.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.CLOSE_BRACKET.getValue()) {
                bracketCounter--;
            } else if (line.charAt(i) == ReservedChars.OPEN_SQUARE_BRACKET.getValue()) {
                squareBracketCounter++;
            } else if (line.charAt(i) == ReservedChars.CLOSE_SQUARE_BRACKET.getValue()) {
                squareBracketCounter--;
            }
            if (wavyBracketCounter == 0 && bracketCounter == 0 && squareBracketCounter == 0
                    && String.valueOf(line.charAt(i)).equals(Operators.DEFINE.getValue())) {
                /*
                Prüfung, ob das Zeichen an dieser Stelle nicht Teil eines Vergleichsoperators 
                ist, welcher "=" enthält. Im positiven Fall kann es keine Zuweisung mehr sein, 
                sondern höchstens eine Kontrollstruktur.
                 */
                for (ComparingOperators op : ComparingOperators.getOperatorsContainingEqualsSign()) {
                    if (line.substring(i - 1).startsWith(op.getValue())) {
                        return -1;
                    }
                }
                if (line.substring(i).startsWith(ComparingOperators.EQUALS.getValue())) {
                    return -1;
                }
                return i;
            }
        }
        return -1;
    }

    private static List<AlgorithmCommand> parseAlgorithmCall(AlgorithmMemory scopeMemory, List<AlgorithmCommand> commands,
            EditorCodeString rightSide, IdentifierType assignedIdentifierType, Identifier identifier, AssignValueType assignValueType) throws AlgorithmCompileException {
        // Kompatibilitätscheck
        Signature calledAlgSignature = getAlgorithmCallDataFromAlgorithmCall(rightSide, scopeMemory, assignedIdentifierType).getSignature();
        // Parameter auslesen;
        Identifier[] parameter = getParameterFromAlgorithmCall(rightSide, calledAlgSignature, commands, scopeMemory);
        scopeMemory.put(identifier.getName(), identifier);
        commands.add(new AssignValueCommand(identifier, calledAlgSignature, parameter, assignValueType, rightSide.getLineNumbers()));
        return commands;
    }

    private static Identifier[] getParameterFromAlgorithmCall(EditorCodeString input, Signature calledAlgSignature, List<AlgorithmCommand> commands, AlgorithmMemory scopeMemory)
            throws ParseAssignValueException {
        try {
            CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(input);
            EditorCodeString[] params = algParseData.getParameters();
            Identifier[] identifiers = new Identifier[params.length];
            for (int i = 0; i < params.length; i++) {
                // 1. Fall: der Parameter ist ein Bezeichner (welcher bereits in der Memory liegt).
                if (scopeMemory.get(params[i].getValue()) != null) {
                    identifiers[i] = scopeMemory.get(params[i].getValue());
                    continue;
                }
                // 2. Fall: der Parameter ist gültiger (abstrakter) Ausdruck vom geforderten Typ oder ein String.
                AbstractExpression argument = null;
                MalString malString = null;
                try {
                    switch (calledAlgSignature.getParameterTypes()[i]) {
                        case EXPRESSION:
                            argument = CompilerUtils.buildExpressionWithScopeMemory(params[i], VALIDATOR, scopeMemory);
                            // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                            CompilerUtils.checkIfAllIdentifiersAreDefined(params[i], argument.getContainedVars(), scopeMemory);
                            break;
                        case BOOLEAN_EXPRESSION:
                            argument = CompilerUtils.buildBooleanExpressionWithScopeMemory(params[i], VALIDATOR, scopeMemory);
                            // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                            CompilerUtils.checkIfAllIdentifiersAreDefined(params[i], argument.getContainedVars(), scopeMemory);
                            break;
                        case MATRIX_EXPRESSION:
                            argument = CompilerUtils.buildMatrixExpressionWithScopeMemory(params[i], VALIDATOR, scopeMemory);
                            // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                            CompilerUtils.checkIfAllIdentifiersAreDefined(params[i], argument.getContainedVars(), scopeMemory);
                            break;
                        case STRING:
                            malString = CompilerUtils.getMalString(params[i], scopeMemory);
                        default:
                            break;
                    }
                } catch (ExpressionException | BooleanExpressionException e) {
                    throw new AlgorithmCompileException(params[i].getLineNumbers(), e);
                }

                if (malString != null) {
                    String genVarName = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
                    Identifier genVarIdentifier = Identifier.createIdentifier(scopeMemory, genVarName, IdentifierType.STRING);
                    identifiers[i] = genVarIdentifier;
                    commands.add(new AssignValueCommand(genVarIdentifier, malString, AssignValueType.NEW, input.getLineNumbers()));
                    scopeMemory.addToMemoryInCompileTime(input.getLineNumbers(), genVarIdentifier);
                } else if (argument != null) {
                    String genVarName = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
                    Identifier genVarIdentifier = Identifier.createIdentifier(scopeMemory, genVarName, IdentifierType.identifierTypeOf(argument));
                    identifiers[i] = genVarIdentifier;
                    commands.add(new AssignValueCommand(genVarIdentifier, argument, AssignValueType.NEW, input.getLineNumbers()));
                    scopeMemory.addToMemoryInCompileTime(input.getLineNumbers(), genVarIdentifier);
                } else {
                    throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, params[i].getValue());
                }
            }
            return identifiers;
        } catch (AlgorithmCompileException e) {
            throw new ParseAssignValueException(e);
        }
    }

    private static AlgorithmCallData getAlgorithmCallDataFromAlgorithmCall(EditorCodeString input, AlgorithmMemory scopeMemory, IdentifierType returnType) throws ParseAssignValueException {
        try {
            CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(input);
            EditorCodeString algName = algParseData.getName();
            EditorCodeString[] params = algParseData.getParameters();

            ParameterData[] paramValues = new ParameterData[params.length];
            for (int i = 0; i < params.length; i++) {
                paramValues[i] = CompilerUtils.parseParameterWithoutType(params[i], VALIDATOR, scopeMemory);
            }

            // Prüfung, ob ein Algorithmus mit diesem Namen bekannt ist.
            Signature algorithmCandidate = null;
            boolean candidateFound;
            for (Signature signature : AlgorithmBuilder.ALGORITHM_SIGNATURES.getAlgorithmSignatureStorage()) {
                if (signature.getName().equals(algName.getValue()) && signature.getParameterTypes().length == params.length) {
                    candidateFound = true;
                    for (int i = 0; i < params.length; i++) {
                        if (paramValues[i].getType() == IdentifierType.STRING) {
                            if (IdentifierType.STRING != signature.getParameterTypes()[i]) {
                                candidateFound = false;
                            }
                        } else if (!areAllVarsContainedInMemory(((AbstractExpression) paramValues[i].getValue()).getContainedVars(), scopeMemory)
                                || !signature.getParameterTypes()[i].isSameOrSuperTypeOf(IdentifierType.identifierTypeOf(paramValues[i].getValue()))) {
                            candidateFound = false;
                        }
                    }
                    if (candidateFound) {
                        // Alle Parameter sind damit automatisch gültige Bezeichner sind.
                        algorithmCandidate = signature;
                        break;
                    }
                }
            }
            if (algorithmCandidate == null) {
                throw new ParseAssignValueException(input.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, algName.getValue());
            }

            // Prüfung, ob Rückgabewert korrekt ist.
            if (!returnType.isSameOrSuperTypeOf(algorithmCandidate.getReturnType())) {
                throw new ParseAssignValueException(input.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES, algorithmCandidate.getReturnType(), returnType);
            }
            return new AlgorithmCallData(algorithmCandidate, paramValues);
        } catch (AlgorithmCompileException e) {
            throw new ParseAssignValueException(e);
        }
    }

    private static boolean areAllVarsContainedInMemory(Set<String> varNames, AlgorithmMemory memory) {
        for (String varName : varNames) {
            if (memory.get(varName) == null) {
                return false;
            }
        }
        return true;
    }

    private static List<AlgorithmCommand> parseVoidCommand(EditorCodeString line, AlgorithmMemory scopeMemory) throws AlgorithmCompileException, NotDesiredCommandException {

        // Falls Unteralgorithmenausrufe vorhanden sind, so müssen diese in separate Variablen ausgelagert werden.
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(line, scopeMemory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        EditorCodeString lineReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        // Struktur des Aufrufs ermitteln.
        String algName = null;
        int numberOfParameters;
        try {
            CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(lineReplaced);
            algName = algParseData.getName().getValue();
            numberOfParameters = algParseData.getParameters().length;
        } catch (AlgorithmCompileException e) {
            throw new NotDesiredCommandException();
        }

        // Auf 1. vom Benutzer definierte Algorithmen und 2. auf Standardalgorithmen prüfen.
        boolean algorithmWithRequiredNameFound = false;
        boolean algorithmWithRequiredNameAndCorrectNumberOfParametersFound = false;
        for (Signature sgn : AlgorithmBuilder.ALGORITHM_SIGNATURES.getAlgorithmSignatureStorage()) {
            if (sgn.getName().equals(algName)) {
                algorithmWithRequiredNameFound = true;
            } else {
                continue;
            }
            if (sgn.getParameterTypes().length == numberOfParameters) {
                algorithmWithRequiredNameAndCorrectNumberOfParametersFound = true;
                try {
                    Identifier[] parameters = getParameterFromAlgorithmCall(line, sgn, commands, scopeMemory);
                    commands.add(new VoidCommand(algName, parameters));
                    return commands;
                } catch (ParseAssignValueException e) {
                }
            }
        }

        if (algorithmWithRequiredNameAndCorrectNumberOfParametersFound) {
            // Es gab einen Algorithmus mit dem richtigen Namen und der korrekten Anzahl von Parametern.
            // Es lag also ein Kompilierfehler bein den Parametern vor.
            throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_SOME_PARAMETER_COULD_NOT_BE_PARSED_IN_COMMAND, algName);
        }
        if (algorithmWithRequiredNameFound) {
            // Es gab zwar einen Algorithmus mit dem richtigen Namen, nur entweder waren die Parameter falsch oder die Signatur war unpassend.
            throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_WRONG_NUMBER_OF_PARAMETERS_IN_COMMAND, algName);
        }
        // Ein Algorithmus mit entsprechendem Namen wurde nicht gefunden.
        throw new AlgorithmCompileException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_NO_SUCH_COMMAND, algName);
    }

    private static List<AlgorithmCommand> parseControlStructure(EditorCodeString line, AlgorithmMemory memory, Algorithm alg, boolean keywordsAllowed) throws AlgorithmCompileException, NotDesiredCommandException {
        // If-Else-Block
        try {
            return parseIfElseControlStructure(line, memory, alg, keywordsAllowed);
        } catch (ParseControlStructureException e) {
            /*
            Es ist zwar eine If-Else-Struktur mit korrekter Bedingung, aber der
            innere Block kann nicht kompiliert werden.
             */
            throw e;
        } catch (BooleanExpressionException e) {
            // Es ist zwar eine If-Else-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(line.getLineNumbers(), e);
        } catch (NotDesiredCommandException e) {
            // Keine If-Else-Struktur. Also weiter.
        }

        // For-Block
        try {
            return parseForControlStructure(line, memory, alg);
        } catch (ParseControlStructureException e) {
            /*
            Es ist zwar eine While-Struktur mit korrekter Bedingung, aber der
            innere Block kann nicht kompiliert werden.
             */
            throw e;
        } catch (BooleanExpressionException e) {
            // Es ist zwar eine While-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(line.getLineNumbers(), e);
        } catch (NotDesiredCommandException e) {
            // Keine While-Struktur. Also weiter.
        }

        // While-Block
        try {
            return parseWhileControlStructure(line, memory, alg);
        } catch (ParseControlStructureException e) {
            /*
            Es ist zwar eine While-Struktur mit korrekter Bedingung, aber der
            innere Block kann nicht kompiliert werden.
             */
            throw e;
        } catch (BooleanExpressionException e) {
            // Es ist zwar eine While-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(line.getLineNumbers(), e);
        } catch (NotDesiredCommandException e) {
            // Keine While-Struktur. Also weiter.
        }

        // Do-While-Block
        try {
            return parseDoWhileControlStructure(line, memory, alg);
        } catch (ParseControlStructureException e) {
            /*
            Es ist zwar eine Do-While-Struktur mit korrekter Bedingung, aber der
            innere Block kann nicht kompiliert werden.
             */
            throw e;
        } catch (BooleanExpressionException e) {
            // Es ist zwar eine While-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(line.getLineNumbers(), e);
        } catch (NotDesiredCommandException e) {
            // Keine Do-While-Struktur. Also weiter.
        }

        throw new NotDesiredCommandException();
    }

    private static List<AlgorithmCommand> parseIfElseControlStructure(EditorCodeString line, AlgorithmMemory memory, Algorithm alg, boolean keywordsAllowed)
            throws AlgorithmCompileException, BooleanExpressionException, NotDesiredCommandException {

        if (!line.startsWith(Keyword.IF.getValue() + ReservedChars.OPEN_BRACKET.getValue())) {
            throw new NotDesiredCommandException();
        }

        int bracketCounter = 1;
        int endOfBooleanCondition = -1;
        for (int i = (Keyword.IF.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(); i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.OPEN_BRACKET.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.CLOSE_BRACKET.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endOfBooleanCondition = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(line.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        EditorCodeString booleanConditionString = line.substring((Keyword.IF.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(), endOfBooleanCondition);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression condition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(booleanConditionString, memory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        EditorCodeString booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        condition = CompilerUtils.buildBooleanExpressionWithScopeMemory(booleanConditionReplaced, VALIDATOR, memory);
        CompilerUtils.checkIfAllIdentifiersAreDefined(booleanConditionString, condition.getContainedVars(), memory);

        // Prüfung, ob line mit "if(boolsche Bedingung){ ..." beginnt.
        if (!line.contains(ReservedChars.BEGIN.getStringValue())
                || !line.contains(ReservedChars.END.getStringValue())
                || line.indexOf(ReservedChars.BEGIN.getValue()) > endOfBooleanCondition + 1) {
            EditorCodeString incorrectPartOfLine = line.substring(endOfBooleanCondition + 1);
            throw new ParseControlStructureException(incorrectPartOfLine.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_MUST_CONTAIN_BEGIN_AND_END,
                    ReservedChars.BEGIN.getValue(), ReservedChars.END.getValue());
        }

        // Block im If-Teil kompilieren.
        bracketCounter = 0;
        int beginBlockPosition = line.indexOf(ReservedChars.BEGIN.getValue()) + 1;
        int endBlockPosition = -1;
        for (int i = line.indexOf(ReservedChars.BEGIN.getValue()); i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.BEGIN.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.END.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endBlockPosition = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(line.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }

        AlgorithmMemory memoryBeforeIfElsePart = memory.copyMemory();

        List<AlgorithmCommand> commandsIfPart;
        if (keywordsAllowed) {
            commandsIfPart = parseConnectedBlockWithKeywords(line.substring(beginBlockPosition, endBlockPosition), memoryBeforeIfElsePart, alg);
        } else {
            commandsIfPart = parseConnectedBlockWithoutKeywords(line.substring(beginBlockPosition, endBlockPosition), memoryBeforeIfElsePart, alg);
        }
        IfElseControlStructure ifElseControlStructure = new IfElseControlStructure(condition, commandsIfPart);

        // Block im Else-Teil kompilieren, falls vorhanden.
        if (endBlockPosition == line.length() - 1) {
            // Kein Else-Teil vorhanden.
            return Collections.singletonList((AlgorithmCommand) ifElseControlStructure);
        }

        EditorCodeString restLine = line.substring(endBlockPosition + 1);
        if (!restLine.startsWith(Keyword.ELSE.getValue() + ReservedChars.BEGIN.getValue())) {
            throw new ParseControlStructureException(restLine.firstChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_EXPECTED, Keyword.ELSE.getValue() + ReservedChars.BEGIN.getStringValue());
        }

        bracketCounter = 0;
        beginBlockPosition = restLine.indexOf(ReservedChars.BEGIN.getValue()) + 1;
        endBlockPosition = -1;
        for (int i = restLine.indexOf(ReservedChars.BEGIN.getValue()); i < restLine.length(); i++) {
            if (restLine.charAt(i) == ReservedChars.BEGIN.getValue()) {
                bracketCounter++;
            } else if (restLine.charAt(i) == ReservedChars.END.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endBlockPosition = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(restLine.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }
        if (endBlockPosition != restLine.length() - 1) {
            EditorCodeString incorrestRestLine = restLine.substring(endBlockPosition);
            throw new ParseControlStructureException(incorrestRestLine.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, line.substring(endBlockPosition + 1).getValue());
        }

        List<AlgorithmCommand> commandsElsePart;
        if (keywordsAllowed) {
            commandsElsePart = parseConnectedBlockWithKeywords(restLine.substring(beginBlockPosition, endBlockPosition), memoryBeforeIfElsePart, alg);
        } else {
            commandsElsePart = parseConnectedBlockWithoutKeywords(restLine.substring(beginBlockPosition, endBlockPosition), memoryBeforeIfElsePart, alg);
        }
        ifElseControlStructure.setCommandsElsePart(commandsElsePart);

        commands.add(ifElseControlStructure);
        return commands;
    }

    private static List<AlgorithmCommand> parseWhileControlStructure(EditorCodeString line, AlgorithmMemory memory, Algorithm alg)
            throws AlgorithmCompileException, BooleanExpressionException, NotDesiredCommandException {

        if (!line.startsWith(Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getValue())) {
            throw new NotDesiredCommandException();
        }

        int bracketCounter = 1;
        int endOfBooleanCondition = -1;
        for (int i = (Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(); i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.OPEN_BRACKET.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.CLOSE_BRACKET.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endOfBooleanCondition = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(line.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        EditorCodeString booleanConditionString = line.substring((Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(), endOfBooleanCondition);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression condition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(booleanConditionString, memory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        EditorCodeString booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        condition = CompilerUtils.buildBooleanExpressionWithScopeMemory(booleanConditionReplaced, VALIDATOR, memory);
        CompilerUtils.checkIfAllIdentifiersAreDefined(booleanConditionString, condition.getContainedVars(), memory);

        // Prüfung, ob line mit "while(boolsche Bedingung){ ..." beginnt.
        if (!line.contains(ReservedChars.BEGIN.getStringValue())
                || !line.contains(ReservedChars.END.getStringValue())
                || line.indexOf(ReservedChars.BEGIN.getValue()) > endOfBooleanCondition + 1) {
            EditorCodeString incorrectPartOfLine = line.substring(endOfBooleanCondition + 1);
            throw new ParseControlStructureException(incorrectPartOfLine.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_MUST_CONTAIN_BEGIN_AND_END,
                    ReservedChars.BEGIN.getValue(), ReservedChars.END.getValue());
        }

        // Block im While-Teil kompilieren.
        bracketCounter = 0;
        int beginBlockPosition = line.indexOf(ReservedChars.BEGIN.getValue()) + 1;
        int endBlockPosition = -1;
        for (int i = line.indexOf(ReservedChars.BEGIN.getValue()); i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.BEGIN.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.END.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endBlockPosition = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(line.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }

        AlgorithmMemory memoryBeforWhileLoop = memory.copyMemory();

        List<AlgorithmCommand> commandsWhilePart = parseConnectedBlockWithKeywords(line.substring(beginBlockPosition, endBlockPosition), memoryBeforWhileLoop, alg);
        WhileControlStructure whileControlStructure = new WhileControlStructure(condition, commandsWhilePart);

        // '}' muss als letztes Zeichen stehen, sonst ist die Struktur nicht korrekt.
        if (endBlockPosition == line.length() - 1) {
            whileControlStructure.getCommands().addAll(commands);
            commands.add(whileControlStructure);
            return commands;
        }

        EditorCodeString incorrectRestLine = line.substring(endBlockPosition + 1);
        throw new ParseControlStructureException(incorrectRestLine.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, incorrectRestLine.getValue());
    }

    private static List<AlgorithmCommand> parseDoWhileControlStructure(EditorCodeString line, AlgorithmMemory memory, Algorithm alg)
            throws AlgorithmCompileException, BooleanExpressionException, NotDesiredCommandException {

        if (!line.startsWith(Keyword.DO.getValue() + ReservedChars.BEGIN.getValue())) {
            throw new NotDesiredCommandException();
        }

        // Block im Do-Teil kompilieren.
        int bracketCounter = 1;
        int beginBlockPosition = line.indexOf(ReservedChars.BEGIN.getValue()) + 1;
        int endBlockPosition = -1;
        for (int i = beginBlockPosition; i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.BEGIN.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.END.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endBlockPosition = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }

        AlgorithmMemory memoryBeforWhileLoop = memory.copyMemory();

        List<AlgorithmCommand> commandsDoPart = parseConnectedBlockWithKeywords(line.substring(beginBlockPosition, endBlockPosition), memoryBeforWhileLoop, alg);

        // While-Bedingung kompilieren
        EditorCodeString whilePart = line.substring(endBlockPosition + 1);
        if (!whilePart.startsWith(Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getValue())) {
            throw new ParseControlStructureException(whilePart.getLineNumbers(), AlgorithmCompileExceptionIds.AC_EXPECTED, Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getStringValue());
        }
        if (!whilePart.endsWith(String.valueOf(ReservedChars.CLOSE_BRACKET.getValue()))) {
            throw new ParseControlStructureException(whilePart.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        EditorCodeString booleanConditionString = line.substring(endBlockPosition + Keyword.WHILE.getValue().length() + 2, line.length() - 1);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression condition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(booleanConditionString, memory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        EditorCodeString booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        condition = CompilerUtils.buildBooleanExpressionWithScopeMemory(booleanConditionReplaced, VALIDATOR, memory);
        CompilerUtils.checkIfAllIdentifiersAreDefined(booleanConditionString, condition.getContainedVars(), memory);

        DoWhileControlStructure doWhileControlStructure = new DoWhileControlStructure(commandsDoPart, condition);
        doWhileControlStructure.getCommands().addAll(commands);
        commands.add(doWhileControlStructure);
        return commands;
    }

    private static List<AlgorithmCommand> parseForControlStructure(EditorCodeString line, AlgorithmMemory memory, Algorithm alg)
            throws AlgorithmCompileException, BooleanExpressionException, NotDesiredCommandException {

        if (!line.startsWith(Keyword.FOR.getValue() + ReservedChars.OPEN_BRACKET.getValue())) {
            throw new NotDesiredCommandException();
        }

        int bracketCounter = 1;
        int endOfForControlPart = -1;
        for (int i = (Keyword.FOR.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(); i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.OPEN_BRACKET.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.CLOSE_BRACKET.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endOfForControlPart = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(line.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        EditorCodeString forControlString = line.substring((Keyword.FOR.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(), endOfForControlPart);

        AlgorithmMemory currentMemory = memory.copyMemory();

        // Die drei for-Anweisungen kompilieren.
        EditorCodeString[] forControlParts = CompilerUtils.splitByKomma(forControlString);

        // Es müssen genau 3 Befehle in der For-Struktur stehen.
        if (forControlParts.length < 3) {
            throw new ParseControlStructureException(forControlParts[forControlParts.length - 1].getLineNumbers(), AlgorithmCompileExceptionIds.AC_EXPECTED, ReservedChars.ARGUMENT_SEPARATOR.getValue());
        }
        if (forControlParts.length > 3) {
            throw new ParseControlStructureException(forControlParts[forControlParts.length - 1].getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        List<AlgorithmCommand> initialization = parseAssignValueCommand(forControlParts[0], currentMemory);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression endLoopCondition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(forControlParts[1], currentMemory);
        List<AlgorithmCommand> commandsEndLoopCondition = algorithmCommandReplacementList.getCommands();
        EditorCodeString booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        endLoopCondition = CompilerUtils.buildBooleanExpressionWithScopeMemory(booleanConditionReplaced, VALIDATOR, currentMemory);
        CompilerUtils.checkIfAllIdentifiersAreDefined(forControlParts[1], endLoopCondition.getContainedVars(), currentMemory);

        AlgorithmMemory memoryBeforeLoop = currentMemory.copyMemory();

        List<AlgorithmCommand> loopAssignment = parseAssignValueCommand(forControlParts[2], currentMemory);
        // Prüfung, ob bei loopAssignment keine weiteren Bezeichner hinzukamen, außer den Technischen.
        checkIfNewIdentifierOccur(forControlParts[2], memoryBeforeLoop, currentMemory);

        // Prüfung, ob line mit "for(a,b,c){ ..." beginnt.
        if (!line.contains(ReservedChars.BEGIN.getStringValue())
                || !line.contains(ReservedChars.END.getStringValue())
                || line.indexOf(ReservedChars.BEGIN.getValue()) > endOfForControlPart + 1) {
            EditorCodeString incorrectPartOfLine = line.substring(endOfForControlPart + 1);
            throw new ParseControlStructureException(incorrectPartOfLine.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_MUST_CONTAIN_BEGIN_AND_END,
                    ReservedChars.BEGIN.getValue(), ReservedChars.END.getValue());
        }

        // Block im For-Teil kompilieren.
        bracketCounter = 0;
        int beginBlockPosition = line.indexOf(ReservedChars.BEGIN.getValue()) + 1;
        int endBlockPosition = -1;
        for (int i = line.indexOf(ReservedChars.BEGIN.getValue()); i < line.length(); i++) {
            if (line.charAt(i) == ReservedChars.BEGIN.getValue()) {
                bracketCounter++;
            } else if (line.charAt(i) == ReservedChars.END.getValue()) {
                bracketCounter--;
            }
            if (bracketCounter == 0) {
                endBlockPosition = i;
                break;
            }
        }
        if (bracketCounter > 0) {
            throw new ParseControlStructureException(line.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }
        List<AlgorithmCommand> commandsForPart = parseConnectedBlockWithKeywords(line.substring(beginBlockPosition, endBlockPosition), currentMemory, alg);
        ForControlStructure forControlStructure = new ForControlStructure(commandsForPart, initialization, commandsEndLoopCondition, endLoopCondition, loopAssignment);

        // Lokale Variable aus dem Speicher memory wieder herausnehmen.
        // '}' muss als letztes Zeichen stehen, sonst ist die Struktur nicht korrekt.
        if (endBlockPosition == line.length() - 1) {
            return Collections.singletonList(forControlStructure);
        }

        EditorCodeString incorrectRestLine = line.substring(endBlockPosition + 1);
        throw new ParseControlStructureException(incorrectRestLine.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, incorrectRestLine.getValue());
    }

    private static void checkIfNewIdentifierOccur(EditorCodeString loopAssignment, AlgorithmMemory memoryBeforeLoop, AlgorithmMemory currentMemory) throws ParseControlStructureException {
        for (String identifierName : currentMemory.keySet()) {
            if (!memoryBeforeLoop.containsKey(identifierName) && !CompilerUtils.isTechnicalIdentifierName(identifierName)) {
                throw new ParseControlStructureException(loopAssignment.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_FOR_NEW_IDENTIFIER_NOT_ALLOWED, identifierName);
            }
        }
    }

    private static List<AlgorithmCommand> parseKeywordCommand(EditorCodeString line, boolean keywordsAllowed) throws AlgorithmCompileException, NotDesiredCommandException {
        if (line.getValue().equals(Keyword.BREAK.toString())) {
            if (keywordsAllowed) {
                return Collections.singletonList(new KeywordCommand(Keyword.BREAK));
            }
            throw new ParseKeywordException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_KEYWORD_NOT_ALLOWED_HERE, Keyword.BREAK);
        }
        if (line.getValue().equals(Keyword.CONTINUE.toString())) {
            if (keywordsAllowed) {
                return Collections.singletonList(new KeywordCommand(Keyword.CONTINUE));
            }
            throw new ParseKeywordException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_KEYWORD_NOT_ALLOWED_HERE, Keyword.CONTINUE);
        }
        throw new NotDesiredCommandException();
    }

    private static List<AlgorithmCommand> parseReturnCommand(EditorCodeString line, AlgorithmMemory scopeMemory, Algorithm alg) throws AlgorithmCompileException, NotDesiredCommandException {
        if (line.startsWith(Keyword.RETURN.getValue())) {
            if (line.getValue().equals(Keyword.RETURN.getValue())) {
                if (alg.getReturnType() != null) {
                    throw new ParseReturnException(line.getLineNumbers(), AlgorithmCompileExceptionIds.AC_MISSING_RETURN_VALUE);
                }
                return Collections.singletonList((AlgorithmCommand) new ReturnCommand(Identifier.NULL_IDENTIFIER));
            }
            EditorCodeString returnValueCandidate = line.substring((Keyword.RETURN.getValue() + " ").length());

            // Void-Algorithmen dürfen nichts zurückgeben.
            if (alg.getReturnType() == null) {
                throw new ParseReturnException(returnValueCandidate.getLineNumbers(), AlgorithmCompileExceptionIds.AC_RETURN_VALUE_IN_VOID_ALGORITHM_NOT_ALLOWED);
            }

            // Klammern links und rechts um den Rückgabewert entfernen.
            while (returnValueCandidate.startsWith(ReservedChars.OPEN_BRACKET.getStringValue()) && returnValueCandidate.endsWith(ReservedChars.CLOSE_BRACKET.getStringValue())) {
                returnValueCandidate = returnValueCandidate.substring(1, returnValueCandidate.length() - 1);
            }

            if (scopeMemory.get(returnValueCandidate.getValue()) == null) {

                AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(returnValueCandidate, scopeMemory);
                List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
                EditorCodeString returnValueReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

                if (scopeMemory.get(returnValueReplaced.getValue()) != null) {
                    return Collections.singletonList((AlgorithmCommand) new ReturnCommand(scopeMemory.get(returnValueCandidate.getValue())));
                }
                if (VALIDATOR.isValidKnownIdentifier(returnValueReplaced.getValue(), alg.getReturnType().getClassOf(), CompilerUtils.extractClassesOfAbstractExpressionIdentifiersFromMemory(scopeMemory))) {
                    throw new ParseReturnException(returnValueCandidate.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, returnValueCandidate.getValue());
                }
                String genVarForReturn = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
                EditorCodeString assignValueCommand = EditorCodeString.createEditorCodeStringWithGivenLineNumber(alg.getReturnType().toString() + " " + genVarForReturn + "=" + returnValueReplaced.getValue(),
                        line.getLineNumbers()[0]);
                List<AlgorithmCommand> additionalCommandsByAssignment = parseAssignValueCommand(assignValueCommand, scopeMemory);
                commands.addAll(additionalCommandsByAssignment);
                commands.add(new ReturnCommand(Identifier.createIdentifier(scopeMemory, genVarForReturn, alg.getReturnType())));
                return commands;
            } else {
                IdentifierType type = scopeMemory.get(returnValueCandidate.getValue()).getType();
                if (!alg.getReturnType().isSameOrSuperTypeOf(type)) {
                    throw new ParseReturnException(returnValueCandidate.getLineNumbers(), AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES, type, alg.getReturnType());
                }
                return Collections.singletonList((AlgorithmCommand) new ReturnCommand(Identifier.createIdentifier(scopeMemory, returnValueCandidate.getValue(), alg.getReturnType())));
            }
        }
        throw new NotDesiredCommandException();
    }

    public static List<AlgorithmCommand> parseConnectedBlockWithKeywords(EditorCodeString input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, true, true);
    }

    public static List<AlgorithmCommand> parseConnectedBlockWithoutKeywords(EditorCodeString input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, true, false);
    }

    public static List<AlgorithmCommand> parseBlockWithKeywords(EditorCodeString input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, false, true);
    }

    public static List<AlgorithmCommand> parseBlockWithoutKeywords(EditorCodeString input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, false, false);
    }

    private static List<AlgorithmCommand> parseCommandBlock(EditorCodeString input, AlgorithmMemory memory, Algorithm alg, boolean connectedBlock, boolean keywordsAllowed) throws AlgorithmCompileException {
        if (!input.isEmpty() && !input.endsWith(String.valueOf(ReservedChars.LINE_SEPARATOR.getValue()))) {
            throw new AlgorithmCompileException(input.lastChar().getLineNumbers(), AlgorithmCompileExceptionIds.AC_MISSING_LINE_SEPARATOR, ReservedChars.LINE_SEPARATOR.getValue());
        }

        AlgorithmMemory memoryInsideBlock;
        if (connectedBlock) {
            memoryInsideBlock = memory.copyMemory();
        } else {
            memoryInsideBlock = memory;
        }

        List<EditorCodeString> linesAsList = new ArrayList<>();
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
            if (wavedBracketCounter == 0 && squareBracketCounter == 0 && input.charAt(i) == ReservedChars.LINE_SEPARATOR.getValue()) {
                endBlockPosition = i;
                linesAsList.add(input.substring(beginBlockPosition, endBlockPosition));
                beginBlockPosition = i + 1;
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
        if (endBlockPosition != input.length() - 1) {
            EditorCodeString incorrectRestInput = input.substring(endBlockPosition);
            throw new AlgorithmCompileException(incorrectRestInput.getLineNumbers(), AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, incorrectRestInput.getValue());
        }

        EditorCodeString[] lines = linesAsList.toArray(new EditorCodeString[linesAsList.size()]);

        List<AlgorithmCommand> commands = new ArrayList<>();
        List<AlgorithmCommand> parsedLine;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                parsedLine = parseLine(lines[i], memoryInsideBlock, alg, keywordsAllowed);
                commands.addAll(parsedLine);
                // Nach dem Kompilieren jeder Kommandozeile Plausibilitätschecks durchführen.
                // Prüfung, ob es bei (beliebigen) Algorithmen keinen Code hinter einem Return gibt.
                CompilerUtils.checkForUnreachableCodeInBlock(lines[i], commands, alg);
            }
        }
        return commands;
    }

    ///////////////////// Methoden für die Zerlegung eines Ausdrucks, welcher Algorithmenaufrufe enthält, in mehrere Befehle ///////////////////////
    private static AlgorithmCommandReplacementData decomposeAssignmentInvolvingAlgorithmCalls(EditorCodeString input, AlgorithmMemory memory) {
        EditorCodeString inputWithGeneratedVars = input;
        List<AlgorithmCommand> commands = new ArrayList<>();

        boolean algorithmCallFound;
        EditorCodeString algorithmCallAsString;
        List<Integer> beginningAlgCall;
        int endingAlgCall;
        do {
            algorithmCallFound = false;
            for (Signature signature : AlgorithmBuilder.ALGORITHM_SIGNATURES.getAlgorithmSignatureStorage()) {
                if (!inputWithGeneratedVars.contains(signature.getName())) {
                    continue;
                }
                beginningAlgCall = getListWithIndicesOfAlgorithmStart(inputWithGeneratedVars, signature.getName());
                for (Integer index : beginningAlgCall) {
                    for (int i = index + signature.getName().length(); i < input.length() + 1; i++) {
                        algorithmCallAsString = inputWithGeneratedVars.substring(index, i);

                        // Der Algorithmusaufruf darf nicht der gesamte input sein.
                        if (algorithmCallAsString.length() == inputWithGeneratedVars.length()) {
                            return new AlgorithmCommandReplacementData(new ArrayList<>(), input);
                        }

                        AlgorithmCallData algorithmCallData = null;
                        try {
                            algorithmCallData = getAlgorithmCallDataFromAlgorithmCall(algorithmCallAsString, memory, IdentifierType.EXPRESSION);
                        } catch (ParseAssignValueException e) {
                        }
                        if (algorithmCallData == null) {
                            try {
                                algorithmCallData = getAlgorithmCallDataFromAlgorithmCall(algorithmCallAsString, memory, IdentifierType.BOOLEAN_EXPRESSION);
                            } catch (ParseAssignValueException e) {
                            }
                        }
                        if (algorithmCallData == null) {
                            try {
                                algorithmCallData = getAlgorithmCallDataFromAlgorithmCall(algorithmCallAsString, memory, IdentifierType.MATRIX_EXPRESSION);
                            } catch (ParseAssignValueException e) {
                            }
                        }
                        if (algorithmCallData == null) {
                            try {
                                algorithmCallData = getAlgorithmCallDataFromAlgorithmCall(algorithmCallAsString, memory, IdentifierType.STRING);
                            } catch (ParseAssignValueException e) {
                            }
                        }

                        if (algorithmCallData == null) {
                            if (i < input.length()) {
                                continue;
                            } else {
                                algorithmCallFound = false;
                                break;
                            }
                        }

                        endingAlgCall = i;
                        algorithmCallFound = true;

                        try {
                            inputWithGeneratedVars = addAssignValueCommandsForNonVarAlgorithmParameters(inputWithGeneratedVars, index, endingAlgCall, algorithmCallData, commands, memory);
                            algorithmCallFound = true;
                            break;
                        } catch (ParseAssignValueException e) {
                        }

                    }
                    if (algorithmCallFound) {
                        break;
                    }
                }

            }
        } while (algorithmCallFound);

        return new AlgorithmCommandReplacementData(commands, inputWithGeneratedVars);
    }

    private static List<Integer> getListWithIndicesOfAlgorithmStart(EditorCodeString input, String algName) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < input.length() - algName.length(); i++) {
            if (input.substring(i).startsWith(algName)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static EditorCodeString addAssignValueCommandsForNonVarAlgorithmParameters(EditorCodeString input, int beginningAlgCall, int endingAlgCall, AlgorithmCallData algorithmCallData,
            List<AlgorithmCommand> commands, AlgorithmMemory scopeMemory) throws ParseAssignValueException {

        Identifier[] inputParameters = new Identifier[algorithmCallData.getParameterValues().length];
        ParameterData parameter;
        for (int i = 0; i < algorithmCallData.getParameterValues().length; i++) {
            parameter = algorithmCallData.getParameterValues()[i];
            if (parameter.getType() == IdentifierType.EXPRESSION && parameter.getValue() instanceof Variable) {
                inputParameters[i] = Identifier.createIdentifier(scopeMemory, ((Variable) parameter.getValue()).getName(), IdentifierType.identifierTypeOf((Expression) parameter.getValue()));
            } else if (parameter.getType() == IdentifierType.BOOLEAN_EXPRESSION && parameter.getValue() instanceof BooleanVariable) {
                inputParameters[i] = Identifier.createIdentifier(scopeMemory, ((BooleanVariable) parameter.getValue()).getName(), IdentifierType.identifierTypeOf((BooleanExpression) parameter.getValue()));
            } else if (parameter.getType() == IdentifierType.MATRIX_EXPRESSION && parameter.getValue() instanceof MatrixVariable) {
                inputParameters[i] = Identifier.createIdentifier(scopeMemory, ((MatrixVariable) parameter.getValue()).getName(), IdentifierType.identifierTypeOf((MatrixExpression) parameter.getValue()));
            } else {
                String genVarName = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
                try {
                    Identifier genVarIdentifier = Identifier.createIdentifier(scopeMemory, genVarName, algorithmCallData.getSignature().getParameterTypes()[i]);
                    inputParameters[i] = genVarIdentifier;
                    commands.add(new AssignValueCommand(genVarIdentifier, parameter.getValue(), AssignValueType.NEW, input.getLineNumbers()));
                    scopeMemory.addToMemoryInCompileTime(input.getLineNumbers(), genVarIdentifier);
                } catch (AlgorithmCompileException e) {
                    throw new ParseAssignValueException(e);
                }
            }
        }

        String genVarNameForCalledAlg = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
        Identifier identifierForCalledAlg = Identifier.createIdentifier(scopeMemory, genVarNameForCalledAlg, algorithmCallData.getSignature().getReturnType());
        try {
            commands.add(new AssignValueCommand(identifierForCalledAlg, algorithmCallData.getSignature(), inputParameters, AssignValueType.NEW, input.getLineNumbers()));
            scopeMemory.addToMemoryInCompileTime(input.getLineNumbers(), identifierForCalledAlg);
        } catch (AlgorithmCompileException e) {
            throw new ParseAssignValueException(e);
        }

        EditorCodeString algorithmCallString = input.substring(beginningAlgCall, endingAlgCall);
        EditorCodeString result = input;
        while (result.contains(algorithmCallString.getValue())) {
            result = result.replaceFirst(algorithmCallString.getValue(), genVarNameForCalledAlg);
        }
        return result;
    }

}
