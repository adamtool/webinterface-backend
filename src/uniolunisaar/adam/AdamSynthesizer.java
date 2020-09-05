package uniolunisaar.adam;

import java.io.IOException;
import java.util.List;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException;
import uniolunisaar.adam.generators.pgwt.CarRouting;
import uniolunisaar.adam.generators.pgwt.Clerks;
import uniolunisaar.adam.generators.pgwt.ContainerTerminal;
import uniolunisaar.adam.generators.pgwt.EmergencyBreakdown;
import uniolunisaar.adam.generators.pgwt.LoopUnrolling;
import uniolunisaar.adam.generators.pgwt.ManufactorySystem;
import uniolunisaar.adam.generators.pgwt.SecuritySystem;
import uniolunisaar.adam.generators.pgwt.SelfOrganizingRobots;
import uniolunisaar.adam.generators.pgwt.Watchdog;
import uniolunisaar.adam.generators.pgwt.Workflow;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.logic.synthesis.builder.graph.symbolic.bddapproach.BDDGraphGameBuilderStepwise;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
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
    public static PetriGameWithTransits genCarRouting(int nb_routes, int nb_cars, String version, boolean withPartition) throws ModuleException {
        PetriGameWithTransits game;
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
    public static PetriGameWithTransits genConcurrentMaschines(int nb_machines, int nb_workpieces, String version, boolean withPartition) throws ModuleException {
        PetriGameWithTransits game;
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
    public static PetriGameWithTransits genContainerTerminal(int nb_systems, boolean withPartition) {
        PetriGameWithTransits game = ContainerTerminal.createSafetyVersion(nb_systems, withPartition);
        return game;
    }

    /**
     *
     * @param nb_clerks >0
     * @param allyes
     * @param withPartition
     * @return
     */
    public static PetriGameWithTransits genDocumentWorkflow(int nb_clerks, boolean allyes, boolean withPartition) {
        PetriGameWithTransits game = allyes ? Clerks.generateCP(nb_clerks, withPartition, false)
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
    public static PetriGameWithTransits genEmergencyBreakdown(int nb_crit, int nb_norm, boolean withPartition) {
        PetriGameWithTransits game = EmergencyBreakdown.createSafetyVersion(nb_crit, nb_norm, withPartition);
        return game;
    }

    /**
     *
     * @param nb_machines >1
     * @param withPartition
     * @return
     */
    public static PetriGameWithTransits genJobProcessing(int nb_machines, boolean withPartition) {
        PetriGameWithTransits game = ManufactorySystem.generate(nb_machines, withPartition, false);
        return game;
    }

    /**
     *
     * @param nb_unrollings > 0
     * @param newChains
     * @param withPartition
     * @return
     */
    public static PetriGameWithTransits genLoopUnrolling(int nb_unrollings, boolean newChains, boolean withPartition) {
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
    public static PetriGameWithTransits genSecuritySystem(int nb_systems, String version, boolean withPartition) throws ModuleException {
        PetriGameWithTransits game;
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
    public static PetriGameWithTransits genSelfReconfiguringRobots(int nb_robots, int nb_destroy, boolean withPartition) {
        PetriGameWithTransits game = SelfOrganizingRobots.generate(nb_robots, nb_destroy, withPartition, false);
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
    public static PetriGameWithTransits genWatchdog(int nb_machines, boolean search, boolean partial_observation, boolean withPartition) {
        return Watchdog.generate(nb_machines, search, partial_observation, withPartition);
    }

    public static String getTikz(PetriGameWithTransits game) {
        return PGTools.pg2Tikz(game);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGameWithTransits getPetriGame(String aptFile) throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableConditionException, CouldNotCalculateException {
        return PGTools.getPetriGameFromAPTString(aptFile, false, true);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%% SOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% BDDSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static boolean existsWinningStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(path);
    }

    /**
     *      * SolverOptions: <\p>
     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
     * "typed"<\p>
     * - maxIncrease: int<\p>
     * - initNOdeNb: int<\p>
     * - cacheSize: int<\p> *
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws ParseException
     * @throws IOException
     * @throws ParameterMissingException
     * @throws SolvingException
     * @throws CalculationInterruptedException
     */
    public static boolean existsWinningStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(path, so);
    }

    public static boolean existsWinningStrategyBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(net);
    }

    /**
     * SolverOptions: <\p>
     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
     * "typed"<\p>
     * - maxIncrease: int<\p>
     * - initNOdeNb: int<\p>
     * - cacheSize: int<\p>
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws ParameterMissingException
     * @throws ParseException
     * @throws SolvingException
     * @throws CalculationInterruptedException
     */
    public static boolean existsWinningStrategyBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(net, so);
    }

    /**
     * SolverOptions: <\p>
     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
     * "typed"<\p>
     * - maxIncrease: int<\p>
     * - initNOdeNb: int<\p>
     * - cacheSize: int<\p>
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws ParameterMissingException
     * @throws ParseException
     * @throws SolvingException
     * @throws CalculationInterruptedException 
     */
    public static boolean existsWinningStrategyBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(net, win, so);
    }

//    public static boolean existsWinningStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(game, win, so);
//    }
    public static PetriGameWithTransits getStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(path);
    }

    /**
     * SolverOptions: <\p>
     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
     * "typed"<\p>
     * - maxIncrease: int<\p>
     * - initNOdeNb: int<\p>
     * - cacheSize: int<\p>
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws ParseException
     * @throws IOException
     * @throws NoStrategyExistentException
     * @throws ParameterMissingException
     * @throws SolvingException
     * @throws CalculationInterruptedException 
     */
    public static PetriGameWithTransits getStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(path, so);
    }

    public static PetriGameWithTransits getStrategyBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(net);
    }

    public static PetriGameWithTransits getStrategyBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(net, so);
    }

    public static PetriGameWithTransits getStrategyBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(net, win, so);
    }

