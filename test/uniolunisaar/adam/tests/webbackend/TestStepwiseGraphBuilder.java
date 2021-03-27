package uniolunisaar.adam.tests.webbackend;

import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import java.io.File;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.explicit.GGBuilderStepwise;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestStepwiseGraphBuilder {

    private static final String inputDir = System.getProperty("examplesfolder") + "/forallsafety/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/safety/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(true);
//        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testStepwise() throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
//        final String path = inputDir + "burglar" + File.separator;
//        final String name = "burglar";

        String apt = ".name \"burglar.net\"\n"
                + ".description \"alarm system\"\n"
                + ".type LPN\n"
                + ".options\n"
                + "condition=\"SAFETY\"\n"
                + "\n"
                + ".places\n"
                + "a[yCoord=330.0, xCoord=750.0]\n"
                + "aa[yCoord=990.0, xCoord=390.0]\n"
                + "ab[yCoord=990.0, xCoord=630.0]\n"
                + "b[yCoord=330.0, xCoord=1230.0]\n"
                + "ba[yCoord=990.0, xCoord=1350.0]\n"
                + "bb[yCoord=990.0, xCoord=1590.0]\n"
                + "ea[yCoord=330.0, xCoord=390.0, env=\"true\"]\n"
                + "eb[yCoord=330.0, xCoord=1590.0, env=\"true\"]\n"
                + "env[yCoord=150.0, xCoord=990.0, env=\"true\"]\n"
                + "pa[yCoord=690.0, xCoord=510.0]\n"
                + "pb[yCoord=690.0, xCoord=1470.0]\n"
                + "qbadA[yCoord=810.0, bad=\"true\", xCoord=2250.0]\n"
                + "qbadB[yCoord=210.0, bad=\"true\", xCoord=2250.0]\n"
                + "s1[yCoord=150.0, xCoord=510.0, env=\"true\"]\n"
                + "s2[yCoord=150.0, xCoord=1470.0, env=\"true\"]\n"
                + "s3[yCoord=450.0, xCoord=510.0]\n"
                + "s4[yCoord=450.0, xCoord=1470.0]\n"
                + "\n"
                + ".transitions\n"
                + "t1[label=\"t1\", yCoord=570.0, xCoord=510.0]\n"
                + "t10[label=\"t10\", yCoord=810.0, xCoord=1830.0]\n"
                + "t11[label=\"t11\", yCoord=690.0, xCoord=1830.0]\n"
                + "t12[label=\"t12\", yCoord=210.0, xCoord=1830.0]\n"
                + "t13[label=\"t13\", yCoord=450.0, xCoord=2010.0]\n"
                + "t14[label=\"t14\", yCoord=690.0, xCoord=2010.0]\n"
                + "t15[label=\"t15\", yCoord=330.0, xCoord=2010.0]\n"
                + "t16[label=\"t16\", yCoord=570.0, xCoord=2010.0]\n"
                + "t2[label=\"t2\", yCoord=570.0, xCoord=630.0]\n"
                + "t3[label=\"t3\", yCoord=570.0, xCoord=1350.0]\n"
                + "t4[label=\"t4\", yCoord=570.0, xCoord=1470.0]\n"
                + "t5[label=\"t5\", yCoord=810.0, xCoord=630.0]\n"
                + "t6[label=\"t6\", yCoord=810.0, xCoord=390.0]\n"
                + "t7[label=\"t7\", yCoord=810.0, xCoord=1590.0]\n"
                + "t8[label=\"t8\", yCoord=810.0, xCoord=1350.0]\n"
                + "t9[label=\"t9\", yCoord=330.0, xCoord=1830.0]\n"
                + "ta[label=\"ta\", yCoord=330.0, xCoord=510.0]\n"
                + "taa[label=\"taa\", yCoord=570.0, xCoord=930.0]\n"
                + "tb[label=\"tb\", yCoord=330.0, xCoord=1470.0]\n"
                + "tbb[label=\"tbb\", yCoord=570.0, xCoord=1050.0]\n"
                + "tt1[label=\"tt1\", yCoord=-210.0, xCoord=2010.0]\n"
                + "tt2[label=\"tt2\", yCoord=30.0, xCoord=2010.0]\n"
                + "tt3[label=\"tt3\", yCoord=-90.0, xCoord=2010.0]\n"
                + "tt4[label=\"tt4\", yCoord=150.0, xCoord=2010.0]\n"
                + "tt5[label=\"tt5\", yCoord=1110.0, xCoord=2010.0]\n"
                + "tt6[label=\"tt6\", yCoord=870.0, xCoord=2010.0]\n"
                + "tt7[label=\"tt7\", yCoord=1230.0, xCoord=2010.0]\n"
                + "tt8[label=\"tt8\", yCoord=990.0, xCoord=2010.0]\n"
                + "tx[label=\"tx\", yCoord=150.0, xCoord=750.0]\n"
                + "ty[label=\"ty\", yCoord=150.0, xCoord=1230.0]\n"
                + "\n"
                + ".flows\n"
                + "t1: {1*s3} -> {1*pa}\n"
                + "t10: {1*ea, 1*bb} -> {1*ea, 1*qbadB}\n"
                + "t11: {1*ea, 1*ab} -> {1*qbadA, 1*ea}\n"
                + "t12: {1*ba, 1*eb} -> {1*qbadB, 1*eb}\n"
                + "t13: {1*env, 1*aa} -> {1*env, 1*qbadA}\n"
                + "t14: {1*env, 1*ab} -> {1*qbadA, 1*env}\n"
                + "t15: {1*ba, 1*env} -> {1*qbadB, 1*env}\n"
                + "t16: {1*bb, 1*env} -> {1*env, 1*qbadB}\n"
                + "t2: {1*a} -> {1*pa}\n"
                + "t3: {1*b} -> {1*pb}\n"
                + "t4: {1*s4} -> {1*pb}\n"
                + "t5: {1*pa} -> {1*ab}\n"
                + "t6: {1*pa} -> {1*aa}\n"
                + "t7: {1*pb} -> {1*bb}\n"
                + "t8: {1*pb} -> {1*ba}\n"
                + "t9: {1*aa, 1*eb} -> {1*qbadA, 1*eb}\n"
                + "ta: {1*a, 1*s1} -> {1*ea, 1*s3}\n"
                + "taa: {1*s3, 1*b} -> {1*pb, 1*pa}\n"
                + "tb: {1*b, 1*s2} -> {1*eb, 1*s4}\n"
                + "tbb: {1*a, 1*s4} -> {1*pb, 1*pa}\n"
                + "tt1: {1*s1, 1*aa} -> {1*qbadA, 1*s1}\n"
                + "tt2: {1*s1, 1*ab} -> {1*s1, 1*qbadA}\n"
                + "tt3: {1*s1, 1*ba} -> {1*qbadB, 1*s1}\n"
                + "tt4: {1*s1, 1*bb} -> {1*s1, 1*qbadB}\n"
                + "tt5: {1*aa, 1*s2} -> {1*qbadA, 1*s2}\n"
                + "tt6: {1*s2, 1*ab} -> {1*s2, 1*qbadA}\n"
                + "tt7: {1*s2, 1*ba} -> {1*s2, 1*qbadB}\n"
                + "tt8: {1*s2, 1*bb} -> {1*s2, 1*qbadB}\n"
                + "tx: {1*env} -> {1*s1}\n"
                + "ty: {1*env} -> {1*s2}\n"
                + "\n"
                + ".initial_marking {1*a, 1*b, 1*env}";

//        PetriGameWithTransits game = PGTools.getPetriGame(path + name + ".apt", true, false);
        PetriGameWithTransits game = PGTools.getPetriGameFromAPTString(apt, true, false);

        BDDGraph bddGraph = new BDDGraph("test");
        GGBuilderStepwise builder = new GGBuilderStepwise(game, bddGraph);
        builder.addSuccessors(bddGraph.getInitial(), game, bddGraph);

//        for (BDDState state : bddGraph.getStates()) {
//            System.out.println(state.getContent());
//            System.out.println("neext");
//            builder.addSuccessors(state, game, bddGraph);
//            for (BDDState state1 : bddGraph.getStates()) {
//                System.out.println(state1.getContent());
//            }
//        }
//        BDDSolverOptions opts = new BDDSolverOptions(true, true);
//        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        solv.initialize();
//        BDDGraph graph = new BDDGraph("burglar_gg");
//        BDDState init = BDDGraphGameBuilderStepwise.addInitialState(graph, solv);
//        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.addSuccessors(init, graph, solv);
//
//        for (BDDState bDDState : succs.getSecond()) {
//            Pair<List<Flow>, List<BDDState>> succers = BDDGraphGameBuilderStepwise.addSuccessors(bDDState, graph, solv);
//
//        }
    }

