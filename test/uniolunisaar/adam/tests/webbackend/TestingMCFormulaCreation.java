package uniolunisaar.adam.tests.webbackend;

import java.io.File;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.AdamModelChecker;
import uniolunisaar.adam.ds.logics.ltl.ILTLFormula;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitFlowLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitFlowLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitFlowLTLMCStatistics;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.logics.NotConvertableException;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PNWTTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestingMCFormulaCreation {

    private static final String inputDir = System.getProperty("examplesfolder") + "/modelchecking/ltl/";
    private static final String outputDir = System.getProperty("testoutputfolder") + "/TestingMCFormulaCreation/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setShortMessageStream(null);
        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    /**
     *
     * @param net
     * @param formula
     * @return
     * @throws ParseException
     * @throws NotConvertableException
     */
    public ILTLFormula createLTLFormula(PetriNetWithTransits net, String formula) throws ParseException, NotConvertableException {
        RunLTLFormula runFormula = AdamModelChecker.parseFlowLTLFormula(net, formula);
        AdamCircuitFlowLTLMCOutputData data = new AdamCircuitFlowLTLMCOutputData(
                outputDir + net.getName(), false, false, false);
        AdamCircuitFlowLTLMCSettings settings = new AdamCircuitFlowLTLMCSettings(data);
        AdamCircuitFlowLTLMCStatistics statistics = new AdamCircuitFlowLTLMCStatistics();
        settings.setStatistics(statistics);

        PetriNet modelCheckingNet = AdamModelChecker.getModelCheckingNet(net, runFormula, settings);
        return AdamModelChecker.getModelCheckingFormula(
                net, modelCheckingNet, runFormula, settings);
    }

    @Test(enabled = true)
    public void testMCFormulaCreation() throws ParseException, IOException, NotConvertableException {
//        String path = inputDir + "ATVA19_motivatingExample.apt";
//        PetriNetWithTransits net = PNWTTools.getPetriNetWithTransitsFromFile(path, false);
        String content = ".name \"MotivatingExample\"\n"
                + ".description \"The motivating example of the ATVA19 conference.\"\n"
                + ".type LPN\n"
                + ".options\n"
                + "condition=\"LTL\"\n"
                + "\n"
                + ".places\n"
                + "A[yCoord=930.0, xCoord=690.0]\n"
                + "C[yCoord=750.0, xCoord=1290.0]\n"
                + "L[yCoord=930.0, xCoord=1770.0]\n"
                + "P[yCoord=510.0, xCoord=1770.0]\n"
                + "S[yCoord=510.0, xCoord=690.0]\n"
                + "p10[yCoord=990.0, xCoord=990.0]\n"
                + "p11[yCoord=450.0, xCoord=990.0]\n"
                + "p12[yCoord=736.84, xCoord=544.2]\n"
                + "p13[yCoord=1230.0, xCoord=450.0]\n"
                + "p14[yCoord=1230.0, xCoord=2070.0]\n"
                + "p16[yCoord=330.0, xCoord=450.0]\n"
                + "p17[yCoord=750.0, xCoord=2070.0]\n"
                + "p18[yCoord=329.65, xCoord=2072.3]\n"
                + "p5[yCoord=1350.0, xCoord=1290.0]\n"
                + "p6[yCoord=704.92, xCoord=1590.0]\n"
                + "p7[yCoord=1110.0, xCoord=1470.0]\n"
                + "p8[yCoord=750.0, xCoord=1890.0]\n"
                + "p9[yCoord=450.0, xCoord=1530.0]\n"
                + "\n"
                + ".transitions\n"
                + "ingress[label=\"ingress\", yCoord=510.0, xCoord=570.0, tfl=\"S -> {S},> -> {S}\"]\n"
                + "t0[label=\"t0\", yCoord=630.0, xCoord=990.0, tfl=\"S -> {C},C -> {C}\", weakFair=\"true\"]\n"
                + "t1[label=\"t1\", yCoord=726.0, xCoord=685.0, tfl=\"S -> {A},A -> {A}\", weakFair=\"true\"]\n"
                + "t10[label=\"t10\", yCoord=1110.0, xCoord=2070.0, weakFair=\"true\"]\n"
                + "t11[label=\"t11\", yCoord=450.0, xCoord=2070.0, weakFair=\"true\"]\n"
                + "t2[label=\"t2\", yCoord=870.0, xCoord=990.0, tfl=\"A -> {C},C -> {C}\", weakFair=\"true\"]\n"
                + "t3[label=\"t3\", yCoord=750.0, xCoord=1769.0, tfl=\"P -> {P},L -> {P}\", weakFair=\"true\"]\n"
                + "t4[label=\"t4\", yCoord=630.0, xCoord=1530.0, tfl=\"P -> {P},C -> {P}\", weakFair=\"true\"]\n"
                + "t5[label=\"t5\", yCoord=810.0, xCoord=1590.0, tfl=\"C -> {C},L -> {C}\", weakFair=\"true\"]\n"
                + "t6[label=\"t6\", yCoord=990.0, xCoord=1470.0, tfl=\"C -> {L},L -> {L}\", weakFair=\"true\"]\n"
                + "t7[label=\"t7\", yCoord=1230.0, xCoord=1290.0]\n"
                + "t9[label=\"t9\", yCoord=450.0, xCoord=450.0, weakFair=\"true\"]\n"
                + "\n"
                + ".flows\n"
                + "ingress: {1*S} -> {1*S}\n"
                + "t0: {1*p11, 1*S, 1*C} -> {1*p11, 1*C, 1*S}\n"
                + "t1: {1*p12, 1*A, 1*S} -> {1*A, 1*S, 1*p12}\n"
                + "t10: {1*p14, 1*p6} -> {1*p17, 1*p7}\n"
                + "t11: {1*p9, 1*p17} -> {1*p18, 1*p8}\n"
                + "t2: {1*p10, 1*A, 1*C} -> {1*C, 1*p10, 1*A}\n"
                + "t3: {1*P, 1*L, 1*p8} -> {1*p8, 1*L, 1*P}\n"
                + "t4: {1*p9, 1*C, 1*P} -> {1*p9, 1*P, 1*C}\n"
                + "t5: {1*p6, 1*L, 1*C} -> {1*L, 1*p6, 1*C}\n"
                + "t6: {1*C, 1*p7, 1*L} -> {1*C, 1*p7, 1*L}\n"
                + "t7: {1*p5} -> {1*p13, 1*p14}\n"
                + "t9: {1*p13, 1*p12} -> {1*p16, 1*p11}\n"
                + "\n"
                + ".initial_marking {1*A, 1*C, 1*L, 1*P, 1*S, 1*p10, 1*p12, 1*p5, 1*p6, 1*p9}";
        PetriNetWithTransits net = PNWTTools.getPetriNetWithTransits(content, false);
        createLTLFormula(net, "A F P");
    }

}
