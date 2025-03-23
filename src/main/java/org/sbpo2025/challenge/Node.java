package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.PriorityQueue;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

public class Node {
	protected BranchAndBound algorithm;
	
	protected BitSet subset;
	protected int totalItems;
	protected int aisleAdded;
	
	double solutionValue;
	int    solutionItems;
	
	protected int lowerBound;
	protected int upperBound;
	
	public Node(BitSet bs, int aisle, int itemsParent, int lb, int ub, BranchAndBound bnb) {
		aisleAdded = aisle;
		subset = bs;
		
		algorithm = bnb;
		lowerBound = lb;
		upperBound = ub;
		
		totalItems = itemsParent + algorithm.model.Q[aisleAdded];
	} 
	
	public double run() throws IloException {
		if(algorithm.MAX_AISLES < subset.cardinality()) 
			return 0;
		
		// Set model bounds.
		algorithm.model.setBounds(lowerBound, Math.min(upperBound, algorithm.inst.UB));
		
		// Add its new aisle to the subset.
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(subset.get(a)) algorithm.model.enableAisle(a);
			else algorithm.model.disableAisle(a);
		}		
		
		// Execute model's optimization.
		algorithm.model.solve();
		
		// If a solution was found, return it.
		if(algorithm.model.getStatus() == IloCplex.Status.Optimal) {
			solutionItems = (int) algorithm.model.getObjValue();
			solutionValue = algorithm.model.getObjValue()/subset.cardinality();
			return solutionValue;
		}
		return 0;
	}
	
	public void addChildren(PriorityQueue<Node> queue) throws IloException {		
		// If the next size of subsets cannot beat the current solution.
		if((Math.floor(algorithm.objVal * (subset.cardinality() + 1) + 1) > algorithm.inst.UB)
			|| (algorithm.MAX_AISLES == subset.cardinality()) || (lowerBound > 0 && lowerBound == solutionItems))
			return;
		
		// Add its children to the BnB queue.
		BitSet bs_child;
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(subset.get(a)) continue;
			if((subset.cardinality() + 1) * algorithm.objVal > algorithm.model.Z[a] + solutionItems) 
				continue;
			
			bs_child = (BitSet) subset.clone();
			bs_child.set(a);
			if(!algorithm.found.containsKey(bs_child)) {
				algorithm.found.put(bs_child, true);
				queue.add(new Node(bs_child, a, totalItems, solutionItems, 
								   Math.min(solutionItems + algorithm.model.Z[a], totalItems + algorithm.model.Q[a]),
								   algorithm));
			}
		}
	}
	
	@Override
	public String toString() {
		String str = aisleAdded + " ";
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(subset.get(a)) str += "1";
			else str += "0";
		}
		return str;
	}
}
