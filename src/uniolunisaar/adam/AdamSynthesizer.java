package uniolunisaar.adam;

import java.io.IOException;
import java.util.List;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.ParameterMissingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
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
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.symbolic.bddapproach.BDDGraphGameBuilderStepwise;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameExtensionHandler;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrenv.DistrEnvBDDSolverOptions;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.explicit.GGBuilder;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrenv.DistrEnvBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;
import uniolunisaar.adam.util.ExplicitBDDGraphTransformer;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PgwtPreconditionChecker;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamSynthesizer {

    public static PgwtPreconditionChecker createPreconditionChecker(PetriGameWithTransits pgwt) {
        return new PgwtPreconditionChecker(pgwt);
    }

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
        return PGTools.getPetriGameFromAPTString(aptFile, false, false);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%% SOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% BDDSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//    public static boolean existsWinningStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(path);
//    }
//
//    /**
//     *      * SolverOptions: <\p>
//     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
//     * "typed"<\p>
//     * - maxIncrease: int<\p>
//     * - initNOdeNb: int<\p>
//     * - cacheSize: int<\p> *
//     *
//     * @param path
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws ParseException
//     * @throws IOException
//     * @throws ParameterMissingException
//     * @throws SolvingException
//     * @throws CalculationInterruptedException
//     */
//    public static boolean existsWinningStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(path, so);
//    }
    public static boolean existsWinningStrategyBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        if (PetriGameExtensionHandler.thereIsOneEnvPlayer(net)) {
            return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(net);
        } else { // there must be one system player otherwise there must have been an exception
            DistrEnvBDDSolverOptions opts = new DistrEnvBDDSolverOptions(false, true);
            var solver = DistrEnvBDDSolverFactory.getInstance().getSolver(net, opts);
            return solver.existsWinningStrategy();
        }
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
        if (PetriGameExtensionHandler.thereIsOneEnvPlayer(net)) {
            return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(net, so);
        } else { // there must be one system player otherwise there must have been an exception
            DistrEnvBDDSolverOptions opts = new DistrEnvBDDSolverOptions(so.isSkipTests(), true);
            var solver = DistrEnvBDDSolverFactory.getInstance().getSolver(net, opts);
            return solver.existsWinningStrategy();
        }
    }

//    /**
//     * SolverOptions: <\p>
//     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
//     * "typed"<\p>
//     * - maxIncrease: int<\p>
//     * - initNOdeNb: int<\p>
//     * - cacheSize: int<\p>
//     *
//     * @param net
//     * @param win
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws ParameterMissingException
//     * @throws ParseException
//     * @throws SolvingException
//     * @throws CalculationInterruptedException
//     */
//    public static boolean existsWinningStrategyBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.existsWinningStrategy(net, win, so);
//    }
//    public static boolean existsWinningStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(game, win, so);
//    }
//    public static PetriGameWithTransits getStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(path, true);
//    }
//    /**
//     * SolverOptions: <\p>
//     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
//     * "typed"<\p>
//     * - maxIncrease: int<\p>
//     * - initNOdeNb: int<\p>
//     * - cacheSize: int<\p>
//     *
//     * @param path
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws ParseException
//     * @throws IOException
//     * @throws NoStrategyExistentException
//     * @throws ParameterMissingException
//     * @throws SolvingException
//     * @throws CalculationInterruptedException
//     */
//    public static PetriGameWithTransits getStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(path, so, true);
//    }
    public static PetriGameWithTransits getStrategyBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        if (PetriGameExtensionHandler.thereIsOneEnvPlayer(net)) {
            return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(net, true);
        } else { // there must be one system player otherwise there must have been an exception
            DistrEnvBDDSolverOptions opts = new DistrEnvBDDSolverOptions(false, true);
            var solver = DistrEnvBDDSolverFactory.getInstance().getSolver(net, opts);
            PetriGameWithTransits strategy = solver.getStrategy();
            PGTools.addCoordinates(solver.getGame(), strategy);
            return strategy;
        }

    }

//    public static PetriGameWithTransits getStrategyBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(net, so, true);
//    }
//
//    public static PetriGameWithTransits getStrategyBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategy(net, win, so, true);
//    }
//    public static PetriGame getStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(game, win, so);
//    }
//    public static BDDGraph getGraphStrategyBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(path);
//    }
//
//    public static BDDGraph getGraphStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(path, so);
//    }
    public static BDDGraph getGraphStrategyBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        if (PetriGameExtensionHandler.thereIsOneEnvPlayer(net)) {
            return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(net);
        } else { // there must be one system player otherwise there must have been an exception
            DistrEnvBDDSolverOptions opts = new DistrEnvBDDSolverOptions(false, true);
            var solver = DistrEnvBDDSolverFactory.getInstance().getSolver(net, opts);
            return solver.getGraphStrategy();
        }
    }

