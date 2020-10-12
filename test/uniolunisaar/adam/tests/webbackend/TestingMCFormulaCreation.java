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

    @Test(enabled=true)
    public void testMCFormulaCreation() throws ParseException, IOException, NotConvertableException {
        String path = inputDir + "ATVA19_motivatingExample.apt";
        PetriNetWithTransits net = PNWTTools.getPetriNetWithTransitsFromFile(path, false);
        createLTLFormula(net, "A F P");
    }

}
