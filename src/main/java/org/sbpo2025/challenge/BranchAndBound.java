package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import ilog.concert.IloException;

public class BranchAndBound extends Approach {
	
	// Model used to optimize.
	LBnBModel lModel;
	RBnBModel rModel;
	
	// Some useful data.
	int TOTAL_NODES = 0;
	int TOTAL_AISLES;
	
	int MAX_AISLES;
	int MIN_AISLES;
	
	ArrayList<Pair<Integer, Integer>> aislesQueue;
	
	
	public BranchAndBound(Instance inst, StopWatch stopWatch, long timeLimit) {
		super(inst, stopWatch, timeLimit);
		lModel = new LBnBModel(inst);
		rModel = new RBnBModel(inst);
		
		TOTAL_AISLES = inst.aisles.size();		
		MAX_AISLES = TOTAL_AISLES;
		MIN_AISLES = 1;
	}
	
	public BranchAndBound(Instance inst, StopWatch stopWatch, long timeLimit, int minAisles, int maxAisles, double sol) {
		super(inst, stopWatch, timeLimit);
		lModel = new LBnBModel(inst);
		rModel = new RBnBModel(inst);
		
		TOTAL_AISLES = inst.aisles.size();
		MIN_AISLES = minAisles;
		MAX_AISLES = maxAisles;
		objVal = sol;
	}
	
	public ChallengeSolution BeFS() {
		try {
			lModel.build();
			rModel.build();
			print_header();
			
			aislesQueue = lModel.sortedAisles;
			
			// Create first-level nodes.
			PriorityQueue<Node> queue = new PriorityQueue<Node>(new NodeComparator());
			queue.add(new Node(new BitSet(inst.aisles.size()), new BitSet(inst.aisles.size()), -1, 0, inst.LB, inst.UB, this));
			
			// Branch-and-Bound.
			Node curr;
			while(queue.peek() != null) {
				if(getRemainingTime(stopWatch) < 600 - 60*3)
					break;
				
				// Get the node on peek and solve it.
				curr = queue.poll(); 
				TOTAL_NODES++;
				
				System.out.println(curr.subset.cardinality() + " " + (TOTAL_AISLES - curr.notAllowed.cardinality()));
				//System.out.println(curr.solutionValue);
				
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
				//System.out.println(queue.size());
				
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		System.out.println("Solution: " + objVal);
		System.out.println("Total nodes: " + TOTAL_NODES + "\n");
		System.out.println("Time: " + stopWatch + "\n");
		
		return solution;
	}
	
	public ChallengeSolution DFS() {
		try {
			lModel.build();
			rModel.build();
			print_header();
			
			aislesQueue = lModel.sortedAisles;
			
			// Create first-level nodes.
			Stack<Node> stack = new Stack<Node>();
			stack.add(new Node(new BitSet(inst.aisles.size()), new BitSet(inst.aisles.size()), -1, 0, inst.LB, inst.UB, this));
			
			// Branch-and-Bound.
			Node curr;
			while(stack.peek() != null) {
				if(getRemainingTime(stopWatch) < 600 - 60*3)
					break;
				
				// Get the node on peek and solve it.
				curr = stack.pop(); 
				TOTAL_NODES++;
				
				//System.out.println(curr);
				//System.out.println(curr.solutionValue);
				
				// If a better solution was found.
				if(curr.solutionValue > objVal && curr.solutionItems >= inst.LB) {
					objVal = curr.solutionValue;
					solution = curr.getSolution();
					
					double result = inst.UB / objVal;
					MAX_AISLES = (int) Math.floor(result);
						
					print_line(curr.subset.cardinality(), MAX_AISLES);
				}
				
				// Branch into two nodes.
				curr.branch(stack);
				//System.out.println(stack.size());
				
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		System.out.println("Solution: " + objVal);
		System.out.println("Total nodes: " + TOTAL_NODES + "\n");
		System.out.println("Time: " + stopWatch + "\n");
		
		return solution;
	}
	
	public ChallengeSolution BFS() {
		try {
			lModel.build();
			rModel.build();
			print_header();
			
			aislesQueue = lModel.sortedAisles;
			
			// Create first-level nodes.
			Queue<Node> queue = new LinkedList<Node>();
			queue.add(new Node(new BitSet(inst.aisles.size()), new BitSet(inst.aisles.size()), -1, 0, inst.LB, inst.UB, this));
			
			// Branch-and-Bound.
			Node curr;
			while(queue.peek() != null) {
				if(getRemainingTime(stopWatch) < 600 - 60*3)
					break;
				
				// Get the node on peek and solve it.
				curr = queue.poll(); 
				TOTAL_NODES++;
				
				//System.out.println(curr);
				System.out.println(curr.height);
				//System.out.println(curr.solutionValue);
				
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
				//System.out.println(queue.size());
				
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		System.out.println("Solution: " + objVal);
		System.out.println("Total nodes: " + TOTAL_NODES + "\n");
		System.out.println("Time: " + stopWatch + "\n");
		
		return solution;
	}
	
	
	// === DEBUGGING AND LOGGING METHODS ===
	private void print_header() throws IloException {
		logln("SPO Optimizer version 1 (Copyright André Luiz F. dos Santos, Pedro Fiorio Baldotto)");
		logln("Thread count: CPLEX using up to " + Runtime.getRuntime().availableProcessors() + " threads");
		logln("Variable types: 1 continuous; " + lModel.p.length + " integer (" + lModel.p.length + " binaries)");
		logln("");
		
		logln("  h  |  H  |  LB  |  UB  |  Incumbent");
	}
	
	public void print_line(int h, int H) throws IloException {
		log(String.format("%4" + "s |", h));
		log(String.format("%4" + "s |", H));
		log(String.format("%5" + "s |", (int) lModel.z.getLB()));
		log(String.format("%5" + "s |", (int) lModel.z.getUB()));
		
		if(objVal > 0) logln(String.format("%12.6f" + "",  objVal));
		else logln(String.format("%12" + "s", "-"));
	}
}
