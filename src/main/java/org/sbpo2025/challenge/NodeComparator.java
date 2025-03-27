package org.sbpo2025.challenge;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
	 @Override
	 public int compare(Node n1, Node n2) { 
		 if(n2.upperBound == n1.upperBound)
			 return  (int) (n2.lowerBound - n1.lowerBound);
		 return (int) (n2.upperBound - n1.upperBound);
	 }
}
