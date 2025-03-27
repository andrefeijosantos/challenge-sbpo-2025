package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.PriorityQueue;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.IloException;

public class BranchAndBound extends Approach {
	
	// Model used to optimize.
	BnBModel model;
	
	// Some useful data.
	int totalNodes = 0;
	int totalAisles;
	
	int MAX_AISLES;
	int MIN_AISLES = 1;
	int MAX_ORDERS;
	
	ArrayList<Pair<Integer, Integer>> aislesQueue;
	
	public BranchAndBound(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
		model = new BnBModel(inst);
		totalAisles = inst.aisles.size();
		MAX_AISLES = totalAisles;
		
		try {
			// Max number of orders.
			MaxOrdersModel maxOrders = new MaxOrdersModel(inst);
			maxOrders.build();
			maxOrders.solve();
			MAX_ORDERS = (int) maxOrders.getObjValue();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public ChallengeSolution BeFS() {
		try {
			model.build();
			print_header();
			
			aislesQueue = model.sortedAisles;
			
			// Create first-level nodes.
			PriorityQueue<Node> queue = new PriorityQueue<Node>(new NodeComparator());
			queue.add(new Node(new BitSet(inst.aisles.size()), new BitSet(inst.aisles.size()), -1, 0, inst.UB, 0, this));
			
			// Branch-and-Bound.
			Node curr;
			while(queue.peek() != null) {
				if(getRemainingTime(stopWatch) < 600 - 60*3)
					break;
				
				// Get the node on peek and solve it.
				curr = queue.poll(); 
				curr.run();
				totalNodes++;
				
				// If a better solution was found.
				if(curr.solutionValue > objVal && curr.solutionItems >= inst.LB) {
					objVal = curr.solutionValue;
					solution = curr.getSolution();
					
					double result = inst.UB / objVal;
					MAX_AISLES = (int) Math.floor(result);
						
					print_line(curr.subset.cardinality(), MAX_AISLES);
				}
				
				// Branch into two nodes.
				curr.branch(queue);
				
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		System.out.println("Solution: " + objVal);
		System.out.println("Total nodes: " + totalNodes + "\n");
		System.out.println("Time: " + stopWatch + "\n");
		
		return solution;
	}
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		logln("Thread count: CPLEX using up to " + Runtime.getRuntime().availableProcessors() + " threads");
		logln("Variable types: 1 continuous; " + model.p.length + " integer (" + model.p.length + " binaries)");
		logln("");
		
		logln("  h  |  H  |  LB  |  UB  |  Incumbent");
	}
	
	public void print_line(int h, int H) throws IloException {
		log(String.format("%4" + "s |", h));
		log(String.format("%4" + "s |", H));
		log(String.format("%5" + "s |", (int) model.z.getLB()));
		log(String.format("%5" + "s |", (int) model.z.getUB()));
		
		if(objVal > 0) logln(String.format("%12.6f" + "",  objVal));
		else logln(String.format("%12" + "s", "-"));
	}
}
