package org.sbpo2025.challenge;

import java.util.BitSet;

import org.apache.commons.lang3.time.StopWatch;


public class TSHeuristic extends Approach {

	// Model to evaluate solutions.
	HeuristicModel model;
	
	// 1 if a-th aisle is in solution; 0, otherwise.
	BitSet aisles;
	
	public TSHeuristic(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
		
		model = new HeuristicModel(inst, Runtime.getRuntime().availableProcessors());
		model.build();
	}
	
	
	public ChallengeSolution optimize() {
		return null;
	}

}
