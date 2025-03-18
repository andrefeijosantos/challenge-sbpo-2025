package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.Status;


public class SPOModel {
	
	// Model instance.
	Instance inst;
	
	// CPLEX model.
	IloCplex model;
	
	// Model constants.
	List<Map<Integer, Integer>> q, w;
	
	// Model variables.
	IloNumVar z;
	List<IloIntVar> y, p;
	List<Map<Pair<Integer, Integer>, IloIntVar>> x;
	
	// Objective Value.
	double objVal = 0;
	
	// Constraint sum_y.
	IloLinearIntExpr sum_y;
	IloConstraint sum_y_constr = null;
	
	// Some other stuff.
	private final long MAX_RUNTIME = (10-1) * 60 * 1000; // 9 minutes
	ChallengeSolution solution = null;
	StopWatch stopWatch;
	
	
	public SPOModel(Instance inst, StopWatch stopWatch) {
		this.inst = inst;
		this.stopWatch = stopWatch;
	}

	public void build() {
		// Building CPLEX model.
		try {			
			model = new IloCplex();
			model.setParam(IloCplex.Param.MIP.Display, 0);
			model.setOut(null);
		} catch(IloException e) {
			System.out.print("No model built. Error: ");
			e.printStackTrace();
		}
		
		buildConsts();
		buildVars();
		buildObjective();	
		buildConstrs();
	}
	
	protected void buildConsts() {	
		q = inst.aisles;
		w = inst.orders;
	}
	
	protected void buildVars() {
		try {	
			Integer val;
			
			// 1, if a-ith aisle was visited; 0, otherwise.
			y = new ArrayList<IloIntVar>(inst.aisles.size());
	        for (int a = 0; a < inst.aisles.size(); a++) 
	            y.add(a, model.boolVar("y_" + a));
	        
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
	
	private void setSumY(int NUM_AISLES) throws IloException {
		if(sum_y_constr != null) model.delete(sum_y_constr);
		sum_y_constr = model.addEq(sum_y, NUM_AISLES);
	}
	
	private void setLB(int lb) throws IloException {
		z.setLB(lb);
	}
	
	public ChallengeSolution optimize() {
		try {
			int H = inst.aisles.size();
			print_header();
			
			for(int h = 1; h <= H; h++) {
				if(getRemainingTime(stopWatch) <= 5) {
					logln("Time Limit reached.");
					break;
				}
				
				// Sets the number of aisles to the model.
				this.setSumY(h);
				
				// Sets the Lower Bound.
				if(solution != null) {
					double d_lb = Math.floor(objVal * h + 1);
					int    new_lb = (int) d_lb;
					
					if(new_lb > inst.UB)
						break;
					
					this.setLB(new_lb);
				}
				
				// Optimizes model for "num_aisles" aisles.
				model.setParam(IloCplex.Param.TimeLimit, getRemainingTime(stopWatch));
				model.solve();
				
				if(model.getStatus() == IloCplex.Status.Optimal) {					
					// If a better solution was found.
					if(model.getObjValue()/h > objVal) {
						objVal = model.getObjValue()/h;
						save_solution();
						
						// Update max_aisles.
						double result = inst.UB / objVal;
						H = (int) Math.floor(result);
					}
					
					if(model.getObjValue() == inst.UB)
						break;
				}
				
				print_line(h, H, model.getStatus());
			}
			
			System.out.println("Optimal Solution: " + objVal + "\n");
			
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solution;
	}
	
	
	
	// === AUXILIAR FUNCTIONS ===
	private void save_solution() throws IloException {
		// Saves the solution.
		Set<Integer> orders = new HashSet<>();
		for(int o = 0; o < p.size(); o++) 
			if(model.getValue(p.get(o)) > .5) 
				orders.add(o);
		
		Set<Integer> aisles = new HashSet<>();
		for(int a = 0; a < y.size(); a++) 
			if(model.getValue(y.get(a)) > .5) 
				aisles.add(a);
		
		solution = new ChallengeSolution(orders, aisles);
	}
	
	protected long getRemainingTime(StopWatch stopWatch) {
        return Math.max(TimeUnit.SECONDS.convert(MAX_RUNTIME - stopWatch.getTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS), 0);
    }
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void log(String text) {
		System.out.print(text);
	}
	
	private void logln(String text) {
		System.out.println(text);
	}
	
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		logln("Thread count: CPLEX using up to " + model.getParam(IloCplex.Param.Threads) + " threads");
		logln("Variable types: 1 continuous; " + x.size() + y.size() + " integer (" + y.size() + " binaries)");
		logln("");
		
		logln("  h  |  H  |  LB  |  UB  |  Incumbent  |  Status  ");
	}
	
	private void print_line(int h, int H, Status status) throws IloException {
		log(String.format("%4" + "s |", h));
		log(String.format("%4" + "s |", H));
		log(String.format("%5" + "s |", (int) z.getLB()));
		log(String.format("%5" + "s |", (int) z.getUB()));
		
		if(objVal > 0) log(String.format("%12.6f" + " |",  objVal));
		else log(String.format("%12" + "s |", "-"));
		
		logln(String.format("%10" + "s", status));
	}
}
