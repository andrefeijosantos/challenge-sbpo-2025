package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.*;
import ilog.cplex.*;


public class ParallelIterative extends Approach {
	
	// CPLEX model.
	ItModel ascendingModel;
	ItModel decendingModel;
	
	// Solutions
	ChallengeSolution ascendingSolution;
	ChallengeSolution decendingSolution;
	
	double ascendingIncumbent;
	double decendingIncumbent;
	
	// Solver informations.
	boolean ascendingOptimal = false;
	boolean ascendingTimeOut = false;
	boolean decendingOptimal = false;
	boolean decendingTimeOut = false;
	boolean optimal;
	
	// Threads informations.
	int ascendingLastIt;
	int decendingLastIt;
	
	// Upper and lower bounds for items.
	int upperBoundItems;
	int lowerBoundItems;
	
	int MIN_AISLES;
	int MAX_AISLES;
	int TOTAL_AISLES;
	
	
	public ParallelIterative(Instance inst, StopWatch stopWatch, long timeLimit) {
		super(inst, stopWatch, timeLimit);
		
		ascendingModel = new ItModel(inst, (int)Math.ceil(Runtime.getRuntime().availableProcessors()/2));
		decendingModel = new ItModel(inst, (int)Math.floor(Runtime.getRuntime().availableProcessors()/2));
		
		ascendingModel.build();
		decendingModel.build();
		
		lowerBoundItems = inst.LB;
		upperBoundItems = inst.UB;
		
		TOTAL_AISLES = inst.aisles.size();
		MAX_AISLES   = inst.aisles.size();
	}

	
	public ChallengeSolution optimize() {
		try {	
			print_header();
			
			// Build and start both threads.
			Thread at = getAscendingThread();
			Thread dt = getDecendingThread();
			
			at.start();
			dt.start();
			
			// Wait for both threads.
			try {
				at.join();
				dt.join();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			// Get the best thread solution.
			if(ascendingIncumbent > decendingIncumbent) {
				solution = ascendingSolution;
				objVal   = ascendingIncumbent;
			} else {
				solution = decendingSolution;
				objVal   = decendingIncumbent;
			}
			
			// Check if any thread found  a optimal solution.
			optimal = ascendingOptimal || decendingOptimal;
			
			logln("");
			logln("Ascending Thread finished at: " + ascendingLastIt);
			logln("Decending Thread finished at: " + decendingLastIt);
			logln("Solution found: " + objVal);
			logln("Prooved optimal? " + optimal + "\n");
			
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	private Thread getDecendingThread() {
		return new Thread() {
			@Override
			public void run() {	
				try {
					for(int h = TOTAL_AISLES; h >= 0 && !ascendingOptimal; h--) {
						decendingLastIt = h;
						
						if(getRemainingTime(stopWatch) <= 5) {
							logln("Time Limit reached (DE).");
							decendingTimeOut = true;
							break;
						}
						
						// Set parameters for running the model for h aisles..
						decendingModel.setTimeLimit(getRemainingTime(stopWatch));
						decendingModel.setSumY(h);
						
						// Set upper and lower bounds.
						decendingModel.setUB(upperBoundItems);
						decendingModel.setLB(lowerBoundItems);
						if(lowerBoundItems > upperBoundItems) break;
						
						// Optimizes model for "num_aisles" aisles.
						decendingModel.solve();
						
						// If a better solution was found.
						if(decendingModel.getStatus() == IloCplex.Status.Feasible || decendingModel.getStatus() == IloCplex.Status.Optimal) {
							if(decendingModel.getObjValue()/h > decendingIncumbent) {		
								decendingIncumbent = decendingModel.getObjValue()/h;
								decendingSolution = decendingModel.saveSolution();
							}
							
							upperBoundItems = (int) decendingModel.getObjValue();
						}
						
						printLine(h, 1, decendingModel, decendingIncumbent, "DE");
					}
				} catch(IloException e) {
					e.printStackTrace();
				}
				
				decendingOptimal = !decendingTimeOut;
			}
		};
	}
	
	private Thread getAscendingThread() {
		return new Thread() {
			@Override
			public void run() {	
				try {
					for(int h = 1; h <= MAX_AISLES  && !decendingOptimal; h++) {
						ascendingLastIt = h;
						
						if(getRemainingTime(stopWatch) <= 5) {
							logln("Time Limit reached (AC).");
							ascendingTimeOut = true;
							optimal = false;
							break;
						}
						
						// Update minimum aisles.
						if(ascendingSolution == null)
							MIN_AISLES = h;
						
						// Set parameters for running the model for h aisles..
						ascendingModel.setTimeLimit(getRemainingTime(stopWatch));
						ascendingModel.setSumY(h);
						
						// Update lower bound.
						lowerBoundItems = Math.max(inst.LB, (int) Math.floor(ascendingIncumbent * h + 1));					
						ascendingModel.setLB(lowerBoundItems);
						
						// Set upper bound for items.
						if(lowerBoundItems > upperBoundItems) break;
						ascendingModel.setUB(upperBoundItems);
						
						// Optimizes model for "num_aisles" aisles.
						ascendingModel.solve();
						
						// If a better solution was found.
						if(ascendingModel.getStatus() == IloCplex.Status.Feasible || ascendingModel.getStatus() == IloCplex.Status.Optimal) { 
							if(ascendingModel.getObjValue()/h > ascendingIncumbent) {						
								ascendingIncumbent = ascendingModel.getObjValue()/h;
								ascendingSolution = ascendingModel.saveSolution();
							}
							
							// Update maximum of aisles.
							MAX_AISLES = (int) Math.floor(inst.UB / ascendingIncumbent);
						}
						
						printLine(h, MAX_AISLES, ascendingModel, ascendingIncumbent, "AC");
					}
				} catch(IloException e) {
					e.printStackTrace();
				}
				
				ascendingOptimal = !ascendingTimeOut;
			}
		};
	}
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void print_header() throws IloException {
		logln("SPO Optimizer (authors: @andrefeijosantos, @PedroFiorio)");
		logln("Approach: Parallel Iterative Solver");
		logln("Thread count: CPLEX using up to " + Runtime.getRuntime().availableProcessors() + " threads");
		logln("Variable types: 1 continuous; " + (ascendingModel.y.length + ascendingModel.p.length) + 
				" integer (" + (ascendingModel.y.length + ascendingModel.p.length) + " binaries)");
		logln("");
		
		logln("  Thread  |  h  |  H  |  LB  |  UB  |  Incumbent  |  Status  ");
	}
	
	private void printLine(int h, int H, ItModel model, double incumbent, String threadName) throws IloException {
		String log = String.format("%9" + "s |", threadName);
		log += String.format("%4" + "s |", h);
		log += String.format("%4" + "s |", H);
		log += String.format("%5" + "s |", (int) model.z.getLB());
		log += String.format("%5" + "s |", (int) model.z.getUB());
		
		if(incumbent > 0) log += String.format("%12.6f" + " |",  incumbent);
		else log += String.format("%12" + "s |", "-");
		
		log += String.format("%10" + "s", model.getStatus());
		logln(log);
	}
}
