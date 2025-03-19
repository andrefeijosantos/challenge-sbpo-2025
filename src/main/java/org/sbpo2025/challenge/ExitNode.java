package org.sbpo2025.challenge;

import java.util.Stack;

public class ExitNode extends Node {

	public ExitNode(int aisle, BranchAndBound bnb) {
		super(aisle, bnb);
	}

	@Override
	public void run() {
		algorithm.aisles.clear(aisle_added);
	}
}
