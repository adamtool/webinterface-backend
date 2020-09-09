package uniolunisaar.adam;

import java.io.IOException;
import java.util.List;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.circuits.CircuitRendererSettings;
import uniolunisaar.adam.ds.logics.ltl.ILTLFormula;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.logics.ltl.LTLFormula;
import uniolunisaar.adam.ds.logics.ltl.flowltl.FlowLTLFormula;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.util.logics.FormulaCreator;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerLTL;
import uniolunisaar.adam.ds.modelchecking.results.LTLModelCheckingResult;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.exceptions.logics.NotConvertableException;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.PnwtAndNbFlowFormulas2PNParallel;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.PnwtAndNbFlowFormulas2PNSequential;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.generators.pnwt.RedundantNetwork;
import uniolunisaar.adam.generators.pnwt.SmartFactory;
import uniolunisaar.adam.generators.pnwt.UpdatingNetwork;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitFlowLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ModelCheckingSettings;
import uniolunisaar.adam.logic.transformers.modelchecking.flowltl2ltl.FlowLTLTransformerIngoingParallel;
import uniolunisaar.adam.logic.transformers.modelchecking.flowltl2ltl.FlowLTLTransformerIngoingSequential;
import uniolunisaar.adam.logic.transformers.modelchecking.flowltl2ltl.FlowLTLTransformerOutgoingParallel;
import uniolunisaar.adam.logic.transformers.modelchecking.flowltl2ltl.FlowLTLTransformerOutgoingSequential;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.PnwtAndNbFlowFormulas2PNParallelInhibitor;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.PnwtAndNbFlowFormulas2PNSequentialInhibitor;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.withoutinittflplaces.PnwtAndNbFlowFormulas2PNParInhibitorNoInit;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.withoutinittflplaces.PnwtAndNbFlowFormulas2PNParallelNoInit;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.withoutinittflplaces.PnwtAndNbFlowFormulas2PNSeqInhibitorNoInit;
import uniolunisaar.adam.logic.transformers.modelchecking.pnwt2pn.withoutinittflplaces.PnwtAndNbFlowFormulas2PNSequentialNoInit;
import uniolunisaar.adam.util.MCTools;
import uniolunisaar.adam.util.logics.LogicsTools;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelChecker {

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% GENERATORS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     *
     * @param nb_products > 0
     * @param nb_shared_machines
     * @param nb_specific_machines
     * @return
     */
    public static PetriNetWithTransits generateSmartFactory(int nb_products, int nb_shared_machines, int nb_specific_machines) {
        return SmartFactory.createFactory(nb_products, nb_shared_machines, nb_specific_machines);
    }

    /**
     *
     * @param nb_nodes > 2
     * @return
     */
    public static PetriNetWithTransits generateSwitchFailure(int nb_nodes) {
        return UpdatingNetwork.create(nb_nodes);
    }

    /**
     *
     * @param nb_nodesU > 0
     * @param nb_nodesD > 0
     * @param version (B, U, F, RF)
     * @return
     */
    public static PetriNetWithTransits generateRedundantPipeline(int nb_nodesU, int nb_nodesD, String version) {
        PetriNetWithTransits net = null;
        if (version.equals("B")) {
            net = RedundantNetwork.getBasis(nb_nodesU, nb_nodesD);
        } else if (version.equals("U")) {
            net = RedundantNetwork.getUpdatingNetwork(nb_nodesU, nb_nodesD);
        } else if (version.equals("F")) {
            net = RedundantNetwork.getUpdatingStillNotFixedMutexNetwork(nb_nodesU, nb_nodesD);
        } else if (version.equals("RF")) {
            net = RedundantNetwork.getUpdatingStillNotFixedMutexNetwork(nb_nodesU, nb_nodesD);
        } else {
//            throw new CommandLineParseException("The version '" + version + "' is not a valid options.");
        }
        return net;
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriNetWithTransits getPetriNetWithTransits(String aptFile) throws ParseException, IOException {
        return PNWTTools.getPetriNetWithTransits(aptFile, true);
    }

    /**
     * Returns true iff the given formula is a plain LTL formula.
     *
     * @param f
     * @return
     */
    public static boolean isLTLFormula(RunLTLFormula f) {
        return (f.getPhi() instanceof ILTLFormula);
    }

    /**
     * Returns a string representation of the LTL formula created from the given
     * winning objective (in the model checking context this is an acceptance
     * condition)
     *
     * @param net
     * @param winCon - only implemented for A_SAFETY, A_REACHABILITY, A_BUCHI
     * @return
     */
    public static String toFlowLTLFormula(PetriNetWithTransits net, Condition.Objective winCon) {
        return FormulaCreator.createLTLFormulaOfWinCon(net, winCon).toSymbolString();
    }

    /**
     * Parses a given string with the
     * uniolunisaar.adam.logic.flowltlparser.FlowLTLFormat.g4 grammar to a flow
     * LTL formula.
     *
     * @param net
     * @param formula
     * @return
     * @throws ParseException
     */
    public static RunLTLFormula parseFlowLTLFormula(PetriNet net, String formula) throws ParseException {
        return FlowLTLParser.parse(net, formula);
    }

    /**
     * Returns a Petri net which is created from the given name (and if
     * !parallel) from the formula which can be used to do standard LTL model
     * checking on Petri nets.If parallel is set the parallel algorithm is used,
     * which is only implemented for one flow formula.Otherwise the sequential
     * approach is used.
     *
     *
     * @param net
     * @param f
     * @param settings
     * @return
     */
    public static PetriNet getModelCheckingNet(PetriNetWithTransits net, RunLTLFormula f, AdamCircuitFlowLTLMCSettings settings) {
        if (isLTLFormula(f)) {
            return net;
        }
        PetriNet mcNet;
        if (settings.getApproach() == ModelCheckingSettings.Approach.PARALLEL) {
// todo:                throw new NotConvertableException("The parallel approach (without inhibitor arcs) is not implemented for more than one flow subformula!. Please use another approach.");
            if (settings.getRendererSettings().getSemantics() == CircuitRendererSettings.TransitionSemantics.INGOING) {
                mcNet= PnwtAndNbFlowFormulas2PNParallelNoInit.createNet4ModelCheckingParallelOneFlowFormula(net);
            } else {
                mcNet= PnwtAndNbFlowFormulas2PNParallel.createNet4ModelCheckingParallelOneFlowFormula(net);
            }
        } else if (settings.getApproach() == ModelCheckingSettings.Approach.PARALLEL_INHIBITOR) {
            if (settings.getRendererSettings().getSemantics() == CircuitRendererSettings.TransitionSemantics.INGOING) {
                mcNet= PnwtAndNbFlowFormulas2PNParInhibitorNoInit.createNet4ModelCheckingParallelOneFlowFormula(net);
            } else {
                mcNet= PnwtAndNbFlowFormulas2PNParallelInhibitor.createNet4ModelCheckingParallelOneFlowFormula(net);
            }
        } else {
            List<FlowLTLFormula> flowLTLFormulas = LogicsTools.getFlowLTLFormulas(f);
            if (settings.getApproach() == ModelCheckingSettings.Approach.SEQUENTIAL_INHIBITOR) {
                if (settings.getRendererSettings().getSemantics() == CircuitRendererSettings.TransitionSemantics.INGOING) {
                    mcNet= PnwtAndNbFlowFormulas2PNSeqInhibitorNoInit.createNet4ModelCheckingSequential(net, flowLTLFormulas.size());
                } else {
                    mcNet= PnwtAndNbFlowFormulas2PNSequentialInhibitor.createNet4ModelCheckingSequential(net, flowLTLFormulas.size(), true);
                }
            }
            if (settings.getRendererSettings().getSemantics() == CircuitRendererSettings.TransitionSemantics.INGOING) {
                mcNet= PnwtAndNbFlowFormulas2PNSequentialNoInit.createNet4ModelCheckingSequential(net, flowLTLFormulas.size());
            } else {
                mcNet= PnwtAndNbFlowFormulas2PNSequential.createNet4ModelCheckingSequential(net, flowLTLFormulas.size(), true);
            }
        }        
        MCTools.addCoordinates(net,mcNet);
        return mcNet;
    }

    /**
     * Returns the transformed formula which can be used to do the standard LTL
     * model checking on the modelCheckingNet.If parallel is set the parallel
     * algorithm is used, which is only implemented for one flow formula.
     *
     * Otherwise the sequential approach is used.
     *
     * @param originalNet - the input net from which the modelCheckingNet had
     * been created
     * @param modelCheckingNet - the net on which the standard LTL model
     * checking would like to be produced
     * @param f - the formula to transform
     * @param settings
     * @return
     * @throws uniolunisaar.adam.exceptions.logics.NotConvertableException
     */
    public static ILTLFormula getModelCheckingFormula(PetriNetWithTransits originalNet, PetriNet modelCheckingNet, RunLTLFormula f, AdamCircuitFlowLTLMCSettings settings) throws NotConvertableException {
        if (settings.getApproach() == ModelCheckingSettings.Approach.PARALLEL) {
            switch (settings.getRendererSettings().getSemantics()) {
                case OUTGOING:
                    return new FlowLTLTransformerOutgoingParallel().createFormula4ModelChecking4CircuitParallel(originalNet, modelCheckingNet, f, settings);
                case INGOING:
                    return new FlowLTLTransformerIngoingParallel().createFormula4ModelChecking4CircuitParallel(originalNet, modelCheckingNet, f, settings);
                default:
                    throw new RuntimeException("The transitions semantics: '" + settings.getRendererSettings().getSemantics() + "' is not yet implemented.");
            }
        } else if (settings.getApproach() == ModelCheckingSettings.Approach.PARALLEL_INHIBITOR) {
            if (null == settings.getRendererSettings().getSemantics()) {
                throw new RuntimeException("The transitions semantics: '" + settings.getRendererSettings().getSemantics() + "' is not yet implemented.");
            } else {
                switch (settings.getRendererSettings().getSemantics()) {
                    case OUTGOING:
                        return new FlowLTLTransformerOutgoingParallel().createFormula4ModelChecking4CircuitParallel(originalNet, modelCheckingNet, f, settings);
                    case INGOING:
                        return new FlowLTLTransformerIngoingParallel().createFormula4ModelChecking4CircuitParallel(originalNet, modelCheckingNet, f, settings);
                    default:
                        throw new RuntimeException("The transitions semantics: '" + settings.getRendererSettings().getSemantics() + "' is not yet implemented.");
                }
            }
        } else if (settings.getApproach() == ModelCheckingSettings.Approach.SEQUENTIAL) {
            if (null == settings.getRendererSettings().getSemantics()) {
                throw new RuntimeException("The transitions semantics: '" + settings.getRendererSettings().getSemantics() + "' is not yet implemented.");
            } else {
                switch (settings.getRendererSettings().getSemantics()) {
                    case OUTGOING:
                        return new FlowLTLTransformerOutgoingSequential().createFormula4ModelChecking4CircuitSequential(originalNet, modelCheckingNet, f, settings);
                    case INGOING:
                        return new FlowLTLTransformerIngoingSequential().createFormula4ModelChecking4CircuitSequential(originalNet, modelCheckingNet, f, settings);
                    default:
                        throw new RuntimeException("The transitions semantics: '" + settings.getRendererSettings().getSemantics() + "' is not yet implemented.");
                }
            }
        }else if (settings.getApproach() == ModelCheckingSettings.Approach.SEQUENTIAL_INHIBITOR) {
            if (null == settings.getRendererSettings().getSemantics()) {
                throw new RuntimeException("The transitions semantics: '" + settings.getRendererSettings().getSemantics() + "' is not yet implemented.");
            } else {
                switch (settings.getRendererSettings().getSemantics()) {
                    case OUTGOING:
                        return new FlowLTLTransformerOutgoingSequential().createFormula4ModelChecking4CircuitSequential(originalNet, modelCheckingNet, f, settings);
                    case INGOING:
                        return new FlowLTLTransformerIngoingSequential().createFormula4ModelChecking4CircuitSequential(originalNet, modelCheckingNet, f, settings);
                    default:
                        throw new RuntimeException("The transitions semantics: '" + settings.getRendererSettings().getSemantics() + "' is not yet implemented.");
                }
            }
        } else {
              throw new RuntimeException("Didn't provided a solution for all approaches yet. Approach '" + settings.getAbcSettings() + "' is missing; sry.");
        }
    }

    /**
     * Checks the flow ltl formula f on the given Petri net 'net' which is
     * annotated with token flows.It stats != null an object is returned
     * containing some statistical data, including the model checking net and
     * formula.
     *
     *
     * @param net
     * @param mc
     * @param f
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws uniol.apt.io.parser.ParseException
     * @throws uniolunisaar.adam.exceptions.logics.NotConvertableException
     * @throws uniolunisaar.adam.exceptions.ProcessNotStartedException
     * @throws uniolunisaar.adam.exceptions.ExternalToolException
     */
    public static LTLModelCheckingResult checkFlowLTLFormula(PetriNetWithTransits net, ModelCheckerFlowLTL mc, RunLTLFormula f) throws InterruptedException, IOException, ParseException, NotConvertableException, ProcessNotStartedException, ExternalToolException {
        return mc.check(net, f);
    }

    /**
     * For a parsed formula f, if f.getPhi() instanceof ILTLFormula holds use
     * the standard LTL model checking procedure with this method and
     * f.toLTLFormula(). Notify the user.
     *
     * It stats != null an object is returned containing some statistical data.
     *
     * @param net
     * @param mc
     * @param f
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws uniol.apt.io.parser.ParseException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     */
    public static LTLModelCheckingResult checkLTLFormula(PetriNetWithTransits net, ModelCheckerLTL mc, LTLFormula f) throws InterruptedException, IOException, ParseException, ProcessNotStartedException, ExternalToolException {
        return mc.check(net, f);
    }
}
