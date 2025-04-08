package org.sbpo2025.challenge;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;


public class PresolveItModel {

	// Test instance.
	Instance inst;
	
	// CPLEX model.
	IloCplex model;
	
	// Model constants.
	List<Map<Integer, Integer>> q, w;
	
	// Model variables.
	public IloNumVar z;
	public IloNumVar[] p;
	public IloNumVar[] y;
	
	// CPLEX configuration
	int numThreads;
	
	// Constraint sumY.
	IloLinearNumExpr sumY;
	IloConstraint sumYConstr = null;
	
	public PresolveItModel(Instance inst, int threads) {
		this.inst = inst;
		numThreads = threads;
	}
	
	public void build() {
		// Building CPLEX model.
		try {			
			model = new IloCplex();
			
			// Set model parameters.
			model.setParam(IloCplex.Param.MIP.Display, 0);
			model.setParam(IloCplex.Param.Threads, numThreads);
			model.setOut(null);
			
			buildConsts();
			buildVars();
			buildConstrs();
			buildObjective();	
			
		} catch(IloException e) {
			System.out.print("No model built. Error: ");
			e.printStackTrace();
		}
	}
	
	protected void buildConsts() {	
		q = inst.aisles;
		w = inst.orders;
	}
	
	protected void buildVars() throws IloException {		
		// 1, if a-ith aisle was visited; 0, otherwise.
		y = new IloNumVar[inst.aisles.size()];
        for (int a = 0; a < inst.aisles.size(); a++) 
            y[a] = model.numVar(0, 1, "y_" + a);
        
        // 1, if o-ith orders was built; 0, otherwise.
		p = new IloNumVar[inst.orders.size()];
        for (int o = 0; o < inst.orders.size(); o++) 
            p[o] = model.numVar(0, 1, "p_" + o);
        
        // Quantity of collected items.
        z = model.numVar(inst.LB, inst.UB, "z");
	}
	
	protected void buildObjective() {
		try {
			model.addMaximize(z);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void buildConstrs() throws IloException {
		Integer value; 
		
		// ( 1 ) z = SUM W_o x p_o
		IloLinearNumExpr sumItems = model.linearNumExpr();
		int totalItems = 0;
		for(int o = 0; o < inst.orders.size(); o++) {
			for(int i : inst.orders.get(o).keySet())
				totalItems += w.get(o).get(i);
			sumItems.addTerm(totalItems, p[o]); 
			totalItems = 0;
		}
		
		model.addEq(z, sumItems);
		
        // ( 2 ) SUM y_a = NUM_AISLES
        sumY = model.linearNumExpr();
        for(int a = 0; a < y.length; a++) 
        	sumY.addTerm(1, y[a]);
        
        
        // ( 3 ) SUM w_i,o x p_o <= SUM q_i,a * y_a  	
        for (int i = 0; i < inst.n; i++) {
        	IloLinearNumExpr sumOrders = model.linearNumExpr();
        	for (int o : inst.itemsPerOrders.get(i).keySet())
        		sumOrders.addTerm(w.get(o).get(i), p[o]);
        	
        	IloLinearNumExpr sumAisles = model.linearNumExpr();
        	for(int a = 0; a < inst.aisles.size(); a++) {
        		value = q.get(a).get(i);
        		if(value != null)
        			sumAisles.addTerm(q.get(a).get(i), y[a]);
        	}

        	model.addLe(sumOrders, sumAisles);
        }
        
        z.setLB(inst.LB);
        z.setUB(inst.UB);
	}
	
	public void setSumY(int NUM_AISLES) throws IloException {
		if(sumYConstr != null) model.delete(sumYConstr);
		sumYConstr = model.addEq(sumY, NUM_AISLES);
	}
	
	public void setLB(int lb) throws IloException {
		z.setLB(lb);
	}
	
	public void setUB(int ub) throws IloException {
		z.setUB(ub);
	}
	
	public void solve() throws IloException {
		model.solve();
	}
	
	public Status getStatus() throws IloException {
		return model.getStatus();
	}
	
	public double getObjValue() throws IloException {
		return model.getObjValue();
	}
	
	public void setTimeLimit(long timeLimit) throws IloException {
		model.setParam(IloCplex.Param.TimeLimit, timeLimit);
	}
	
	public double getValue(IloIntVar var) throws IloException {
		return model.getValue(var);
	}
	
	public double getValue(IloNumVar var) throws IloException {
		return model.getValue(var);
	}
	
	public int getNumThreads() throws IloException {
		return model.getParam(IloCplex.Param.Threads);
	}
	
	public ChallengeSolution saveSolution() throws IloException {
		Set<Integer> orders = new HashSet<>();
		int size_o = inst.orders.size();
		for(int o = 0; o < size_o; o++) 
			if(model.getValue(p[o]) > .5) 
				orders.add(o);
		
		Set<Integer> aisles = new HashSet<>();
		for(int a = 0; a < y.length; a++) 
			if(model.getValue(y[a]) > .5) 
				aisles.add(a);
		
		return new ChallengeSolution(orders, aisles);
	}
}
