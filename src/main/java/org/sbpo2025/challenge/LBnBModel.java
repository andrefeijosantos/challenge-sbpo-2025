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

public class LBnBModel extends BasicModel {
	
	// Model constants.
	public int[] Z;
	
	public ArrayList<Pair<Integer, Integer>> sortedAisles;
	
	// Model constraints.
	List<IloRange> constrs;
	
	public LBnBModel(Instance inst) {
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
				
		sortedAisles = new ArrayList<Pair<Integer, Integer>>(inst.aisles.size());
		for(int a = 0; a < inst.aisles.size(); a++) {
			int Q_a = 0;
			for(int i : inst.aisles.get(a).keySet()) 
				Q_a += inst.aisles.get(a).get(i);
			sortedAisles.add(a, Pair.of(a, Q_a));
		}
		sortedAisles.sort(pairComparator);
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