//    public static BDDGraph getGraphStrategyBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(net, so);
//    }
//
//    public static BDDGraph getGraphStrategyBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphStrategy(net, win, so);
//    }
//    public static BDDGraph getGraphStrategyBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(game, win, so);
//    }
//    public static BDDGraph getGraphGameBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(path);
//    }
//
//    public static BDDGraph getGraphGameBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(path, so);
//    }
    public static BDDGraph getGraphGameBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
        if (PetriGameExtensionHandler.thereIsOneEnvPlayer(net)) {
            return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(net);
        } else { // there must be one system player otherwise there must have been an exception
            DistrEnvBDDSolverOptions opts = new DistrEnvBDDSolverOptions(false, true);
            var solver = DistrEnvBDDSolverFactory.getInstance().getSolver(net, opts);
            return solver.getGraphGame();
        }
    }

//    public static BDDGraph getGraphGameBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(net, so);
//    }
//
//    public static BDDGraph getGraphGameBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getGraphGame(net, win, so);
//    }
    public static BDDSolver<? extends Condition<?>, ? extends BDDSolvingObject<?>, ? extends BDDSolverOptions> getBDDSolver(PetriGameWithTransits game, Condition.Objective win, BDDSolverOptions so) throws SolvingException {
        if (PetriGameExtensionHandler.thereIsOneEnvPlayer(game)) {
            return DistrSysBDDSolverFactory.getInstance().getSolver(game, win, so);
        } else { // there must be one system player otherwise there must have been an exception
            DistrEnvBDDSolverOptions opts = new DistrEnvBDDSolverOptions(so.isSkipTests(), true);
            var solver = DistrEnvBDDSolverFactory.getInstance().getSolver(game, win, opts);
            return solver;
        }

    }

    public static <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions> BDDState getInitialGraphGameStateBDD(BDDGraph graph, BDDSolver<W, SO, SOP> solver) {
        return BDDGraphGameBuilderStepwise.addInitialState(graph, solver);
    }

    public static <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions> Pair<List<Flow>, List<BDDState>> getSuccessorsBDD(BDDState state, BDDGraph graph, BDDSolver<W, SO, SOP> solver) {
        return BDDGraphGameBuilderStepwise.addSuccessors(state, graph, solver);
    }

    // the explicit versions
    public static BDDState getInitialGraphGameState(PetriGameWithTransits pg) {
        DecisionSet init = GGBuilder.getInstance().createInitDecisionSet(pg);
        BDDState bddstate = ExplicitBDDGraphTransformer.decisionset2BDDState(init);
        bddstate.putExtension("dcs", init);
        return bddstate;
    }

    public static <W extends Condition<W>, SO extends BDDSolvingObject<W>, SOP extends BDDSolverOptions> Pair<List<Flow>, List<BDDState>> getSuccessors(BDDState state, BDDGraph graph, BDDSolver<W, SO, SOP> solver) {
        return BDDGraphGameBuilderStepwise.addSuccessors(state, graph, solver);
    }
//
//    public static BDDGraph getGraphGameBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(game, win, so);
//    }
//
//    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(String path) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(path, true);
//    }
//
//    /**
//     * SolverOptions: <\p>
//     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
//     * "typed"<\p>
//     * - maxIncrease: int<\p>
//     * - initNOdeNb: int<\p>
//     * - cacheSize: int<\p>
//     *
//     * @param path
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws ParseException
//     * @throws IOException
//     * @throws NoStrategyExistentException
//     * @throws ParameterMissingException
//     * @throws SolvingException
//     * @throws CalculationInterruptedException
//     */
//    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(path, so, true);
//    }
//
//    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(PetriGameWithTransits net) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(net, true);
//    }
//
//    /**
//     * SolverOptions: <\p>
//     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
//     * "typed"<\p>
//     * - maxIncrease: int<\p>
//     * - initNOdeNb: int<\p>
//     * - cacheSize: int<\p>
//     *
//     *
//     * @param net
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws NoStrategyExistentException
//     * @throws ParameterMissingException
//     * @throws ParseException
//     * @throws SolvingException
//     * @throws CalculationInterruptedException
//     */
//    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(PetriGameWithTransits net, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(net, so, true);
//    }
//
//    /**
//     *      * SolverOptions: <\p>
//     * - libraryName: "buddy", "cudd", "cal", "j", "java", "jdd", "test",
//     * "typed"<\p>
//     * - maxIncrease: int<\p>
//     * - initNOdeNb: int<\p>
//     * - cacheSize: int<\p>
//     *
//     * @param net
//     * @param win
//     * @param so
//     * @return
//     * @throws CouldNotFindSuitableConditionException
//     * @throws NotSupportedGameException
//     * @throws NetNotSafeException
//     * @throws NoSuitableDistributionFoundException
//     * @throws NoStrategyExistentException
//     * @throws ParameterMissingException
//     * @throws ParseException
//     * @throws SolvingException
//     * @throws CalculationInterruptedException
//     */
//    public static Pair<BDDGraph, PetriGameWithTransits> getStrategiesBDD(PetriGameWithTransits net, Condition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException, CalculationInterruptedException {
//        return uniolunisaar.adam.behavior.AdamSynthesisBDDBehavior.getStrategies(net, win, so, true);
//    }
//
////    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame game, Condition win, BDDSolverOptions so) throws CouldNotFindSuitableConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
////        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(game, win, so);
////    }
//    // %%%%%%%%%%%%%%%%%%%%%%%%%%% QBFSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
}
