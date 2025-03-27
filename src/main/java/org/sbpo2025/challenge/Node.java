package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.Status;

public class Node {
	protected BranchAndBound algorithm;
	
	protected BitSet notAllowed;
	protected BitSet subset;
	
	protected int aisleAdded;
	
	double solutionValue = 0;
	double solutionItems = 0;
	
	protected double lowerBound;
	protected double upperBound;
	
	int[] numItems;
	int height;
	
	
	public Node(BitSet bs, BitSet tabu, int aisle, double lb, double ub, int h, BranchAndBound bnb) {
		algorithm = bnb;
		
		subset = bs;
	    notAllowed = tabu;
		
		aisleAdded = aisle;
		upperBound = ub;
		lowerBound = lb;
		height = h;
		
		if(eq(lowerBound, upperBound) || aisle == -1) {
			solutionValue = lowerBound;
			solutionItems = (int)solutionValue * subset.cardinality();
		}
	} 
	
	// Esse aqui é para quando temos o double, que é do arco da velha.
	private boolean eq(double d1, double d2) {
		return Math.abs(d1 - d2) <= 0.00001;
	}
	
	public double run() throws IloException {
		if(subset.cardinality() < algorithm.MIN_AISLES || subset.cardinality() > algorithm.MAX_AISLES || solutionValue > 0) 
			return 0;
		
		// Set model bounds.
		algorithm.model.setBounds(lowerBound*subset.cardinality(), Math.min(upperBound*subset.cardinality(), algorithm.inst.UB));
		
		// Add its new aisle to the subset.
		algorithm.model.resetItemAmounts();
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(!subset.get(a)) continue;
			
			for(int i : algorithm.inst.aisles.get(a).keySet())
				algorithm.model.addItemAmount(i, algorithm.model.q.get(a).get(i));
		}		
		
		// Execute model's optimization.
		algorithm.model.setBounds(algorithm.inst.LB, algorithm.inst.UB);
		algorithm.model.solve();
		
		// If a solution was found, return it.
		if(algorithm.model.getStatus() != Status.Infeasible) {
			solutionItems = algorithm.model.getObjValue();
			solutionValue = solutionItems/subset.cardinality();
			return solutionValue;
		}
		
		solutionItems = 0;
		solutionValue = 0;
		return 0;
	}
	
	public void branch(PriorityQueue<Node> queue) throws IloException {	
		// If the next size of subsets cannot beat the current solution.
		if((Math.floor(algorithm.objVal * (subset.cardinality() + 1) + 1) > algorithm.inst.UB)
			|| (algorithm.MAX_AISLES <= subset.cardinality()) || (height >= algorithm.totalAisles))
			return;
		
		Node lChild = getLeftChild();
		Node rChild = getRightChild();

		if(lChild.upperBound >= algorithm.objVal)
			queue.add(lChild);
		
		if(rChild.upperBound >= algorithm.objVal)
			queue.add(rChild);
	}
	
	protected Node getLeftChild() {
		int idx = algorithm.aislesQueue.get(height).getLeft();
		
		BitSet tabu = (BitSet) notAllowed.clone();
		BitSet bs = (BitSet) subset.clone();
		bs.set(idx);
		
		int num = Math.max(1, subset.cardinality());
		return new Node(bs, tabu, idx, solutionItems/(subset.cardinality()+1), (upperBound*num)/(subset.cardinality()+1), height+1, algorithm);
	}
	
	protected Node getRightChild() {
		int idx = algorithm.aislesQueue.get(height).getLeft();
		
		BitSet bs = (BitSet) subset.clone();		
		BitSet tabu = (BitSet) notAllowed.clone();
		tabu.set(idx);
		
		numItems = new int[algorithm.inst.n];
		for(int i = 0; i < algorithm.model.Z.length; i++) 
			numItems[i] = algorithm.model.Z[i];
		
		for(int a = 0; a < algorithm.inst.aisles.size(); a++) 
			if(tabu.get(a)) 
				for(int i : algorithm.inst.aisles.get(a).keySet()) 
					numItems[i] -= algorithm.inst.aisles.get(a).get(i);

		
		double ubItems = 0, totalOrders = 0;
		for(int o = 0; o < algorithm.inst.orders.size(); o++) {
			if(totalOrders == algorithm.MAX_ORDERS) break;
			if(!notBuildable(o)) {
				ubItems += algorithm.model.sortedOrders.get(o).getRight();
				totalOrders++;
			}
		}
		ubItems = Math.min(ubItems, algorithm.inst.UB);
		
		return new Node(bs, tabu, -1, solutionValue, ubItems/(subset.cardinality()+1), height+1, algorithm);
	}
	
	private boolean notBuildable(int order) {
		for(int i : algorithm.inst.orders.get(order).keySet()) 
			if(algorithm.inst.orders.get(order).get(i) < numItems[i])
				return true;
		return false;
	}
	
	@Override
	public String toString() {
		String str = "Subset:      ";
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(subset.get(a)) str += "1";
			else str += "0";
		}
		str += "\n";
		str += "Not allowed: ";
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(notAllowed.get(a)) str += "1";
			else str += "0";
		}
		return str;
	}
	
	public ChallengeSolution getSolution() throws IloException {
		Set<Integer> orders = algorithm.model.getOrders();
		
		Set<Integer> aisles = new HashSet<Integer>();
		for(int a = 0; a < algorithm.inst.aisles.size(); a++)
			if(subset.get(a)) aisles.add(a);
		
		return new ChallengeSolution(orders, aisles);
	}
}
