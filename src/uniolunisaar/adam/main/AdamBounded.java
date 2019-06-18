package uniolunisaar.adam.main;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolver;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolverFactory;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolverOptions;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamBounded {

    public static void main(String[] args) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        QbfConSolverOptions options = new QbfConSolverOptions();
        // TODO: add your parameters here to the options
        QbfConSolver<? extends Condition> solver = QbfConSolverFactory.getInstance().getSolver(args[0], true, options);
        // TODO: don't know how you did it, but here you can do what ever you want
        boolean succ = solver.existsWinningStrategy();
    }

}
