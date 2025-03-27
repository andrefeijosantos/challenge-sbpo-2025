package org.sbpo2025.challenge;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloRange;

public class MinOrdersModel extends BasicModel {

	// Model constants.
	public int[] Z;
	
	public MinOrdersModel(Instance inst) {
		super(inst);
	}
	
	@Override
	protected void buildConstsSpecific() throws IloException {   
		Z = new int[inst.n]; int Z_i = 0;
		for(int i = 0; i < inst.n; i++) {
			for(int a : inst.itemsPerAisles.get(i).keySet())
				Z_i += q.get(a).get(i);
			Z[i] = Z_i; Z_i = 0;
		}
	}
	
	@Override
	protected void buildConstrsSpecific() throws IloException {      
		IloRange constr;
        		
        // ( 3 ) SUM w_i,o x p_o <= Q_i
        for (int i = 0; i < inst.n; i++) {
        	IloLinearIntExpr sum = model.linearIntExpr();
        	
        	for (int o : inst.itemsPerOrders.get(i).keySet())
        		sum.addTerm(w.get(o).get(i), p[o]);

        	constr = model.range(0, sum, Z[i]);
        	model.add(constr);
        }
	}
	
	@Override
	protected void buildObjective() {
		try {
			IloLinearIntExpr sumP = model.linearIntExpr();
			
			for(int o = 0; o < inst.orders.size(); o++)
				sumP.addTerm(1, p[o]);
			
			model.addMinimize(sumP);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	public void setLB(int lb) throws IloException {
		z.setLB(lb);
	}
	
	public void setUB(int ub) throws IloException {
		z.setUB(ub);
	}
}
