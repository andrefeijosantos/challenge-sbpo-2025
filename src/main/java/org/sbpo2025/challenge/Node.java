package org.sbpo2025.challenge;

import java.util.Stack;

import ilog.concert.IloException;

public class Node {
	protected BranchAndBound algorithm;
	
	protected int aisle_added;
	protected int totalItems;
	
	public Node(int aisle, int itemsParent, BranchAndBound bnb) {
		aisle_added = aisle;
		algorithm = bnb;
		totalItems = itemsParent;
	} 
	
	public double run() throws IloException {
		return 0;
	}
	
	public void addChildren(Stack<Node> stack)  throws IloException {
		return;
	}
}
