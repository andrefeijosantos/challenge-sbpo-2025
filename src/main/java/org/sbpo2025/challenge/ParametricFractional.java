package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.Status;

public class ParametricFractional extends Approach {

	ParametricModel paramModel;
	
	final double TOL = 1e-6;
	
	public ParametricFractional(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
		paramModel = new ParametricModel(inst);
		paramModel.build();
	}

	
	public ChallengeSolution optimize() {
		try {
			printHeader();
			
			double rAst = 0;
			double value;
			int it = 0;
			
			while(getRemainingTime(stopWatch) <= 5) {	
				paramModel.setTimeLimit(getRemainingTime(stopWatch));
				paramModel.setRatio(rAst);
				paramModel.solve();
				
				objVal = paramModel.getObjValue(); it++;
				printLine(it, rAst, (int) paramModel.getValue(paramModel.sumItems), (int) paramModel.getValue(paramModel.sumAisles));
				value = paramModel.getObjValue();
				
				if(value >= 0 && value <= TOL)
					break;
				else {
					rAst = paramModel.getValue(paramModel.sumItems)/paramModel.getValue(paramModel.sumAisles);
					solution = paramModel.saveSolution();
					objVal = rAst;
				}
			}
			
			logln("");
			logln("Solution found: " + rAst);
			logln("Prooved optimal? " + (paramModel.getStatus() == Status.Optimal) + "\n");
			
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void printHeader() throws IloException {
		logln("SPO Optimizer version 1 (authors: @andrefeijosantos, @PedroFiorio)");
		logln("Thread count: CPLEX using up to " + paramModel.getNumThreads() + " threads");
		logln("Variable types: 1 continuous; " + (paramModel.y.length + paramModel.p.length) + " binaries");
		logln("");
		
		logln("  it  |    q*    |  N  |  D  |  f(q*)  ");
	}
	
	private void printLine(int it, double rAst, int N, int D) throws IloException {
		log(String.format("%5" + "s |", it));
		log(String.format("%9.5f" + " |", rAst));
		log(String.format("%4" + "s |", (int) N));
		log(String.format("%4" + "s |", (int) D));
		logln(String.format("%8.3f" + "", objVal));
	}
}
