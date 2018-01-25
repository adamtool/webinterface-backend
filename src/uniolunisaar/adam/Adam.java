package uniolunisaar.adam;

import java.io.IOException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.ds.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.logic.AdamBehavior;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class Adam {

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GENERATORS %%%%%%%%%%%%%%%%%%%%%%%%%%%%   
    public static PetriNet genConcurrentMaschines(int nb_machines, int nb_workpieces) {
        return uniolunisaar.adam.generators.AdamBehavior.genConcurrentMaschines(nb_machines, nb_workpieces);
    }

    public static PetriNet genContainerTerminal(int nb_systems) {
        return uniolunisaar.adam.generators.AdamBehavior.genContainerTerminal(nb_systems);
    }

    public static PetriNet genDocumentWorkflow(int nb_clerks, boolean allyes) {
        return uniolunisaar.adam.generators.AdamBehavior.genDocumentWorkflow(nb_clerks, allyes);
    }

    public static PetriNet genEmergencyBreakdown(int nb_crit, int nb_norm) {
        return uniolunisaar.adam.generators.AdamBehavior.genEmergencyBreakdown(nb_crit, nb_norm);
    }

    public static PetriNet genJobProcessing(int nb_machines) {
        return uniolunisaar.adam.generators.AdamBehavior.genJobProcessing(nb_machines);
    }

    public static PetriNet genSecuritySystem(int nb_systems) {
        return uniolunisaar.adam.generators.AdamBehavior.genSecuritySystem(nb_systems);
    }

    public static PetriNet genSelfReconfiguringRobots(int nb_robots, int nb_destroy) {
        return uniolunisaar.adam.generators.AdamBehavior.genSelfReconfiguringRobots(nb_robots, nb_destroy);
    }

    public static PetriNet genWatchdog(int nb_machines, boolean search, boolean partial_observation) {
        return uniolunisaar.adam.generators.AdamBehavior.genWatchdog(nb_machines, search, partial_observation);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGame getPetriGame(String aptFile) throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableWinningConditionException {
        return AdamBehavior.getPetriGame(aptFile);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static String getAPT(PetriNet net) throws RenderException {
        return AdamBehavior.getAPT(net, true);
    }

    public static String getDot(PetriNet net, boolean withLabels) {
        return AdamBehavior.getDot(net, withLabels);
    }

    public static String getTikz(PetriNet net) {
        return AdamBehavior.getTikz(net);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%% SOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
// %%%%%%%%%%%%%%%%%%%%%%%%%%% BDDSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static boolean existsWinningStrategyBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(path);
    }

    public static boolean existsWinningStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(path, so);
    }

    public static boolean existsWinningStrategyBDD(PetriNet net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net);
    }

    public static boolean existsWinningStrategyBDD(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net, so);
    }

    public static boolean existsWinningStrategyBDD(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net, win, so);
    }

    public static boolean existsWinningStrategyBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(game, win, so);
    }

    public static PetriNet getStrategyBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(path);
    }

    public static PetriNet getStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(path, so);
    }

    public static PetriNet getStrategyBDD(PetriNet net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net);
    }

    public static PetriNet getStrategyBDD(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net, so);
    }

    public static PetriNet getStrategyBDD(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net, win, so);
    }

    public static PetriNet getStrategyBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(game, win, so);
    }

    public static BDDGraph getGraphStrategyBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(path);
    }

    public static BDDGraph getGraphStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(path, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriNet net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net);
    }

    public static BDDGraph getGraphStrategyBDD(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net, win, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(game, win, so);
    }

    public static BDDGraph getGraphGameBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(path);
    }

    public static BDDGraph getGraphGameBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(path, so);
    }

    public static BDDGraph getGraphGameBDD(PetriNet net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net);
    }

    public static BDDGraph getGraphGameBDD(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net, so);
    }

    public static BDDGraph getGraphGraphBDD(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGraph(net, win, so);
    }

    public static BDDGraph getGraphGameBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(game, win, so);
    }

    public static Pair<BDDGraph, PetriNet> getStrategiesBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(path);
    }

    public static Pair<BDDGraph, PetriNet> getStrategiesBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(path, so);
    }

    public static Pair<BDDGraph, PetriNet> getStrategiesBDD(PetriNet net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net);
    }

    public static Pair<BDDGraph, PetriNet> getStrategiesBDD(PetriNet net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net, so);
    }

    public static Pair<BDDGraph, PetriNet> getStrategiesBDD(PetriNet net, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net, win, so);
    }

    public static Pair<BDDGraph, PetriNet> getStrategiesBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(game, win, so);
    }

}
