package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
import modelChecker.ModelChecker;
import modelChecker.SimpleModelChecker;
import model.Model;
import org.junit.Rule;
import org.junit.rules.TestName;

public class ModelCheckerTest {
    private static Model model;
    private static Model model2;
    private static Model model3, model4;
    private static Model basicModel;
    private static Model pModel;
    private static Model lecture12;
    private SimpleModelChecker mc;
    private static int count = 1;

    @BeforeClass
    public static void setup() throws IOException {
        model = Model.parseModel("src/test/resources/examples/model1.json");
        model2 = Model.parseModel("src/test/resources/examples/model2.json");
        model3 = Model.parseModel("src/test/resources/examples/model3.json");
        model4 = Model.parseModel("src/test/resources/examples/model4.json");
        basicModel = Model.parseModel("src/test/resources/basicLogic/basicModel.json");
        pModel = Model.parseModel("src/test/resources/pModel.json");
        lecture12 = Model.parseModel("src/test/resources/lecture12/model.json");
        
    }

    @Rule
    public TestName testName = new TestName();

    @Before 
    public void beforeEach() throws IOException {
        mc = new SimpleModelChecker();
        System.out.println("Test nr: " + count + " Test name: " + testName.getMethodName());
        count++;
    }
    
    /*
     * An example of how to set up and call the model building methods and make
     * a call to the model checker itself. The contents of model.json,
     * constraint1.json and ctl.json are just examples, you need to add new
     * models and formulas for the mutual exclusion task.
     **/
    /*@Test
    public void buildAndCheckModel() {
        try {
            Model model = Model.parseModel("src/test/resources/model1.json");

            
            //StateFormula query = new FormulaParser("src/test/resources/ctl1.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/true.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            mc.check(model,query);
            // TO IMPLEMENT
            //assertTrue(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }*/

    @Test
    public void testTrueQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/true.json").parse();

            assertTrue(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test // I thought parser should not allow FALSE as it's not in state formula rules :)
    public void testFalseQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/false.json").parse();

            assertFalse(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void testNegationQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/negation.json").parse();

            assertTrue(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void doubleNegationQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/doubleNegation.json").parse();

            assertTrue(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void andQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/and.json").parse();
            assertTrue(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void orQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/or.json").parse();
            assertTrue(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void advancedBasicsQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/advancedLogin.json").parse();
            assertTrue(mc.check(model, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void justP() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/p.json").parse();
            assertTrue("justP failed", mc.check(pModel, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void pAndQ() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/basicLogic/pAndQ.json").parse();
            assertTrue("PandQ failed", mc.check(pModel, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void allPathsPNext() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/simplePathLogic/allHavePAsNext.json").parse();
            //StateFormula negatedQuery = new FormulaParser("src/test/resources/allHavePAsNext.json", true).parse();
            assertFalse(mc.check(basicModel, query));
            assertEquals("s2-s0", mc.getBetterTrace());
            //assertFalse(mc.check(basicModel, negatedQuery));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void atLeastOnePathPNext() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/simplePathLogic/oneHasPAsNext.json").parse();
            assertFalse(mc.check(basicModel, query));
            //assertFalse(mc.check(basicModel, negatedQuery));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void lecture1() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/lecture12/slide1.json").parse();
            assertTrue(mc.check(lecture12, query));
            assertEquals("s0-s1", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // Examples tested
    
    @Test
    public void lecture12s1() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/lecture12/slide1.json").parse();
            assertTrue(mc.check(lecture12, query));
            assertEquals(mc.getBetterTrace(), "s0-s1");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void lecture12s2() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/lecture12/slide2.json").parse();
            assertFalse(mc.check(lecture12, query));
            assertEquals(mc.getBetterTrace(), "s0-s2");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void lecture12s3() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/lecture12/slide3.json").parse();
            assertTrue(mc.check(lecture12, query));
            assertEquals(mc.getBetterTrace(), "s0-s1-s0");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void lecture12s4() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/lecture12/slide4.json").parse();
            assertFalse(mc.check(lecture12, query));
            assertEquals("s0-act1-s2", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // Model 1
    /*@Test
    public void ctl1M1() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/ctl1.json").parse();
            assertFalse(mc.check(model, query));
            //assertEquals(mc.getBetterTrace(), "s0-s1");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }*/


    // Model 2

    @Test
    public void ctl2m2() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/ctl2.json").parse();
            assertTrue(mc.check(model2, query));
            assertEquals("s0-act1-s1-act2-s2", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // O for opposite

    @Test
    public void ctl2Om2() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/ctl2O.json").parse();
            assertTrue(mc.check(model2, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void constraint1() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/constraint1.json").parse();
            assertFalse(mc.check(model2, query));
            assertEquals("s0-act1-s1-act2-s3", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // Model 3

    @Test
    public void ctl2m3() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/ctl2.json").parse();
            assertTrue(mc.check(model3, query));
            assertEquals("s0-act1-s1-act2-s3", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void ctl2Om3() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/ctl2O.json").parse();
            assertFalse(mc.check(model3, query));
            assertEquals("s0-act1-s1-act2-s2", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // Model 4

    @Test
    public void constraint1M4() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/constraint1.json").parse();
            assertTrue(mc.check(model4, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void constraint1OM4() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/examples/constraint1O.json").parse();
            assertTrue(mc.check(model4, query));
            assertEquals("s0-act1-s1-act3-s2", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void alwaysQM4() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/simplePathLogic/alwaysQ.json").parse();
            assertTrue(mc.check(model4, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void alwaysPM4() {
        try {
            StateFormula query = new FormulaParser("src/test/resources/simplePathLogic/alwaysP.json").parse();
            assertFalse(mc.check(model4, query));
            assertEquals("s0", mc.getBetterTrace());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
