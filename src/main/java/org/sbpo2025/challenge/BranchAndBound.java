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
	
	public BranchAndBound(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
		total_aisles = inst.aisles.size();
		aisles = new BitSet(total_aisles);
		found = new HashMap<BitSet, Boolean>();
		model = new BnBModel(inst);
	}
	
	public ChallengeSolution optimize() {
		Stack<Node> stack = new Stack<Node>();
		model.build();
		
		for(int a = 0; a < total_aisles; a++) 
			stack.add(new EnterNode(a, 0, this));
		
		try {
			Node curr; double node_value;
			while(!stack.empty()) {
				total_nodes++;
				curr = stack.pop();
				
				node_value = curr.run();
				if(node_value > objVal) {
					objVal = node_value;
					solution = model.saveSolution();
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
}
