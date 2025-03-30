package org.sbpo2025.challenge;

import java.util.HashSet;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;


public class MaxAislesModel extends BasicModel {

	// CPLEX configuration
	int numThreads;
	
	// Model variables.
	IloIntVar[] y;
	
	// Constraint sumY.
	IloLinearIntExpr sumY;
	
	public MaxAislesModel(Instance inst, int threads) {
		super(inst);
		
		numThreads = threads;
	}
	
	@Override
	protected void buildSpecific() {
		try {
			model.setParam(IloCplex.Param.Threads, numThreads);
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void buildVarsSpecific() throws IloException {		
		// 1, if a-ith aisle was visited; 0, otherwise.
		y = new IloIntVar[inst.aisles.size()];
        for (int a = 0; a < inst.aisles.size(); a++) 
            y[a] = model.boolVar("y_" + a);
	}
	
	@Override
	protected void buildConstrsSpecific() throws IloException {
		Integer value; 
		
        // ( 2 ) SUM y_a = NUM_AISLES
        sumY = model.linearIntExpr();
        for(int a = 0; a < y.length; a++) 
        	sumY.addTerm(1, y[a]);
        
        
        // ( 3 ) SUM w_i,o x p_o <= SUM q_i,a * y_a  	
        for (int i = 0; i < inst.n; i++) {
        	IloLinearIntExpr sumOrders = model.linearIntExpr();
        	for (int o : inst.itemsPerOrders.get(i).keySet())
        		sumOrders.addTerm(w.get(o).get(i), p[o]);
        	
        	IloLinearIntExpr sumAisles = model.linearIntExpr();
        	for(int a = 0; a < inst.aisles.size(); a++) {
        		value = q.get(a).get(i);
        		if(value != null)
        			sumAisles.addTerm(q.get(a).get(i), y[a]);
        	}

        	model.addLe(sumOrders, sumAisles);
        }
	}
	
	protected void buildObjective() {
		try {
			model.addMinimize(sumY);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	public void setZ(int zValue) throws IloException {
		z.setLB(zValue); z.setUB(zValue);
	}
	
	@Override
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
