package test.algorithms;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import algorithmexecuter.AlgorithmBuilder;
import algorithmexecuter.AlgorithmExecuter;
import algorithmexecuter.booleanexpression.BooleanConstant;
import algorithmexecuter.enums.IdentifierType;
import algorithmexecuter.exceptions.AlgorithmCompileException;
import algorithmexecuter.exceptions.AlgorithmExecutionException;
import algorithmexecuter.model.identifier.Identifier;
import algorithmexecuter.model.Algorithm;
import algorithmexecuter.model.utilclasses.MalString;
import algorithmexecuter.model.utilclasses.malstring.MalStringCharSequence;
import algorithmexecuter.output.AlgorithmOutputPrinter;
import exceptions.EvaluationException;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextPane;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class AlgorithmExecutionTests {

    @BeforeClass
    public static void init() {
        AlgorithmOutputPrinter.getInstance().setOutputArea(new JTextPane());
    }

    @Before
    public void teardown() {
        AlgorithmOutputPrinter.getInstance().clearOutput();
    }

    ////////////////////////// Allgemeine Tests //////////////////////////
    
    @Test
    public void executeEmptyMainAlgorithmTest() {
        String input = "main(){\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 1);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            assertTrue(mainAlg.getCommands().isEmpty());
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result == Identifier.NULL_IDENTIFIER);
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    ////////////////////////// Tests: Zuweisungen //////////////////////////
    
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
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            List<Algorithm> algorithmList = AlgorithmBuilder.ALGORITHMS.getAlgorithms();
            assertEquals(algorithmList.size(), 2);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("ggt"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("5")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmsWithStringReturnTypeTest() {
        String input = "string main(){\n"
                + "	expression a=3;\n"
                + "	string s=\"a hat den Wert \"+a;\n"
                + "	return s;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.STRING);
            assertTrue(result.getName().equals("s"));
            assertEquals(1, ((MalString) result.getRuntimeValue()).getMalStringSummands().length);
            assertEquals("a hat den Wert 3", ((MalStringCharSequence) ((MalString) result.getRuntimeValue()).getMalStringSummands()[0]).getStringValue());
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmsWithStringReturnTypeAndBracketsInsideStringTest() {
        String input = "string main(){\n"
                + "	expression a=3;\n"
                + "	expression b=5;\n"
                + "	string s=\"a+b hat den Wert \"+((a+b)+\". Dies wurde eben ausgegeben.\");\n"
                + "	return s;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.STRING);
            assertTrue(result.getName().equals("s"));
            assertEquals(1, ((MalString) result.getRuntimeValue()).getMalStringSummands().length);
            assertEquals("a+b hat den Wert 8. Dies wurde eben ausgegeben.", ((MalStringCharSequence) ((MalString) result.getRuntimeValue()).getMalStringSummands()[0]).getStringValue());
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmsWithStringReturnTypeAndConmplexStringTest() {
        String input = "string main(){\n"
                + "	string s=\"x\";\n"
                + "	string t=\"y\";\n"
                + "	string result=\"s+t hat den Wert \"+((s+t)+\". Dies wurde eben ausgegeben.\");\n"
                + "	return result;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.STRING);
            assertTrue(result.getName().equals("result"));
            assertEquals(1, ((MalString) result.getRuntimeValue()).getMalStringSummands().length);
            assertEquals("s+t hat den Wert xy. Dies wurde eben ausgegeben.", ((MalStringCharSequence) ((MalString) result.getRuntimeValue()).getMalStringSummands()[0]).getStringValue());
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmCallingAlgorithmOfStringTypeTest() {
        String input = "string main(){\n"
                + "	expression a=5;\n"
                + "	string s=f(a); \n"
                + "	return s;\n"
                + "}\n"
                + "\n"
                + "string f(expression a){\n"
                + "	return \"Test!\";\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.STRING);
            assertTrue(result.getName().equals("s"));
            assertEquals(1, ((MalString) result.getRuntimeValue()).getMalStringSummands().length);
            assertEquals("Test!", ((MalStringCharSequence) ((MalString) result.getRuntimeValue()).getMalStringSummands()[0]).getStringValue());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }
    
    @Test
    public void executeAlgorithmWithComplexStringTest() {
        String input = "string main(){\n"
                + "	string s=\"Aufruf\";\n"
                + "	string t= s+\"!\";\n"
                + "	return t;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.STRING);
            assertTrue(result.getName().equals("t"));
            assertEquals("Aufruf!", ((MalStringCharSequence) ((MalString) result.getRuntimeValue()).getMalStringSummands()[0]).getStringValue());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }
    
    ////////////////////////// Tests: Kontrollstrukturen //////////////////////////
    
    @Test
    public void executeAlgorithmsWithIfElseControlStructureTest() {
        String input = "expression main(){\n"
                + "	expression a=exp(1);\n"
                + "	if(a>2){\n"
                + "		return a;\n"
                + "	}\n"
                + "	a=a+1;\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("exp(1)")));
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }

        input = "expression main(){\n"
                + "	expression a=exp(1);\n"
                + "	expression b=7;\n"
                + "	if(a>b){\n"
                + "		return a;\n"
                + "	}\n"
                + "	a=a+1;\n"
                + "	return a;\n"
                + "}";
        mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("1+exp(1)")));
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }

        input = "expression main(){\n"
                + "	expression a=exp(1);\n"
                + "	if(a>2){\n"
                + "		a=a+1;\n"
                + "		expression b=2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("1+exp(1)")));
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }

        input = "expression main(){\n"
                + "	booleanexpression a=false;\n"
                + "	if(a==true){\n"
                + "		return 5;\n"
                + "	}\n"
                + "	return 7;\n"
                + "}";
        mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().startsWith("#"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("7")));
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmsWithIfElseControlStructureAndStringComparisonTest() {
        String input = "expression main(){\n"
                + "	string s=\"Teststring\";\n"
                + "	if(s+\"!\"==\"Teststring!\"){\n"
                + "		return 2;\n"
                + "	}\n"
                + "	return 3;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.TWO));
        } catch (AlgorithmCompileException e) {
            fail("Der Algorithmus " + input + " konnte nicht kompiliert werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmWithBooleanExpressionTest() {
        String input = "booleanexpression main(){\n"
                + "	expression a=1;\n"
                + "	booleanexpression b=a>=1/2;\n"
                + "	if(b){\n"
                + "		return true;\n"
                + "	}\n"
                + "	return false;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.BOOLEAN_EXPRESSION);
            assertTrue(result.getName().equals("#1"));
            assertTrue(((BooleanConstant) result.getRuntimeValue()).getValue());
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmWithIdentifierDeclarationAndIfElseTest() {
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
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("b"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("13")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmWithWhileControlStructureTest() {
        String input = "expression main(){\n"
                + "	expression a=1;\n"
                + "	while(a<6){\n"
                + "		a=2*a;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("8")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void parseAlgorithmWithAlgorithmCallInWhileConditionTest() {
        String input = "expression main(){\n"
                + "	expression a=1;\n"
                + "	while(f(a)*g(a)<=24){\n"
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
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("6")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmWithDoWhileControlStructureTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	do{\n"
                + "		a=a+1;\n"
                + "	}\n"
                + "	while(a<10);\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("10")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
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
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.THREE));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeSimpleAlgorithmWithForLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7,i=i+1){\n"
                + "		a=a+i^2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("96")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmWithAlgorithmCallsInForLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=1;\n"
                + "	for(expression i=0,f(i)<=10,i=g(i)){\n"
                + "		a=3*a+2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "expression f(expression i){\n"
                + "	return 2*i;\n"
                + "}\n"
                + "\n"
                + "expression g(expression i){\n"
                + "	return i^2+1;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("161")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }
    
    @Test
    public void executeAlgorithmWithWhileLoopAndNewAssignmentInsideLoopTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	while(a<8){\n"
                + "		expression b=1;\n"
                + "		a=a+b;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("8")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    ////////////////////////// Tests: Kontrollstrukturen mit break, continue, return //////////////////////////
    
    @Test
    public void executeAlgorithmWithForLoopAndBreakTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7,i=i+1){\n"
                + "		a=a+i^2;\n"
                + "		if(i==4){\n"
                + "			break;\n"
                + "		}\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("35")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmWithForLoopAndContinueTest() {
        String input = "expression main(){\n"
                + "	expression a=5;\n"
                + "	for(expression i=0,i<7,i=i+1){\n"
                + "		if(i<4){\n"
                + "			continue;\n"
                + "		}\n"
                + "		a=a+i^2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("82")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeVoidAlgorithmWithEarlyReturnTest() {
        String input = "main(){\n"
                + "	expression a=1;\n"
                + "     print(\"Das soll ausgegeben werden!\");\n"
                + "	if(a>0){\n"
                + "         return;\n"
                + "     }\n"
                + "     print(\"Das soll nicht ausgegeben werden!\");\n"
                + "}";
        Algorithm mainAlg = null;

        // AlgorithmOutputPrinter wird gemockt, damit man die Aufrufe der Methode printLine() nachverfolgen kann. 
        AlgorithmOutputPrinter printerMock = Mockito.mock(AlgorithmOutputPrinter.class);
        AlgorithmOutputPrinter.setMockInstance(printerMock);

        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result == Identifier.NULL_IDENTIFIER);

            Mockito.verify(printerMock, Mockito.times(1)).printLine("Das soll ausgegeben werden!");
            Mockito.verify(printerMock, Mockito.never()).printLine("Das soll nicht ausgegeben werden!");
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }
    
    ////////////////////////// Tests: Komplexe Berechnungen //////////////////////////

    @Test
    public void executeEuclideanAlgorithmTest() {
        String input = "expression main(){\n"
                + "	expression a=ggt(68,51);\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "expression ggt(expression a,expression b){\n"
                + "	if(a<b){\n"
                + "		return ggt(b,a);\n"
                + "	}\n"
                + "	expression r=mod(a,b);\n"
                + "	while(r!=0){\n"
                + "		expression c=a;\n"
                + "		a=b;\n"
                + "		b=mod(c,b);\n"
                + "             r=mod(a,b);"
                + "	}\n"
                + "	return b;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("17")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeEuclideanAlgorithmTest2() {
        String input = "expression main(){\n"
                + "	expression a=ggt(34,51);\n"
                + "	return a;\n"
                + "}\n"
                + "\n"
                + "expression ggt(expression a,expression b){\n"
                + "	if(a<b){\n"
                + "		return ggt(b,a);\n"
                + "	}\n"
                + "	expression r=mod(a,b);\n"
                + "	while(r!=0){\n"
                + "		expression c=a;\n"
                + "		a=b;\n"
                + "		b=mod(c,b);\n"
                + "		r=mod(a,b);\n"
                + "	}\n"
                + "	return b;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((Expression) result.getRuntimeValue()).equals(Expression.build("17")));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    ////////////////////////// Tests: Standardfunktionen //////////////////////////
    
    @Test
    public void executeAlgorithmWithApproxMatrixTest() {
        String input = "matrixexpression main(){\n"
                + "	matrixexpression a=[5/3;sin(1)];\n"
                + "	a=a+[1/3;0];\n"
                + "	a=approx(a);\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.MATRIX_EXPRESSION);
            assertTrue(result.getName().equals("a"));
            assertTrue(((MatrixExpression) result.getRuntimeValue()).isMatrix());
            assertEquals(new Dimension(1, 2), ((MatrixExpression) result.getRuntimeValue()).getDimension());
            assertTrue(Expression.TWO.turnToApproximate().equals(((Matrix) result.getRuntimeValue()).getEntry(0, 0)));
            assertEquals(0.84, (((Constant) ((Matrix) result.getRuntimeValue()).getEntry(1, 0)).getApproxValue()), 0.002);
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    ////////////////////////// Tests: Impliziter Typecast //////////////////////////
    
    @Test
    public void executeAlgorithmWithDifferentTypeCastCasesTest1() {
        String input = "matrixexpression main(){\n"
                + "	matrixexpression a=[7];\n"
                + "	matrixexpression b=f();\n"
                + "	matrixexpression c=a+b;\n"
                + "	return c;\n"
                + "}\n"
                + "\n"
                + "matrixexpression f(){\n"
                + "	expression a=5;\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.MATRIX_EXPRESSION);
            assertTrue(result.getName().equals("c"));
            assertTrue(((MatrixExpression) result.getRuntimeValue()).isMatrix());
            assertTrue(new Matrix(BigDecimal.valueOf(12)).equals((Matrix) result.getRuntimeValue()));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    @Test
    public void executeAlgorithmWithDifferentTypeCastCasesTest2() {
        String input = "matrixexpression main(){\n"
                + "	matrixexpression a=[7];\n"
                + "	expression b=f();\n"
                + "	matrixexpression c=b*a;\n"
                + "	return c;\n"
                + "}\n"
                + "\n"
                + "expression f(){\n"
                + "	expression a=5;\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            Identifier result = AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            assertTrue(result.getType() == IdentifierType.MATRIX_EXPRESSION);
            assertTrue(result.getName().equals("c"));
            assertTrue(((MatrixExpression) result.getRuntimeValue()).isMatrix());
            assertTrue(new Matrix(BigDecimal.valueOf(35)).equals((Matrix) result.getRuntimeValue()));
        } catch (AlgorithmCompileException e) {
            fail(input + " konnte nicht geparst werden.");
        } catch (Exception e) {
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        }
    }

    ////////////////////////// Tests: Fehler während der Ausführung //////////////////////////
    
    @Test
    public void executeAlgorithmWithNotDefinedMatrixTest() {
        String input = "matrixexpression main(){\n"
                + "	matrixexpression a=[3,0;-2,1];\n"
                + "	for(expression i=0,i<4,i=i+1){\n"
                + "		a=3*a+2;\n"
                + "	}\n"
                + "	return a;\n"
                + "}";
        Algorithm mainAlg = null;
        try {
            AlgorithmBuilder.parseAlgorithmFile(input);
            mainAlg = AlgorithmBuilder.ALGORITHMS.getMainAlgorithm();
            AlgorithmExecuter.executeAlgorithm(Collections.singletonList(mainAlg));
            fail("Der Algorithmus " + mainAlg + " konnte nicht ausgeführt werden.");
        } catch (AlgorithmCompileException | AlgorithmExecutionException e) {
            fail(input + " konnte trotz nicht wohldefinierter Matriux ausgeführt werden.");
        } catch (EvaluationException e) {
        }
    }

}
