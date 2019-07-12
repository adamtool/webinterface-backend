package uniolunisaar.adam.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
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
import uniolunisaar.adam.logic.transformers.pn2aiger.AigerRenderer;
import uniolunisaar.adam.logic.transformers.pn2aiger.AigerRenderer.OptimizationsComplete;
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
     * be annotated within the APT input file) 5 -> optimized rendering of the
     * system 6 -> optimized rendering of the McHyper result 7 -> if != "" sizes
     * would be written to the given path
     *
     * @throws ParseException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotConvertableException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     */
    public static void main(String[] args) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException {
        int idInput = 0;
        int idOutput = 1;
        int idVeri = 2;
        int idABC = 3;
        int idFormula = 4;
        int idOptSys = 5;
        int idOptComp = 6;
        int idOutSizes = 7;

        String input = args[idInput];
        PetriNet net = Tools.getPetriNet(input);

        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);
        String formula = (args[idFormula].isEmpty()) ? (String) pnwt.getExtension("formula") : args[idFormula];
        RunFormula f = FlowLTLParser.parse(pnwt, formula);

        String output = args[idOutput];

        ModelcheckingStatistics stats;
        if (!args[idOutSizes].isEmpty()) {
            stats = new ModelcheckingStatistics(args[idOutSizes]);
        } else {
            stats = new ModelcheckingStatistics();
        }
        stats.setPrintSysCircuitSizes(true);
        // add nb switches to file for the SDN paper        
        if (!args[idOutSizes].isEmpty()) {
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(args[idOutSizes] + "_sw"))) {
                wr.append("nb_switches: ").append((CharSequence) pnwt.getExtension("nb_switches"));
            }
        }

        Abc.VerificationAlgo algo = null;
        String veri = args[idVeri];
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

        String abcParameter = args[idABC];

        String optimizations = args[idOptSys];
        AigerRenderer.OptimizationsSystem optisSys = AigerRenderer.OptimizationsSystem.NONE;
        if (optimizations.equals("GATES")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES;
        } else if (optimizations.equals("GATES-AND-INDICES")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES;
        } else if (optimizations.equals("GATES-AND-INDICES-EXTRA")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES_EXTRA;
        }

        String allOpts = args[idOptComp];
        OptimizationsComplete optsComp = OptimizationsComplete.NONE;
        if (allOpts.equals("REGEX-GATES")) {
            optsComp = OptimizationsComplete.NB_GATES_BY_REGEX;
        } else if (allOpts.equals("REGEX-GATES-SQUE")) {
            optsComp = OptimizationsComplete.NB_GATES_BY_REGEX_WITH_IDX_SQUEEZING;
        } else if (allOpts.equals("DS-GATES")) {
            optsComp = OptimizationsComplete.NB_GATES_BY_DS;
        } else if (allOpts.equals("DS-GATES-SQUE")) {
            optsComp = OptimizationsComplete.NB_GATES_BY_DS_WITH_IDX_SQUEEZING;
        } else if (allOpts.equals("DS-GATES-SQUE-EXTRA")) {
            optsComp = OptimizationsComplete.NB_GATES_BY_DS_WITH_IDX_SQUEEZING_AND_EXTRA_LIST;
        }

        ModelCheckerFlowLTL mc = new ModelCheckerFlowLTL(optisSys, optsComp);
        if (algo != null) {
            mc.setVerificationAlgo(algo);
        }
        mc.setAbcParameters(abcParameter);

        ModelCheckingOutputData data = new ModelCheckingOutputData(output, false, false, false);
        mc.check(pnwt, f, data, stats);
        if (!args[idOutSizes].isEmpty()) {
            stats.addResultToFile();
            // add ABC times to the file
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(args[idOutSizes], true))) {
                wr.append("\nABC time:").append(String.valueOf(stats.getAbc_sec()));
                wr.append("\nABC memory:").append(String.valueOf(stats.getAbc_mem()));
            }
        }
    }

}
