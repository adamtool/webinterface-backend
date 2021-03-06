package uniolunisaar.adam.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.circuits.CircuitRendererSettings;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitFlowLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitFlowLTLMCStatistics;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.exceptions.logics.NotConvertableException;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc.VerificationAlgo;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitMCSettings;
import uniolunisaar.adam.logic.transformers.petrinet.pn2aiger.AigerRenderer;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelcheckerATVA2019 {

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
        RunLTLFormula f = FlowLTLParser.parse(pnwt, formula);

        String output = args[1];

        AdamCircuitFlowLTLMCStatistics stats = null;
        if (!args[5].isEmpty()) {
            stats = new AdamCircuitFlowLTLMCStatistics(args[5]);
        } else {
            stats = new AdamCircuitFlowLTLMCStatistics();
        }
        stats.setPrintSysCircuitSizes(false);
        // add nb switches to file for the SDN paper        
        if (!args[5].isEmpty()) {
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(args[5] + "_sw"))) {
                wr.append("nb_switches: ").append((CharSequence) pnwt.getExtension("nb_switches"));
            }
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

        AdamCircuitFlowLTLMCOutputData data = new AdamCircuitFlowLTLMCOutputData(output, false, false, false);

        AdamCircuitLTLMCSettings settings = new AdamCircuitLTLMCSettings(
                data,
                AdamCircuitMCSettings.Maximality.MAX_INTERLEAVING_IN_CIRCUIT,
                AdamCircuitMCSettings.Stuttering.PREFIX_REGISTER,
                CircuitRendererSettings.TransitionSemantics.OUTGOING,
                CircuitRendererSettings.TransitionEncoding.LOGARITHMIC,
                CircuitRendererSettings.AtomicPropositions.PLACES_AND_TRANSITIONS,
                AigerRenderer.OptimizationsSystem.NONE, AigerRenderer.OptimizationsComplete.NONE,
                algo);

        if (algo == null) {
            settings.setVerificationAlgo(new VerificationAlgo[]{VerificationAlgo.IC3});
        }
        settings.setAbcParameters(abcParameter);
        settings.setStatistics(stats);

        ModelCheckerFlowLTL mc = new ModelCheckerFlowLTL(settings);
        mc.check(pnwt, f);
        if (!args[5].isEmpty()) {
            stats.addResultToFile();
        }
    }

}
