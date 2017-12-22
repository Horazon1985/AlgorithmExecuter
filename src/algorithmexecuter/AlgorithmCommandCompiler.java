package algorithmexecuter;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixVariable;
import static algorithmexecuter.AlgorithmCompiler.VALIDATOR;
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
import algorithmexecuter.exceptions.BlockCompileException;
import algorithmexecuter.exceptions.BooleanExpressionException;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.exceptions.DeclareIdentifierException;
import algorithmexecuter.exceptions.NotDesiredCommandException;
import algorithmexecuter.exceptions.ParseAssignValueException;
import algorithmexecuter.exceptions.ParseControlStructureException;
import algorithmexecuter.exceptions.ParseKeywordException;
import algorithmexecuter.exceptions.ParseReturnException;
import algorithmexecuter.exceptions.ParseVoidException;
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
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.model.utilclasses.ParameterData;
import exceptions.ExpressionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AlgorithmCommandCompiler {

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
    private static List<AlgorithmCommand> parseLine(String line, AlgorithmMemory memory, Algorithm alg, boolean keywordsAllowed) throws AlgorithmCompileException {
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

        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_COMMAND_COUND_NOT_BE_PARSED, line);
    }

    private static List<AlgorithmCommand> parseDeclareIdentifierCommand(String line, AlgorithmMemory scopeMemory) throws AlgorithmCompileException, NotDesiredCommandException {
        // Typ ermitteln. Dieser ist != null, wenn es sich definitiv um eine Identifierdeklaration handelt.
        IdentifierType type = getTypeIfIsValidDeclareIdentifierCommand(line);
        if (type == null) {
            throw new NotDesiredCommandException();
        }

        String identifierName = line.substring(type.toString().length() + 1);
        if (!VALIDATOR.isValidIdentifier(identifierName)) {
            throw new DeclareIdentifierException(AlgorithmCompileExceptionIds.AC_ILLEGAL_CHARACTER, identifierName);
        }
        // Prüfung, ob dieser Identifier bereits existiert.
        if (scopeMemory.containsIdentifier(identifierName)) {
            throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, identifierName);
        }
        Identifier identifier = Identifier.createIdentifier(scopeMemory, identifierName, type);
        scopeMemory.put(identifierName, identifier);
        return Collections.singletonList((AlgorithmCommand) new DeclareIdentifierCommand(identifier));
    }

    private static IdentifierType getTypeIfIsValidDeclareIdentifierCommand(String line) {
        for (IdentifierType type : IdentifierType.values()) {
            if (line.startsWith(type.toString() + " ")) {
                return type;
            }
        }
        return null;
    }

    private static List<AlgorithmCommand> parseAssignValueCommand(String line, AlgorithmMemory scopeMemory) throws AlgorithmCompileException, NotDesiredCommandException {
        // Ermittlung der Stelle des Zuweisungsoperators "=", falls vorhanden. 
        int defineCharPosition = getPositionOfDefineCharIfIsAssignValueCommandIfValid(line);
        if (defineCharPosition < 0) {
            throw new NotDesiredCommandException();
        }

        String leftSide = line.substring(0, defineCharPosition);
        String rightSide = line.substring(defineCharPosition + 1);
        // Linke Seite behandeln.
        IdentifierType type = null;
        for (IdentifierType t : IdentifierType.values()) {
            if (leftSide.startsWith(t.toString() + " ")) {
                type = t;
                break;
            }
        }

        String identifierName;
        AssignValueType assignValueType;
        if (type != null) {

            identifierName = leftSide.substring((type.toString() + " ").length());
            assignValueType = AssignValueType.NEW;
            // Prüfung, ob dieser Identifier gültigen Namen besitzt.
            if (!VALIDATOR.isValidIdentifier(identifierName)) {
                throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_ILLEGAL_CHARACTER, identifierName);
            }
            // Prüfung, ob dieser Identifier bereits existiert.
            if (scopeMemory.containsIdentifier(identifierName)) {
                throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, identifierName);
            }
        } else {
            identifierName = leftSide;
            assignValueType = AssignValueType.CHANGE;
            // Prüfung, ob dieser Identifier bereits existiert.
            if (!scopeMemory.containsIdentifier(identifierName)) {
                throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, identifierName);
            }
            type = scopeMemory.get(identifierName).getType();
        }

        // Rechte Seite behandeln.
        Identifier identifier = Identifier.createIdentifier(scopeMemory, identifierName, type);
        if (type != null) {
            AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(rightSide, scopeMemory);
            List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
            String rightSideReplaced = algorithmCommandReplacementList.getSubstitutedExpression();
            if (type != IdentifierType.STRING) {
                try {
                    AbstractExpression expr = (AbstractExpression) CompilerUtils.parseParameterAgaingstType(rightSideReplaced, VALIDATOR, scopeMemory, type);
                    scopeMemory.put(identifierName, identifier);
                    commands.add(new AssignValueCommand(identifier, expr, assignValueType));
                    return commands;
                } catch (BooleanExpressionException | ExpressionException e) {
                    return parseAlgorithmCall(scopeMemory, commands, rightSideReplaced, type, identifier, assignValueType);
                }
            } else {
                try {
                    MalString malString = CompilerUtils.getMalString(rightSideReplaced, scopeMemory);
                    scopeMemory.put(identifierName, identifier);
                    commands.add(new AssignValueCommand(identifier, malString, assignValueType));
                    return commands;
                } catch (AlgorithmCompileException e) {
                    return parseAlgorithmCall(scopeMemory, commands, rightSideReplaced, type, identifier, assignValueType);
                }
            }
        }

        throw new NotDesiredCommandException();

    }

    private static int getPositionOfDefineCharIfIsAssignValueCommandIfValid(String line) {
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
            String rightSide, IdentifierType assignedIdentifierType, Identifier identifier, AssignValueType assignValueType) throws AlgorithmCompileException {
        // 1. Auf vom benutzer definierte Algorithmen prüfen.
        try {
            // Kompatibilitätscheck
            Signature calledAlgSignature = getAlgorithmCallDataFromAlgorithmCall(rightSide, scopeMemory, assignedIdentifierType).getSignature();
            // Parameter auslesen;
            Identifier[] parameter = getParameterFromAlgorithmCall(rightSide, calledAlgSignature, commands, scopeMemory);
            scopeMemory.put(identifier.getName(), identifier);
            commands.add(new AssignValueCommand(identifier, calledAlgSignature, parameter, assignValueType));
            return commands;
        } catch (AlgorithmCompileException e) {
            // 2. Auf Standardbefehle überprüfen.
            CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(rightSide);
            String algName = algParseData.getName();
            for (Algorithm alg : AlgorithmCompiler.FIXED_ALGORITHMS) {
                Signature sgn = alg.getSignature();
                if (sgn.getName().equals(algName) && alg.getReturnType() == assignedIdentifierType) {
                    try {
                        Identifier[] parameters = getParameterFromAlgorithmCall(rightSide, sgn, commands, scopeMemory);
                        scopeMemory.put(identifier.getName(), identifier);
                        AssignValueCommand resultCommand = new AssignValueCommand(identifier, alg.getSignature(), parameters, assignValueType);
                        commands.add(resultCommand);
                        return commands;
                    } catch (ParseAssignValueException ex) {
                    }
                }
            }
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, rightSide);
        }
    }

    private static Identifier[] getParameterFromAlgorithmCall(String input, Signature calledAlgSignature, List<AlgorithmCommand> commands, AlgorithmMemory scopeMemory)
            throws ParseAssignValueException {
        try {
            CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(input);
            String[] params = algParseData.getParameters();
            Identifier[] identifiers = new Identifier[params.length];
            for (int i = 0; i < params.length; i++) {
                // 1. Fall: der Parameter ist ein Bezeichner (welcher bereits in der Memory liegt).
                if (scopeMemory.get(params[i]) != null) {
                    identifiers[i] = scopeMemory.get(params[i]);
                    continue;
                }
                // 2. Fall: der Parameter ist gültiger (abstrakter) Ausdruck vom geforderten Typ oder ein String.
                AbstractExpression argument = null;
                MalString malString = null;
                try {
                    switch (calledAlgSignature.getParameterTypes()[i]) {
                        case EXPRESSION:
                            argument = Expression.build(params[i], VALIDATOR);
                            // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                            CompilerUtils.checkIfAllIdentifiersAreDefined(argument.getContainedVars(), scopeMemory);
                            CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, argument.getContainedVars(), scopeMemory);
                            break;
                        case BOOLEAN_EXPRESSION:
                            argument = BooleanExpression.build(params[i], VALIDATOR, CompilerUtils.extractTypesFromMemory(scopeMemory));
                            // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                            CompilerUtils.checkIfAllIdentifiersAreDefined(argument.getContainedVars(), scopeMemory);
                            CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, ((BooleanExpression) argument).getContainedExpressionVars(), scopeMemory);
                            CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.BOOLEAN_EXPRESSION, ((BooleanExpression) argument).getContainedBooleanVars(scopeMemory), scopeMemory);
                            CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.MATRIX_EXPRESSION, ((BooleanExpression) argument).getContainedMatrixVars(), scopeMemory);
                            break;
                        case MATRIX_EXPRESSION:
                            argument = MatrixExpression.build(params[i], VALIDATOR, VALIDATOR);
                            // Prüfung auf Wohldefiniertheit aller auftretenden Bezeichner.
                            CompilerUtils.checkIfAllIdentifiersAreDefined(((MatrixExpression) argument).getContainedVars(), scopeMemory);
                            CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.EXPRESSION, ((MatrixExpression) argument).getContainedExpressionVars(), scopeMemory);
                            CompilerUtils.checkIfIdentifiersAreOfCorrectType(IdentifierType.MATRIX_EXPRESSION, ((MatrixExpression) argument).getContainedMatrixVars(), scopeMemory);
                            break;
                        case STRING:
                            malString = CompilerUtils.getMalString(params[i], scopeMemory);
                        default:
                            break;
                    }
                } catch (ExpressionException | BooleanExpressionException e) {
                    throw new AlgorithmCompileException(e);
                }

                if (malString != null) {
                    String genVarName = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
                    Identifier genVarIdentifier = Identifier.createIdentifier(scopeMemory, genVarName, IdentifierType.STRING);
                    identifiers[i] = genVarIdentifier;
                    commands.add(new AssignValueCommand(genVarIdentifier, malString, AssignValueType.NEW));
                    scopeMemory.addToMemoryInCompileTime(genVarIdentifier);
                } else if (argument != null) {
                    String genVarName = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
                    Identifier genVarIdentifier = Identifier.createIdentifier(scopeMemory, genVarName, IdentifierType.identifierTypeOf(argument));
                    identifiers[i] = genVarIdentifier;
                    commands.add(new AssignValueCommand(genVarIdentifier, argument, AssignValueType.NEW));
                    scopeMemory.addToMemoryInCompileTime(genVarIdentifier);
                } else {
                    throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, params[i]);
                }
            }
            return identifiers;
        } catch (AlgorithmCompileException e) {
            throw new ParseAssignValueException(e);
        }
    }

    private static AlgorithmCallData getAlgorithmCallDataFromAlgorithmCall(String input, AlgorithmMemory scopeMemory, IdentifierType returnType) throws ParseAssignValueException {
        try {
            CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(input);
            String algName = algParseData.getName();
            String[] params = algParseData.getParameters();

            ParameterData[] paramValues = new ParameterData[params.length];
            for (int i = 0; i < params.length; i++) {
                paramValues[i] = CompilerUtils.parseParameterWithoutType(params[i], VALIDATOR, scopeMemory);
            }

            // Prüfung, ob ein Algorithmus mit diesem Namen bekannt ist.
            Signature algorithmCandidate = null;
            boolean candidateFound;
            for (Signature signature : AlgorithmCompiler.ALGORITHM_SIGNATURES.getAlgorithmSignatureStorage()) {
                if (signature.getName().equals(algName) && signature.getParameterTypes().length == params.length) {
                    candidateFound = true;
                    for (int i = 0; i < params.length; i++) {
                        if (paramValues[i].getType() == IdentifierType.STRING) {
                            if (IdentifierType.STRING != signature.getParameterTypes()[i]) {
                                candidateFound = false;
                            }
                        } else if (!areAllVarsContainedInMemory(((AbstractExpression) paramValues[i].getValue()).getContainedVars(), scopeMemory)
                                || IdentifierType.identifierTypeOf(paramValues[i].getValue()) != signature.getParameterTypes()[i]) {
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
                throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, algName);
            }

            // Prüfung, ob Rückgabewert korrekt ist.
            if (!algorithmCandidate.getReturnType().equals(returnType)) {
                throw new ParseAssignValueException(AlgorithmCompileExceptionIds.AC_WRONG_RETURN_TYPE);
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

    private static List<AlgorithmCommand> parseVoidCommand(String line, AlgorithmMemory scopeMemory) throws AlgorithmCompileException, NotDesiredCommandException {

        // Falls Unteralgorithmenausrufe vorhanden sind, so müssen diese in separate Variablen ausgelagert werden.
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(line, scopeMemory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        String lineReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        // Struktur des Aufrufs ermitteln.
        String algName = null;
        try {
            CompilerUtils.AlgorithmParseData algParseData = CompilerUtils.getAlgorithmParseData(lineReplaced);
            algName = algParseData.getName();
        } catch (AlgorithmCompileException e) {
            throw new NotDesiredCommandException();
        }

        // 1. Auf vom Benutzer definierte Algorithmen prüfen.
        for (Signature sgn : AlgorithmCompiler.ALGORITHM_SIGNATURES.getAlgorithmSignatureStorage()) {
            if (sgn.getName().equals(algName)) {
                try {
                    Identifier[] parameters = getParameterFromAlgorithmCall(line, sgn, commands, scopeMemory);
                    commands.add(new VoidCommand(algName, parameters));
                    return commands;
                } catch (ParseAssignValueException e) {
                    throw new ParseVoidException(e);
                }
            }
        }

        // 2. Auf Standardbefehle überprüfen.
        for (Algorithm alg : AlgorithmCompiler.FIXED_ALGORITHMS) {
            Signature sgn = alg.getSignature();
            if (sgn.getName().equals(algName)) {
                try {
                    Identifier[] parameters = getParameterFromAlgorithmCall(line, sgn, commands, scopeMemory);
                    commands.add(new VoidCommand(algName, parameters));
                    return commands;
                } catch (ParseAssignValueException e) {
                }
            }
        }

        throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_NO_SUCH_COMMAND, algName);
    }

    private static List<AlgorithmCommand> parseControlStructure(String line, AlgorithmMemory memory, Algorithm alg, boolean keywordsAllowed) throws AlgorithmCompileException, NotDesiredCommandException {
        // If-Else-Block
        try {
            return parseIfElseControlStructure(line, memory, alg, keywordsAllowed);
        } catch (ParseControlStructureException e) {
            /*
            Es ist zwar eine If-Else-Struktur mit korrekter Bedingung, aber der
            innere Block kann nicht kompiliert werden.
             */
            throw e;
        } catch (BooleanExpressionException | BlockCompileException e) {
            // Es ist zwar eine If-Else-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(e);
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
        } catch (BooleanExpressionException | BlockCompileException e) {
            // Es ist zwar eine While-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(e);
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
        } catch (BooleanExpressionException | BlockCompileException e) {
            // Es ist zwar eine While-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(e);
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
        } catch (BooleanExpressionException | BlockCompileException e) {
            // Es ist zwar eine While-Struktur, aber die Bedingung kann nicht kompiliert werden.
            throw new AlgorithmCompileException(e);
        } catch (NotDesiredCommandException e) {
            // Keine Do-While-Struktur. Also weiter.
        }

        throw new NotDesiredCommandException();
    }

    private static List<AlgorithmCommand> parseIfElseControlStructure(String line, AlgorithmMemory memory, Algorithm alg, boolean keywordsAllowed)
            throws AlgorithmCompileException, BooleanExpressionException, BlockCompileException, NotDesiredCommandException {

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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        String booleanConditionString = line.substring((Keyword.IF.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(), endOfBooleanCondition);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression condition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(booleanConditionString, memory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        String booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        Map<String, IdentifierType> typesMap = CompilerUtils.extractTypesFromMemory(memory);
        condition = BooleanExpression.build(booleanConditionReplaced, VALIDATOR, typesMap);
        CompilerUtils.checkIfAllIdentifiersAreDefined(condition.getContainedVars(), memory);

        // Prüfung, ob line mit "if(boolsche Bedingung){ ..." beginnt.
        if (!line.contains(ReservedChars.BEGIN.getStringValue())
                || !line.contains(ReservedChars.END.getStringValue())
                || line.indexOf(ReservedChars.BEGIN.getValue()) > endOfBooleanCondition + 1) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_MUST_CONTAIN_BEGIN_AND_END,
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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
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

        String restLine = line.substring(endBlockPosition + 1);
        if (!restLine.startsWith(Keyword.ELSE.getValue() + ReservedChars.BEGIN.getValue())) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_KEYWORD_EXPECTED, Keyword.ELSE.getValue());
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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }
        if (endBlockPosition != restLine.length() - 1) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, line.substring(endBlockPosition + 1));
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

    private static List<AlgorithmCommand> parseWhileControlStructure(String line, AlgorithmMemory memory, Algorithm alg)
            throws AlgorithmCompileException, BooleanExpressionException, BlockCompileException, NotDesiredCommandException {

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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        String booleanConditionString = line.substring((Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(), endOfBooleanCondition);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression condition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(booleanConditionString, memory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        String booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        Map<String, IdentifierType> typesMap = CompilerUtils.extractTypesFromMemory(memory);
        condition = BooleanExpression.build(booleanConditionReplaced, VALIDATOR, typesMap);
        CompilerUtils.checkIfAllIdentifiersAreDefined(condition.getContainedVars(), memory);

        // Prüfung, ob line mit "while(boolsche Bedingung){ ..." beginnt.
        if (!line.contains(ReservedChars.BEGIN.getStringValue())
                || !line.contains(ReservedChars.END.getStringValue())
                || line.indexOf(ReservedChars.BEGIN.getValue()) > endOfBooleanCondition + 1) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_MUST_CONTAIN_BEGIN_AND_END,
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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
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

        throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, line.substring(endBlockPosition + 1));
    }

    private static List<AlgorithmCommand> parseDoWhileControlStructure(String line, AlgorithmMemory memory, Algorithm alg)
            throws AlgorithmCompileException, BooleanExpressionException, BlockCompileException, NotDesiredCommandException {

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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }

        AlgorithmMemory memoryBeforWhileLoop = memory.copyMemory();

        List<AlgorithmCommand> commandsDoPart = parseConnectedBlockWithKeywords(line.substring(beginBlockPosition, endBlockPosition), memoryBeforWhileLoop, alg);

        // While-Bedingung kompilieren
        String whilePart = line.substring(endBlockPosition + 1);
        if (!whilePart.startsWith(Keyword.WHILE.getValue() + ReservedChars.OPEN_BRACKET.getValue())) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_KEYWORD_EXPECTED, Keyword.WHILE.getValue());
        }
        if (!whilePart.endsWith(String.valueOf(ReservedChars.CLOSE_BRACKET.getValue()))) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        String whileConditionString = line.substring(endBlockPosition + Keyword.WHILE.getValue().length() + 2, line.length() - 1);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression condition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(whileConditionString, memory);
        List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
        String booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        Map<String, IdentifierType> typesMap = CompilerUtils.extractTypesFromMemory(memory);
        condition = BooleanExpression.build(booleanConditionReplaced, VALIDATOR, typesMap);
        CompilerUtils.checkIfAllIdentifiersAreDefined(condition.getContainedVars(), memory);

        DoWhileControlStructure doWhileControlStructure = new DoWhileControlStructure(commandsDoPart, condition);
        doWhileControlStructure.getCommands().addAll(commands);
        commands.add(doWhileControlStructure);
        return commands;
    }

    private static List<AlgorithmCommand> parseForControlStructure(String line, AlgorithmMemory memory, Algorithm alg)
            throws AlgorithmCompileException, BooleanExpressionException, BlockCompileException, NotDesiredCommandException {

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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        String forControlString = line.substring((Keyword.FOR.getValue() + ReservedChars.OPEN_BRACKET.getValue()).length(), endOfForControlPart);

        AlgorithmMemory currentMemory = memory.copyMemory();

        // Die drei for-Anweisungen kompilieren.
        String[] forControlParts = CompilerUtils.splitByKomma(forControlString);

        // Es müssen genau 3 Befehle in der For-Struktur stehen.
        if (forControlParts.length < 3) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_EXPECTED, ReservedChars.ARGUMENT_SEPARATOR.getValue());
        }
        if (forControlParts.length > 3) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.CLOSE_BRACKET.getValue());
        }

        List<AlgorithmCommand> initialization = parseAssignValueCommand(forControlParts[0], currentMemory);

        // Die boolsche Bedingung kann wieder Algorithmenaufrufe enthalten. Daher muss sie in "elementare" Teile zerlegt werden.
        BooleanExpression endLoopCondition;
        AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(forControlParts[1], currentMemory);
        List<AlgorithmCommand> commandsEndLoopCondition = algorithmCommandReplacementList.getCommands();
        String booleanConditionReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

        Map<String, IdentifierType> typesMap = CompilerUtils.extractTypesFromMemory(currentMemory);
        endLoopCondition = BooleanExpression.build(booleanConditionReplaced, VALIDATOR, typesMap);
        CompilerUtils.checkIfAllIdentifiersAreDefined(endLoopCondition.getContainedVars(), currentMemory);

        AlgorithmMemory memoryBeforeLoop = currentMemory.copyMemory();

        List<AlgorithmCommand> loopAssignment = parseAssignValueCommand(forControlParts[2], currentMemory);
        // Prüfung, ob bei loopAssignment keine weiteren Bezeichner hinzukamen, außer den Technischen.
        checkIfNewIdentifierOccur(memoryBeforeLoop, currentMemory);

        // Prüfung, ob line mit "for(a,b,c){ ..." beginnt.
        if (!line.contains(ReservedChars.BEGIN.getStringValue())
                || !line.contains(ReservedChars.END.getStringValue())
                || line.indexOf(ReservedChars.BEGIN.getValue()) > endOfForControlPart + 1) {
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_MUST_CONTAIN_BEGIN_AND_END,
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
            throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ReservedChars.END.getValue());
        }
        List<AlgorithmCommand> commandsForPart = parseConnectedBlockWithKeywords(line.substring(beginBlockPosition, endBlockPosition), currentMemory, alg);
        ForControlStructure forControlStructure = new ForControlStructure(commandsForPart, initialization, commandsEndLoopCondition, endLoopCondition, loopAssignment);

        // Lokale Variable aus dem Speicher memory wieder herausnehmen.
        // '}' muss als letztes Zeichen stehen, sonst ist die Struktur nicht korrekt.
        if (endBlockPosition == line.length() - 1) {
            return Collections.singletonList(forControlStructure);
        }

        throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, line.substring(endBlockPosition + 1));
    }

    private static void checkIfNewIdentifierOccur(AlgorithmMemory memoryBeforeLoop, AlgorithmMemory currentMemory) throws ParseControlStructureException {
        for (String identifierName : currentMemory.keySet()) {
            if (!memoryBeforeLoop.containsKey(identifierName) && !CompilerUtils.isTechnicalIdentifierName(identifierName)) {
                throw new ParseControlStructureException(AlgorithmCompileExceptionIds.AC_CONTROL_STRUCTURE_FOR_NEW_IDENTIFIER_NOT_ALLOWED, identifierName);
            }
        }
    }

    private static List<AlgorithmCommand> parseKeywordCommand(String line, boolean keywordsAllowed) throws AlgorithmCompileException, NotDesiredCommandException {
        if (line.equals(Keyword.BREAK.toString())) {
            if (keywordsAllowed) {
                return Collections.singletonList(new KeywordCommand(Keyword.BREAK));
            }
            throw new ParseKeywordException(AlgorithmCompileExceptionIds.AC_KEYWORD_NOT_ALLOWED_HERE, Keyword.BREAK);
        }
        if (line.equals(Keyword.CONTINUE.toString())) {
            if (keywordsAllowed) {
                return Collections.singletonList(new KeywordCommand(Keyword.CONTINUE));
            }
            throw new ParseKeywordException(AlgorithmCompileExceptionIds.AC_KEYWORD_NOT_ALLOWED_HERE, Keyword.CONTINUE);
        }
        throw new NotDesiredCommandException();
    }

    private static List<AlgorithmCommand> parseReturnCommand(String line, AlgorithmMemory scopeMemory, Algorithm alg) throws AlgorithmCompileException, NotDesiredCommandException {
        if (line.startsWith(Keyword.RETURN.getValue() + " ")) {
            if (line.equals(Keyword.RETURN.getValue() + ReservedChars.LINE_SEPARATOR)) {
                return Collections.singletonList((AlgorithmCommand) new ReturnCommand(null));
            }
            String returnValueCandidate = line.substring((Keyword.RETURN.getValue() + " ").length());

            if (scopeMemory.get(returnValueCandidate) == null) {

                AlgorithmCommandReplacementData algorithmCommandReplacementList = decomposeAssignmentInvolvingAlgorithmCalls(returnValueCandidate, scopeMemory);
                List<AlgorithmCommand> commands = algorithmCommandReplacementList.getCommands();
                String returnValueReplaced = algorithmCommandReplacementList.getSubstitutedExpression();

                if (scopeMemory.get(returnValueReplaced) != null) {
                    return Collections.singletonList((AlgorithmCommand) new ReturnCommand(scopeMemory.get(returnValueCandidate)));
                }
                if (VALIDATOR.isValidKnownIdentifier(returnValueReplaced,CompilerUtils.extractClassesOfAbstractExpressionIdentifiersFromMemory(scopeMemory))) {
                    throw new ParseReturnException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, returnValueCandidate);
                }
                String genVarForReturn = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
                String assignValueCommand = alg.getReturnType().toString() + " " + genVarForReturn + "=" + returnValueReplaced;
                List<AlgorithmCommand> additionalCommandsByAssignment = parseAssignValueCommand(assignValueCommand, scopeMemory);
                commands.addAll(additionalCommandsByAssignment);
                commands.add(new ReturnCommand(Identifier.createIdentifier(scopeMemory, genVarForReturn, alg.getReturnType())));
                return commands;
            } else {
                return Collections.singletonList((AlgorithmCommand) new ReturnCommand(Identifier.createIdentifier(scopeMemory, returnValueCandidate, alg.getReturnType())));
            }
        }
        throw new NotDesiredCommandException();
    }

    public static List<AlgorithmCommand> parseConnectedBlockWithKeywords(String input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, true, true);
    }

    public static List<AlgorithmCommand> parseConnectedBlockWithoutKeywords(String input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, true, false);
    }

    public static List<AlgorithmCommand> parseBlockWithKeywords(String input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, false, true);
    }

    public static List<AlgorithmCommand> parseBlockWithoutKeywords(String input, AlgorithmMemory memory, Algorithm alg) throws AlgorithmCompileException {
        return parseCommandBlock(input, memory, alg, false, false);
    }

    private static List<AlgorithmCommand> parseCommandBlock(String input, AlgorithmMemory memory, Algorithm alg, boolean connectedBlock, boolean keywordsAllowed) throws AlgorithmCompileException {
        if (!input.isEmpty() && !input.endsWith(String.valueOf(ReservedChars.LINE_SEPARATOR.getValue()))) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_MISSING_LINE_SEPARATOR, ReservedChars.LINE_SEPARATOR.getValue());
        }

        AlgorithmMemory memoryBeforeBlockBeginning;
        if (connectedBlock) {
            memoryBeforeBlockBeginning = memory.copyMemory();
        } else {
            memoryBeforeBlockBeginning = memory;
        }

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
            if (wavedBracketCounter == 0 && squareBracketCounter == 0 && input.charAt(i) == ReservedChars.LINE_SEPARATOR.getValue()) {
                endBlockPosition = i;
                linesAsList.add(input.substring(beginBlockPosition, endBlockPosition));
                beginBlockPosition = i + 1;
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
        if (endBlockPosition != input.length() - 1) {
            throw new AlgorithmCompileException(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, input.substring(endBlockPosition));
        }

        String[] lines = linesAsList.toArray(new String[linesAsList.size()]);

        List<AlgorithmCommand> commands = new ArrayList<>();
        for (String line : lines) {
            if (!line.isEmpty()) {
                commands.addAll(parseLine(line, memoryBeforeBlockBeginning, alg, keywordsAllowed));
            }
        }
        return commands;
    }

    ///////////////////// Methoden für die Zerlegung eines Ausdrucks, welcher Algorithmenaufrufe enthält, in mehrere Befehle ///////////////////////
    private static AlgorithmCommandReplacementData decomposeAssignmentInvolvingAlgorithmCalls(String input, AlgorithmMemory memory) {
        String inputWithGeneratedVars = input;
        List<AlgorithmCommand> commands = new ArrayList<>();

        boolean algorithmCallFound;
        String algorithmCallAsString;
        List<Integer> beginningAlgCall;
        int endingAlgCall;
        do {
            algorithmCallFound = false;
            for (Signature signature : AlgorithmCompiler.ALGORITHM_SIGNATURES.getAlgorithmSignatureStorage()) {
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

    private static List<Integer> getListWithIndicesOfAlgorithmStart(String input, String algName) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < input.length() - algName.length(); i++) {
            if (input.substring(i).startsWith(algName)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static String addAssignValueCommandsForNonVarAlgorithmParameters(String input, int beginningAlgCall, int endingAlgCall, AlgorithmCallData algorithmCallData,
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
                    commands.add(new AssignValueCommand(genVarIdentifier, parameter.getValue(), AssignValueType.NEW));
                    scopeMemory.addToMemoryInCompileTime(genVarIdentifier);
                } catch (AlgorithmCompileException e) {
                    throw new ParseAssignValueException(e);
                }
            }
        }

        String genVarNameForCalledAlg = CompilerUtils.generateTechnicalIdentifierName(scopeMemory);
        Identifier identifierForCalledAlg = Identifier.createIdentifier(scopeMemory, genVarNameForCalledAlg, algorithmCallData.getSignature().getReturnType());
        try {
            commands.add(new AssignValueCommand(identifierForCalledAlg, algorithmCallData.getSignature(), inputParameters, AssignValueType.NEW));
            scopeMemory.addToMemoryInCompileTime(identifierForCalledAlg);
        } catch (AlgorithmCompileException e) {
            throw new ParseAssignValueException(e);
        }

        String algorithmCallString = input.substring(beginningAlgCall, endingAlgCall);
        String result = input;
        while (result.contains(algorithmCallString)) {
            result = result.replace(algorithmCallString, genVarNameForCalledAlg);
        }
        return result;
    }

}
