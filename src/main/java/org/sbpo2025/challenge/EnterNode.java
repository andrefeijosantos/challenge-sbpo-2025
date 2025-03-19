package org.sbpo2025.challenge;

import java.util.Stack;

public class EnterNode extends Node {

	public EnterNode(int aisle, BranchAndBound bnb) {
		super(aisle, bnb);
	}

	@Override
	public void run() {
		System.out.print(aisle_added + " ");
		algorithm.aisles.set(aisle_added);
		for(int a = 0; a < algorithm.total_aisles; a++) {
			if(algorithm.aisles.get(a)) System.out.print("1");
			else System.out.print("0");
		}
		System.out.println("");
	}

	@Override
	public void addChildren(Stack<Node> stack) {
		stack.add(new ExitNode(aisle_added, algorithm));
		for(int a = 0; a < algorithm.total_aisles; a++) {
			if(algorithm.aisles.get(a)) continue;
			
			algorithm.aisles.set(a);
			if(!algorithm.found.containsKey(algorithm.aisles)) {
				algorithm.found.put(algorithm.aisles, true);
				stack.add(new EnterNode(a, algorithm));
			}
			algorithm.aisles.clear(a);
		}
	}
}
