package org.sbpo2025.challenge;

import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;

public class MaxOrdersModel extends BasicModel {
	
	public MaxOrdersModel(Instance inst) {
		super(inst);
	}
	
	@Override
	protected void buildConstrsSpecific() throws IloException {
		IloIntVar var;
        
        // ( 3 ) SUM SUM x_i,o,a <= q_i,a
        for (int a = 0; a < inst.aisles.size(); a++) 
	        for (int i : inst.aisles.get(a).keySet()) {
	        	IloLinearIntExpr sum_xo = model.linearIntExpr();
	        	for (int o = 0; o < inst.orders.size(); o++) {
	        		var = x.get(o).get(Pair.of(i, a));
	        		if(var != null) sum_xo.addTerm(1, var);
	        	}
	        	
	        	model.addLe(sum_xo, q.get(a).get(i));
	        }
	}
	
	protected void buildObjective() {
		try {
			IloLinearIntExpr sum_p = model.linearIntExpr();
			
			for(int o = 0; o < inst.orders.size(); o++)
				sum_p.addTerm(1, p.get(o));
			
			model.addMaximize(sum_p);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
}
