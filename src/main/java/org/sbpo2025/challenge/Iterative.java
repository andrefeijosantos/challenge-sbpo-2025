package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.Status;


public class Iterative extends Approach {
	
	// CPLEX model.
	ItModel model;
	
	public Iterative(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
		this.model = new ItModel(inst, 5);
	}

	
	public ChallengeSolution optimize() {
		try {		
			model.build();			
			print_header();
			
			
			int H = inst.aisles.size();
			for(int h = 1; h <= H; h++) {
				if(getRemainingTime(stopWatch) <= 5) {
					logln("Time Limit reached.");
					break;
				}
				
				// Sets the Lower Bound.
				if(solution != null) {
					int new_lb = (int) Math.floor(objVal * h + 1);
					if(new_lb > inst.UB) break;
					model.setLB(new_lb);
				}
				
				// Set parameters for running the model for h aisles..
				model.setTimeLimit(getRemainingTime(stopWatch));
				model.setSumY(h);
				
				// Optimizes model for "num_aisles" aisles.
				model.solve();
				
				// If a better solution was found.
				if(model.getStatus() != IloCplex.Status.Infeasible && model.getObjValue()/h > objVal) {					
					objVal = model.getObjValue()/h;
					solution = model.saveSolution();
					
					// Update max_aisles.
					double result = inst.UB / objVal;
					H = (int) Math.floor(result);
				}
				
				print_line(h, H, model.getStatus());
			}
			
			logln("Optimal Solution: " + objVal + "\n");
			
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		logln("Thread count: CPLEX using up to " + model.getNumThreads() + " threads");
		logln("Variable types: 1 continuous; " + (model.y.length + model.p.length) + " integer (" + (model.y.length + model.p.length) + " binaries)");
		logln("");
		
		logln("  h  |  H  |  LB  |  UB  |  Incumbent  |  Status  ");
	}
	
	private void print_line(int h, int H, Status status) throws IloException {
		log(String.format("%4" + "s |", h));
		log(String.format("%4" + "s |", H));
		log(String.format("%5" + "s |", (int) model.z.getLB()));
		log(String.format("%5" + "s |", (int) model.z.getUB()));
		
		if(objVal > 0) log(String.format("%12.6f" + " |",  objVal));
		else log(String.format("%12" + "s |", "-"));
		
		logln(String.format("%10" + "s", status));
	}
}
