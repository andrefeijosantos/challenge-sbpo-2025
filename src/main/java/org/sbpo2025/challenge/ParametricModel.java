package org.sbpo2025.challenge;

import java.util.HashSet;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;


public class ParametricModel extends BasicModel {

	// Model constants.
	double ratio = 0;
	
	// Model variables.
	public IloIntVar[] y;
	
	// Model expressions.
	IloLinearIntExpr sumItems, sumAisles, objective;
	
	
	public ParametricModel(Instance inst) {
		super(inst);
	}
	
	protected void buildSpecific() throws IloException {
		// Set model parameters.
		model.setParam(IloCplex.Param.MIP.Display, 0);
		model.setOut(null);
	}
	
	protected void buildVarsSpecific() {
		try {	
			// 1, if a-ith aisle was visited; 0, otherwise.
			y = new IloIntVar[inst.aisles.size()];
	        for (int a = 0; a < inst.aisles.size(); a++) 
	            y[a] = model.boolVar("y_" + a);

		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void buildObjective() {
		try {
			if(model.getObjective() != null)
				model.delete(model.getObjective());
			
			model.addMaximize(model.sum(z, model.prod(-1 * ratio, sumAisles)));
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void buildConstrs() throws IloException {
		Integer value; 
		
		// ( 1 ) z = SUM W_o x p_o
		sumItems = model.linearIntExpr();
		int totalItems = 0;
		for(int o = 0; o < inst.orders.size(); o++) {
			for(int i : inst.orders.get(o).keySet())
				totalItems += w.get(o).get(i);
			sumItems.addTerm(totalItems, p[o]); 
			totalItems = 0;
		}
		
		model.addEq(z,  sumItems);
		
        // ( 2 ) SUM y_a = NUM_AISLES
        sumAisles = model.linearIntExpr();
        for(int a = 0; a < y.length; a++) 
        	sumAisles.addTerm(1, y[a]);
        
        
        // ( 3 ) SUM w_i,o x p_o <= SUM q_i,a * y_a  	
        for (int i = 0; i < inst.n; i++) {
        	IloLinearIntExpr sumOrders = model.linearIntExpr();
        	for (int o : inst.itemsPerOrders.get(i).keySet())
        		sumOrders.addTerm(w.get(o).get(i), p[o]);
        	
        	IloLinearIntExpr sumY = model.linearIntExpr();
        	for(int a = 0; a < inst.aisles.size(); a++) {
        		value = q.get(a).get(i);
        		if(value != null)
        			sumY.addTerm(q.get(a).get(i), y[a]);
        	}

        	model.addLe(sumOrders, sumY);
        }
	}
	
	public void setRatio(double r) {
		ratio = r;
		buildObjective();
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
