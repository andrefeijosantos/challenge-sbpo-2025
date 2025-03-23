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
import ilog.cplex.IloCplex.Status;

public class BnBModel extends BasicModel {

	// Model constants.
	public int[] Q, W, Z;
	
	// Model constraints.
	List<Map<Integer, IloRange>> constrs;
	boolean[] used;
	
	public BnBModel(Instance inst) {
		super(inst);
	}

	@Override
	protected void buildConstsSpecific() throws IloException {				
		W = new int[inst.orders.size()];
		int W_o = 0;
		
		for(int o = 0; o < inst.orders.size(); o++) {
			for(int i : inst.orders.get(o).keySet())
				W_o += inst.orders.get(o).get(i);
			W[o] = W_o; W_o = 0;
		}
		
		
		Q = new int[inst.aisles.size()];
		int Q_a = 0;
		
		for(int a = 0; a < inst.aisles.size(); a++) {
			for(int i : inst.aisles.get(a).keySet()) 
				Q_a += inst.aisles.get(a).get(i);
			Q[a] = Q_a; Q_a = 0;
		}

		Z = new int[inst.aisles.size()];
		int Z_a = 0;
		
		used = new boolean[inst.aisles.size()];
		for(int a = 0; a < inst.aisles.size(); a++) {
			used[a] = false;
			for(int o : getRelatedOrders(a)) 
				Z_a += W[o];
			Z[a] = Z_a; Z_a = 0;
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
		if(used[a]) return;
		
		Map<Integer, IloRange> aux = constrs.get(a);
		for(int i : inst.aisles.get(a).keySet())
			aux.get(i).setBounds(0, q.get(a).get(i));
		used[a] = true;
	}
	
	public void disableAisle(int a) throws IloException {
		if(!used[a]) return;
		
		Map<Integer, IloRange> aux = constrs.get(a);
		for(int i : inst.aisles.get(a).keySet())
			aux.get(i).setBounds(0, 0);
		used[a] = false;
	}
	
	public void deleteBounds() throws IloException {
		setBounds(0, Double.MAX_VALUE);
	}
	
	public void setBounds(double d, double e) throws IloException {
		z.setLB(d); z.setUB(e);
	}
	
	@Override
	public ChallengeSolution saveSolution() throws IloException {
		Set<Integer> orders = new HashSet<>();
		int size_o = inst.orders.size();
		for(int o = 0; o < size_o; o++) 
			if(model.getValue(p[o]) > .5) 
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
	
	public Set<Integer> getRelatedOrders(int a) {
		Set<Integer> orders = new HashSet<Integer>();
		for(int i : inst.aisles.get(a).keySet()) 
			for(int o : inst.items.get(i).keySet()) 
				orders.add(o);
		return orders;
	}
	
	public Set<Integer> getOrders() throws IloException {
		if(model.getStatus() != Status.Optimal)
			return new HashSet<Integer>();
		
		Set<Integer> orders = new HashSet<Integer>();
		int size_o = inst.orders.size();
		for(int o = 0; o < size_o; o++) 
			if(model.getValue(p[o]) > .5) 
				orders.add(o);		
		return orders;
	}
}