//    public static PetriGame getStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(game, win, so);
//    }
    public static BDDGraph getGraphStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(path);
    }

    public static BDDGraph getGraphStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(path, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(net);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(net, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(net, win, so);
    }

//    public static BDDGraph getGraphStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(game, win, so);
//    }
    public static BDDGraph getGraphGameBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(path);
    }

    public static BDDGraph getGraphGameBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(path, so);
    }

    public static BDDGraph getGraphGameBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(net);
    }

    public static BDDGraph getGraphGameBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(net, so);
    }

    public static BDDGraph getGraphGameBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(net, win, so);
    }

    public static DistrSysBDDSolver<? extends Condition<?>> getBDDSolver(PetriGameWithTransits game, Condition.Objective win, BDDSolverOptions so) throws SolvingException {
        return DistrSysBDDSolverFactory.getInstance().getSolver(game, win, so);
    }

    public static BDDState getInitialGraphGameState(BDDGraph graph, DistrSysBDDSolver<? extends Condition<?>> solver) {
        return BDDGraphGameBuilderStepwise.addInitialState(graph, solver);
    }

    public static Pair<List<Flow>, List<BDDState>> getSuccessors(BDDState state, BDDGraph graph, DistrSysBDDSolver<? extends Condition<?>> solver) {
        return BDDGraphGameBuilderStepwise.addSuccessors(state, graph, solver);
    }
//
//    public static BDDGraph getGraphGameBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(game, win, so);
//    }

    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(path);
    }

    /**
     * SolverOptions: <\p>
     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
     * "typed"<\p>
     * - maxIncrease: int<\p>
     * - initNOdeNb: int<\p>
     * - cacheSize: int<\p>
     *
     * @param path
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws ParseException
     * @throws IOException
     * @throws NoStrategyExistentException
     * @throws ParameterMissingException
     * @throws SolvingException
     * @throws CalculationInterruptedException
     */
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(path, so);
    }

    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(net);
    }

    /**
     * SolverOptions: <\p>
     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
     * "typed"<\p>
     * - maxIncrease: int<\p>
     * - initNOdeNb: int<\p>
     * - cacheSize: int<\p>
     *
     *
     * @param net
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws NoStrategyExistentException
     * @throws ParameterMissingException
     * @throws ParseException
     * @throws SolvingException
     * @throws CalculationInterruptedException
     */
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(net, so);
    }

    /**
     *      * SolverOptions: <\p>
     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
     * "typed"<\p>
     * - maxIncrease: int<\p>
     * - initNOdeNb: int<\p>
     * - cacheSize: int<\p>
     *
     * @param net
     * @param win
     * @param so
     * @return
     * @throws CouldNotFindSuitableConditionException
     * @throws NotSupportedGameException
     * @throws NetNotSafeException
     * @throws NoSuitableDistributionFoundException
     * @throws NoStrategyExistentException
     * @throws ParameterMissingException
     * @throws ParseException
     * @throws SolvingException
     * @throws CalculationInterruptedException
     */
    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(net, win, so);
    }

//    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(game, win, so);
//    }
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% QBFSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
}
