package uniolunisaar.adam;

import java.util.ArrayList;
import java.util.List;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.winningconditions.WinningCondition;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamModelChecker {

    public static String toFlowLTLFormula(PetriGame game, WinningCondition.Objective winCon) {
        List<Place> specialPlaces = new ArrayList<>();
        for (Place p : game.getPlaces()) {
            if (game.isSpecial(p)) {
                specialPlaces.add(p);
            }
        }
        StringBuilder sb = new StringBuilder("A(");
        switch (winCon) {
            case A_SAFETY:
                sb.append("G(");
                for (Place specialPlace : specialPlaces) {
                    sb.append("!(").append(specialPlace.getId()).append(")").append(" AND ");
                }
                sb.setLength(sb.length() - 5);
                sb.append(")")

        }
        sb.append(")");
        return sb.toString();
    }
}
