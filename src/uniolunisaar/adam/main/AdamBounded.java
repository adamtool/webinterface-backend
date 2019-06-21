package uniolunisaar.adam.main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolver;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolverFactory;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolverOptions;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolver;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolverFactory;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolverOptions;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamBounded {
	
	private static int bb;
	/**
	 * 
	 * @param args  
	 * 				0 -> seq oder tc
	 * 				1 -> bound n
	 *      		2 -> benchmark name or .apt file
	 *      		3 -> benchmark parameter or bound b
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws CouldNotFindSuitableConditionException
	 * @throws SolvingException
	 * @throws CalculationInterruptedException
	 * @throws NoStrategyExistentException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, InterruptedException, NoStrategyExistentException {
		if ((args.length != 4) && (args.length != 3))
			printHelpAndExit("Invalid number of arguments");
		String option = args[0];
		String nn = args[1];
		String benchmark = args[2];
		
        if (!(option.equals("tc"))&&!(option.equals("seq"))) {
        	System.out.println("Invalid solver option, tc for true concurrent and seq for sequential solving available");
        	return;
        }
        int n = Integer.parseInt(nn);
        
        String filename = checkValidBenchmarkAndParameter(args, benchmark);
        
        PrintStream silentStream = new PrintStream(new OutputStream(){
            public void write(int b) {
                //Do nothing
            }
        });
        PrintStream originalStream = System.out;
        if (option.equals("tc")) {
        	for (int i = 2; i <= n; ++i) {
        		System.setOut(silentStream);
        		QbfConSolverOptions options = new QbfConSolverOptions(i, bb);
                QbfConSolver<? extends Condition> solver = QbfConSolverFactory.getInstance().getSolver(filename, true, (QbfConSolverOptions)options);
                boolean succ = solver.existsWinningStrategy();
                System.setOut(originalStream);
                String result = succ ? "SAT" : "UNSAT";
                System.out.println("Solver: tc; Benchmark/File: " + benchmark + "; Bound: " + i + " Result: " + result);
                if (succ) {
                	PGTools.savePG2Dot("strategy",solver.getStrategy(), false);
                	System.out.println("Output strategy to strategy.dot");
                	return;
                }   	
        	}
        
        } else {
        	for (int i = 2; i <= n; ++i) {
        		System.setOut(silentStream);
        		QbfSolverOptions options = new QbfSolverOptions(i, bb);
                QbfSolver<? extends Condition> solver = QbfSolverFactory.getInstance().getSolver(filename, true, (QbfSolverOptions)options);
                boolean succ = solver.existsWinningStrategy();
                String result = succ ? "SAT" : "UNSAT";
                System.setOut(originalStream);
                System.out.println("Solver: seq; Benchmark/File: " + benchmark + "; Bound: " + i + " Result: " + result);
                if (succ) {
                	PGTools.savePG2Dot("strategy",solver.getStrategy(), false);
                	System.out.println("Output strategy to strategy.dot");
                	return;
                }
        	}
        
        }
    }
	
	/**
	 * Prints the error message, outputs the help and terminates in failure status.
	 * @param errorMessage
	 */
	private static void printHelpAndExit(String errorMessage) {
		String help = "Usage: ./adam_bounded  [tc|seq] [bound n] [benchmark name | intput file] [benchmark parameter | bound b]\n\n"
					+ " arg 1:        tc for the true concurrent solver, seq for the sequential solver\n"
					+ " arg 2:        the maximal bound the solver is executed with \n"
					+ " arg 3:        the name of the benchmark: AS, CA, DR, PL, DW or an input .apt file\n"
					+ " arg 4:        the parameter for the chosen benchmark or the bound b for the input file\n";
		System.out.println(errorMessage);
		System.out.println("");
		System.out.print(help);
		System.exit(1);
	}
	
	/**
	 * Returns the file name as string for the chosen benchmark and parameter.
	 * @param parameter
	 * @param benchmark
	 * @return
	 */
	private static String checkValidBenchmarkAndParameter(String[] args, String benchmark) {
		String filename = null;
		if (benchmark.endsWith(".apt")) {
			bb = Integer.parseInt(args[3]);
			return benchmark;
		}
		int parameter = Integer.parseInt(args[3]);
		switch (benchmark) { 
		case "AS":
			if (! (2 <= parameter && parameter <= 3))
				printHelpAndExit("AS only allows parameters 2 and 3");
			bb = parameter;
			return "benchmarks_ATVA/" + parameter + "_burglar.apt";
		case "CA":
			if (! (2 <= parameter && parameter <= 5))
				printHelpAndExit("CA only allows parameters 2 to 5");
			bb = 1;
			return "benchmarks_ATVA/" + parameter + "_IndependentNets.apt";
		case "DR":
			if (! (2 <= parameter && parameter <= 5))
				printHelpAndExit("CA only allows parameters 2 to 5");
			bb = 0;
			return "benchmarks_ATVA/" + parameter + "_DR.apt";
		case "PL":
			if (! (1 <= parameter && parameter <= 7))
				printHelpAndExit("PL only allows parameters 1 to 7");
			bb = parameter;
			return "benchmarks_ATVA/" + parameter + "_ProductionLine.apt";
		case "DW":
			if (! (1 <= parameter && parameter <= 11))
				printHelpAndExit("DW only allows parameters 1 to 11");
			return "benchmarks_ATVA/" + parameter + "_clerks.apt";
		default:
			printHelpAndExit("Invalid benchmark name! Only AS, CA, DR, PL and DW available");
			return null;
		}
	}

}
