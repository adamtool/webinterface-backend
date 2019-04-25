package uniolunisaar.adam;

import java.io.IOException;
import java.util.List;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.CouldNotCalculateException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraphGameBuilderStepwise;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamSynthesizer {

    public static String getTikz(PetriGame game) {
        return PGTools.pg2Tikz(game);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGame getPetriGame(String aptFile) throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableConditionException, CouldNotCalculateException {
        return PGTools.getPetriGame(aptFile, false, true);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%% SOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% BDDSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static boolean existsWinningStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(path);
    }

    public static boolean existsWinningStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(path, so);
    }

    public static boolean existsWinningStrategyBDD(PetriGame net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net);
    }

    public static boolean existsWinningStrategyBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net, so);
    }

    public static boolean existsWinningStrategyBDD(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net, win, so);
    }

//    public static boolean existsWinningStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(game, win, so);
//    }
    public static PetriGame getStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(path);
    }

    public static PetriGame getStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(path, so);
    }

    public static PetriGame getStrategyBDD(PetriGame net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net);
    }

    public static PetriGame getStrategyBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net, so);
    }

    public static PetriGame getStrategyBDD(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net, win, so);
    }

//    public static PetriGame getStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(game, win, so);
//    }
    public static BDDGraph getGraphStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(path);
    }

    public static BDDGraph getGraphStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(path, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGame net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net, win, so);
    }

//    public static BDDGraph getGraphStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(game, win, so);
//    }
    public static BDDGraph getGraphGameBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(path);
    }

    public static BDDGraph getGraphGameBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(path, so);
    }

    public static BDDGraph getGraphGameBDD(PetriGame net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net);
    }

    public static BDDGraph getGraphGameBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net, so);
    }

    public static BDDGraph getGraphGameBDD(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net, win, so);
    }

    public static BDDSolver<? extends Condition> getBDDSolver(PetriGame game, Condition.Objective win, BDDSolverOptions so) throws SolvingException {
        return BDDSolverFactory.getInstance().getSolver(game, win, false, so);
    }

    public static BDDState getInitialGraphGameState(BDDGraph graph, BDDSolver<? extends Condition> solver) {
        return BDDGraphGameBuilderStepwise.getInitialState(graph, solver);
    }

    public static Pair<List<Flow>, List<BDDState>> getSuccessors(BDDState state, BDDGraph graph, BDDSolver<? extends Condition> solver) {
        return BDDGraphGameBuilderStepwise.getSuccessors(state, graph, solver);
    }
//
//    public static BDDGraph getGraphGameBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(game, win, so);
//    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(path);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(path, so);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net, so);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net, win, so);
    }

//    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(game, win, so);
//    }
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% QBFSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
}
