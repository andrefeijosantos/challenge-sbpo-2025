package org.sbpo2025.challenge;

import java.util.Stack;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

public class EnterNode extends Node {

	public EnterNode(int aisle, int itemsParent, double lb, BranchAndBound bnb) {
		super(aisle, itemsParent, lb, bnb);
	}

	@Override
	public double run() throws IloException {
		if(algorithm.MAX_HEIGHT < algorithm.aisles.cardinality())
			return 0;
		
		// Add its new aisle to the subset.
		algorithm.aisles.set(aisle_added);
		totalItems += algorithm.model.Q.get(aisle_added);

		// Set model bounds.
		algorithm.model.setBounds(Math.max(Math.floor(algorithm.objVal * algorithm.aisles.cardinality() + 1), algorithm.inst.LB), 
								  Math.min(totalItems, algorithm.inst.UB));
		
		// If a solution can not be found, return.
		if(algorithm.model.z.getUB() < algorithm.model.z.getLB())
			return 0;
		
		
		// Execute model's optimization.
		algorithm.model.enableAisle(aisle_added);
		algorithm.model.solve();
		
		// If a solution was found, return it.
		if(algorithm.model.getStatus() == IloCplex.Status.Optimal) {
			solutionValue = algorithm.model.getObjValue()/algorithm.aisles.cardinality();
			return solutionValue;
		}
		return 0;
	}

	@Override
	public void addChildren(Stack<Node> stack) throws IloException {
		// Add the exist of this node to the BnB stack.
		stack.add(new ExitNode(aisle_added, totalItems, solutionValue, algorithm));
		
		// If the next size of subsets cannot beat the current solution.
		if((Math.floor(algorithm.objVal * (algorithm.aisles.cardinality() + 1) + 1) > algorithm.inst.UB)
			|| (algorithm.MAX_HEIGHT == algorithm.aisles.cardinality()))
			return;
		
		// Add its children to the BnB stack.
		for(int a = 0; a < algorithm.total_aisles; a++) {
			if(algorithm.aisles.get(a)) continue;
			
			algorithm.aisles.set(a);
			if(!algorithm.found.containsKey(algorithm.aisles)) {
				algorithm.found.put(algorithm.aisles, true);
				stack.add(new EnterNode(a, totalItems, solutionValue, algorithm));
			}
			algorithm.aisles.clear(a);
		}
	}
	
	public void printSubSet() {
		System.out.print(aisle_added + " ");
		for(int a = 0; a < algorithm.total_aisles; a++) {
			if(algorithm.aisles.get(a)) System.out.print("1");
			else System.out.print("0");
		}
		System.out.println("");
	}
}
