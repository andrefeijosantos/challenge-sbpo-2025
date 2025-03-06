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
	int[] M, K;
	int[][] q, w;
	int[][][] ub, lb;
	
	// Model variables.
	IloNumVar f;
	IloIntVar z;
	IloIntVar[] y;
	IloIntVar[][][] x;
	
	// Objective function.
	IloLinearNumExpr obj;
	
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
		System.out.println("Model built successfully.");
	}
	
	private void buildConsts() {
		// Quantity of i-th item present on a-th aisle.
		q = new int[inst.n][inst.aisles.size()];
		for (int i = 0; i < inst.n; i++)
        	for (int a = 0; a < inst.aisles.size(); a++) 
        		q[i][a] = inst.aisles.get(a).containsKey(i)? inst.aisles.get(a).get(i): 0;
		
		// Quantity of i-th item used on o-th order.
		w = new int[inst.n][inst.orders.size()];
		for (int i = 0; i < inst.n; i++)
        	for (int o = 0; o < inst.orders.size(); o++)
        		w[i][o] = inst.orders.get(o).containsKey(i)? inst.orders.get(o).get(i) : 0;
		
		K = new int[inst.orders.size()];
        for (int o = 0; o < inst.orders.size(); o++) {
        	int min_quantity = Integer.MAX_VALUE, min_item_idx = -1;
        	for (int i = 0; i < inst.n; i++)
        		if(w[i][o] > 0 && w[i][o] < min_quantity) {
        			min_quantity = w[i][o];
        			min_item_idx = i;
        		}
        	K[o] = min_item_idx;
        }
		
		// Upper Bound for x_i,o,a.
		lb = new int[inst.n][inst.orders.size()][inst.aisles.size()];
		ub = new int[inst.n][inst.orders.size()][inst.aisles.size()];
		M  = new int[inst.aisles.size()];
		for (int a = 0; a < inst.aisles.size(); a++) {
			M[a] = 0;
			for (int i = 0; i < inst.n; i++)
	        	for (int o = 0; o < inst.orders.size(); o++) {
        			lb[i][o][a] = 0;
        			
        			int sum_w = 0;
        			for(int i_ = 0; i_ < inst.orders.size(); i_++)
        				sum_w = w[i_][o];
        			ub[i][o][a] = Math.min(q[i][a], sum_w);
        			
        			M[a] += ub[i][o][a];
        		}
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
	        			if(ub[i][o][a] < lb[i][o][a] || ub[i][o][a] == 0)
	        				x[i][o][a] = null;
	        			else
	        				x[i][o][a] = model.intVar(lb[i][o][a], ub[i][o][a], "x_" + i + "_" + o + "_" + a);
	        		}
	        
	        // Quantity of collected items.
	        z = model.intVar(inst.LB, inst.UB, "z");
	        
	        // Objective Function.
	        f = model.numVar(0, Double.MAX_VALUE, "f");
	        
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	private void buildObjective() {
		try {
			obj = model.linearNumExpr();
			obj.addTerm(1, f);
			model.addMaximize(obj);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	private void buildConstrs() {
		try {
			// ( 1 ) Objective Function: f = sum_x/sum_y.
			IloLinearIntExpr sum_x = model.linearIntExpr();
	        for (int i = 0; i < inst.n; i++) 
	        	for (int o = 0; o < inst.orders.size(); o++)
	        		for (int a = 0; a < inst.aisles.size(); a++) {
	        			if(x[i][o][a] == null) continue;
	        			sum_x.addTerm(1, x[i][o][a]);
	        		}
	        
	        IloLinearIntExpr sum_y = model.linearIntExpr();
	        for(int a = 0; a < y.length; a++) 
	        	sum_y.addTerm(1, y[a]);
	        
	        model.addEq(model.prod(f, sum_y), z);
	        
	        // ( 2 ) z = SUM SUM SUM x_i,o,a
	        model.addEq(z, sum_x);
	        
	        // ( 3 ) SUM x_i,o,a <= q_i,a
	        for (int a = 0; a < inst.aisles.size(); a++) 
		        for (int i = 0; i < inst.n; i++) {
		        	IloLinearIntExpr sum_xo = model.linearIntExpr();
		        	for (int o = 0; o < inst.orders.size(); o++) {
		        			if(x[i][o][a] == null) continue;
		        			sum_xo.addTerm(1, x[i][o][a]);
		        	}
		        	model.addLe(sum_xo, q[i][a]);
		        }
	        
	        // ( 4 ) M x y_a >= SUM SUM SUM x_i,o,a
	        for(int a = 0; a < inst.aisles.size(); a++) {
	        	IloLinearIntExpr sum_io = model.linearIntExpr();
	        	for(int i = 0; i < inst.n; i++)
	        		for(int o = 0; o < inst.orders.size(); o++) {
	        			if(x[i][o][a] == null) continue;
	        			sum_io.addTerm(1, x[i][o][a]);
	        		}
	        	
	        	model.addGe(model.prod(M[a], y[a]), sum_io);
	        }
	        
	        // ( 5 ) w_i,o * SUM x_K_o,o,a = w_K_o,o * SUM x_i,o,a
	        for(int o = 0; o < inst.orders.size(); o++) {
	        	for(int i = 0; i < inst.n; i++) {
	        		IloLinearIntExpr sum_xl = model.linearIntExpr(), sum_xr = model.linearIntExpr();
	        		for(int a = 0; a < inst.aisles.size(); a++) {
	        			if(x[K[o]][o][a] != null) sum_xl.addTerm(1, x[K[o]][o][a]);
	        			if(x[i][o][a] != null) sum_xr.addTerm(1, x[i][o][a]);
	        		}
	        		model.addEq(model.prod(w[i][o], sum_xl), model.prod(w[K[o]][o], sum_xr));
	        	}
	        }
	        
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	public double optimize(StopWatch stopWatch) {
		try {
			model.solve();
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
}
