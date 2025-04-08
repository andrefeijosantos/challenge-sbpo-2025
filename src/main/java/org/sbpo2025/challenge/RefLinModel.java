package org.sbpo2025.challenge;

import java.util.HashSet;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;


public class RefLinModel extends BasicModel {

	// Model constants.
	double Uu, Lu;
	
	// Model variables.
	public IloIntVar[] y;
	public IloNumVar[] g, t;
	public IloNumVar u;
	
	// Model expressions.
	IloLinearNumExpr objective;
	
	
	public RefLinModel(Instance inst) {
		super(inst);
		
		Lu = 1.0/inst.aisles.size();
		Uu = 1;
	}
	
	protected void buildVarsSpecific() {
		try {	
			// 1, if a-ith aisle was visited; 0, otherwise.
			y = new IloIntVar[inst.aisles.size()];
	        for (int a = 0; a < inst.aisles.size(); a++) 
	            y[a] = model.boolVar("y_" + a);
	        
	        // 1/SUM y_a
	        u = model.numVar(Lu, Uu, "u");

	        // u, if o-ith order was built; 0, otherwise.
			t = new IloNumVar[inst.orders.size()];
	        for (int o = 0; o < inst.orders.size(); o++) 
	            t[o] = model.numVar(0, 1, "t_" + o);
	        
	        // u, if a-ith aisle was visited; 0, otherwise.
			g = new IloNumVar[inst.aisles.size()];
	        for (int a = 0; a < inst.aisles.size(); a++) 
	            g[a] = model.numVar(0, 1, "g_" + a);
	        

		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void buildObjective() {
		try {
			model.addMaximize(objective);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void buildConstrs() throws IloException {
		Integer value; 
		
		objective = model.linearNumExpr();
		int totalItems = 0;
		for(int o = 0; o < inst.orders.size(); o++) {
			for(int i : inst.orders.get(o).keySet())
				totalItems += w.get(o).get(i);
			objective.addTerm(totalItems, t[o]); 
			totalItems = 0;
		}
		
		model.addLe(model.prod(inst.LB, u),  objective);
		model.addLe(objective, model.prod(inst.UB, u));
        
        
        // ( 3 ) SUM w_i,o x p_o <= SUM q_i,a * y_a  	
        for (int i = 0; i < inst.n; i++) {
        	IloLinearNumExpr sumOrders = model.linearNumExpr();
        	for (int o : inst.itemsPerOrders.get(i).keySet())
        		sumOrders.addTerm(w.get(o).get(i), t[o]);
        	
        	IloLinearNumExpr sumY = model.linearNumExpr();
        	for(int a = 0; a < inst.aisles.size(); a++) {
        		value = q.get(a).get(i);
        		if(value != null)
        			sumY.addTerm(q.get(a).get(i), g[a]);
        	}

        	model.addLe(sumOrders, sumY);
        }
        
        for(int a = 0; a < inst.aisles.size(); a++) {
        	model.addLe(g[a], model.prod(Uu, y[a]));
        	model.addGe(g[a], model.prod(Lu, y[a]));
        	model.addLe(g[a], model.sum(u, model.prod(-Lu, model.sum(1, model.prod(-1, y[a])))));
        	model.addGe(g[a], model.sum(u, model.prod(-Uu, model.sum(1, model.prod(-1, y[a])))));
        }
        
        for(int o = 0; o < inst.orders.size(); o++) {
        	model.addLe(t[o], model.prod(Uu, p[o]));
        	model.addGe(t[o], model.prod(Lu, p[o]));
        	model.addLe(t[o], model.sum(u, model.prod(-Lu, model.sum(1, model.prod(-1, p[o])))));
        	model.addGe(t[o], model.sum(u, model.prod(-Uu, model.sum(1, model.prod(-1, p[o])))));
        }
        
        // ( 2 ) SUM y_a = NUM_AISLES
		IloLinearNumExpr sumAisles = model.linearNumExpr();
        for(int a = 0; a < g.length; a++) 
        	sumAisles.addTerm(1, g[a]);
        
        model.addEq(sumAisles, 1);
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
