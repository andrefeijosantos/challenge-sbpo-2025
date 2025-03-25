package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import ilog.concert.IloException;

public class Node {
	protected BranchAndBound algorithm;
	
	protected BitSet notAllowed;
	protected BitSet subset;
	
	protected int totalItems;
	protected int aisleAdded;
	
	double solutionValue = 0;
	int    solutionItems = 0;
	
	protected int lowerBound;
	protected int upperBound;
	
	int height;
	
	public Node(BitSet bs, BitSet tabu, int aisle, int itemsParent, int lb, int ub, int h, BranchAndBound bnb) {
		subset = bs;
		notAllowed = tabu;
		
		aisleAdded = aisle;
		algorithm = bnb;
		lowerBound = lb;
		upperBound = ub;
		height = h;
		
		if(lowerBound == upperBound) {
			solutionItems = lb;
			solutionValue = subset.cardinality() > 0? solutionItems/subset.cardinality() : 0;
		}
		
		totalItems = aisleAdded >= 0? itemsParent + algorithm.model.Z[aisleAdded] : 0;
	} 
	
	public void branch(PriorityQueue<Node> queue) throws IloException {	
		// If the next size of subsets cannot beat the current solution.
		if((Math.floor(algorithm.objVal * (subset.cardinality() + 1) + 1) > algorithm.inst.UB)
			|| (algorithm.MAX_AISLES <= subset.cardinality()) || (height >= algorithm.inst.aisles.size()))
			return;
		
		if((aisleAdded == 71 || aisleAdded == 97) && subset.cardinality() == 1) {
			System.out.println("achei 111111");
		}
		
		for(int a = 0; a < algorithm.inst.aisles.size(); a++) {
			if(subset.get(a))           algorithm.branchingModel.forceAisle(a);
			else if(notAllowed.get(a))  algorithm.branchingModel.disableAisle(a);
			else                        algorithm.branchingModel.enableAisle(a);
		}
		
		algorithm.branchingModel.setSumY(subset.cardinality()+1);
		algorithm.branchingModel.z.setLB(solutionItems);
		algorithm.branchingModel.z.setUB(algorithm.inst.UB);
		
		algorithm.branchingModel.solve();
		
		int childSolution = (int) algorithm.branchingModel.getObjValue();
		
		int next = -1;
		for(int a = 0; a < algorithm.inst.aisles.size(); a++) {
			if(subset.get(a)) continue;
			if(algorithm.branchingModel.getValue(algorithm.branchingModel.y[a]) > .5) {
				next = a; break;
			}
		}
		
		if(next == 71 || next == 97) {
			System.out.println("achei 22222222");
		}
		
		if(((aisleAdded == 71 && next == 97) || (aisleAdded == 97 && next == 71)) && subset.cardinality() == 1) {
			System.out.println("achei 33333");
			System.out.println(childSolution + " " + (subset.cardinality()+1));
		}
		
		if(childSolution/(subset.cardinality()+1) > algorithm.objVal && childSolution >= algorithm.inst.LB) {
			algorithm.objVal = childSolution/(subset.cardinality()+1);
			//algorithm.solution = algorithm.branchingModel.getOrders();
			
			double result = algorithm.inst.UB / algorithm.objVal;
			algorithm.MAX_AISLES = (int) Math.floor(result);
				
			algorithm.print_line(subset.cardinality()+1, algorithm.MAX_AISLES);
		}
		
		queue.add(getLeftChild(next, childSolution));
		queue.add(getRightChild(next));
	}
	
	protected Node getLeftChild(int idx, int objVal) {
		BitSet bs = (BitSet) subset.clone();
		bs.set(idx);
		
		BitSet tabu = (BitSet) notAllowed.clone();
		
		return new Node(bs, tabu, idx, totalItems, objVal, objVal, height+1, algorithm);
	}
	
	protected Node getRightChild(int idx) {
		BitSet bs = (BitSet) subset.clone();
		
		BitSet tabu = (BitSet) notAllowed.clone();
		tabu.set(idx);
		
		return new Node(bs, tabu, -1, totalItems, solutionItems, solutionItems, height+1, algorithm);
	}
	
	@Override
	public String toString() {
		String str = "Subset:      ";
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(subset.get(a)) str += "1";
			else str += "0";
		}
		str += "\n";
		str += "Not allowed: ";
		for(int a = 0; a < algorithm.totalAisles; a++) {
			if(subset.get(a)) str += "1";
			else str += "0";
		}
		return str;
	}
	
	public ChallengeSolution getSolution() throws IloException {
		Set<Integer> orders = algorithm.model.getOrders();
		
		Set<Integer> aisles = new HashSet<Integer>();
		for(int a = 0; a < algorithm.inst.aisles.size(); a++)
			if(subset.get(a)) aisles.add(a);
		
		return new ChallengeSolution(orders, aisles);
	}
}
