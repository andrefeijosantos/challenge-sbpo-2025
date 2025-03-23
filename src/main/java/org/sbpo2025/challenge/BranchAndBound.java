package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.IloException;

public class BranchAndBound extends Approach {
	
	BnBModel model;
	
	// Important data structures.
	HashMap<BitSet, Boolean> found;
	
	// Some useful data.
	int totalNodes = 1;
	int totalAisles;
	int MAX_AISLES;
	
	public BranchAndBound(Instance inst, StopWatch stopWatch, long time_limit) {
		super(inst, stopWatch, time_limit);
				
		found = new HashMap<BitSet, Boolean>();
		model = new BnBModel(inst);
		
		totalAisles = inst.aisles.size();
		MAX_AISLES = totalAisles;
	}
	
	public ChallengeSolution BeFS() {
		try {
			model.build();
			print_header();
			
			// Create first-level nodes.
			PriorityQueue<Node> queue = new PriorityQueue<Node>(new NodeComparator());
			for(int a = 0; a < totalAisles; a++) {
				BitSet bs = new BitSet(totalAisles); bs.set(a);
				queue.add(new Node(bs, a, 0, 0, Math.min(model.Z[a], model.Q[a]), this));
			}
			
			// Branch-and-Bound.
			Node curr; double node_value;
			while(queue.peek() != null) {
				if(getRemainingTime(stopWatch) < 600 - 60*3)
					break;
					
				curr = queue.poll();
				totalNodes++;
				node_value = curr.run();
				
				//logln("" + curr.subset.cardinality() + " " + curr.lowerBound + " " + curr.upperBound + " " + curr.solutionItems);
				
				// If a better solution was found.
				if(node_value > objVal && curr.solutionItems >= inst.LB) {
					objVal = node_value;
					solution = model.saveSolution();
					
					double result = inst.UB / objVal;
					MAX_AISLES = (int) Math.floor(result);
						
					print_line(curr.subset.cardinality(), MAX_AISLES);
				}
				else if(curr.solutionItems < inst.LB)
					curr.addChildren(queue);
				
				//logln("" + queue.size());
				
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		System.out.println("Solution: " + objVal);
		System.out.println("Total nodes: " + totalNodes + "\n");
		
		return solution;
	}
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		logln("Thread count: CPLEX using up to " + Runtime.getRuntime().availableProcessors() + " threads");
		logln("Variable types: 1 continuous; " + model.x.size() + " integer (" + " binaries)");
		logln("");
		
		logln("  h  |  H  |  LB  |  UB  |  Incumbent");
	}
	
	private void print_line(int h, int H) throws IloException {
		log(String.format("%4" + "s |", h));
		log(String.format("%4" + "s |", H));
		log(String.format("%5" + "s |", (int) model.z.getLB()));
		log(String.format("%5" + "s |", (int) model.z.getUB()));
		
		if(objVal > 0) logln(String.format("%12.6f" + "",  objVal));
		else logln(String.format("%12" + "s", "-"));
	}
}
