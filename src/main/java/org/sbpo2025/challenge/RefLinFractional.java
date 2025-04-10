package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.Status;


public class RefLinFractional extends Approach {

	RefLinModel refLinModel;
	
	public RefLinFractional(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
		refLinModel = new RefLinModel(inst);
		refLinModel.build();
	}

	public ChallengeSolution optimize() {
		try {
			printHeader();
			
			refLinModel.setTimeLimit(getRemainingTime(stopWatch));
			refLinModel.solve();
			objVal = refLinModel.getObjValue();
			solution = refLinModel.saveSolution();
			
			logln("");
			logln("Solution found: " + objVal);
			logln("Proved optimal? " + (refLinModel.getStatus() == Status.Optimal) + "\n");
			
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	private void printHeader() throws IloException {
		logln("SPO Optimizer version 1 (authors: @andrefeijosantos, @PedroFiorio)");
		logln("Thread count: CPLEX using up to " + refLinModel.getNumThreads() + " threads");
		logln("Variable types: 1 continuous; " + (refLinModel.y.length + refLinModel.p.length) + " binaries");
		logln("");
	}
}
