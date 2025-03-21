package org.sbpo2025.challenge;

import ilog.concert.IloException;

public class ExitNode extends Node {

	public ExitNode(int aisle, int itemsParent, double lb, BranchAndBound bnb) {
		super(aisle, itemsParent, lb, bnb);
	}

	@Override
	public double run() throws IloException {
		// Remove its aisle to the subset.
		algorithm.aisles.clear(aisle_added);
		algorithm.model.disableAisle(aisle_added);
		
		return 0;
	}
}