//    @Test
//    public void testStepwiseFirstExample() throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
//        final String path = inputDir + "firstExamplePaper" + File.separator;
//        final String name = "firstExamplePaper";
//        BDDSolverOptions opts = new BDDSolverOptions(true, true);
//        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(path + name + ".apt", opts);
//        solv.initialize();
//        BDDGraph graph = new BDDGraph("firstExamplePaper_gg");
//        BDDState init = BDDGraphGameBuilderStepwise.addInitialState(graph, solv);
//        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.addSuccessors(init, graph, solv);
//        for (BDDState bDDState : succs.getSecond()) {
////            Pair<List<Flow>, List<BDDState>> succers = BDDGraphGameBuilderStepwise.getSuccessors(bDDState, graph, solv);
////            System.out.println(bDDState.toString());
//        }
//    }
//
//    @Test
//    public void testSuccessors() throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableConditionException, CouldNotCalculateException, SolvingException {
//        final String apt = ".name \"Workflow_M3WP1\"\n"
//                + ".type LPN\n"
//                + ".options\n"
//                + "condition=\"A_SAFETY\"\n"
//                + "\n"
//                + ".places\n"
//                + "A0[token=2]\n"
//                + "A1[token=4]\n"
//                + "A2[token=6]\n"
//                + "B00[bad=\"true\", token=1]\n"
//                + "B10[bad=\"true\", token=1]\n"
//                + "B20[bad=\"true\", token=1]\n"
//                + "Env[env=\"true\"]\n"
//                + "G0[token=2]\n"
//                + "G1[token=4]\n"
//                + "G2[token=6]\n"
//                + "M00[token=1]\n"
//                + "M10[token=1]\n"
//                + "M20[token=1]\n"
//                + "S0[token=1]\n"
//                + "e[env=\"true\"]\n"
//                + "testP0[token=3]\n"
//                + "testP1[token=5]\n"
//                + "testP2[token=7]\n"
//                + "\n"
//                + ".transitions\n"
//                + "t0[label=\"t0\"]\n"
//                + "t1[label=\"t1\"]\n"
//                + "t10[label=\"t10\"]\n"
//                + "t11[label=\"t11\"]\n"
//                + "t2[label=\"t2\"]\n"
//                + "t3[label=\"t3\"]\n"
//                + "t4[label=\"t4\"]\n"
//                + "t5[label=\"t5\"]\n"
//                + "t6[label=\"t6\"]\n"
//                + "t7[label=\"t7\"]\n"
//                + "t8[label=\"t8\"]\n"
//                + "t9[label=\"t9\"]\n"
//                + "test0[label=\"test0\"]\n"
//                + "test1[label=\"test1\"]\n"
//                + "test2[label=\"test2\"]\n"
//                + "\n"
//                + ".flows\n"
//                + "t0: {1*Env} -> {1*e, 1*testP1, 1*testP2, 1*A2, 1*A1}\n"
//                + "t1: {1*Env} -> {1*e, 1*A0, 1*testP2, 1*A2, 1*testP0}\n"
//                + "t10: {1*M20} -> {1*B20}\n"
//                + "t11: {1*M20, 1*A2} -> {1*G2}\n"
//                + "t2: {1*Env} -> {1*A0, 1*testP0, 1*e, 1*testP1, 1*A1}\n"
//                + "t3: {1*S0} -> {1*M00}\n"
//                + "t4: {1*M00} -> {1*B00}\n"
//                + "t5: {1*A0, 1*M00} -> {1*G0}\n"
//                + "t6: {1*S0} -> {1*M10}\n"
//                + "t7: {1*M10} -> {1*B10}\n"
//                + "t8: {1*M10, 1*A1} -> {1*G1}\n"
//                + "t9: {1*S0} -> {1*M20}\n"
//                + "test0: {1*S0, 1*testP0} -> {1*S0}\n"
//                + "test1: {1*S0, 1*testP1} -> {1*S0}\n"
//                + "test2: {1*S0, 1*testP2} -> {1*S0}\n"
//                + "\n"
//                + ".initial_marking {1*Env, 1*S0}";
//
//        PetriGameWithTransits game = PGTools.getPetriGameFromAPTString(apt, true, true);
//        BDDSolverOptions opts = new BDDSolverOptions(true, true);
//        DistrSysBDDSolver<? extends Condition> solv = DistrSysBDDSolverFactory.getInstance().getSolver(game, opts);
//        solv.initialize();
//        BDDGraph graph = new BDDGraph("firstExamplePaper_gg");
//        BDDState init = BDDGraphGameBuilderStepwise.addInitialState(graph, solv);
//        Pair<List<Flow>, List<BDDState>> succs = BDDGraphGameBuilderStepwise.addSuccessors(init, graph, solv);
//        for (BDDState bDDState : succs.getSecond()) {
//            Pair<List<Flow>, List<BDDState>> succs2 = BDDGraphGameBuilderStepwise.addSuccessors(bDDState, graph, solv);
////            System.out.println(succs2.getSecond().size());
//        }
//    }
}
