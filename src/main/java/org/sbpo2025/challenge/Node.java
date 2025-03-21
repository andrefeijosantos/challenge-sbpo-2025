package org.sbpo2025.challenge;

import java.util.Stack;

import ilog.concert.IloException;

public class Node {
	protected BranchAndBound algorithm;
	
	protected int aisle_added;
	protected int totalItems;
	protected double lowerBound;
	
	double solutionValue = 0;
	
	public Node(int aisle, int itemsParent, double lb, BranchAndBound bnb) {
		aisle_added = aisle;
		algorithm = bnb;
		totalItems = itemsParent;
		lowerBound = lb;
	} 
	
	public double run() throws IloException {
		return 0;
	}
	
	public void addChildren(Stack<Node> stack)  throws IloException {
		return;
	}
}
