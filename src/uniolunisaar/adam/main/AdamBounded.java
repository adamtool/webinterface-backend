package uniolunisaar.adam.main;

import java.io.IOException;

import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolver;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolverFactory;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolverOptions;
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
	
	/**
	 * 
	 * @param args  
	 * 				0 -> seq oder tc
	 * 				1 -> bound n
	 *      		2 -> benchmark name
	 *      		3 -> benchmark parameter
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws CouldNotFindSuitableConditionException
	 * @throws SolvingException
	 * @throws CalculationInterruptedException
	 */
	public static void main(String[] args) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
		String option = args[0];
		String b = args[1];
		String benchmark = args[2];
		String benchmarkParameter = args[3];  
        
        if (!(option.equals("tc"))&&!(option.equals("seq"))) {
        	System.out.println("Invalid solver option, tc for true concurrent and seq for sequential solving available");
        	return;
        }
        int parameter = Integer.parseInt(benchmarkParameter);
        int bound = Integer.parseInt(b);
        
        String filename = checkValidBenchmarkAndParameter(parameter, benchmark);
        if (option.equals("tc")) {
        	for (int i = 2; i <= bound;) {
        		QbfConSolverOptions options = new QbfConSolverOptions();
                QbfConSolver<? extends Condition> solver = QbfConSolverFactory.getInstance().getSolver(filename, true, (QbfConSolverOptions)options);
                boolean succ = solver.existsWinningStrategy();
                System.out.println("Solver: tc; Benchmark: " + benchmark + "; Bound: " + bound);
                if (succ)
                	return;
        	}
        
        } else {
        	for (int i = 2; i <= bound;) {
        		QbfSolverOptions options = new QbfSolverOptions();
                QbfSolver<? extends Condition> solver = QbfSolverFactory.getInstance().getSolver(filename, true, (QbfSolverOptions)options);
                boolean succ = solver.existsWinningStrategy();
                System.out.println("Solver: seq; Benchmark: " + benchmark + "; Bound: " + bound);
                if (succ)
                	return;
        	}
        
        }
    }
	
	/**
	 * Returns the file name as string for the chosen benchmark and parameter.
	 * @param parameter
	 * @param benchmark
	 * @return
	 */
	private static String checkValidBenchmarkAndParameter(int parameter, String benchmark) {
		String filename = null;
		switch (benchmark) {
		case "AS":
			if (! (2 <= parameter && parameter <= 3))
				System.out.println("AS only allows parameters 2 and 3");
			return "benchmarks_ATVA/" + parameter + "_burglar.apt";
		case "CA":
			return filename;
		case "DR":
			return filename;
		case "PL":
			return filename;
		case "DW":
			return filename;
		default:
			System.out.println("Invalid benchmark name! Only AS, CA, DR, PL and DW available");
			return null;
		}
	}

}
