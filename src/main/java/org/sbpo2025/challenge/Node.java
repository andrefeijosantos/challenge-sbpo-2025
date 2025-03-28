package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import ilog.concert.IloException;

public class Node {
	protected BranchAndBound algorithm;
	
	protected BitSet notAllowed;
	protected BitSet subset;
	
	protected int aisleAdded;
	protected int height;
	
	double solutionValue = 0;
	double solutionItems = 0;
	
	protected double lowerBound = 0;
	protected double upperBound = Double.MAX_VALUE;
	
	protected double lowerBoundItems;
	protected double upperBoundItems;
	
	
	public Node(BitSet bs, BitSet tabu, int aisle, int h, double lbItems, double ubItems, BranchAndBound bnb) {
		algorithm = bnb;
		
		subset = bs;
	    notAllowed = tabu;
		
		aisleAdded = aisle;
		height = h;
		
		lowerBoundItems = lbItems;
		upperBoundItems = ubItems;
		
		if(eq(lowerBound, upperBound) || aisle == -1) {
			solutionValue = lowerBound;
			solutionItems = (int)solutionValue * subset.cardinality();
		}
	} 
	
	private boolean eq(double d1, double d2) {
		return Math.abs(d1 - d2) <= 0.00001;
	}
	
	public double process() {
		return 0;
	}
	
	public void branch(PriorityQueue<Node> queue) throws IloException {	
		if(height >= algorithm.TOTAL_AISLES ||
		   subset.cardinality() >= algorithm.MAX_AISLES ||
		   (algorithm.TOTAL_AISLES - notAllowed.cardinality() <= algorithm.MIN_AISLES)) 
			return;
		
		Node lChild = getLeftChild();
		Node rChild = getRightChild();

		if(lChild.upperBound > algorithm.objVal)
			queue.add(lChild);
		
		if(rChild.upperBound > algorithm.objVal)
			queue.add(rChild);
	}
	
	public void branch(Stack<Node> stack) throws IloException {	
		if(height >= algorithm.TOTAL_AISLES ||
		   subset.cardinality() >= algorithm.MAX_AISLES) 
			return;
		//if(Math.floor(algorithm.objVal * (subset.cardinality() + 1) + 1) > algorithm.inst.UB)
			//return;
		
		Node lChild = getLeftChild();
		Node rChild = getRightChild();

		if(rChild.upperBound >= algorithm.objVal)
			stack.add(rChild);
		
		if(lChild.upperBound >= algorithm.objVal)
			stack.add(lChild);
	}
	
	public void branch(Queue<Node> queue) throws IloException {	
		if(height >= algorithm.TOTAL_AISLES ||
		   subset.cardinality() >= algorithm.MAX_AISLES) 
			return;
		//if(Math.floor(algorithm.objVal * (subset.cardinality() + 1) + 1) > algorithm.inst.UB)
			//return;
		
		Node lChild = getLeftChild();
		Node rChild = getRightChild();

		if(rChild.upperBound >= algorithm.objVal)
			queue.add(rChild);
		
		if(lChild.upperBound >= algorithm.objVal)
			queue.add(lChild);
	}
	
	protected Node getLeftChild() {
		int idx = algorithm.aislesQueue.get(height).getLeft();
		
		BitSet tabu = (BitSet) notAllowed.clone();
		BitSet bs = (BitSet) subset.clone();
		bs.set(idx);
		
		return new LNode(bs, tabu, idx, height+1, lowerBoundItems, upperBoundItems, algorithm);
	}
	
	protected Node getRightChild() {
		int idx = algorithm.aislesQueue.get(height).getLeft();
		
		BitSet bs = (BitSet) subset.clone();		
		BitSet tabu = (BitSet) notAllowed.clone();
		tabu.set(idx);
		
		return new RNode(bs, tabu, -1, height+1, lowerBoundItems, upperBoundItems, algorithm);
	}
	
	@Override
	public String toString() {
		String str = "Subset:      ";
		for(int a = 0; a < algorithm.TOTAL_AISLES; a++) {
			if(subset.get(a)) str += "1";
			else str += "0";
		}
		str += "\n";
		str += "Not allowed: ";
		for(int a = 0; a < algorithm.TOTAL_AISLES; a++) {
			if(notAllowed.get(a)) str += "1";
			else str += "0";
		}
		return str;
	}
	
	public ChallengeSolution getSolution() throws IloException {
		return null;
	}
}
