package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

public class BasicModel {
	
	// Test instance.
	Instance inst;
	
	// CPLEX model.
	IloCplex model;
	
	// Model constants.
	List<Map<Integer, Integer>> q, w;
	
	// Model variables.
	public IloNumVar z;
	public List<IloIntVar> p;
	public List<Map<Pair<Integer, Integer>, IloIntVar>> x;
	
	
	public BasicModel(Instance inst) {
		this.inst = inst;
	}
	
	public void build() {
		// Building CPLEX model.
		try {			
			model = new IloCplex();
			model.setParam(IloCplex.Param.MIP.Display, 0);
			model.setOut(null);
			
			buildConsts();
			buildConstsSpecific();
			buildVars();
			buildVarsSpecific();
			buildObjective();	
			buildConstrs();
			buildConstrsSpecific();
			
		} catch(IloException e) {
			System.out.print("No model built. Error: ");
			e.printStackTrace();
		}
	}
	
	protected void buildConsts() {	
		q = inst.aisles;
		w = inst.orders;
	}
	
	protected void buildConstsSpecific() throws IloException {
		return;
	}
	
	protected void buildVars() {
		try {	
			Integer val;
	        
			// 1, if o-ith orders was built; 0, otherwise.
			p = new ArrayList<IloIntVar>(inst.orders.size());
	        for (int o = 0; o < inst.orders.size(); o++) 
	            p.add(o, model.boolVar("p_" + o));
	        
	        // Quantity of item i, for order o, collected on a-th aisle.
	        x = new ArrayList<Map<Pair<Integer, Integer>, IloIntVar>>(inst.orders.size());
	        for (int o = 0; o < inst.orders.size(); o++) {
	        	
	        	x.add(o, new HashMap<Pair<Integer, Integer>, IloIntVar>());
	        	for (int i : inst.orders.get(o).keySet())
	        		for (int a = 0; a < inst.aisles.size(); a++) {
	        			val = q.get(a).get(i);
	        			if(val != null)
	        				x.get(o).put(Pair.of(i, a), model.intVar(0, Math.min(w.get(o).get(i), val), 
	        						"x_" + i + "_" + o + "_" + a));
	        		}
	        }
	        
	        // Quantity of collected items.
	        z = model.numVar(inst.LB, inst.UB, "z");
	        
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void buildVarsSpecific() throws IloException {
		return;
	}
	
	protected void buildObjective() {
		try {
			model.addMaximize(z);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	private void buildConstrs() {
		try {			
			IloIntVar var;
			
	        // ( 1 ) z = SUM SUM SUM x_i,o,a
			IloLinearIntExpr sum_xioa = model.linearIntExpr();
			for (int o = 0; o < inst.orders.size(); o++)
				for (int i : inst.orders.get(o).keySet()) 
	        		for (int a = 0; a < inst.aisles.size(); a++) {
	        			var = x.get(o).get(Pair.of(i, a));
	        			if(var != null) sum_xioa.addTerm(1, var);
	        		}
	        
	        model.addEq(z, sum_xioa); 

	        	        	        
	        // ( 5 ) SUM x_i,o,a = w_i,o * p_o
	        for(int o = 0; o < inst.orders.size(); o++) {
	        	for(int i : inst.orders.get(o).keySet()) {
	        		IloLinearIntExpr sum_xa = model.linearIntExpr();
	        		
	        		for(int a = 0; a < inst.aisles.size(); a++) {
	        			var = x.get(o).get(Pair.of(i, a));
	        			if(var != null) sum_xa.addTerm(1, var);
	        		}
	        		
	        		model.addEq(sum_xa, model.prod(w.get(o).get(i), p.get(o)));
	        	}
	        }
	        
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void buildConstrsSpecific() throws IloException {
		return;
	}
	
	
	public void solve() throws IloException {
		model.solve();
	}
	
	public Status getStatus() throws IloException {
		return model.getStatus();
	}
	
	public double getObjValue() throws IloException {
		return model.getObjValue();
	}
	
	public void setTimeLimit(long timeLimit) throws IloException {
		model.setParam(IloCplex.Param.TimeLimit, timeLimit);
	}
	
	public ChallengeSolution saveSolution() throws IloException {
		return null;
	}
}
