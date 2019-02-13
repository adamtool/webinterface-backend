package uniolunisaar.adam;

import java.io.IOException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.logics.ltl.ILTLFormula;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.logics.ltl.LTLFormula;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunFormula;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.util.logics.FormulaCreator;
import uniolunisaar.adam.logic.modelchecking.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.logic.modelchecking.circuits.ModelCheckerLTL;
import uniolunisaar.adam.ds.modelchecking.ModelCheckingResult;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.exception.logics.NotConvertableException;
import uniolunisaar.adam.logic.transformers.flowltl.FlowLTLTransformerParallel;
import uniolunisaar.adam.logic.transformers.flowltl.FlowLTLTransformerSequential;
import uniolunisaar.adam.logic.transformers.pnwt2pn.PnwtAndFlowLTLtoPNParallel;
import uniolunisaar.adam.logic.transformers.pnwt2pn.PnwtAndFlowLTLtoPNSequential;
import uniolunisaar.adam.ds.modelchecking.ModelcheckingStatistics;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelChecker {

    /**
     * Returns true iff the given formula is a plain LTL formula.
     * 
     * @param f
     * @return 
     */
    public static boolean isLTLFormula(RunFormula f) {
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
    public static RunFormula parseFlowLTLFormula(PetriNet net, String formula) throws ParseException {
        return FlowLTLParser.parse(net, formula);
    }

    /**
     * Returns a Petri net which is created from the given name (and if
     * !parallel) from the formula which can be used to do standard LTL model
     * checking on Petri nets.
     *
     * If parallel is set the parallel algorithm is used, which is only
     * implemented for one flow formula. Otherwise the sequential approach is
     * used.
     *
     * @param net
     * @param f
     * @param parallel
     * @return
     */
    public static PetriNet getModelCheckingNet(PetriNetWithTransits net, RunFormula f, boolean parallel) {
        if (parallel) {
            return PnwtAndFlowLTLtoPNParallel.createNet4ModelCheckingParallelOneFlowFormula(net);
        } else {
            return PnwtAndFlowLTLtoPNSequential.createNet4ModelCheckingSequential(net, f, true);
        }
    }

    /**
     * Returns the transformed formula which can be used to do the standard LTL
     * model checking on the modelCheckingNet.
     *
     * If parallel is set the parallel algorithm is used, which is only
     * implemented for one flow formula. Otherwise the sequential approach is
     * used.
     *
     * @param originalNet - the input net from which the modelCheckingNet had
     * been created
     * @param modelCheckingNet - the net on which the standard LTL model
     * checking would like to be produced
     * @param f - the formula to transform
     * @param parallel
     * @return
     * @throws uniolunisaar.adam.exception.logics.NotConvertableException
     */
    public static ILTLFormula getModelCheckingFormula(PetriNetWithTransits originalNet, PetriNet modelCheckingNet, RunFormula f, boolean parallel) throws NotConvertableException {
        if (parallel) {
            return FlowLTLTransformerParallel.createFormula4ModelChecking4CircuitParallel(originalNet, modelCheckingNet, f);
        } else {
            return FlowLTLTransformerSequential.createFormula4ModelChecking4CircuitSequential(originalNet, modelCheckingNet, f, true);
        }
    }

    /**
     * Checks the flow ltl formula f on the given Petri net 'net' which is
     * annotated with token flows.
     *
     * It stats != null an object is returned containing some statistical data,
     * including the model checking net and formula.
     *
     * @param net
     * @param mc
     * @param f
     * @param path - a path where additionally files are saved.
     * @param stats
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws uniol.apt.io.parser.ParseException
     * @throws uniolunisaar.adam.exception.logics.NotConvertableException
     * @throws uniolunisaar.adam.exceptions.ProcessNotStartedException
     * @throws uniolunisaar.adam.exceptions.ExternalToolException
     */
    public static ModelCheckingResult checkFlowLTLFormula(PetriNetWithTransits net, ModelCheckerFlowLTL mc, RunFormula f, String path, ModelcheckingStatistics stats) throws InterruptedException, IOException, ParseException, NotConvertableException, ProcessNotStartedException, ExternalToolException {
        return mc.check(net, f, path, false, stats);
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
     * @param path
     * @param stats
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws uniol.apt.io.parser.ParseException
     * @throws ProcessNotStartedException
     * @throws ExternalToolException
     */
    public static ModelCheckingResult checkLTLFormula(PetriNetWithTransits net, ModelCheckerLTL mc, LTLFormula f, String path, ModelcheckingStatistics stats) throws InterruptedException, IOException, ParseException, ProcessNotStartedException, ExternalToolException {
        return mc.check(net, f, path, false, stats);
    }
}
