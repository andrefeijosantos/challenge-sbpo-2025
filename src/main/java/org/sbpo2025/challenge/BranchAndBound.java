package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Stack;

import org.apache.commons.lang3.time.StopWatch;

public class BranchAndBound {
	BitSet aisles;
	int total_aisles;
	int total_nodes = 0;
	
	HashMap<BitSet, Boolean> found;
	
	StopWatch stopWatch;
	
	ChallengeSolution solution = null;
	
	public BranchAndBound(Instance inst, StopWatch stopWatch) {
		total_aisles = inst.aisles.size();
		this.stopWatch = stopWatch;
		aisles = new BitSet(total_aisles);
		found = new HashMap<BitSet, Boolean>();
	}
	
	public ChallengeSolution optimize() {
		Stack<Node> stack = new Stack<Node>();
		
		for(int a = 0; a < total_aisles; a++) 
			stack.add(new EnterNode(a, this));
		
		Node curr;
		while(!stack.empty()) {
			total_nodes++;
			curr = stack.pop();
			
			curr.run();
			curr.addChildren(stack);
		}
		
		System.out.println("Total nodes: " + total_nodes/2 + 1);
		
		return solution;
	}
}
