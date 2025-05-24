package org.sbpo2025.challenge;

import org.apache.commons.lang3.time.StopWatch;

import ilog.concert.IloException;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;


enum Method {
	Iterative,
	GeneralParallelIterative,
	ParallelIterative,
	ParamFractional,
	RLFractional,
	ItRL
}

enum ItemsDistribution {
	AllOneItem,
	MixedItemsQuantities,
	AllMultipleItems
}

public class ChallengeSolver {
	
    ChallengeSolution solution = null;
    private Instance inst;
    
    // Time Limit
    int HOURS   = 4, 
        MINUTES = 0, 
        SECONDS = 0;

    public ChallengeSolver(Instance instance) {
        this.inst = instance;
    }

    public ChallengeSolution solve(Method method, StopWatch stopWatch) {
    	switch(method) {
	    	case Iterative:
	        	Iterative itModel = new Iterative(this.inst, stopWatch, getTimeLimitInSeconds(), 30000);
	        	solution = itModel.optimize();
	        	break;

	    	case GeneralParallelIterative:
	    		ParallelIterative genParallelIterative = new ParallelIterative(this.inst, ItemsDistribution.AllMultipleItems, stopWatch, getTimeLimitInSeconds());
	        	solution = genParallelIterative.optimize();
	    		break;
	        	
	    	case ParallelIterative:
	    		ParallelIterative parallelIterative = new ParallelIterative(this.inst, getItemsDistribution(), stopWatch, getTimeLimitInSeconds());
	        	solution = parallelIterative.optimize();
	    		break;
	    		
	    	case ParamFractional:
	    		ParametricFractional paramFractional = new ParametricFractional(this.inst, stopWatch, getTimeLimitInSeconds());
	        	solution = paramFractional.optimize();
	    		break;
	    		
	    	case RLFractional:
	    		RefLinFractional refLinFractional = new RefLinFractional(this.inst, stopWatch, getTimeLimitInSeconds());
	        	solution = refLinFractional.optimize();
	    		break;
	    	
	    	case ItRL:
	    		ParallelIterative model1 = new ParallelIterative(this.inst, getItemsDistribution(), stopWatch, (int)(3.5*60*1000));
	    		RefLinFractional model2 = new RefLinFractional(this.inst, stopWatch, getTimeLimitInSeconds());
	        	solution = model1.optimize();
	    		
	        	if(!model1.optimal) {
		    		try {
		    			model2.init(model1, solution);
		    			model1 = null;
		    		} catch(IloException e) {
		    			e.printStackTrace();
		    		}
		    		
		        	solution = model2.optimize();
	        	}
	    		break;
    	}
    	
    	
    	System.out.println("Is Feasible: " + isSolutionFeasible());
    	System.out.println("Objective Value: " + computeObjectiveFunction());
    	System.out.println("Time: " + stopWatch);
    	
        return solution;
    }
    
    protected ItemsDistribution getItemsDistribution() {
    	ItemsDistribution res = ItemsDistribution.AllOneItem;
    	
    	int multipleItemsCnt = 0;
    	for(int o = 0; o < inst.orders.size(); o++)
    		if(inst.orders.get(o).keySet().size() > 1) {
    			res = ItemsDistribution.MixedItemsQuantities;
    			multipleItemsCnt++;
    		}
    	
    	if(multipleItemsCnt == inst.orders.size())
    		res = ItemsDistribution.AllMultipleItems;
    	
    	return res;
    }
    
    protected int getTimeLimitInSeconds() {
    	return 1000*(3600 * HOURS + 60 * MINUTES + SECONDS);
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
