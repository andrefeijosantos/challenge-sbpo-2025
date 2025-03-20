// To compile: mvn clean package
// To run: java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar <input-file> <output-file>

package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

public class Challenge {
	
    public static void main(String[] args) {
        // Start the stopwatch to track the running time
        StopWatch stopWatch = StopWatch.createStarted();

        // Check for the correct usage of command-line arguments.
        if (args.length != 2) {
            System.out.println("Usage: java -Djava.library.path=C:\\Applications\\CPLEX_Studio2211\\opl\\bin\\x64_win64 "
            		+ "-jar target/ChallengeSBPO2025-1.0.jar <inputFilePath> <outputFilePath>");
            return;
        }
        
        // Read instance file.
        Instance instance = new Instance(args[0]);
        
        // Execute solver for instance read.
        var solver = new ChallengeSolver(instance);
        ChallengeSolution solution = solver.solve(Method.BranchAndBound, stopWatch);
        
        instance.writeOutput(solution, args[1]);
    }
}
