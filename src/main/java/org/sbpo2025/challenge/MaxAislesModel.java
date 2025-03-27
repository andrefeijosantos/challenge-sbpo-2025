package org.sbpo2025.challenge;

import ilog.concert.IloException;

public class MaxAislesModel extends ItModel {

	public MaxAislesModel(Instance inst) {
		super(inst, 20);
	}
	
	@Override
	protected void buildObjective() {
		try {
			model.addMaximize(sumY);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
}
