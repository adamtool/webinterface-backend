package uniolunisaar.adam;

import java.io.IOException;
import java.io.PrintStream;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.AdamBehavior;
import uniolunisaar.adam.exceptions.pg.CouldNotCalculateException;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class Adam {

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% GENERATORS %%%%%%%%%%%%%%%%%%%%%%%%%%%%   
    public static PetriGame genConcurrentMaschines(int nb_machines, int nb_workpieces) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genConcurrentMaschines(nb_machines, nb_workpieces);
    }

    public static PetriGame genContainerTerminal(int nb_systems) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genContainerTerminal(nb_systems);
    }

    public static PetriGame genDocumentWorkflow(int nb_clerks, boolean allyes) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genDocumentWorkflow(nb_clerks, allyes);
    }

    public static PetriGame genEmergencyBreakdown(int nb_crit, int nb_norm) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genEmergencyBreakdown(nb_crit, nb_norm);
    }

    public static PetriGame genJobProcessing(int nb_machines) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genJobProcessing(nb_machines);
    }

    public static PetriGame genSecuritySystem(int nb_systems) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genSecuritySystem(nb_systems);
    }

    public static PetriGame genSelfReconfiguringRobots(int nb_robots, int nb_destroy) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genSelfReconfiguringRobots(nb_robots, nb_destroy);
    }

    public static PetriGame genWatchdog(int nb_machines, boolean search, boolean partial_observation) {
        return uniolunisaar.adam.generators.synthesis.AdamBehavior.genWatchdog(nb_machines, search, partial_observation);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static PetriGame getPetriGame(String aptFile) throws NotSupportedGameException, ParseException, IOException, CouldNotFindSuitableConditionException, CouldNotCalculateException {
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
