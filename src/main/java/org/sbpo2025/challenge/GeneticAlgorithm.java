package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;

public class GeneticAlgorithm extends Approach {
	
	// Parameters
	int _PopSize = 100;
	int _NumIters = 100;
	double _MutationRate = 0.2;
	
	// Random numbers settings
	private static final long SEED = 1L;
    private static final Random RANDOM = new Random(SEED);
	
	int _MinAisles, _MaxAisles;
	
	Set<Individual> _Population;
	int _IndIdControl = 0;
	
	public GeneticAlgorithm(Instance inst, StopWatch stopWatch, long time_limit, int minAisles, int maxAisles) {
		super(inst, stopWatch, time_limit);
		_MinAisles = minAisles;
		_MaxAisles = maxAisles;
		_Population = new HashSet<>();
	}
	
	public void optimize() {
		
		generateInitialPop();
	
		for (int iter = 1; iter < _NumIters; iter++) {
			matching();
			recombination();
			adaptation();
			mutation();
			selection();
		}
	}
	
	private void generateInitialPop() {
		
		List<Integer> aislesList = IntStream.range(0, inst.aisles.size()).boxed().collect(Collectors.toList());
		
		for (int ind = 0; ind < _PopSize; ind++) {
			List<Integer> shuffledAisles = new ArrayList<Integer>(aislesList); 
			Collections.shuffle(shuffledAisles, RANDOM);
			
			int totalY = _MinAisles + RANDOM.nextInt(_MaxAisles - _MinAisles + 1);
			BitSet cromossome = new BitSet(inst.aisles.size());
			for (int i = 0; i < totalY; i++) cromossome.set(shuffledAisles.get(i));
			
			_Population.add(new Individual(++_IndIdControl, cromossome));
		}
		
	}
	
	private void matching() {}
	
	private void recombination() {}
	
	private void adaptation() {}
	
	private void mutation() {}
	
	private void selection() {}
	
}
