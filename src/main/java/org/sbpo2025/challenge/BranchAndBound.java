package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Stack;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.IloException;

public class BranchAndBound extends Approach {
	
	BnBModel model;
	
	// Important data structures.
	HashMap<BitSet, Boolean> found;
	BitSet aisles;	
	
	// Some useful data.
	int total_nodes = 1;
	int total_aisles;
	int MAX_HEIGHT;
	
	public BranchAndBound(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
				
		found = new HashMap<BitSet, Boolean>();
		model = new BnBModel(inst);
		aisles = new BitSet(total_aisles);
		
		total_aisles = inst.aisles.size();
		MAX_HEIGHT = total_aisles;
	}
	
	public ChallengeSolution optimize() {
		try {
			model.build();
			print_header();
			
			Stack<Node> stack = new Stack<Node>();
			for(int a = 0; a < total_aisles; a++) 
				stack.add(new EnterNode(a, 0, 0, this));
			
			Node curr; double node_value;
			while(!stack.empty()) {
				if(getRemainingTime(stopWatch) < 600 - 60*3)
					break;
					
				curr = stack.pop();
				total_nodes++;
				
				node_value = curr.run();
				
				// If a better solution was found.
				if(node_value > objVal) {
					objVal = node_value;
					solution = model.saveSolution();
					
					double result = inst.UB / objVal;
					MAX_HEIGHT = (int) Math.floor(result);
							
					print_line(aisles.cardinality(), MAX_HEIGHT);
				}
	
				curr.addChildren(stack);
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		System.out.println("Solution: " + objVal);
		System.out.println("Total nodes: " + total_nodes/2 + "\n");
		
		return solution;
	}
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		logln("Thread count: CPLEX using up to " + Runtime.getRuntime().availableProcessors() + " threads");
		logln("Variable types: 1 continuous; " + model.x.size() + " integer (" + " binaries)");
		logln("");
		
		logln("  h  |  H  |  LB  |  UB  |  Incumbent");
	}
	
	private void print_line(int h, int H) throws IloException {
		log(String.format("%4" + "s |", h));
		log(String.format("%4" + "s |", H));
		log(String.format("%5" + "s |", (int) model.z.getLB()));
		log(String.format("%5" + "s |", (int) model.z.getUB()));
		
		if(objVal > 0) logln(String.format("%12.6f" + "",  objVal));
		else logln(String.format("%12" + "s", "-"));
	}
}
