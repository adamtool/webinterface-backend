package uniolunisaar.adam.main;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolver;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolverFactory;
import uniolunisaar.adam.bounded.qbfapproach.solver.QbfSolverOptions;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolver;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolverFactory;
import uniolunisaar.adam.bounded.qbfconcurrent.solver.QbfConSolverOptions;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.solver.SolverOptions;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamBounded {
	private static CommandLine cmd;
	private static Options commandLineOptions;
	private static int b;
	
	public static void main(String[] args) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException {
        commandLineOptions = new Options();	
		Option solverOpt = new Option("solver", true, "The solver to use, either \"tc\" or \"seq\" ");
        solverOpt.setRequired(true);
        Option benchmarkOpt = new Option("benchmark", true, "The benchmark to solve, either \"AS\", \"CA\",\"DR\",\"PL\" or \"DW\" ");
        benchmarkOpt.setRequired(true);
        Option parameterOpt = new Option("parameter", true, "The parameter of the benchmark");
        parameterOpt.setRequired(true);
        Option boundOpt = new Option("bound",true, "The maximal bound that is solved");
        commandLineOptions.addOption(solverOpt);
        commandLineOptions.addOption(benchmarkOpt);
        commandLineOptions.addOption(parameterOpt);
        commandLineOptions.addOption(boundOpt);
        
        BasicParser parser = new BasicParser();
        try {
			cmd = parser.parse(commandLineOptions, args);
			
		} catch (org.apache.commons.cli.ParseException e) {
			System.out.println("Parsing the commandline failed, terminating.");
			System.exit(1);
		}      
        
        if (!(cmd.getOptionValue("solver").equals("tc"))&&!(cmd.getOptionValue("solver").equals("seq"))) {
        	printHelpAndExit("Invalid solver option, tc for true concurrent and seq for sequential solving available");
        	return;
        }
        int parameter = Integer.parseInt(cmd.getOptionValue("parameter"));
        int bound = Integer.parseInt(cmd.getOptionValue("bound"));
        String benchmark = cmd.getOptionValue("benchmark");
        
        String filename = checkValidBenchmarkAndParameter(parameter, benchmark);
        if (cmd.getOptionValue("solver").equals("tc")) {
        	for (int i = 2; i <= bound;) {
        		QbfConSolverOptions options = new QbfConSolverOptions(i,b);
                QbfConSolver<? extends Condition> solver = QbfConSolverFactory.getInstance().getSolver(filename, true, (QbfConSolverOptions)options);
                boolean succ = solver.existsWinningStrategy();
                System.out.println("Solver: tc; Benchmark: " + benchmark + "; Bound: " + bound);
                if (succ)
                	return;
        	}
        
        }else {
        	for (int i = 2; i <= bound;) {
        		QbfSolverOptions options = new QbfSolverOptions(i,b);
                QbfSolver<? extends Condition> solver = QbfSolverFactory.getInstance().getSolver(filename, true, (QbfSolverOptions)options);
                boolean succ = solver.existsWinningStrategy();
                System.out.println("Solver: seq; Benchmark: " + benchmark + "; Bound: " + bound);
                if (succ)
                	return;
        	}
        
        }
    }
	
	private static void printHelpAndExit(String errorMessage) {
		System.out.println(errorMessage);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("AdamQbf", commandLineOptions);
		
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
				printHelpAndExit("AS only allows parameters 2 and 3");
				b = parameter;
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
			printHelpAndExit("Invalid benchmark name! Only AS, CA, DR, PL and DW available");
			return null;
		}
	}

}
