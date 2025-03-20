package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;


public class ItModel extends BasicModel {

	// Model variables.
	List<IloIntVar> y;
	
	// Constraint sum_y.
	IloLinearIntExpr sum_y;
	IloConstraint sum_y_constr = null;
	
	public ItModel(Instance inst) {
		super(inst);
	}

	@Override
	protected void buildVarsSpecific() throws IloException {
		// 1, if a-ith aisle was visited; 0, otherwise.
		y = new ArrayList<IloIntVar>(inst.aisles.size());
        for (int a = 0; a < inst.aisles.size(); a++) 
            y.add(a, model.boolVar("y_" + a));
	}
	
	@Override
	protected void buildConstrsSpecific() throws IloException {
		IloIntVar var;
		
        // ( 2 ) SUM y_a = NUM_AISLES
        sum_y = model.linearIntExpr();
        for(int a = 0; a < y.size(); a++) 
        	sum_y.addTerm(1, y.get(a));
        
        
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
        
        
        // ( 4 ) SUM SUM x_i,o,a >= 1 -> y_a = 1 
        for(int a = 0; a < inst.aisles.size(); a++) {
        	IloLinearIntExpr sum_xio = model.linearIntExpr();
        	for(int o = 0; o < inst.orders.size(); o++) 
        		for (int i : inst.orders.get(o).keySet()) {
        			var = x.get(o).get(Pair.of(i, a));
        			if(var != null) sum_xio.addTerm(1, var);
        		}
        	
        	model.add(model.ifThen(model.ge(sum_xio, 0.5), model.eq(y.get(a), 1)));
        }
	}
	
	public void setSumY(int NUM_AISLES) throws IloException {
		if(sum_y_constr != null) model.delete(sum_y_constr);
		sum_y_constr = model.addEq(sum_y, NUM_AISLES);
	}
	
	public void setLB(int lb) throws IloException {
		z.setLB(lb);
	}
	
	@Override
	public ChallengeSolution saveSolution() throws IloException {
		Set<Integer> orders = new HashSet<>();
		for(int o = 0; o < p.size(); o++) 
			if(model.getValue(p.get(o)) > .5) 
				orders.add(o);
		
		Set<Integer> aisles = new HashSet<>();
		for(int a = 0; a < y.size(); a++) 
			if(model.getValue(y.get(a)) > .5) 
				aisles.add(a);
		
		return new ChallengeSolution(orders, aisles);
	}
}
