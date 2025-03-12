package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;


import ilog.concert.*;
import ilog.cplex.*;


public class SPOModel {
	
	// SPO instance.
	Instance inst;
	
	// CPLEX model.
	IloCplex model;
	
	// Model constants.
	int[] K;
	int[][] q, w;
	int[][][] ub, lb;
	
	// Model variables.
	IloNumVar z;
	IloIntVar[] y;
	IloIntVar[][][] x;
	
	// Objective Value.
	double objVal = Double.MIN_VALUE;
	
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
		q = new int[inst.n][inst.aisles.size()];
		for (int i = 0; i < q.length; i++)
        	for (int a = 0; a < q[i].length; a++) 
        		q[i][a] = inst.aisles.get(a).containsKey(i)? inst.aisles.get(a).get(i): 0;
		
		// Quantity of i-th item used on o-th order.
		w = new int[inst.n][inst.orders.size()];
		for (int i = 0; i < w.length; i++)
        	for (int o = 0; o < w[i].length; o++)
        		w[i][o] = inst.orders.get(o).containsKey(i)? inst.orders.get(o).get(i) : 0;
		
		// The reference-item for each order.
		K = new int[inst.orders.size()];
        for (int o = 0; o < K.length; o++) {
        	int min_quantity = Integer.MAX_VALUE, min_item_idx = -1;
        	for (int i = 0; i < inst.n; i++)
        		if(w[i][o] > 0 && w[i][o] < min_quantity) {
        			min_quantity = w[i][o];
        			min_item_idx = i;
        		}
        	K[o] = min_item_idx;
        }
		
		// Upper and Lower bounds for x_i,o,a.
		lb = new int[inst.n][inst.orders.size()][inst.aisles.size()];
		ub = new int[inst.n][inst.orders.size()][inst.aisles.size()];
		for (int i = 0; i < lb.length; i++)
	        for (int o = 0; o < lb[i].length; o++) {
	        	for (int a = 0; a < lb[i][o].length; a++) {
        			lb[i][o][a] = 0;
        			ub[i][o][a] = Math.min(q[i][a], w[i][o]);
        		}
		}
	}
	
	private void buildVars() {
		try {
			// 1, if a-ith aisle was visited; 0, otherwise.
			y = new IloIntVar[inst.aisles.size()];
	        for (int a = 0; a < y.length; a++) 
	            y[a] = model.boolVar("y_" + a);
	        
	        // Quantity of item i, for order o, collected on a-th aisle.
	        x = new IloIntVar[inst.n][inst.orders.size()][inst.aisles.size()];
	        for (int i = 0; i < x.length; i++)
	        	for (int o = 0; o < x[i].length; o++)
	        		for (int a = 0; a < x[i][o].length; a++) {
	        			if(ub[i][o][a] < lb[i][o][a] || ub[i][o][a] == 0)
	        				x[i][o][a] = null;
	        			else
	        				x[i][o][a] = model.intVar(lb[i][o][a], ub[i][o][a], "x_" + i + "_" + o + "_" + a);
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
	        for (int i = 0; i < inst.n; i++) 
	        	for (int o = 0; o < inst.orders.size(); o++)
	        		for (int a = 0; a < inst.aisles.size(); a++) {
	        			if(x[i][o][a] == null) continue;
	        			sum_xioa.addTerm(1, x[i][o][a]);
	        		}
	        
	        model.addEq(z, sum_xioa);
	        
	        
	        // ( 2 ) SUM y_a = NUM_AISLES
	        sum_y = model.linearIntExpr();
	        for(int a = 0; a < y.length; a++) 
	        	sum_y.addTerm(1, y[a]);
	        
	        	        
	        // ( 3 ) SUM SUM x_i,o,a <= q_i,a
	        for (int a = 0; a < inst.aisles.size(); a++) 
		        for (int i = 0; i < inst.n; i++) {
		        	IloLinearIntExpr sum_xo = model.linearIntExpr();
		        	for (int o = 0; o < inst.orders.size(); o++) {
		        		if(x[i][o][a] == null) continue;
		        		sum_xo.addTerm(1, x[i][o][a]);
		        	}
		        	model.addLe(sum_xo, q[i][a]);
		        }
	        
	        
	        // ( 4 ) SUM SUM x_i,o,a >= 1 -> y_a = 1 
	        for(int a = 0; a < inst.aisles.size(); a++) {
	        	IloLinearIntExpr sum_xio = model.linearIntExpr();
	        	for(int i = 0; i < x.length; i++)
	        		for(int o = 0; o < x[i].length; o++) {
	        			if(x[i][o][a] == null) continue;
	        			sum_xio.addTerm(1, x[i][o][a]);
	        		}
	        	
	        	model.add(model.ifThen(model.ge(sum_xio, 0.5), model.ge(y[a], .5)));
	        }
	        
	        // ( 5 ) w_i,o * SUM x_K[o],o,a = w_K[o],o * SUM x_i,o,a
	        for(int o = 0; o < inst.orders.size(); o++) {
	        	for(int i = 0; i < inst.n; i++) {
	        		if(w[i][o] == 0) continue;
	        		
	        		IloLinearIntExpr sum_xl = model.linearIntExpr(), sum_xr = model.linearIntExpr();
	        		
	        		for(int a = 0; a < inst.aisles.size(); a++) {
	        			if(x[K[o]][o][a] != null) sum_xl.addTerm(1, x[K[o]][o][a]);
	        			if(x[i][o][a] != null) sum_xr.addTerm(1, x[i][o][a]);
	        		}
	        		
	        		if(i == K[o]) 
	        			model.add(model.or(model.eq(w[K[o]][o], sum_xl), model.eq(0, sum_xl)));
	        		else
	        			model.addEq(model.prod(w[i][o], sum_xl), model.prod(w[K[o]][o], sum_xr));
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
			System.out.println("Number of aisles ranging from 1 to " + inst.aisles.size());
			int max_aisles = inst.aisles.size();
			
			for(int num_aisles = 1; num_aisles <= max_aisles; num_aisles++) {	
				System.out.println("* Number of aisles: " + num_aisles);
				
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
				
				System.out.println("* Status: " + model.getStatus());
				if(model.getStatus() == IloCplex.Status.Optimal) {
					System.out.println("* Items quantity: " + model.getValue(z));
					System.out.println("* ObjVal: " + model.getObjValue()/num_aisles);
					
					// If a better solution was found.
					if(model.getObjValue()/num_aisles > objVal) {
						objVal = model.getObjValue()/num_aisles;
						
						// Saves the solution.
						Set<Integer> orders = new HashSet<>();
						for(int o = 0; o < inst.orders.size(); o++) {
							for(int a = 0; a < inst.aisles.size(); a++) {
								if(x[K[o]][o][a] == null) continue;
								if(model.getValue(x[K[o]][o][a]) > .5) {
									orders.add(o);
									break;
								}
							}
						}
						
						Set<Integer> aisles = new HashSet<>();
						for(int a = 0; a < y.length; a++) 
							if(model.getValue(y[a]) > .5) 
								aisles.add(a);
						
						solution = new ChallengeSolution(orders, aisles);
						
						// Update max_aisles.
						double result = inst.UB / objVal;
						max_aisles = (int) Math.ceil(result);
					}
					
					if(model.getObjValue() == inst.UB)
						break;
				}
				
				System.out.println("\n");
			}
			
			System.out.println("Optimal Solution: " + objVal);
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
}
