package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.Status;


public class SPOModel {
	
	// SPO instance.
	Instance inst;
	
	// CPLEX model.
	IloCplex model;
	
	// Model constants.
	List<Integer> M;
	HashMap<Pair<Integer, Integer>, Integer> q, w;
	HashMap<Triple, Integer> ub;
	
	// Model variables.
	IloNumVar z;
	List<IloIntVar> y, p;
	HashMap<Triple, IloIntVar> x;
	
	// Objective Value.
	double objVal = 0;
	
	// Constraint sum_y.
	IloLinearIntExpr sum_y;
	IloConstraint sum_y_constr = null;
	
	
	public SPOModel(Instance instance) {
		this.inst = instance;
	}
	
	public void build() {
		// Building CPLEX model.
		try {			
			model = new IloCplex();
			model.setOut(null);
		} catch(IloException e) {
			System.out.print("No model built. Error: ");
			e.printStackTrace();
		}
		
		buildConsts();
		buildVars();
		buildObjective();	
		buildConstrs();
	}
	
	private void buildConsts() {		
		// Quantity of i-th item present on a-th aisle.
		q = new HashMap<Pair<Integer, Integer>, Integer>();
		M = new ArrayList<Integer>(inst.aisles.size());
		for (int a = 0; a < inst.aisles.size(); a++) {
			int M_a = 0;
			for (int i : inst.aisles.get(a).keySet()) {
				if(inst.aisles.get(a).get(i) == 0) continue;
        		q.put(Pair.of(i, a), inst.aisles.get(a).get(i));
        		M_a += inst.aisles.get(a).get(i);
			}
			M.add(a, M_a);
		}
		
		// Quantity of i-th item used on o-th order.
		w = new HashMap<Pair<Integer, Integer>, Integer>();
		for (int o = 0; o < inst.orders.size(); o++)
			for (int i : inst.orders.get(o).keySet()) {
				if(inst.orders.get(o).get(i) == 0) continue;
        		w.put(Pair.of(i, o), inst.orders.get(o).get(i));
			}
		
		// Upper and Lower bounds for x_i,o,a.
		ub = new HashMap<Triple, Integer>();
		for (int o = 0; o < inst.orders.size(); o++) {
			for (int i : inst.orders.get(o).keySet())
	        	for (int a = 0; a < inst.aisles.size(); a++) {
	        		if(!q.containsKey(Pair.of(i, a))) continue;
        			ub.put(Triple.of(i, o, a), Math.min(q.get(Pair.of(i, a)), w.get(Pair.of(i, o))));
	        	}
		}
	}
	
