package uniolunisaar.adam.main;

import java.io.IOException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunFormula;
import uniolunisaar.adam.ds.modelchecking.ModelcheckingStatistics;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.exceptions.logics.NotConvertableException;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc.VerificationAlgo;
import uniolunisaar.adam.logic.modelchecking.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.util.logics.transformers.logics.ModelCheckingOutputData;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelchecker {

    /**
     *
     * @param args 0 -> Input path to APT 1 -> Output path for data 2 ->
     * Verifier 3 -> ABC parameters 4 -> Formula (if "", then it's expected to
     * be annotated within the APT input file) 5 -> if != "" sizes would be
     * written to the given path
     *
     * @throws ParseException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotConvertableException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     */
    public static void main(String[] args) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException {
        String input = args[0];
        PetriNet net = Tools.getPetriNet(input);

        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);
        String formula = (args[4].isEmpty()) ? (String) pnwt.getExtension("formula") : args[4];
        RunFormula f = FlowLTLParser.parse(pnwt, formula);

        String output = args[1];

        ModelcheckingStatistics stats = null;
        if (!args[5].isEmpty()) {
            stats = new ModelcheckingStatistics(args[5]);
        } else {
            stats = new ModelcheckingStatistics();
        }

        Abc.VerificationAlgo algo = null;
        String veri = args[2];
        if (veri.equals("IC3")) {
            algo = VerificationAlgo.IC3;
        } else if (veri.equals("INT")) {
            algo = VerificationAlgo.INT;
        } else if (veri.equals("BMC")) {
            algo = VerificationAlgo.BMC;
        } else if (veri.equals("BMC2")) {
            algo = VerificationAlgo.BMC2;
        } else if (veri.equals("BMC3")) {
            algo = VerificationAlgo.BMC3;
        }

        String abcParameter = args[3];

        ModelCheckerFlowLTL mc = new ModelCheckerFlowLTL();
        if (algo != null) {
            mc.setVerificationAlgo(algo);
        }
        mc.setAbcParameters(abcParameter);

        ModelCheckingOutputData data = new ModelCheckingOutputData(output, false, false, false);
        mc.check(pnwt, f, data, stats);
        if (!args[5].isEmpty()) {
            stats.addResultToFile();
        }
    }

}
