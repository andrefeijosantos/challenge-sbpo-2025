package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloRange;

public class BnBModel extends BasicModel {

	List<Map<Integer, IloRange>> constrs;
	
	// Model constants
	public List<Integer> Q;
	
	public BnBModel(Instance inst) {
		super(inst);
	}

	@Override
	protected void buildConstsSpecific() throws IloException {
		Q = new ArrayList<Integer>(inst.aisles.size());
		int Q_a;
		
		for(int a = 0; a < inst.aisles.size(); a++) {
			Q_a = 0;
			
			for(int i : inst.aisles.get(a).keySet())
				Q_a += inst.aisles.get(a).get(i);
			
			Q.add(a, Q_a);
		}
	}
	
	@Override
	protected void buildConstrsSpecific() throws IloException {
		IloIntVar var;        
		IloRange constr;
        
        // ( 3 ) SUM SUM x_i,o,a <= q_i,a
		constrs = new ArrayList<Map<Integer, IloRange>>(inst.aisles.size());
        for (int a = 0; a < inst.aisles.size(); a++) {
        	constrs.add(a, new HashMap<Integer, IloRange>());
        	
	        for (int i : inst.aisles.get(a).keySet()) {
	        	IloLinearIntExpr sum_xo = model.linearIntExpr();
	        	for (int o = 0; o < inst.orders.size(); o++) {
	        		var = x.get(o).get(Pair.of(i, a));
	        		if(var != null) sum_xo.addTerm(1, var);
	        	}

	        	constr = model.range(0, sum_xo, 0);
	        	model.add(constr);
	        	constrs.get(a).put(i, constr);
	        }
        }
	}
	
	public void enableAisle(int a) throws IloException {
		Map<Integer, IloRange> aux = constrs.get(a);
		for(int i : inst.aisles.get(a).keySet())
			aux.get(i).setBounds(0, q.get(a).get(i));
	}
	
	public void disableAisle(int a) throws IloException {
		Map<Integer, IloRange> aux = constrs.get(a);
		for(int i : inst.aisles.get(a).keySet())
			aux.get(i).setBounds(0, 0);
	}
	
	public void setBounds(double d, double e) throws IloException {
		z.setLB(d); z.setUB(e);
	}
	
	@Override
	public ChallengeSolution saveSolution() throws IloException {
		Set<Integer> orders = new HashSet<>();
		for(int o = 0; o < p.size(); o++) 
			if(model.getValue(p.get(o)) > .5) 
				orders.add(o);
		
		Set<Integer> aisles = new HashSet<>();
		for(int a = 0; a < inst.aisles.size(); a++) {
			Map<Integer, IloRange> aux = constrs.get(a);
			for(int i : inst.aisles.get(a).keySet()) {
				if(aux.get(i).getUB() != 0)
					aisles.add(a);
				break;
			}
		}
		
		return new ChallengeSolution(orders, aisles);
	}
}