	private void buildVars() {
		try {
			// 1, if a-ith aisle was visited; 0, otherwise.
			y = new ArrayList<IloIntVar>(inst.aisles.size());
	        for (int a = 0; a < inst.aisles.size(); a++) 
	            y.add(a, model.boolVar("y_" + a));
	        
			// 1, if a-ith aisle was visited; 0, otherwise.
			p = new ArrayList<IloIntVar>(inst.orders.size());
	        for (int o = 0; o < inst.orders.size(); o++) 
	            p.add(o, model.boolVar("p_" + o));
	        
	        // Quantity of item i, for order o, collected on a-th aisle.
	        x = new HashMap<Triple, IloIntVar>();
	        for (int o = 0; o < inst.orders.size(); o++)
	        	for (int i : inst.orders.get(o).keySet())
	        		for (int a = 0; a < inst.aisles.size(); a++) {
	        			if(!ub.containsKey(Triple.of(i, o, a))) continue;
	        			x.put(Triple.of(i, o, a), model.intVar(0, ub.get(Triple.of(i, o, a)), "x_" + i + "_" + o + "_" + a));
	        		}
	        
	        // Quantity of collected items.
	        z = model.numVar(inst.LB, inst.UB, "z");
	        
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	private void buildObjective() {
		try {
			model.addMaximize(z);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	private void buildConstrs() {
		try {					        	        
	        // ( 1 ) z = SUM SUM SUM x_i,o,a
			IloLinearIntExpr sum_xioa = model.linearIntExpr();
			for (int o = 0; o < inst.orders.size(); o++)
				for (int i : inst.orders.get(o).keySet()) 
	        		for (int a = 0; a < inst.aisles.size(); a++) {
	        			if(!x.containsKey(Triple.of(i, o, a))) continue;
	        			sum_xioa.addTerm(1, x.get(Triple.of(i, o, a)));
	        		}
	        
	        model.addEq(z, sum_xioa); 
	        
	        
	        // ( 2 ) SUM y_a = NUM_AISLES
	        sum_y = model.linearIntExpr();
	        for(int a = 0; a < y.size(); a++) 
	        	sum_y.addTerm(1, y.get(a));

	        	        
	        // ( 3 ) SUM SUM x_i,o,a <= q_i,a
	        for (int a = 0; a < inst.aisles.size(); a++) 
		        for (int i : inst.aisles.get(a).keySet()) {
		        	IloLinearIntExpr sum_xo = model.linearIntExpr();
		        	for (int o = 0; o < inst.orders.size(); o++) {
		        		if(!x.containsKey(Triple.of(i, o, a))) continue;
		        		sum_xo.addTerm(1, x.get(Triple.of(i, o, a)));
		        	}
		        	model.addLe(sum_xo, q.get(Pair.of(i, a)));
		        }
	        
	        
	        // ( 4 ) SUM SUM x_i,o,a >= 1 -> y_a = 1 
	        for(int a = 0; a < inst.aisles.size(); a++) {
	        	IloLinearIntExpr sum_xio = model.linearIntExpr();
	        	for(int o = 0; o < inst.orders.size(); o++) 
	        		for (int i : inst.orders.get(o).keySet()) {
	        			if(!x.containsKey(Triple.of(i, o, a))) continue;
	        			sum_xio.addTerm(1, x.get(Triple.of(i, o, a)));
	        		}
	        	
	        	model.addGe(model.prod(M.get(a),  y.get(a)), sum_xio);
	        }
	        
	        
	        // ( 5 ) w_i,o * SUM x_K[o],o,a = w_K[o],o * SUM x_i,o,a
	        for(int o = 0; o < inst.orders.size(); o++) {
	        	for(int i : inst.orders.get(o).keySet()) {
	        		IloLinearIntExpr sum_xa = model.linearIntExpr();
	        		
	        		for(int a = 0; a < inst.aisles.size(); a++) 
	        			if(x.containsKey(Triple.of(i, o, a))) sum_xa.addTerm(1, x.get(Triple.of(i, o, a)));
	        		
	        		model.addEq(sum_xa, model.prod(w.get(Pair.of(i, o)), p.get(o)));
	        	}
	        }
	        
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	private void setSumY(int NUM_AISLES) throws IloException {
		if(sum_y_constr != null) model.delete(sum_y_constr);
		sum_y_constr = model.addEq(sum_y, NUM_AISLES);
	}
	
	private void setLB(int lb) throws IloException {
		z.setLB(lb);
	}
	
	public ChallengeSolution optimize(StopWatch stopWatch) {
		ChallengeSolution solution = null;
		
		try {
			print_header();
			int max_aisles = inst.aisles.size();
			
			for(int num_aisles = 1; num_aisles <= max_aisles; num_aisles++) {	
				
				// Sets the number of aisles to the model.
				this.setSumY(num_aisles);
				
				// Sets the Lower Bound.
				if(solution != null) {
					double d_lb = Math.floor(objVal * num_aisles + 1);
					int    new_lb = (int) d_lb;
					
					if(new_lb > inst.UB)
						break;
					
					this.setLB(new_lb);
				}
				
				// Optimizes model for "num_aisles" aisles.
				model.solve();
				
				if(model.getStatus() == IloCplex.Status.Optimal) {					
					// If a better solution was found.
					if(model.getObjValue()/num_aisles > objVal) {
						objVal = model.getObjValue()/num_aisles;
						
						// Saves the solution.
						Set<Integer> orders = new HashSet<>();
						for(int o = 0; o < inst.orders.size(); o++)
							for(int a = 0; a < inst.aisles.size(); a++) {
								Boolean found = false;
								for(int i : inst.orders.get(o).keySet()) {
									if(!x.containsKey(Triple.of(i, o, a))) continue;
									if(model.getValue(x.get(Triple.of(i, o, a))) > .5) {
										orders.add(o);
										found = true;
										break;
									}
								}
								if(found) break;
							}
							
						
						Set<Integer> aisles = new HashSet<>();
						for(int a = 0; a < y.size(); a++) 
							if(model.getValue(y.get(a)) > .5) 
								aisles.add(a);
						
						solution = new ChallengeSolution(orders, aisles);
						
						// Update max_aisles.
						double result = inst.UB / objVal;
						max_aisles = (int) Math.floor(result);
					}
					
					if(model.getObjValue() == inst.UB)
						break;
				}
				
				print_line(num_aisles, max_aisles, model.getStatus());
			}
			
			System.out.println("Optimal Solution: " + objVal);
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void log(String text) {
		System.out.print(text);
	}
	
	private void logln(String text) {
		System.out.println(text);
	}
	
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		logln("Thread count: CPLEX using up to " + model.getParam(IloCplex.Param.Threads) + " threads");
		logln("Variable types: 1 continuous; " + x.size() + y.size() + " integer (" + y.size() + " binaries)");
		logln("");
		
		logln("  h  |  H  |  LB  |  UB  |  Incumbent  |  Status  ");
	}
	
	private void print_line(int h, int H, Status status) throws IloException {
		log(String.format("%4" + "s |", h));
		log(String.format("%4" + "s |", H));
		log(String.format("%5" + "s |", (int) z.getLB()));
		log(String.format("%5" + "s |", (int) z.getUB()));
		
		if(objVal > 0) log(String.format("%12.6f" + " |",  objVal));
		else log(String.format("%12" + "s |", "-"));
		
		logln(String.format("%10" + "s", status));
	}
}
