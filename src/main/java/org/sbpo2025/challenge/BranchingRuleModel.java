package org.sbpo2025.challenge;

import ilog.concert.IloException;

public class BranchingRuleModel extends ItModel {

	public BranchingRuleModel(Instance inst) {
		super(inst);
	}
	
	public void forceAisle(int a) throws IloException {
		y[a].setLB(1); y[a].setUB(1);
	}
	
	public void enableAisle(int a) throws IloException {
		y[a].setLB(0); y[a].setUB(1);
	}
	
	public void disableAisle(int a) throws IloException {
		y[a].setLB(0); y[a].setUB(0);
	}
}
