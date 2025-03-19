package org.sbpo2025.challenge;

import java.util.Stack;

public class Node {
	protected BranchAndBound algorithm;
	
	protected int aisle_added;
	protected int total_items = 0;
	
	public Node(int aisle, BranchAndBound bnb) {
		aisle_added = aisle;
		algorithm = bnb;
	} 
	
	public void run() {
		return;
	}
	
	public void addChildren(Stack<Node> stack) {
		return;
	}
}
