package uniolunisaar.adam.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.PnmlPNParser;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.io.renderer.impl.LoLAPNRenderer;
import uniolunisaar.adam.ds.logics.ltl.ILTLFormula;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunFormula;
import uniolunisaar.adam.ds.modelchecking.ModelCheckingResult;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitFlowLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitFlowLTLMCStatistics;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.exceptions.logics.NotConvertableException;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc.VerificationAlgo;
import uniolunisaar.adam.logic.modelchecking.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.logic.modelchecking.circuits.ModelCheckerLTL;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.logic.parser.logics.mccformula.MCCXMLFormulaParser;
import uniolunisaar.adam.logic.transformers.pn2aiger.AigerRenderer;
import uniolunisaar.adam.logic.transformers.pn2aiger.AigerRenderer.OptimizationsComplete;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.settings.AdamCircuitFlowLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.AdamCircuitLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.LoLASettings;
import uniolunisaar.adam.ds.modelchecking.settings.ModelCheckingSettings.Approach;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitLTLMCStatistics;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PNTools;
import uniolunisaar.adam.util.benchmarks.modelchecking.BenchmarksMC;
import uniolunisaar.adam.util.logics.LogicsTools;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelchecker {

    /**
     * Needs to get a ;-separated string as args[0]. The order must fit to
     * mainWithParameterList. z.B. TACAS'20:
     * file,,IC3,,formula,NONE,NONE,inCircuitWithNotStuckingFormula,GFANDNpi,,logCod,parIn,sdn,EDACC
     *
     * @param args
     * @throws ParseException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotConvertableException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws RenderException
     */
    public static void main(String[] args) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException, SAXException, ParserConfigurationException, RenderException {
        mainWithParameterList(args[0].split(","));
    }

    /**
     *
     * @param args<\p>
     * 0 -> Input path to APT<\p>
     * 1 -> Output path for data<\p>
     * 2 -> Verifier<\p>
     * 3 -> ABC parameters<\p>
     * 4 -> Formula (if "", then it's expected to be annotated within the APT
     * input file)<\p>
     * 5 -> optimized rendering of the system<\p>
     * 6 -> optimized rendering of the McHyper result<\p>
     * 7 -> maximality (NONE, inCircuit, inFormula,
     * inCircuitWithNotStuckingFormula)<\p>
     * 8 -> stucking in subnet approaches (GFO, GFANDNpi, ANDGFNpi,
     * GFoANDNpi)<\p>
     * 9 -> if != "" sizes would be written to the given path <\p>
     * 10 -> if == "logCod" codes the transition logarithmically in the circuit
     * <\p>
     * 11 -> approach (seq, seqIn, par, parIn)<\p>
     * 12 -> if "gen" then only creates a text file of the formulas for ADAM and
     * LoLA and converts the net also into LoLA format <\p>
     * if "mcc" expecting a Petri net in pnml format and a path to a formula in
     * the mcc format as input, checks all formulas in this call<\p>
     * if "mccOne" only checks one given LTL formula with one net<\p>
     * if "lola" tries to check the SDN examples with lola else compares the SDN
     * approach for several gate optimizations<\p>
     * if "cpMCC" is used to give the needed output of the mcc contest for the
     * comparison to lola, itstools, and enpac <\p>
     * 13 -> EDACC (if EDACC everything is put to silence, not files are created
     * and only the relevant outputs are send to channel edacc)
     *
     *
     * @throws ParseException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotConvertableException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     */
    public static void mainWithParameterList(String[] args) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException, SAXException, ParserConfigurationException, RenderException {
        if (args[13].equals("EDACC")) {
            BenchmarksMC.EDACC = true;
            Logger.getInstance().addMessageStream("edacc", System.out);
            Logger.getInstance().setSilent(true);
        }
        if (args[12].equals("gen")) {
            genExamples(args);
        } else if (args[12].equals("lola")) {
            checkSDNLoLA(args);
        } else {
            check(args);
        }
    }

    /**
     * args[0] -> should contain the input Petri net in pnml format <\p>
     * args[1] -> should contain the output path <\p>
     * args[4] -> should contain the input xml file with the formulas in the mcc
     * format <\p>
     *
     * @param args
     * @throws uniol.apt.io.parser.ParseException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws uniol.apt.io.renderer.RenderException
     * @throws uniolunisaar.adam.exceptions.logics.NotConvertableException
     */
    public static void genExamples(String[] args) throws ParseException, IOException, SAXException, ParserConfigurationException, RenderException, NotConvertableException {
        // ADAM part
        PetriNet net = new PnmlPNParser().parseFile(args[0]);
        Map<String, ILTLFormula> formula = MCCXMLFormulaParser.parseLTLFromFile(args[4], net);
        StringBuilder sb = new StringBuilder();
        StringBuilder sbLolA = new StringBuilder();
        for (Map.Entry<String, ILTLFormula> entry : formula.entrySet()) {
            String key = entry.getKey();
            ILTLFormula value = entry.getValue();
            sb.append(key).append(";").append(value.toString()).append("\n");
            sbLolA.append(key).append(";").append("ALLPATH ").append(value.toLoLA()).append("\n");
        }
        Tools.saveFile(args[1] + "_formulas_adam.txt", sb.toString());
        Tools.saveFile(args[1] + "_formulas_lola.txt", sbLolA.toString());
        // LoLa part
        String lolaNet = new LoLAPNRenderer().render(net);
        Tools.saveFile(args[1].substring(0, args[1].lastIndexOf("/") + 1) + "model.lola", lolaNet);
    }

    /**
     * TODO: adapt the ids in this comment to the main method
     *
     * @param args 0 -> Input path to APT 1 -> Output path for data 2 ->
     * Verifier 3 -> ABC parameters 4 -> Formula (if "", then it's expected to
     * be annotated within the APT input file) 5 -> optimized rendering of the
     * system 6 -> optimized rendering of the McHyper result 7 -> if != "" sizes
     * would be written to the given path 8 -> iff mcc expecting a Petri net in
     * pnml format and a path to a formula in the mcc format as input
     *
     * @throws ParseException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotConvertableException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     */
    public static void check(String[] args) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException, SAXException, ParserConfigurationException {
        int idInput = 0;
        int idOutput = 1;
        int idVeri = 2;
        int idABC = 3;
        int idFormula = 4;
        int idOptSys = 5;
        int idOptComp = 6;
        int idMax = 7;
        int idStuckInSubnet = 8;
        int idOutSizes = 9;
        int idLogCod = 10;
        int idApproach = 11;
        int idMCC = 12;

        // For both approaches 
        String[] veris = args[idVeri].split("\\|");
        Abc.VerificationAlgo[] algos = new Abc.VerificationAlgo[veris.length];
        for (int i = 0; i < veris.length; i++) {
            String veri = veris[i];
            Abc.VerificationAlgo algo = null;
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
            algos[i] = algo;
        }

        String abcParameter = args[idABC];

        String optimizations = args[idOptSys];
        AigerRenderer.OptimizationsSystem optisSys = AigerRenderer.OptimizationsSystem.NONE;
        if (optimizations.equals("GATES")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES;
        } else if (optimizations.equals("GATES-AND-EQCOM")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_EQCOM;
        } else if (optimizations.equals("GATES-AND-INDICES")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES;
        } else if (optimizations.equals("GATES-AND-INDICES-AND-EQCOM")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES_AND_EQCOM;
        } else if (optimizations.equals("GATES-AND-INDICES-EXTRA")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES_EXTRA;
        } else if (optimizations.equals("GATES-AND-INDICES-EXTRA-AND-EQCOM")) {
            optisSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES_EXTRA_AND_EQCOM;
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

        String output = args[idOutput];

        AdamCircuitLTLMCSettings.Maximality max = AdamCircuitFlowLTLMCSettings.Maximality.MAX_INTERLEAVING_IN_CIRCUIT;
        boolean inCircuitWithNotStucking = false;
        if (args[idMax].equals("NONE")) {
            max = AdamCircuitLTLMCSettings.Maximality.MAX_NONE;
        } else if (args[idMax].equals("inFormula")) {
            max = AdamCircuitLTLMCSettings.Maximality.MAX_INTERLEAVING;
        } else if (args[idMax].equals("inCircuitWithNotStuckingFormula")) {
            inCircuitWithNotStucking = true;
        }

        boolean logCod = false;
        if (args[idLogCod].equals("logCod")) {
            logCod = true;
        }

        String input = args[idInput];
        if (args[idMCC].equals("mcc")) { // the mcc case
            AdamCircuitLTLMCStatistics stats;
            if (!args[idOutSizes].isEmpty()) {
                stats = new AdamCircuitLTLMCStatistics(args[idOutSizes]);
            } else {
                stats = new AdamCircuitLTLMCStatistics();
            }
            stats.setPrintSysCircuitSizes(true);
            checkMCCAllFormulasAtOnce(input, output, algos, abcParameter, stats, args, idFormula, idOutSizes, max, logCod);
        } else if (args[idMCC].equals("mccOne")) {
            AdamCircuitLTLMCStatistics stats;
            if (!args[idOutSizes].isEmpty()) {
                stats = new AdamCircuitLTLMCStatistics(args[idOutSizes]);
            } else {
                stats = new AdamCircuitLTLMCStatistics();
            }
            stats.setPrintSysCircuitSizes(true);
            checkMCCOneFormula(input, output, algos, abcParameter, stats, args, idFormula, idOutSizes, max, logCod);
        } else if (args[idMCC].equals("cpMCC")) {
            // don't waste to much time for writing and so on
//            AdamCircuitLTLMCStatistics stats;
//            if (!args[idOutSizes].isEmpty()) {
//                stats = new AdamCircuitLTLMCStatistics(args[idOutSizes]);
//            } else {
//                stats = new AdamCircuitLTLMCStatistics();
//            }
//            stats.setPrintSysCircuitSizes(true);
//            compareWithAllOtherMCCTools(input, output, algos, abcParameter, stats, args, idFormula, idOutSizes, max, logCod);
            compareWithAllOtherMCCTools(input, output, algos, abcParameter, null, args, idFormula, idOutSizes, max, logCod);
        } else { // the optimization case
            AdamCircuitFlowLTLMCStatistics stats;
            if (!args[idOutSizes].isEmpty()) {
                stats = new AdamCircuitFlowLTLMCStatistics(args[idOutSizes]);
            } else {
                stats = new AdamCircuitFlowLTLMCStatistics();
            }
            stats.setPrintSysCircuitSizes(true);
            checkSDNExamples(input, output, optisSys, optsComp, algos, abcParameter, stats, args, idFormula, idOutSizes, max, args[idStuckInSubnet], logCod, inCircuitWithNotStucking, args[idApproach]);
        }
    }

    private static void checkMCCOneFormula(String input, String output,
            Abc.VerificationAlgo[] algo, String abcParameter, AdamCircuitLTLMCStatistics stats,
            String[] args, int idFormula, int idOutSizes, AdamCircuitLTLMCSettings.Maximality max, boolean logCod) throws ParseException, IOException, SAXException, ParserConfigurationException, InterruptedException, ProcessNotStartedException, ExternalToolException {
        PetriNet net = new PnmlPNParser().parseFile(input);
        PNTools.annotateProcessFamilyID(net);

        AdamCircuitLTLMCSettings settings = new AdamCircuitLTLMCSettings();
        settings.setMaximality(max);
        settings.setSemantics(LogicsTools.TransitionSemantics.OUTGOING);
        settings.setStuttering(AdamCircuitLTLMCSettings.Stuttering.PREFIX_REGISTER);
        if (algo != null) {
            settings.setVerificationAlgo(algo);
        }
        settings.setAbcParameters(abcParameter);

        AdamCircuitLTLMCOutputData data = new AdamCircuitLTLMCOutputData(output + "_out", false, false);
        settings.setOutputData(data);
        settings.setStatistics(stats);

        settings.setCodeInputTransitionsBinary(logCod);

        ModelCheckerLTL mc = new ModelCheckerLTL(settings); // todo: currently no optimizations integrated
        RunFormula f = FlowLTLParser.parse(net, args[idFormula]);
        ModelCheckingResult result = mc.check(net, f.toLTLFormula());

        if (!args[idOutSizes].isEmpty()) {
            stats.addResultToFile();
            // add ABC times to the file
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(args[idOutSizes], true))) {
                wr.append("\nAlgo:").append(result.getAlgo().name());
                wr.append("\nABC time:").append(String.valueOf(stats.getAbc_sec()));
                wr.append("\nABC memory:").append(String.valueOf(stats.getAbc_mem()));
            }
        }

    }

    private static void checkMCCAllFormulasAtOnce(String input, String output,
            Abc.VerificationAlgo[] algo, String abcParameter, AdamCircuitLTLMCStatistics stats,
            String[] args, int idFormula, int idOutSizes, AdamCircuitLTLMCSettings.Maximality max, boolean logCod) throws ParseException, IOException, SAXException, ParserConfigurationException, InterruptedException, ProcessNotStartedException, ExternalToolException {
        stats.setAppend(true);
        PetriNet net = new PnmlPNParser().parseFile(input);
        PNTools.annotateProcessFamilyID(net);
        Map<String, ILTLFormula> formula = MCCXMLFormulaParser.parseLTLFromFile(args[idFormula], net);

        AdamCircuitLTLMCSettings settings = new AdamCircuitLTLMCSettings();
        settings.setMaximality(max);
        settings.setSemantics(LogicsTools.TransitionSemantics.OUTGOING);
        settings.setStuttering(AdamCircuitLTLMCSettings.Stuttering.PREFIX_REGISTER);
        if (algo != null) {
            settings.setVerificationAlgo(algo);
        }
        settings.setAbcParameters(abcParameter);

        settings.setStatistics(stats);

        settings.setCodeInputTransitionsBinary(logCod);

        ModelCheckerLTL mc = new ModelCheckerLTL(settings); // todo: currently no optimizations integrated

        for (Map.Entry<String, ILTLFormula> entry : formula.entrySet()) {
            String id = entry.getKey();
            ILTLFormula f = entry.getValue();

            AdamCircuitLTLMCOutputData data = new AdamCircuitLTLMCOutputData(output + "_" + id, false, false);
            settings.setOutputData(data);
            ModelCheckingResult result = mc.check(net, f);

            if (!args[idOutSizes].isEmpty()) {
                stats.addResultToFile();
                // add ABC times to the file
                try (BufferedWriter wr = new BufferedWriter(new FileWriter(args[idOutSizes], true))) {
                    wr.append("\nAlgo:").append(result.getAlgo().name());
                    wr.append("\nABC time:").append(String.valueOf(stats.getAbc_sec()));
                    wr.append("\nABC memory:").append(String.valueOf(stats.getAbc_mem()));
                }
            }
        }
    }

    private static void compareWithAllOtherMCCTools(String input, String output,
            Abc.VerificationAlgo[] algo, String abcParameter, AdamCircuitLTLMCStatistics stats,
            String[] args, int idFormula, int idOutSizes, AdamCircuitLTLMCSettings.Maximality max, boolean logCod) {

        Entry<String, ILTLFormula> entry = null;
        try {

            PetriNet net;
            Map<String, ILTLFormula> formulas;
            try {
                net = new PnmlPNParser().parseFile(input);
                PNTools.annotateProcessFamilyID(net);
                formulas = MCCXMLFormulaParser.parseLTLFromFile(args[idFormula], net);
            } catch (ParseException | IOException | SAXException | ParserConfigurationException ex) {
                Logger.getInstance().addError("Error msg: " + ex.getMessage(), ex);
                Logger.getInstance().addMessage(true, "CANNOT_COMPUTE");
                return;
            }

            // in this case there should only be one
            entry = formulas.entrySet().iterator().next();

            AdamCircuitLTLMCSettings settings = new AdamCircuitLTLMCSettings();
            settings.setMaximality(max);
            settings.setSemantics(LogicsTools.TransitionSemantics.OUTGOING);
            settings.setStuttering(AdamCircuitLTLMCSettings.Stuttering.PREFIX_REGISTER);
            if (algo != null) {
                settings.setVerificationAlgo(algo);
            }
            settings.setAbcParameters(abcParameter);

//            stats.setAppend(true);
//            stats.setMeasure_abc(false);
//            settings.setStatistics(stats);

            settings.setCodeInputTransitionsBinary(logCod);

            ModelCheckerLTL mc = new ModelCheckerLTL(settings); // todo: currently no optimizations integrated

            AdamCircuitLTLMCOutputData data = new AdamCircuitLTLMCOutputData(output + "_" + entry.getKey(), false, false);
            settings.setOutputData(data);

            ModelCheckingResult result = mc.check(net, entry.getValue());
            Logger.getInstance().addMessage(true, "FORMULA " + entry.getKey() + " " + result.getSatisfied().name() + " SOME TECHNIQUE");

//            if (!args[idOutSizes].isEmpty()) {
//                stats.addResultToFile();
//            }
        } catch (InterruptedException | IOException | ParseException | ProcessNotStartedException | ExternalToolException ex) {
            if (entry != null) {
                Logger.getInstance().addError("Error msg: " + ex.getMessage(), ex);
                Logger.getInstance().addMessage(true, "FORMULA " + entry.getKey() + " CANNOT_COMPUTE");
            } else {
                Logger.getInstance().addError("Error msg: " + ex.getMessage(), ex);
                Logger.getInstance().addMessage(true, "CANNOT_COMPUTE");
            }
        }
    }

    private static void checkSDNExamples(String input, String output,
            AigerRenderer.OptimizationsSystem optisSys, OptimizationsComplete optsComp, Abc.VerificationAlgo[] algo, String abcParameter, AdamCircuitFlowLTLMCStatistics stats,
            String[] args, int idFormula, int idOutSizes, AdamCircuitLTLMCSettings.Maximality max, String stuckInSubnet, boolean logCod, boolean inCircuitWithoutStucking, String approach) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException {
        PetriNet net = Tools.getPetriNet(input);
        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);

        String formula = (args[idFormula].isEmpty()) ? (String) pnwt.getExtension("formula") : args[idFormula];
        RunFormula f = FlowLTLParser.parse(pnwt, formula);

        Approach appr = Approach.PARALLEL_INHIBITOR;
        if (approach.equals("seq")) {
            appr = Approach.SEQUENTIAL;
        } else if (approach.equals("seqIn")) {
            appr = Approach.SEQUENTIAL_INHIBITOR;
        } else if (approach.equals("par")) {
            appr = Approach.PARALLEL;
        } else if (approach.equals("parIn")) {
            appr = Approach.PARALLEL_INHIBITOR;
        } else {
            throw new RuntimeException("Not exceptable key for the approach " + approach);
        }

        // add nb switches to file for the SDN paper                
        if (!args[idOutSizes].isEmpty()) {
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(args[idOutSizes] + "_sw"))) {
                wr.append("nb_switches: ").append((CharSequence) pnwt.getExtension("nb_switches"));
            }
        }
        if (BenchmarksMC.EDACC) {
            Logger.getInstance().addMessage("nb_switches: " + pnwt.getExtension("nb_switches").toString(), "edacc");
        }
        AdamCircuitFlowLTLMCSettings settings = new AdamCircuitFlowLTLMCSettings(optisSys, optsComp);
        settings.setMaximality(max);
        settings.setSemantics(LogicsTools.TransitionSemantics.OUTGOING);
        settings.setStuttering(AdamCircuitLTLMCSettings.Stuttering.PREFIX_REGISTER);
        settings.setInitFirst(true);
        settings.setApproach(appr);
        if (algo != null) {
            settings.setVerificationAlgo(algo);
        }
        settings.setAbcParameters(abcParameter);

        AdamCircuitFlowLTLMCOutputData data = new AdamCircuitFlowLTLMCOutputData(output, false, false, false);
        settings.setOutputData(data);
        settings.setStatistics(stats);

        if (stuckInSubnet.equals("GFO")) {
            settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.GFo);
        } else if (stuckInSubnet.equals("ANDGFNpi")) {
            settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.ANDGFNpi);
        } else if (stuckInSubnet.equals("GFANDNpi")) {
            settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.GFANDNpi);
        } else if (stuckInSubnet.equals("GFoANDNpi")) {
            settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.GFANDNpiAndo);
        }

        settings.setCodeInputTransitionsBinary(logCod);
        settings.setNotStuckingAlsoByMaxInCircuit(inCircuitWithoutStucking);

        ModelCheckerFlowLTL mc = new ModelCheckerFlowLTL(settings);
        ModelCheckingResult result = mc.check(pnwt, f);

        if (!args[idOutSizes].isEmpty()) {
            stats.addResultToFile();
            // add ABC times to the file
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(args[idOutSizes], true))) {
                wr.append("\nAlgo:").append(result.getAlgo().name());
                wr.append("\nABC time:").append(String.valueOf(stats.getAbc_sec()));
                wr.append("\nABC memory:").append(String.valueOf(stats.getAbc_mem()));
            }
        }
        if (BenchmarksMC.EDACC) {
            Logger.getInstance().addMessage("Algo: " + result.getAlgo().name(), "edacc");
            Logger.getInstance().addMessage("ABC time: " + String.valueOf(stats.getAbc_sec()), "edacc");
            Logger.getInstance().addMessage("ABC memory: " + String.valueOf(stats.getAbc_mem()), "edacc");
        }
    }

    private static void checkSDNLoLA(String[] args) throws ParseException, IOException, InterruptedException, NotConvertableException, ProcessNotStartedException, ExternalToolException {
        String input = args[0];
        String outputPath = args[1];
        int idFormula = 4;
        PetriNet net = Tools.getPetriNet(input);
        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);

        String formula = (args[idFormula].isEmpty()) ? (String) pnwt.getExtension("formula") : args[idFormula];
        RunFormula f = FlowLTLParser.parse(pnwt, formula);

        LoLASettings settings = new LoLASettings();
        settings.setApproach(Approach.SEQUENTIAL);
        settings.setOutputPath(outputPath);

        ModelCheckerFlowLTL mc = new ModelCheckerFlowLTL(settings);
        ModelCheckingResult result = mc.check(pnwt, f);
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(outputPath + "_result.txt"))) {
            wr.append("Result: ").append(result.getSatisfied().toString());
        }
    }

}
