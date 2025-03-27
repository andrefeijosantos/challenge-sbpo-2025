package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloRange;

public class BnBModel extends BasicModel {
	
	// Model constants.
	public int[] Z, Q, W;
	public int   sumZ = 0;
	
	public ArrayList<Pair<Integer, Integer>> sortedAisles;
	public ArrayList<Pair<Integer, Integer>> sortedOrders;
	
	// Model constraints.
	List<IloRange> constrs;
	
	public BnBModel(Instance inst) {
		super(inst);
	}

	@Override
	protected void buildConstsSpecific() throws IloException {   
		Comparator<Pair<Integer, Integer>> pairComparator = new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> p, Pair<Integer, Integer> q) {
                return q.getRight() - p.getRight();
            }
        };
				
		Q = new int[inst.aisles.size()]; int Q_a = 0;
		sortedAisles = new ArrayList<Pair<Integer, Integer>>(inst.aisles.size());
		for(int a = 0; a < inst.aisles.size(); a++) {
			for(int i : inst.aisles.get(a).keySet()) 
				Q_a += inst.aisles.get(a).get(i);
			Q[a] = Q_a; Q_a = 0;
			sortedAisles.add(a, Pair.of(a, Q[a]));
		}
		sortedAisles.sort(pairComparator);
		
		W = new int[inst.orders.size()]; int W_o = 0;
		sortedOrders = new ArrayList<Pair<Integer, Integer>>(inst.orders.size());
		for(int o = 0; o < inst.orders.size(); o++) {
			for(int i : inst.orders.get(o).keySet())
				W_o += inst.orders.get(o).get(i);
			W[o] = W_o; W_o = 0;
			sortedOrders.add(o, Pair.of(o, W[o]));
		}
		sortedOrders.sort(pairComparator);
		
		Z = new int[inst.n]; int Z_i = 0;
		for(int i = 0; i < inst.n; i++) {
			for(int a : inst.itemsPerAisles.get(i).keySet())
				Z_i += q.get(a).get(i);
			Z[i] = Z_i; sumZ += Z_i; Z_i = 0;
		}
	}
	
	@Override
	protected void buildConstrsSpecific() throws IloException {      
		IloRange constr;
        		
        // ( 3 ) SUM w_i,o x p_o <= Q_i
		constrs = new ArrayList<IloRange>(inst.n);     	
        for (int i = 0; i < inst.n; i++) {
        	IloLinearIntExpr sum = model.linearIntExpr();
        	
        	for (int o : inst.itemsPerOrders.get(i).keySet())
        		sum.addTerm(w.get(o).get(i), p[o]);

        	constr = model.range(0, sum, 0);
        	model.add(constr);
        	constrs.add(i, constr);
        }
	}
	
	public void addItemAmount(int i, int amt) throws IloException {		
		IloRange rng = constrs.get(i);
		rng.setBounds(0, rng.getUB() + amt);
	}
	
	public void resetItemAmounts() throws IloException {
		for (int i = 0; i < inst.n; i++) {
			IloRange rng = constrs.get(i);
			rng.setBounds(0, 0);
		}
	}
	
	public void setBounds(double d, double e) throws IloException {
		z.setLB(d); z.setUB(e);
	}
	
	public Set<Integer> getOrders() throws IloException {
		Set<Integer> orders = new HashSet<>();
		int size_o = inst.orders.size();
		for(int o = 0; o < size_o; o++) 
			if(model.getValue(p[o]) > .5) 
				orders.add(o);
		
		return orders;
	}
}
