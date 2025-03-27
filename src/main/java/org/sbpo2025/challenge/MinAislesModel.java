package org.sbpo2025.challenge;

import ilog.concert.IloException;

public class MinAislesModel extends ItModel {

	public MinAislesModel(Instance inst) {
		super(inst, 20);
	}
	
	@Override
	protected void buildObjective() {
		try {
			model.addMinimize(sumY);
			
		} catch(IloException e) {
			e.printStackTrace();
		}
	}
}
