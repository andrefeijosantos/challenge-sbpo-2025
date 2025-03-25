package org.sbpo2025.challenge;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
	 @Override
	 public int compare(Node n1, Node n2) { 
		// n2 (already on queue) is feasible.
		if(n2.solutionItems >= n2.algorithm.inst.LB) {
			 if(n1.solutionItems >= n1.algorithm.inst.LB) 
				 return (int) (n2.solutionValue - n1.solutionValue);
			 
			 else return 1;
		 }
		 
		// n2 (already on queue) is infeasible.
		 if(n1.solutionItems < n1.algorithm.inst.LB)
			 return (int) (n2.solutionValue - n1.solutionValue);
		 
		 else return -1;
	 }
}
