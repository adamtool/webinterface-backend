package uniolunisaar.adam.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.logics.ctl.flowctl.separate.RunCTLSeparateFormula;
import uniolunisaar.adam.ds.modelchecking.results.ModelCheckingResult;
import uniolunisaar.adam.ds.modelchecking.settings.ModelCheckingSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ctl.FlowCTLLoLAModelcheckingSettings;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.exceptions.logics.NotConvertableException;
import uniolunisaar.adam.logic.modelchecking.ctl.ModelCheckerFlowCTL;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelcheckerAccessControl {

    /**
     *
     * @param args
     *
     * 0 -> benchmark (airport, office, university,
     * semicolon-separated-scalability)<\p>
     * 1 -> approach (parIn, seqIn)
     *
     * @throws ParseException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotConvertableException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     */
    public static void main(String[] args) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException {
        String benchmark = args[0];
        String approach = args[1];
        FlowCTLLoLAModelcheckingSettings settings = new FlowCTLLoLAModelcheckingSettings(benchmark, false);
        switch (approach) {
            case "parIn":
                settings.setApproach(ModelCheckingSettings.Approach.PARALLEL_INHIBITOR);
                break;
            case "seqIn":
                settings.setApproach(ModelCheckingSettings.Approach.SEQUENTIAL_INHIBITOR);
                break;
            default:
                System.out.println("There is no approach: " + approach + "."
                        + " Usage: java -jar adam.jar <benchmark (airport|office|university|semicolon-separated-scalability)> <approach (parIn|seqIn)>");
                return;
        }
        ModelCheckerFlowCTL mc = new ModelCheckerFlowCTL(settings);
        PetriNetWithTransits pn = null;
        RunCTLSeparateFormula formula = null;
        switch (benchmark) {
            case "airport":
                break;
            case "office":
                break;
            case "university":
                break;
            default: // do what ever we want to do with the scalability benchmark (load and parse, or parse the semicolon-separated list and start a generator)
                break;
        }
        try {
            ModelCheckingResult result = mc.check(pn, formula);
            System.out.println("Benchmark '" + benchmark + "' is satisfied: " + result.getSatisfied().toString());
        } catch (RenderException | FileNotFoundException ex) {
            System.out.println("We are sorry, the modelchecking routine finished with an error: " + ex.toString());
        }
    }

}
