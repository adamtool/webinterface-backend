package uniolunisaar.adam;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 *
 * @author Manuel Gieseking
 */
public class Adam {

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPORTER %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public static String pn2pnml(PetriNet net) throws RenderException {
        return PNTools.pn2pnml(net);
    }

    public static void save2pnml(String path, PetriNet net) throws FileNotFoundException, RenderException {
        PNTools.save2pnml(path, net);
    }

    public static void save2pdf(String path, PetriNet net) throws FileNotFoundException, RenderException {
        PNTools.savePN2PDF(path, net, false, false);
    }

    public static String getAPT(PetriGameWithTransits net) throws RenderException {
        return PNWTTools.getAPT(net, true, true);
    }

    public static String getDot(PetriGameWithTransits game, boolean withLabels) {
        return PNWTTools.pnwt2Dot(game, withLabels);
    }

    public static Condition.Objective getCondition(PetriNetWithTransits net) throws CouldNotFindSuitableConditionException {
        return PGTools.parseConditionFromNetExtensionText(net);
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
