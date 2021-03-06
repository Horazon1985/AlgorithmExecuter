package test.algorithms;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import algorithmexecuter.AlgorithmBuilder;
import algorithmexecuter.CompilerUtils;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.enums.Keyword;
import algorithmexecuter.enums.ReservedChars;
import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.exceptions.constants.AlgorithmCompileExceptionIds;
import algorithmexecuter.lang.translator.Translator;
import algorithmexecuter.model.Algorithm;
import algorithmexecuter.model.Signature;
import algorithmexecuter.model.command.AssignValueCommand;
import algorithmexecuter.model.command.ControlStructure;
import algorithmexecuter.model.command.DoWhileControlStructure;
import algorithmexecuter.model.command.ForControlStructure;
import algorithmexecuter.model.command.IfElseControlStructure;
import algorithmexecuter.model.command.ReturnCommand;
import algorithmexecuter.model.command.VoidCommand;
import algorithmexecuter.model.command.WhileControlStructure;
import algorithmexecuter.model.utilclasses.EditorCodeString;
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.model.utilclasses.malstring.MalStringAbstractExpression;
import algorithmexecuter.model.utilclasses.malstring.MalStringCharSequence;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class AlgorithmCompileTests {

    @Test
    public void preprocessMainTest() {
        String input = "main() {expression    a =     sin( 5)  ;   a = a+   5  ;   }   ";
        String outputFormatted = CompilerUtils.preprocessAlgorithm(input);
        String outputFormattedExpected = "main(){expression a=sin(5);a=a+ 5;}";
        assertTrue(outputFormatted.equals(outputFormattedExpected));
    }

    @Test
    public void preprocessMainWithEditorCodeStringTest() {
        EditorCodeString input = new EditorCodeString("main() { expression    a =     sin( 5   )  ;   a = a+   5  ;   }   ");
        EditorCodeString outputFormatted = CompilerUtils.preprocessAlgorithm(input);
        EditorCodeString outputFormattedExpected = new EditorCodeString("main(){expression a=sin(5);a=a+ 5;}");
        assertTrue(outputFormatted.equals(outputFormattedExpected));
    }

    @Test
    public void preprocessAlgorithmTest() {
        String input = "alg(expression   a   ,  expression b) {return  a  ;   }   ";
        String outputFormatted = CompilerUtils.preprocessAlgorithm(input);
        String outputFormattedExpected = "alg(expression a,expression b){return a;}";
        assertTrue(outputFormatted.equals(outputFormattedExpected));
    }

    @Test
    public void preprocessAlgorithmWithEditorCodeStringTest() {
        EditorCodeString input = new EditorCodeString("alg(expression   a   ,  expression b) {return  a  ;   }   ");
        EditorCodeString outputFormatted = CompilerUtils.preprocessAlgorithm(input);
        EditorCodeString outputFormattedExpected = new EditorCodeString("alg(expression a,expression b){return a;}");
        assertTrue(outputFormatted.equals(outputFormattedExpected));
    }

    @Test
    public void preprocessNonTrivialAlgorithmTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	do{\n"
                + "		a=a+1;\n"
                + "	}\n"
                + "	while(a<10);\n"
                + "	return a;\n"
                + "}";
        String outputFormatted = CompilerUtils.preprocessAlgorithm(input);
        String outputFormattedExpected = "expression main(){expression a=5;do{a=a+1;}while(a<10);return a;}";
        assertTrue(outputFormatted.equals(outputFormattedExpected));
    }

    @Test
    public void preprocessNonTrivialAlgorithmWithEditorCodeStringTest() {
        EditorCodeString input = new EditorCodeString("expression main(){expression a=5;do  {a=a+1;}   while(a<10);return a;}");
        EditorCodeString outputFormatted = CompilerUtils.preprocessAlgorithm(input);
        EditorCodeString outputFormattedExpected = new EditorCodeString("expression main(){expression a=5;do{a=a+1;}while(a<10);return a;}");
        assertTrue(outputFormatted.equals(outputFormattedExpected));
    }

    @Test
    public void parseSimpleAlgorithmWithReturnTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 2);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseSimpleAlgorithmTest() {
        String input = "main(){\n"
                + "	expression a=5;\n"
                + "	a=a+5;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), null);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 2);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseSimpleAlgorithmWithReturnTypeStringTest() {
        String input = "string main(){\n"
                + "	expression a=3;\n"
                + "	string s=\"a hat den Wert \"+a;\n"
                + "	return s;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.STRING);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertEquals(2, ((MalString) ((AssignValueCommand) mainAlg.getCommands().get(1)).getTargetValue()).getMalStringSummands().length);
            assertEquals("a hat den Wert ", ((MalStringCharSequence) ((MalString) ((AssignValueCommand) mainAlg.getCommands().get(1)).getTargetValue()).getMalStringSummands()[0]).getStringValue());
            assertEquals(Variable.create("a"), ((MalStringAbstractExpression) ((MalString) ((AssignValueCommand) mainAlg.getCommands().get(1)).getTargetValue()).getMalStringSummands()[1]).getAbstractExpression());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());
            assertEquals("s", ((ReturnCommand) mainAlg.getCommands().get(2)).getIdentifier().getName());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithReturnTypeStringTest() {
        String input = "string main(){\n"
                + "	expression a=3;\n"
                + "	expression b=5;\n"
                + "	string s=\"a+b hat den Wert \"+((a+b)+\". Dies wurde eben ausgegeben.\");\n"
                + "	return s;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.STRING);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 4);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertEquals(3, ((MalString) ((AssignValueCommand) mainAlg.getCommands().get(2)).getTargetValue()).getMalStringSummands().length);
            assertEquals("a+b hat den Wert ", ((MalStringCharSequence) ((MalString) ((AssignValueCommand) mainAlg.getCommands().get(2)).getTargetValue()).getMalStringSummands()[0]).getStringValue());
            assertTrue(Variable.create("a").add(Variable.create("b")).equals((Expression) ((MalStringAbstractExpression) ((MalString) ((AssignValueCommand) mainAlg.getCommands().get(2)).getTargetValue()).getMalStringSummands()[1]).getAbstractExpression()));
            assertEquals(". Dies wurde eben ausgegeben.", ((MalStringCharSequence) ((MalString) ((AssignValueCommand) mainAlg.getCommands().get(2)).getTargetValue()).getMalStringSummands()[2]).getStringValue());
            assertTrue(mainAlg.getCommands().get(3).isReturnCommand());
            assertEquals("s", ((ReturnCommand) mainAlg.getCommands().get(3)).getIdentifier().getName());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseSimpleAlgorithmWithPrintCommandTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	print(\"Wert: \"+a);\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 4);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isVoidCommand());
            assertEquals(((VoidCommand) mainAlg.getCommands().get(2)).getName(), "print");
            assertEquals(((VoidCommand) mainAlg.getCommands().get(2)).getIdentifiers().length, 1);
            assertEquals(((VoidCommand) mainAlg.getCommands().get(2)).getIdentifiers()[0].getType(), IdentifierType.STRING);
            assertTrue(mainAlg.getCommands().get(3).isReturnCommand());
            assertEquals("a", ((ReturnCommand) mainAlg.getCommands().get(3)).getIdentifier().getName());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseSimpleAlgorithmWithIfElseTest() {
        String input = "expression main(){\n"
                + "	expression a=3;\n"
                + "	expression b=5;\n"
                + "	if(a==3){\n"
                + "		return a;\n"
                + "	}else{\n"
                + "		return b;\n"
                + "	}\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isIfElseControlStructure());
            assertEquals(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsIfPart().size(), 1);
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsIfPart().get(0).isReturnCommand());
            assertEquals(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsElsePart().size(), 1);
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsElsePart().get(0).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithIfElseTest() {
        String input = "expression main(){\n"
                + "	expression a=3;\n"
                + "	expression b=5;\n"
                + "	if(a==3){\n"
                + "		expression c=8;\n"
                + "		return a;\n"
                + "	}else{\n"
                + "		expression c=10;\n"
                + "		return b;\n"
                + "	}\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isIfElseControlStructure());
            assertEquals(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsIfPart().size(), 2);
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsIfPart().get(0).isAssignValueCommand());
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsIfPart().get(1).isReturnCommand());
            assertEquals(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsElsePart().size(), 2);
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsElsePart().get(0).isAssignValueCommand());
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsElsePart().get(1).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithIdentifierDeclarationAndIfElseTest() {
        String input = "expression main(){\n"
                + "	expression a=2;\n"
                + "	expression b;\n"
                + "	if(a==1){\n"
                + "		b=7;\n"
                + "	}else{\n"
                + "		b=13;\n"
                + "	}\n"
                + "	return b;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm alg = AlgorithmBuilder.ALGORITHMS.getAlgorithms().get(0);
            assertEquals(alg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(alg.getName(), "main");
            assertEquals(alg.getInputParameters().length, 0);
            assertEquals(alg.getCommands().size(), 4);
            assertTrue(alg.getCommands().get(0).isAssignValueCommand());
            assertTrue(alg.getCommands().get(1).isDeclareIDentifierCommand());
            assertTrue(alg.getCommands().get(2).isIfElseControlStructure());
            assertTrue(alg.getCommands().get(3).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithIfElseAndBreakTest() {
        String input = "expression main(){\n"
                + "	expression a=2;\n"
                + "	expression b;\n"
                + "	if(a==1){\n"
                + "		b=7;\n"
                + "	}else{\n"
                + "		b=13;\n"
                + "		break;\n"
                + "	}\n"
                + "	return b;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail(input + " konnte geparst werden, obwohl es Compilerfehler enthielt.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_KEYWORD_NOT_ALLOWED_HERE, Keyword.BREAK));
        }
    }

    @Test
    public void parseSimpleAlgorithmWithIfElseForMatrixComparisonTest() {
        String input = "matrixexpression main(){\n"
                + "	matrixexpression a=[1,1;2,-5]*[3;4];\n"
                + "	matrixexpression b=[7;15];\n"
                + "	if(a==b){\n"
                + "		return a;\n"
                + "	}else{\n"
                + "		return b;\n"
                + "	}\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.MATRIX_EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isIfElseControlStructure());
            assertEquals(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsIfPart().size(), 1);
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsIfPart().get(0).isReturnCommand());
            assertEquals(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsElsePart().size(), 1);
            assertTrue(((IfElseControlStructure) mainAlg.getCommands().get(2)).getCommandsElsePart().get(0).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithAlgorithmUsageInConditionTest() {
        String input = "expression main(){\n"
                + "	expression a=4;\n"
                + "	expression b=6;\n"
                + "	if(ggt(a,b)==2){\n"
                + "		return 5;\n"
                + "	}else{\n"
                + "		return 7;\n"
                + "	}\n"
                + "}\n"
                + "\n"
                + "expression ggt(expression a,expression b){\n"
                + "	return gcd(a,b);\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 2);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 4);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isIfElseControlStructure());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseSimpleAlgorithmWithWhileLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=1;\n"
                + "	while(a<6){\n"
                + "		a=2*a;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isWhileControlStructure());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());
            assertEquals(((WhileControlStructure) mainAlg.getCommands().get(1)).getCommands().size(), 1);
            assertTrue(((WhileControlStructure) mainAlg.getCommands().get(1)).getCommands().get(0).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithAlgorithmCallInWhileConditionTest() {
        String input = "expression main(){\n"
                + "	expression a=1;\n"
                + "	while(f(a)*g(a)<6){\n"
                + "		a=a+1;\n"
                + "	}\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "expression f(expression a){\n"
                + "	return a-1;\n"
                + "}\n"
                + "\n"
                + "expression g(expression a){\n"
                + "	return a+1;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 3);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getCommands().size(), 5);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isWhileControlStructure());
            assertTrue(mainAlg.getCommands().get(4).isReturnCommand());
            assertEquals(((WhileControlStructure) mainAlg.getCommands().get(3)).getCommands().size(), 3);
            assertTrue(((WhileControlStructure) mainAlg.getCommands().get(3)).getCommands().get(0).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseSimpleAlgorithmWithDoWhileLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	do{\n"
                + "		a=a+1;\n"
                + "	}\n"
                + "	while(a<10);\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isDoWhileControlStructure());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());
            assertEquals(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].size(), 1);
            assertTrue(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].get(0).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithAlgorithmCallInDoWhileConditionTest() {
        String input = "expression main(){\n"
                + "	expression a=1;\n"
                + "	do{\n"
                + "		a=a+1;\n"
                + "	}\n"
                + "	while(f(a)*g(a)<6);\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "expression f(expression a){\n"
                + "	return a-1;\n"
                + "}\n"
                + "\n"
                + "expression g(expression a){\n"
                + "	return a+1;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 3);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getCommands().size(), 5);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isDoWhileControlStructure());
            assertTrue(mainAlg.getCommands().get(4).isReturnCommand());
            assertEquals(((DoWhileControlStructure) mainAlg.getCommands().get(3)).getCommands().size(), 3);
            assertTrue(((DoWhileControlStructure) mainAlg.getCommands().get(3)).getCommands().get(0).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseSimpleAlgorithmWithForLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7,i=i+1){\n"
                + "		a=a+i^2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isForControlStructure());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());
            assertEquals(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].size(), 1);
            assertTrue(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].get(0).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithAlgorithmCallInForConditionTest() {
        String input = "expression main(){\n"
                + "	expression a=1;\n"
                + "	for(expression i=0,f(i)<10,i=i+1){\n"
                + "		a=3*a+1;\n"
                + "	}\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "expression f(expression a){\n"
                + "	return a-1;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 2);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isForControlStructure());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());
            assertEquals(((ForControlStructure) mainAlg.getCommands().get(1)).getCommands().size(), 1);
            assertTrue(((ForControlStructure) mainAlg.getCommands().get(1)).getCommands().get(0).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithForLoopAndBreakTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7,i=i+1){\n"
                + "		a=a+i^2;\n"
                + "		if(i==5){\n"
                + "			break;\n"
                + "		}\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isForControlStructure());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());
            assertEquals(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].size(), 2);
            assertTrue(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].get(0).isAssignValueCommand());
            assertTrue(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].get(1).isIfElseControlStructure());
            assertEquals(((ControlStructure) ((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].get(1)).getCommandBlocks()[0].size(), 1);
            assertTrue(((ControlStructure) ((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].get(1)).getCommandBlocks()[0].get(0).isKeywordCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithForLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7,i=i+1){\n"
                + "		a=a+i^2;\n"
                + "	}\n"
                + "	expression i=10;\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 4);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isForControlStructure());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isReturnCommand());
            assertEquals(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].size(), 1);
            assertTrue(((ControlStructure) mainAlg.getCommands().get(1)).getCommandBlocks()[0].get(0).isAssignValueCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmCallingAnotherAlgorithmTest() {
        String input = "expression main(){\n"
                + "	expression a=15;\n"
                + "	expression b=25;\n"
                + "	expression ggt=computeggt(a,b);\n"
                + "	return ggt;\n"
                + "}\n"
                + "\n"
                + "expression computeggt(expression a,expression b){\n"
                + "	expression result=gcd(a,b);\n"
                + "	return result;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 2);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            Algorithm ggtAlg;
            if (algorithmList.get(0).getName().equals("computeggt")) {
                ggtAlg = algorithmList.get(0);
            } else {
                ggtAlg = algorithmList.get(1);
            }

            // Prüfung für den Hauptalgorithmus "main".
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 4);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isReturnCommand());

            // Prüfung für den aufgerufenen Algorithmus "computeggt".
            assertEquals(ggtAlg.getName(), "computeggt");
            assertEquals(ggtAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(ggtAlg.getInputParameters().length, 2);
            assertEquals(ggtAlg.getCommands().size(), 2);
            assertTrue(ggtAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(ggtAlg.getCommands().get(1).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmCallingTheCorrectAlgorithmTest() {
        String input = "expression main(){\n"
                + "	expression res=5*myggt(10,14)*7;\n"
                + "	return res;\n"
                + "}\n"
                + "\n"
                + "expression ggt(expression a,expression b){\n"
                + "	expression result=gcd(a,b);\n"
                + "	return result;\n"
                + "}\n"
                + "\n"
                + "expression myggt(expression a,expression b){\n"
                + "	expression result=gcd(a,b);\n"
                + "	return result;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 3);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS), ggtAlg = null, myggtAlg = null;
            for (Algorithm alg : AlgorithmBuilder.ALGORITHMS.getAlgorithms()) {
                switch (alg.getName()) {
                    case "ggt":
                        ggtAlg = alg;
                        break;
                    case "myggt":
                        myggtAlg = alg;
                        break;
                    default:
                        break;
                }
            }

            assertTrue(mainAlg != null);
            assertTrue(ggtAlg != null);
            assertTrue(myggtAlg != null);

            // Prüfung für den Hauptalgorithmus "main".
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 5);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(((AssignValueCommand) mainAlg.getCommands().get(0)).getTargetAlgorithm() == null);
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(((AssignValueCommand) mainAlg.getCommands().get(1)).getTargetAlgorithm() == null);
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertEquals(((AssignValueCommand) mainAlg.getCommands().get(2)).getTargetAlgorithm(), myggtAlg);
            assertTrue(mainAlg.getCommands().get(3).isAssignValueCommand());
            assertTrue(((AssignValueCommand) mainAlg.getCommands().get(3)).getTargetAlgorithm() == null);
            assertTrue(mainAlg.getCommands().get(4).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmCallingAnotherAlgorithmInOneAssignmentTest() {
        String input = "expression main(){\n"
                + "	expression ggt=computeggt(15,25)*exp(2)*computeggt(15,25);\n"
                + "	return ggt;\n"
                + "}\n"
                + "\n"
                + "expression computeggt(expression a,expression b){\n"
                + "	expression result=gcd(a,b);\n"
                + "	return result;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 2);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS), ggtAlg;
            if (algorithmList.get(0).getName().equals("computeggt")) {
                ggtAlg = algorithmList.get(0);
            } else {
                ggtAlg = algorithmList.get(1);
            }

            // Prüfung für den Hauptalgorithmus "main".
            assertEquals(mainAlg.getCommands().size(), 5);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(((AssignValueCommand) mainAlg.getCommands().get(0)).getTargetValue() != null);
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(((AssignValueCommand) mainAlg.getCommands().get(1)).getTargetValue() != null);
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertEquals(ggtAlg, ((AssignValueCommand) mainAlg.getCommands().get(2)).getTargetAlgorithm());
            assertTrue(mainAlg.getCommands().get(3).isAssignValueCommand());
            assertTrue(((AssignValueCommand) mainAlg.getCommands().get(3)).getTargetValue() != null);
            assertTrue(mainAlg.getCommands().get(4).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithNonTrivialReturnCommandTest() {
        String input = "expression main(){\n"
                + "	expression a=3;\n"
                + "	return 7*a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 1);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            // Prüfung für den Hauptalgorithmus "main".
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithAlgorithmCallContainingStringArgumentTest1() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	string s=\"Aufruf\"; \n"
                + "	f(s);\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "f(string s){\n"
                + "	print(s);\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 2);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            Algorithm printAlg;
            if (algorithmList.get(0).getName().equals("f")) {
                printAlg = algorithmList.get(0);
            } else {
                printAlg = algorithmList.get(1);
            }

            // Prüfung für den Hauptalgorithmus "main" und den Algorithmus f.
            assertEquals(mainAlg.getCommands().size(), 4);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isVoidCommand());
            assertTrue(mainAlg.getCommands().get(3).isReturnCommand());

            assertTrue(printAlg.getCommands().get(0).isVoidCommand());
            assertEquals(((VoidCommand) printAlg.getCommands().get(0)).getSignature(),
                    new Signature(null, "print", new IdentifierType[]{IdentifierType.STRING}));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithAlgorithmCallContainingStringArgumentTest2() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	string s=f(a); \n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "string f(expression a){\n"
                + "	return \"Test!\";\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 2);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            Algorithm stringAlg;
            if (algorithmList.get(0).getName().equals("f")) {
                stringAlg = algorithmList.get(0);
            } else {
                stringAlg = algorithmList.get(1);
            }

            // Prüfung für den Hauptalgorithmus "main" und den Algorithmus f.
            assertEquals(mainAlg.getCommands().size(), 3);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isReturnCommand());

            assertTrue(stringAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(stringAlg.getCommands().get(1).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithPrintTest() {
        String input = "expression main(){\n"
                + "	string s=\"Aufruf\";\n"
                + "	f(s+\"!\");\n"
                + "	return 5;\n"
                + "}\n"
                + "\n"
                + "f(string s){\n"
                + "	print(s);\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 5);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isVoidCommand());
            assertEquals("f", ((VoidCommand) mainAlg.getCommands().get(2)).getName());
            assertTrue(mainAlg.getCommands().get(3).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(4).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithEntryTest() {
        String input = "matrixexpression main(){\n"
                + "	matrixexpression a=[3,0;-2,1];\n"
                + "	return [entry(a,1,1);1];\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.MATRIX_EXPRESSION);
            assertEquals(mainAlg.getName(), "main");
            assertEquals(mainAlg.getInputParameters().length, 0);
            assertEquals(mainAlg.getCommands().size(), 6);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(4).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(5).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }
    
    @Test
    public void parseAlgorithmWithDoubleAlgorithmCallTest() {
        String input = "expression main() {\n" +
            "	expression x = f(g());\n" +
            "	return x;\n" +
            "}\n" +
            "\n" +
            "expression g() {\n" +
            "	return 1;\n" +
            "}\n" +
            "\n" +
            "expression f(expression a) {\n" +
            "	return a+2;\n" +
            "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 3);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getCommands().size(), 4);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    @Test
    public void parseAlgorithmWithComplexDoubleAlgorithmCallTest() {
        String input = "expression main() {\n" +
            "	expression x = f(g()+1,4);\n" +
            "	return x;\n" +
            "}\n" +
            "\n" +
            "expression g() {\n" +
            "	return 1;\n" +
            "}\n" +
            "\n" +
            "expression f(expression a, expression b) {\n" +
            "	return a+b+2;\n" +
            "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 3);

            Algorithm mainAlg = CompilerUtils.getMainAlgorithm(AlgorithmBuilder.ALGORITHMS);
            assertEquals(mainAlg.getReturnType(), IdentifierType.EXPRESSION);
            assertEquals(mainAlg.getCommands().size(), 6);
            assertTrue(mainAlg.getCommands().get(0).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(1).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(2).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(3).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(4).isAssignValueCommand());
            assertTrue(mainAlg.getCommands().get(5).isReturnCommand());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        }
    }

    /////////////////// Test für nichtkompilierbare Algorithmen ////////////////////
    @Test
    public void parseAlgorithmWithMIssingLineSeparatorTest() {
        String input = "main(){expression a=exp(1)}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz fehlendem Semikolon kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_MISSING_LINE_SEPARATOR,
                    ReservedChars.LINE_SEPARATOR.getValue()));
        }
    }

    @Test
    public void parseAlgorithmWithUninitializedIdentifierTest() {
        String input = "expression main(){\n"
                + "	expression a;\n"
                + "	expression b=a;\n"
                + "	expression c=5;\n"
                + "	return c;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz Benutzung eines nicht initialisierten Bezeichners kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_IDENTIFIER_MAYBE_NOT_INITIALIZED,
                    "a"));
        }
    }

    @Test
    public void parseAlgorithmWithUnreachableCodeTest() {
        String input = "expression main(){\n"
                + "	expression a=exp(1);\n"
                + "	return a;\n"
                + "	expression b=a+5;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz unerreichbarem Code kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_UNREACHABLE_CODE, "main"));
        }
    }

    @Test
    public void parseAlgorithmWithoutReturnCommandTest1() {
        String input = "expression main(){\n"
                + "	expression x=2;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz fehlendem 'return' kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_MISSING_RETURN_STATEMENT));
        }
    }

    @Test
    public void parseAlgorithmWithoutReturnCommandTest2() {
        String input = "expression main(){\n"
                + "	expression result=1;\n"
                + "	if(result>0){\n"
                + "		result=3*result;\n"
                + "	}\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz fehlendem 'return' kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_MISSING_RETURN_STATEMENT));
        }
    }

    @Test
    public void parseAlgorithmWithDoubledParametersTest() {
        String input = "expression main(){\n"
                + "	expression ggt=computeggt(15,25);\n"
                + "	return ggt;\n"
                + "}\n"
                + "\n"
                + "expression computeggt(expression a,expression a){\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz doppelt vorkommender Parameter in einem Algorithmusheader kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_IDENTIFIER_ALREADY_DEFINED, "a"));
        }
    }
    
    @Test
    public void parseAlgorithmWithWrongForLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7){\n"
                + "		a=a+i^2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz doppelt fehlerhafter For-Struktur kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_EXPECTED, ReservedChars.ARGUMENT_SEPARATOR.getValue()));
        }
    }

    @Test
    public void parseAlgorithmWithWrongForLoopWithFourCommandsInHeaderTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7,i++,i=i+7){\n"
                + "		a=a+i^2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz doppelt fehlerhafter For-Struktur kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_BRACKET_EXPECTED, ")"));
        }
    }

    @Test
    public void parseAlgorithmWithNotNecessaryDefinedVariableTest() {
        String input = "expression main(){\n"
                + "	expression a;\n"
                + "	expression b;\n"
                + "	if (true) {;\n"
                + "          a=10;\n"
                + "          b=5;\n"
                + "	}else{\n"
                + "          b=7;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz nicht initialisierter Variable kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_IDENTIFIER_MAYBE_NOT_INITIALIZED, "a"));
        }
    }

    @Test
    public void parseAlgorithmWithNotNecessaryDefinedVariableAndWhileLoopTest() {
        String input = "expression main(){\n"
                + "	expression a;\n"
                + "	expression b=1;\n"
                + "	while (b<3) {;\n"
                + "          a=1;\n"
                + "          b=b+1;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz nicht initialisierter Variable kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_IDENTIFIER_MAYBE_NOT_INITIALIZED, "a"));
        }
    }

    @Test
    public void parseAlgorithmsContainingEquivalentSignaturesTest() {
        String input = "main() {\n"
                + "}\n"
                + "\n"
                + "expression f(expression a, string b) {\n"
                + "	return 5;\n"
                + "}\n"
                + "\n"
                + "expression f(matrixexpression a, string b) {\n"
                + "	return 5;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz zweier äquivalenter Signaturen kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_ALGORITHM_ALREADY_EXISTS,
                    new Signature(IdentifierType.EXPRESSION, "f", new IdentifierType[]{IdentifierType.MATRIX_EXPRESSION, IdentifierType.STRING}).toStringWithoutReturnType()));
        }
    }

    @Test
    public void callingNonExistingAlgorithmTest() {
        String input = "expression main(){\n"
                + "	expression a=g();\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "string f() {\n"
                + "	string x = \"Hi\";\n"
                + "	return x;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz nicht unbekanntem Symbol 'g()' kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL, "g()"));
        }
    }

    @Test
    public void parseAlgorithmWithIncompatibleTypesTest1() {
        String input = "expression main(){\n"
                + "	string a=\"Hi\";\n"
                + "	return a;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz inkompatibler Typen kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES,
                    IdentifierType.STRING.getValue(), IdentifierType.EXPRESSION.getValue()));
        }
    }

    @Test
    public void parseAlgorithmWithIncompatibleTypesTest2() {
        String input = "expression main(){\n"
                + "	matrixexpression a=[7];\n"
                + "	expression b=5;\n"
                + "	expression c=a+b;\n"
                + "	return c;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz inkompatibler Typen kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES,
                    IdentifierType.MATRIX_EXPRESSION.getValue(), IdentifierType.EXPRESSION.getValue()));
        }
    }

    @Test
    public void parseAlgorithmWithIncompatibleTypesTest3() {
        String input = "matrixexpression main(){\n"
                + "	booleanexpression a=5>7;\n"
                + "	booleanexpression b=sin(3)<2;\n"
                + "	matrixexpression c=a|b;\n"
                + "	return c;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz inkompatibler Typen kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES,
                    IdentifierType.BOOLEAN_EXPRESSION.getValue(), IdentifierType.MATRIX_EXPRESSION.getValue()));
        }
    }

    @Test
    public void callingIncompatibleAlgorithmTest() {
        String input = "expression main(){\n"
                + "	expression a=f();\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "string f() {\n"
                + "	string x = \"Hi\";\n"
                + "	return x;\n"
                + "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz inkompatibler Typen kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_INCOMPATIBLE_TYPES,
                    IdentifierType.STRING.getValue(), IdentifierType.EXPRESSION.getValue()));
        }
    }

    @Test
    public void callingAlgorithmWithIncompatibleParameterNumberTest() {
        String input = "expression main(){\n" +
        "    expression x=f(g());\n" +
        "    return x;\n" +
        "}\n" +
        "\n" +
        "expression g(){\n" +
        "    return 1;\n" +
        "}\n" +
        "\n" +
        "expression f(expression a,expression b){\n" +
        "    return a+b+2;\n" +
        "}";
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            fail("Der Algorithmus " + input + " wurde trotz inkompatibler Parameter kompiliert.");
        } catch (AlgorithmCompileException e) {
            assertEquals(e.getMessage(), Translator.translateOutputMessage(AlgorithmCompileExceptionIds.AC_CANNOT_FIND_SYMBOL,
                    "f"));
        }
    }
    

    
    
    

}
