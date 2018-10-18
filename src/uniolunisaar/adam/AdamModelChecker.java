package uniolunisaar.adam;

import java.io.IOException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.logic.flowltl.IRunFormula;
import uniolunisaar.adam.logic.flowltl.RunFormula;
import uniolunisaar.adam.logic.flowltlparser.FlowLTLParser;
import uniolunisaar.adam.logic.util.FormulaCreator;
import uniolunisaar.adam.modelchecker.circuits.CounterExample;
import uniolunisaar.adam.modelchecker.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.modelchecker.transformers.FlowLTLTransformerParallel;
import uniolunisaar.adam.modelchecker.transformers.FlowLTLTransformerSequential;
import uniolunisaar.adam.modelchecker.transformers.PetriNetTransformerParallel;
import uniolunisaar.adam.modelchecker.transformers.PetriNetTransformerSequential;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelChecker {

    /**
     * Returns a string representation of the LTL formula created from the given
     * winning objective (in the model checking context this is an acceptance
     * condition)
     *
     * @param game
     * @param winCon - only implemented for A_SAFETY, A_REACHABILITY, A_BUCHI
     * @return
     */
    public static String toFlowLTLFormula(PetriGame game, WinningCondition.Objective winCon) {
        return FormulaCreator.createLTLFormulaOfWinCon(game, winCon).toSymbolString();
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
    public static IRunFormula parseFlowLTLFormula(PetriNet net, String formula) throws ParseException {
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
     * @param game
     * @param f
     * @param parallel
     * @return
     */
    public static PetriNet getModelCheckingNet(PetriGame game, RunFormula f, boolean parallel) {
        if (parallel) {
            return PetriNetTransformerParallel.createNet4ModelCheckingParallel(game);
        } else {
            return PetriNetTransformerSequential.createNet4ModelCheckingSequential(game, f);
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
     */
    public static IRunFormula getModelCheckingFormula(PetriGame originalNet, PetriNet modelCheckingNet, RunFormula f, boolean parallel) {
        if (parallel) {
            return FlowLTLTransformerParallel.createFormula4ModelChecking4CircuitParallel(originalNet, modelCheckingNet, f);
        } else {
            return FlowLTLTransformerSequential.createFormula4ModelChecking4CircuitSequential(originalNet, modelCheckingNet, f);
        }
    }

    /**
     * Checks the flow ltl formula f on the given Petri net 'net' which is
     * annotated with token flows.
     *
     * If parallel is set the parallel algorithm is used, which is only
     * implemented for one flow formula. Otherwise the sequential approach is
     * used.
     *
     * @param net
     * @param f
     * @param parallel
     * @param path - a path where additionally files are saved.
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public static CounterExample checkFlowLTLFormula(PetriGame net, RunFormula f, boolean parallel, String path) throws InterruptedException, IOException {
        if (parallel) {
            return ModelCheckerFlowLTL.checkWithParallelApproach(net, f, path, true);
        } else {
            return ModelCheckerFlowLTL.checkWithSequentialApproach(net, f, path, true);
        }
    }
}
