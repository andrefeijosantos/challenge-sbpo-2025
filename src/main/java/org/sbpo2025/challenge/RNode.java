package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import ilog.concert.IloException;
import ilog.cplex.IloCplex.Status;

public class RNode extends Node {

	public RNode(BitSet bs, BitSet tabu, int aisle, int h, double lbItems, double ubItems, BranchAndBound bnb) {
		super(bs, tabu, aisle, h, lbItems, ubItems, bnb);
		
		lowerBound = lowerBoundItems/Math.min(algorithm.MAX_AISLES, algorithm.TOTAL_AISLES-notAllowed.cardinality());
		
		if(algorithm.TOTAL_AISLES - notAllowed.cardinality() <= algorithm.MAX_AISLES)
			upperBound = process();
		
		upperBoundItems = Math.min(upperBoundItems, solutionItems);
	}

	@Override
	public double process() {
		try {			
			// Set model bounds.
			algorithm.rModel.setBounds(lowerBoundItems, upperBoundItems);
			
			// Add its new aisle to the subset.
			algorithm.rModel.resetItemAmounts();
			for(int a = 0; a < algorithm.TOTAL_AISLES; a++) {
				if(!notAllowed.get(a)) continue;
				
				for(int i : algorithm.inst.aisles.get(a).keySet())
					algorithm.rModel.rmvItemAmount(i, algorithm.rModel.q.get(a).get(i));
			}		
			
			// Execute model's optimization.
			algorithm.rModel.solve();
			
			// If a solution was found, return it.
			if(algorithm.rModel.getStatus() != Status.Infeasible) {
				solutionItems = algorithm.rModel.getObjValue();
				solutionValue = solutionItems/Math.min(algorithm.MAX_AISLES, algorithm.TOTAL_AISLES-notAllowed.cardinality());
			}
			
		} catch(IloException e) {
			e.printStackTrace();
		}
		
		return solutionItems/(Math.max(subset.cardinality(), algorithm.MIN_AISLES));
	}
	
	public ChallengeSolution getSolution() throws IloException {
		Set<Integer> orders = algorithm.rModel.getOrders();
		
		Set<Integer> aisles = new HashSet<Integer>();
		for(int a = 0; a < algorithm.inst.aisles.size(); a++)
			if(subset.get(a)) aisles.add(a);
		
		return new ChallengeSolution(orders, aisles);
	}
}
