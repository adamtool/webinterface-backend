package uniolunisaar.adam.util;

import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;

/**
 * This class is just to not implement the stepwise graph explorer in the
 * web interface new for the explicit approach.
 *
 * @author Manuel Gieseking
 */
public class ExplicitBDDGraphTransformer {

    /**
     * @param dcs
     * @return
     */
    public static BDDState decisionset2BDDState(DecisionSet dcs) {
        BDDState state = new BDDState(null, -1, dcs.toString());
        return state;
    }
}
