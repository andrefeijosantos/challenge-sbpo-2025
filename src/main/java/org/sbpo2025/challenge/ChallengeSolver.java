package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;


enum Method {
	Iterative,
	BranchAndBound
}

public class ChallengeSolver {
	
    ChallengeSolution solution = null;
    private Instance inst;

    public ChallengeSolver(Instance instance) {
        this.inst = instance;
    }

    public ChallengeSolution solve(Method method, StopWatch stopWatch) {
    	switch(method) {
	    	case Iterative:
	        	SPOModel model = new SPOModel(this.inst, stopWatch);
	        	model.build();
	        	solution = model.optimize();
	        	break;
	        	
	    	case BranchAndBound:
	    		BranchAndBound bnb = new BranchAndBound(this.inst, stopWatch);
	    		bnb.optimize();
	    		break;
    	}
    	
    	System.out.println("Is Feasible: " + isSolutionFeasible());
    	System.out.println("Objective Value: " + computeObjectiveFunction());
    	System.out.println("Time: " + stopWatch);
    	
        return solution;
    }



    protected boolean isSolutionFeasible() {
        Set<Integer> selectedOrders = solution.orders();
        Set<Integer> visitedAisles = solution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return false;
        }

        int[] totalUnitsPicked = new int[inst.n];
        int[] totalUnitsAvailable = new int[inst.n];

        // Calculate total units picked
        for (int order : selectedOrders) {
            for (Map.Entry<Integer, Integer> entry : inst.orders.get(order).entrySet()) {
                totalUnitsPicked[entry.getKey()] += entry.getValue();
            }
        }

        // Calculate total units available
        for (int aisle : visitedAisles) {
            for (Map.Entry<Integer, Integer> entry : inst.aisles.get(aisle).entrySet()) {
                totalUnitsAvailable[entry.getKey()] += entry.getValue();
            }
        }

        // Check if the total units picked are within bounds
        int totalUnits = Arrays.stream(totalUnitsPicked).sum();
        if (totalUnits < inst.LB || totalUnits > inst.UB) {
            return false;
        }

        // Check if the units picked do not exceed the units available
        for (int i = 0; i < inst.n; i++) {
            if (totalUnitsPicked[i] > totalUnitsAvailable[i]) {
                return false;
            }
        }

        return true;
    }

    protected double computeObjectiveFunction() {
        Set<Integer> selectedOrders = solution.orders();
        Set<Integer> visitedAisles = solution.aisles();
        if (selectedOrders == null || visitedAisles == null || selectedOrders.isEmpty() || visitedAisles.isEmpty()) {
            return 0.0;
        }
        int totalUnitsPicked = 0;

        // Calculate total units picked
        for (int order : selectedOrders) {
            totalUnitsPicked += inst.orders.get(order).values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        // Calculate the number of visited aisles
        int numVisitedAisles = visitedAisles.size();

        // Objective function: total units picked / number of visited aisles
        return (double) totalUnitsPicked / numVisitedAisles;
    }
}
