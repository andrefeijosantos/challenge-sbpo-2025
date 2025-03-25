package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloRange;

public class BnBModel extends BasicModel {
	
	// Model constants.
	public int[] Z;
	
	// Model constraints.
	List<IloRange> constrs;
	
	public BnBModel(Instance inst) {
		super(inst);
	}
	
	@Override
	protected void buildConstrsSpecific() throws IloException {      
		IloRange constr;
        		
        // ( 3 ) SUM w_i,o x p_o <= Q_i
		constrs = new ArrayList<IloRange>(inst.n);     	
        for (int i = 0; i < inst.n; i++) {
        	IloLinearIntExpr sum = model.linearIntExpr();
        	
        	for (int o : inst.items.get(i).keySet())
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
