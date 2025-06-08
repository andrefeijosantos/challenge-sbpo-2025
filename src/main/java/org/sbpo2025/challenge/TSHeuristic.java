package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;


public class TSHeuristic extends Approach {

	// Tabu list.
	Map<Integer, Map<BitSet, Boolean>> tabu;
	
	// Model to evaluate solutions.
	boolean timeOut = false,
			timeOutMv1 = false,
			timeOutMv2 = false;
	
	// 1 if a-th aisle is in solution; 0, otherwise.
	BitSet aisles;
	int numItems;
	
	// Initial solution data.
	BSearch initialSolution;
	int minAisles,
	    maxAisles;
	
	// Moves.
	Thread mv1Thread;
	Thread mv2Thread;
	Move mv1, mv2;
	double mv1Value,
		   mv2Value;
	ChallengeSolution mv1Solution,
					  mv2Solution;
	HeuristicModel mv1Model, mv2Model;
	
	
	public TSHeuristic(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
		
		aisles = new BitSet();
		aisles.clear();
		
		initialSolution = new BSearch(inst, stopWatch, (int)(2*60*1000), 10);

		mv1Model = new HeuristicModel(inst, Runtime.getRuntime().availableProcessors());
		mv1Model.build();
		
		mv2Model = new HeuristicModel(inst, Runtime.getRuntime().availableProcessors());
		mv2Model.build();
		
		/*for(int a = 0; a < inst.aisles.size(); a++) {
			int cnt = 0;
			for(int i : inst.aisles.get(a).keySet())
				if(inst.aisles.get(a).get(i) >= inst.D.get(i)) cnt++;
			log(cnt + "/" + inst.aisles.get(a).keySet().size());
			
			if(cnt == inst.aisles.get(a).keySet().size()) logln("<===========");
			else logln("");
		} 
		
		logln("\n\n");*/
	}
	
	
	public ChallengeSolution optimize() {
		try {
		
			// Finds a initial solution using parallel iterative exact method.
			solution = initialSolution.optimize();
			for(int a : solution.aisles())
				aisles.set(a);
			minAisles = initialSolution.ascendingLastNotAborted + 1;
			maxAisles = initialSolution.decendingLastNotAborted + 1;
			objVal = initialSolution.objVal;
			
			logln("Running TS for range [" + minAisles + ", " + maxAisles + "]");
			
			while(!timeOut) {
				Move mv = move();
				
				if(mv == null || mv.newObj() <= 0 || Math.abs(mv.newObj() - objVal) <= 1) break;
				else {
					objVal = mv.newObj();
					logln("" + mv);
					switch(mv.mvType()) {
					case 1:
						aisles.clear(mv.a1());
						break;
					case 2:
						aisles.set(mv.a1());
						break;
					}
				}
			}

			
		} catch(IloException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	public Move move() throws IloException, InterruptedException {
		Move mv = new Move(-1.0, -1, -1, -1);
		
		mv1Thread = getMv1Thread();
		mv2Thread = getMv2Thread();
		
		mv1Thread.start();
		mv2Thread.start();
		
		mv1Thread.join();
		mv2Thread.join();
		
		timeOut = timeOutMv1 | timeOutMv2;
		if(mv1Solution == null && mv2Solution == null)
			return null;
		
		if(mv1Value > mv2Value) {
			solution = mv1Solution;
			mv = mv1;
		} else {
			solution = mv2Solution;
			mv = mv2;
		}
		
		return mv;
	}
	
	// Remove aisles.
	private Thread getMv1Thread() {
		return new Thread() {
			@Override
			public void run() {	
				try {
					mv1Solution = null;
					mv1Value = objVal;
					
					if(aisles.cardinality() > minAisles) {
						BitSet copy = (BitSet) aisles.clone();
						
						for(int a = 0; a < inst.aisles.size(); a++) {
							if(!copy.get(a)) continue;
							
							copy.clear(a);
							
							mv1Model.setAisles(copy);
							
							mv1Model.setTimeLimit(getRemainingTime(stopWatch));
							mv1Model.solve();
							
							if(mv1Model.getStatus() != IloCplex.Status.Infeasible && mv1Model.getStatus() != IloCplex.Status.Unknown &&
									mv1Model.getObjValue()/copy.cardinality() > mv1Value) {
								mv1Value = mv1Model.getObjValue()/copy.cardinality();
								mv1 = new Move(mv1Value, 1, a, a);
								mv1Solution = mv1Model.saveSolution();
							}
							
							copy.set(a);
							
							// Time out.
							if(mv1Model.getStatus() != IloCplex.Status.Infeasible && mv1Model.getStatus() != IloCplex.Status.Optimal) {
								timeOutMv1 = true;
								break;
							}
						}
					}
				} catch(IloException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	// Add aisles.
	private Thread getMv2Thread() {
		return new Thread() {
			@Override
			public void run() {	
				try {
					mv2Solution = null;
					mv2Value = objVal;
					
					if(aisles.cardinality() > minAisles) {
						BitSet copy = (BitSet) aisles.clone();
						
						for(int a = 0; a < inst.aisles.size(); a++) {
							if(!copy.get(a)) continue;
							
							copy.clear(a);
							
							mv2Model.setAisles(copy);
							
							mv2Model.setTimeLimit(getRemainingTime(stopWatch));
							mv2Model.solve();
							
							if(mv2Model.getStatus() != IloCplex.Status.Infeasible && mv2Model.getStatus() != IloCplex.Status.Unknown &&
									mv2Model.getObjValue()/copy.cardinality() > mv2Value) {
								mv2Value = mv2Model.getObjValue()/copy.cardinality();
								mv2 = new Move(mv2Value, 1, a, a);
								mv2Solution = mv2Model.saveSolution();
							}
							
							copy.set(a);
							
							// Time out.
							if(mv2Model.getStatus() != IloCplex.Status.Infeasible && mv2Model.getStatus() != IloCplex.Status.Optimal) {
								timeOutMv2 = true;
								break;
							}
						}
					}
				} catch(IloException e) {
					e.printStackTrace();
				}
			}
		};
	}

	public void logAisles() {
		for(int a = 0; a < inst.aisles.size(); a++) {
			if(aisles.get(a)) log("1");
			else log("0");
		}
		logln("");
	}
}
