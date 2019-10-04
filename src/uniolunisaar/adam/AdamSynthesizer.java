package uniolunisaar.adam;

import java.io.IOException;
import java.util.List;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.ParameterMissingException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.CouldNotCalculateException;
import uniolunisaar.adam.generators.pg.CarRouting;
import uniolunisaar.adam.generators.pg.Clerks;
import uniolunisaar.adam.generators.pg.ContainerTerminal;
import uniolunisaar.adam.generators.pg.EmergencyBreakdown;
import uniolunisaar.adam.generators.pg.LoopUnrolling;
import uniolunisaar.adam.generators.pg.ManufactorySystem;
import uniolunisaar.adam.generators.pg.SecuritySystem;
import uniolunisaar.adam.generators.pg.SelfOrganizingRobots;
import uniolunisaar.adam.generators.pg.Watchdog;
import uniolunisaar.adam.generators.pg.Workflow;
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

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GENERATORS %%%%%%%%%%%%%%%%%%%%%%%%%%%%   
    /**
     *
     * @param nb_routes > 1
     * @param nb_cars > 1
     * @param version (A, E, AR)
     * @param withPartition
     * @return
     * @throws ModuleException
     */
    public static PetriGame genCarRouting(int nb_routes, int nb_cars, String version, boolean withPartition) throws ModuleException {
        PetriGame game;
        if (version.equals("A")) {
            game = CarRouting.createAReachabilityVersion(nb_routes, nb_cars, withPartition);
        } else if (version.equals("E")) {
            game = CarRouting.createEReachabilityVersion(nb_routes, nb_cars, withPartition);
        } else if (version.equals("AR")) {
            game = CarRouting.createAReachabilityVersionWithRerouting(nb_routes, nb_cars, withPartition);
        } else {
            throw new ModuleException("The version '" + version + "' not yet implemented.");
        }
        return game;
    }

    /**
     *
     * @param nb_machines >1
     * @param nb_workpieces >0
     * @param version (CAV15, BJ19)
     * @param withPartition
     * @return
     * @throws ModuleException
     */
    public static PetriGame genConcurrentMaschines(int nb_machines, int nb_workpieces, String version, boolean withPartition) throws ModuleException {
        PetriGame game;
        if (version.equals("CAV15")) {
            game = Workflow.generateNewAnnotationPoster(nb_machines, nb_workpieces, withPartition, false);
        } else if (version.equals("BJ19")) {
            game = Workflow.generateImprovedVersion(nb_machines, nb_workpieces, withPartition, false);
        } else {
            throw new ModuleException("The version '" + version + "' not yet implemented.");
        }
        return game;
    }

    /**
     *
     * @param nb_systems >1
     * @param withPartition
     * @return
     */
    public static PetriGame genContainerTerminal(int nb_systems, boolean withPartition) {
        PetriGame game = ContainerTerminal.createSafetyVersion(nb_systems, withPartition);
        return game;
    }

    /**
     *
     * @param nb_clerks >0
     * @param allyes
     * @param withPartition
     * @return
     */
    public static PetriGame genDocumentWorkflow(int nb_clerks, boolean allyes, boolean withPartition) {
        PetriGame game = allyes ? Clerks.generateCP(nb_clerks, withPartition, false)
                : Clerks.generateNonCP(nb_clerks, withPartition, false);
        return game;
    }

    /**
     * nb_crit+ nb_norm >1
     *
     * @param nb_crit
     * @param nb_norm
     * @param withPartition
     * @return
     */
    public static PetriGame genEmergencyBreakdown(int nb_crit, int nb_norm, boolean withPartition) {
        PetriGame game = EmergencyBreakdown.createSafetyVersion(nb_crit, nb_norm, withPartition);
        return game;
    }

    /**
     *
     * @param nb_machines >1
     * @param withPartition
     * @return
     */
    public static PetriGame genJobProcessing(int nb_machines, boolean withPartition) {
        PetriGame game = ManufactorySystem.generate(nb_machines, withPartition, false);
        return game;
    }

    public static PetriGame genLoopUnrolling(int nb_unrollings, boolean newChains, boolean withPartition) {
        return LoopUnrolling.createESafetyVersion(nb_unrollings, newChains, withPartition);
    }

    /**
     *
     * @param nb_systems >1
     * @param version (S, R, SHL)
     * @param withPartition
     * @return
     * @throws ModuleException
     */
    public static PetriGame genSecuritySystem(int nb_systems, String version, boolean withPartition) throws ModuleException {
        PetriGame game;
        if (version.equals("S")) {
            game = SecuritySystem.createSafetyVersion(nb_systems, withPartition);
        } else if (version.equals("R")) {
            game = SecuritySystem.createReachabilityVersion(nb_systems, withPartition);
        } else if (version.equals("SHL")) {
            game = SecuritySystem.createSafetyVersionForHLRep(nb_systems, withPartition);
        } else {
            throw new ModuleException("The version '" + version + "' not yet implemented.");
        }
        return game;
    }

    /**
     *
     * @param nb_robots > 1
     * @param nb_destroy > 0
     * @param withPartition
     * @return
     */
    public static PetriGame genSelfReconfiguringRobots(int nb_robots, int nb_destroy, boolean withPartition) {
        PetriGame game = SelfOrganizingRobots.generate(nb_robots, nb_destroy, withPartition, false);
        return game;
    }

    /**
     *
     * @param nb_machines > 0
     * @param search
     * @param partial_observation
     * @param withPartition
     * @return
     */
    public static PetriGame genWatchdog(int nb_machines, boolean search, boolean partial_observation, boolean withPartition) {
        return Watchdog.generate(nb_machines, search, partial_observation, withPartition);
    }

    public static String getTikz(PetriGame game) {
        return PGTools.pg2Tikz(game);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGame getPetriGame(String aptFile) throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableConditionException, CouldNotCalculateException {
        return PGTools.getPetriGameFromAPTString(aptFile, false, true);
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
        return BDDGraphGameBuilderStepwise.addInitialState(graph, solver);
    }

    public static Pair<List<Flow>, List<BDDState>> getSuccessors(BDDState state, BDDGraph graph, BDDSolver<? extends Condition> solver) {
        return BDDGraphGameBuilderStepwise.addSuccessors(state, graph, solver);
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
