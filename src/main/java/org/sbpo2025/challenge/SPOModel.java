package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;


import ilog.concert.*;
import ilog.cplex.*;



public class SPOModel {
	
	// SPO instance.
	Instance inst;
	
	// CPLEX model.
	IloCplex model;
	
	// Model constants.
	int[][] q, w;
	int[][][] ub, lb;
	
	// Model variables.
	IloIntVar z;
	IloIntVar[] y;
	IloIntVar[][][] x;
	
	public SPOModel(Instance instance) {
		this.inst = instance;
		
		// Building CPLEX model.
		try {			
			model = new IloCplex();
		} catch(IloException e) {
			System.out.print("No model built. Error: ");
			e.printStackTrace();
		}
	}
	
	public void build() {
		buildConsts();
		buildVars();
		buildObjective();
		buildConstrs();
	}
	
	private void buildConsts() {
		// Quantity of i-th item present on a-th aisle.
		for (int i = 0; i < inst.n; i++)
        	for (int a = 0; a < inst.aisles.size(); a++) 
        		q[i][a] = inst.aisles.get(a).get(i);
		
		// Quantity of i-th item used on o-th order.
		for (int i = 0; i < inst.n; i++)
        	for (int o = 0; o < inst.orders.size(); o++)
        		w[i][o] = inst.orders.get(o).get(i);
		
		// Upper Bound for x_i,o,a
		for (int i = 0; i < inst.n; i++)
        	for (int o = 0; o < inst.orders.size(); o++)
        		for (int a = 0; a < inst.aisles.size(); a++) {
        			lb[i][o][a] = 0;
        			
        			int sum_w = 0;
        			for(int i_ = 0; i_ < inst.orders.size(); i_++)
        				sum_w = w[i_][o];
        			ub[i][o][a] = Math.min(q[i][a], sum_w);
        		}
	}
	
	private void buildVars() {
		try {
			// 1, if a-ith aisle was visited; 0, otherwise.
			y = new IloIntVar[inst.aisles.size()];
	        for (int i = 0; i < inst.aisles.size(); i++) 
	            y[i] = model.boolVar("y_" + i);
	        
	        // Quantity of item i, for order o, collected on a-th aisle.
	        x = new IloIntVar[inst.n][inst.orders.size()][inst.aisles.size()];
	        for (int i = 0; i < inst.n; i++)
	        	for (int o = 0; o < inst.orders.size(); o++)
	        		for (int a = 0; a < inst.aisles.size(); a++) {
	        			if(ub[i][o][a] <= lb[i][o][a])
	        				x[i][o][a] = null;
	        			else
	        				x[i][o][a] = model.intVar(lb[i][o][a], ub[i][o][a], "x_" + i + "_" + o + "_" + a);
	        		}
	        
	        // Quantity of collected items.
	        z = model.intVar(inst.LB, inst.UB, "z");
	        
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	private void buildObjective() {
		
	}
	
	private void buildConstrs() {
		
	}
	
	public double optimize(StopWatch stopWatch) {
		return 0;
	}
}
