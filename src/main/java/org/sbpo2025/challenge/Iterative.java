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
		this.model = new ItModel(inst);
	}

	
	public ChallengeSolution optimize() {
		try {
			model.build();
			
			int H = inst.aisles.size();
			print_header();
			
			for(int h = 1; h <= H; h++) {
				if(getRemainingTime(stopWatch) <= 5) {
					logln("Time Limit reached.");
					break;
				}
				
				// Sets the number of aisles to the model.
				model.setSumY(h);
				
				// Sets the Lower Bound.
				if(solution != null) {
					double d_lb = Math.floor(objVal * h + 1);
					int    new_lb = (int) d_lb;
					
					if(new_lb > inst.UB)
						break;
					
					model.setLB(new_lb);
				}
				
				// Optimizes model for "num_aisles" aisles.
				model.setTimeLimit(getRemainingTime(stopWatch));
				model.solve();
				
				if(model.getStatus() == IloCplex.Status.Optimal) {					
					// If a better solution was found.
					if(model.getObjValue()/h > objVal) {
						objVal = model.getObjValue()/h;
						solution = model.saveSolution();
						
						// Update max_aisles.
						double result = inst.UB / objVal;
						H = (int) Math.floor(result);
					}
					
					if(model.getObjValue() == inst.UB)
						break;
				}
				
				print_line(h, H, model.getStatus());
			}
			
			System.out.println("Optimal Solution: " + objVal + "\n");
			
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		// logln("Thread count: CPLEX using up to " + model.getParam(IloCplex.Param.Threads) + " threads");
		logln("Variable types: 1 continuous; " + model.x.size() + model.y.size() + " integer (" + model.y.size() + " binaries)");
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
