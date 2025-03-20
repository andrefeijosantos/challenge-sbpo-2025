package org.sbpo2025.challenge;

import java.util.Stack;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

public class EnterNode extends Node {

	public EnterNode(int aisle, int itemsParent, BranchAndBound bnb) {
		super(aisle, itemsParent, bnb);
	}

	@Override
	public double run() throws IloException {
		// Add its new aisle to the subset.
		algorithm.aisles.set(aisle_added);
		totalItems += algorithm.model.Q.get(aisle_added);

		if(algorithm.objVal > 0) {
			double d_lb = Math.floor(algorithm.objVal * algorithm.aisles.cardinality() + 1);
			int    new_lb = (int) d_lb;
			
			if(new_lb > algorithm.inst.UB)
				return 0;
			
			algorithm.model.setBounds(new_lb, algorithm.model.z.getUB());
		}
		
		
		if(totalItems < algorithm.model.z.getLB())
			return 0;
		
		
		// Execute model's optimization.
		algorithm.model.enableAisle(aisle_added);
		algorithm.model.solve();
		
		
		if(algorithm.model.getStatus() == IloCplex.Status.Optimal) {
			
			algorithm.logln(""+algorithm.model.getObjValue() + " " + algorithm.aisles.cardinality());
			algorithm.logln(""+algorithm.model.getObjValue()/algorithm.aisles.cardinality());
			
			return algorithm.model.getObjValue()/algorithm.aisles.cardinality();	
		}
		
		//algorithm.logln(algorithm.model.getStatus()+"");	
		return 0;
	}

	@Override
	public void addChildren(Stack<Node> stack) throws IloException {
		// Add the exist of this node to the BnB stack.
		stack.add(new ExitNode(aisle_added, totalItems, algorithm));
		
		if(algorithm.model.z.getLB() > algorithm.inst.UB)
			return;
		
		// Add its children to the BnB stack.
		for(int a = 0; a < algorithm.total_aisles; a++) {
			if(algorithm.aisles.get(a)) continue;
			
			algorithm.aisles.set(a);
			if(!algorithm.found.containsKey(algorithm.aisles)) {
				algorithm.found.put(algorithm.aisles, true);
				stack.add(new EnterNode(a, totalItems, algorithm));
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
