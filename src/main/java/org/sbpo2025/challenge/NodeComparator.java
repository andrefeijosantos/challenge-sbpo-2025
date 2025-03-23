package org.sbpo2025.challenge;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
	 @Override
	 public int compare(Node n1, Node n2) {
		// n2 (already on queue) is infeasible.
		if(n2.upperBound < n2.algorithm.inst.LB) {
			 if(n1.upperBound < n1.algorithm.inst.LB) {
				 if(n2.lowerBound == n1.lowerBound)
					 return n2.upperBound - n1.upperBound;
				 return n2.lowerBound - n1.lowerBound;
			 }
			 
			 else return -1;
		 }
		 
		 // No information about n2 feasibility.
		 if(n1.lowerBound > n1.algorithm.inst.LB) 
			 return -1;
		 
		 else if(n1.upperBound < n1.algorithm.inst.LB)
			 return 1;
		 
		 if(n2.lowerBound == n1.lowerBound)
			 return n2.upperBound - n1.upperBound;
		 return n2.lowerBound - n1.lowerBound;
	 }
}
