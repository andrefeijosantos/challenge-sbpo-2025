package org.sbpo2025.challenge;

import java.util.List;
import java.util.Map;

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
	public IloIntVar[] p;
	
	
	public BasicModel(Instance inst) {
		this.inst = inst;
	}
	
	public void build() {
		// Building CPLEX model.
		try {			
			model = new IloCplex();
			
			buildConsts();
			buildConstsSpecific();
			buildVars();
			buildVarsSpecific();
			buildConstrs();
			buildConstrsSpecific();
			buildObjective();	
			
			buildSpecific();
			
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
			// 1, if o-ith orders was built; 0, otherwise.
			p = new IloIntVar[inst.orders.size()];
	        for (int o = 0; o < inst.orders.size(); o++) 
	            p[o] = model.boolVar("p_" + o);
	        
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
	
	protected void buildConstrs() throws IloException {
		IloLinearIntExpr sumItems = model.linearIntExpr();
		int totalItems = 0;
		for(int o = 0; o < inst.orders.size(); o++) {
			for(int i : inst.orders.get(o).keySet())
				totalItems += w.get(o).get(i);
			sumItems.addTerm(totalItems, p[o]); 
			totalItems = 0;
		}
		
		model.addEq(z, sumItems);
	}
	
	protected void buildConstrsSpecific() throws IloException {
		return;
	}
	
	protected void buildSpecific() throws IloException {
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
	
	public double getValue(IloIntVar var) throws IloException {
		return model.getValue(var);
	}
	
	public double getValue(IloNumVar var) throws IloException {
		return model.getValue(var);
	}
	
	public double getValue(IloLinearIntExpr expr) throws IloException {
		return model.getValue(expr);
	}
	
	public int getNumThreads() throws IloException {
		return model.getParam(IloCplex.Param.Threads);
	}
	
	public ChallengeSolution saveSolution() throws IloException {
		return null;
	}
}
