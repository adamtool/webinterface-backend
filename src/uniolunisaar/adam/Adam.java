package uniolunisaar.adam;

import java.io.IOException;
import java.io.PrintStream;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.exceptions.CouldNotFindSuitableWinningConditionException;
import uniolunisaar.adam.ds.exceptions.NetNotSafeException;
import uniolunisaar.adam.ds.exceptions.NoStrategyExistentException;
import uniolunisaar.adam.ds.exceptions.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.exceptions.NotSupportedGameException;
import uniolunisaar.adam.logic.exceptions.ParameterMissingException;
import uniolunisaar.adam.ds.exceptions.SolvingException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;
import uniolunisaar.adam.logic.AdamBehavior;
import uniolunisaar.adam.logic.exceptions.CouldNotCalculateException;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class Adam {

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GENERATORS %%%%%%%%%%%%%%%%%%%%%%%%%%%%   
    public static PetriGame genConcurrentMaschines(int nb_machines, int nb_workpieces) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genConcurrentMaschines(nb_machines, nb_workpieces);
    }

    public static PetriGame genContainerTerminal(int nb_systems) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genContainerTerminal(nb_systems);
    }

    public static PetriGame genDocumentWorkflow(int nb_clerks, boolean allyes) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genDocumentWorkflow(nb_clerks, allyes);
    }

    public static PetriGame genEmergencyBreakdown(int nb_crit, int nb_norm) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genEmergencyBreakdown(nb_crit, nb_norm);
    }

    public static PetriGame genJobProcessing(int nb_machines) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genJobProcessing(nb_machines);
    }

    public static PetriGame genSecuritySystem(int nb_systems) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genSecuritySystem(nb_systems);
    }

    public static PetriGame genSelfReconfiguringRobots(int nb_robots, int nb_destroy) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genSelfReconfiguringRobots(nb_robots, nb_destroy);
    }

    public static PetriGame genWatchdog(int nb_machines, boolean search, boolean partial_observation) {
        return uniolunisaar.adam.generators.games.AdamBehavior.genWatchdog(nb_machines, search, partial_observation);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGame getPetriGame(String aptFile) throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableWinningConditionException, CouldNotCalculateException {
        return AdamBehavior.getPetriGame(aptFile);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static String getAPT(PetriGame net) throws RenderException {
        return AdamBehavior.getAPT(net, true);
    }

    public static String getDot(PetriGame game, boolean withLabels) {
        return AdamBehavior.getDot(game, withLabels);
    }

    public static String getTikz(PetriGame game) {
        return AdamBehavior.getTikz(game);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%% SOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% BDDSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static boolean existsWinningStrategyBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(path);
    }

    public static boolean existsWinningStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(path, so);
    }

    public static boolean existsWinningStrategyBDD(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net);
    }

    public static boolean existsWinningStrategyBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net, so);
    }

    public static boolean existsWinningStrategyBDD(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(net, win, so);
    }

//    public static boolean existsWinningStrategyBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.existsWinningStrategy(game, win, so);
//    }

    public static PetriGame getStrategyBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(path);
    }

    public static PetriGame getStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(path, so);
    }

    public static PetriGame getStrategyBDD(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net);
    }

    public static PetriGame getStrategyBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net, so);
    }

    public static PetriGame getStrategyBDD(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(net, win, so);
    }

//    public static PetriGame getStrategyBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategy(game, win, so);
//    }

    public static BDDGraph getGraphStrategyBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(path);
    }

    public static BDDGraph getGraphStrategyBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(path, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net, so);
    }

    public static BDDGraph getGraphStrategyBDD(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(net, win, so);
    }

//    public static BDDGraph getGraphStrategyBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphStrategy(game, win, so);
//    }

    public static BDDGraph getGraphGameBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(path);
    }

    public static BDDGraph getGraphGameBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(path, so);
    }

    public static BDDGraph getGraphGameBDD(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net);
    }

    public static BDDGraph getGraphGameBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net, so);
    }

    public static BDDGraph getGraphGameBDD(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(net, win, so);
    }
//
//    public static BDDGraph getGraphGameBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getGraphGame(game, win, so);
//    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(String path) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(path);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(String path, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, ParseException, IOException, NoStrategyExistentException, ParameterMissingException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(path, so);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame net) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame net, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net, so);
    }

    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame net, WinningCondition.Objective win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException, SolvingException {
        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(net, win, so);
    }

//    public static Pair<BDDGraph, PetriGame> getStrategiesBDD(PetriGame game, WinningCondition win, BDDSolverOptions so) throws CouldNotFindSuitableWinningConditionException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParameterMissingException, ParseException {
//        return uniolunisaar.adam.symbolic.bddapproach.AdamBehavior.getStrategies(game, win, so);
//    }
    // %%%%%%%%%%%%%%%%%%%%%%%%%%% QBFSOLVER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    // %%%%%%%%%%%%%%%%%%%%%%%%%%% LOGGER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Sets the streams of the logger to the given streams as long they are not
     * null.
     *
     * @param shortMessageStream - for short messages, standard is std.out, can
     * made silent by Logger.getInstance().setVerbose(false)
     * @param verboseMessageStream - for verbose messages, standard is std.out,
     * can be activated by Logger.getInstance().setVerbose(true)
     * @param warningStream - for warning, standard std.out
     * @param errorStream - for error, standard std.err
     */
    public static void setOutputStreams(PrintStream shortMessageStream, PrintStream verboseMessageStream, PrintStream warningStream, PrintStream errorStream) {
        if (shortMessageStream != null) {
            Logger.getInstance().setErrorStream(shortMessageStream);
        }
        if (verboseMessageStream != null) {
            Logger.getInstance().setErrorStream(verboseMessageStream);
        }
        if (warningStream != null) {
            Logger.getInstance().setErrorStream(warningStream);
        }
        if (errorStream != null) {
            Logger.getInstance().setErrorStream(errorStream);
        }
    }
}
