package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;

public class GeneticAlgorithm extends Approach {
	
	// Parameters
	int _PopSize = 100;
	int _NumGenerations = 100;
	double _MutationRate = 0.2;
	double _RankPression = 1.5; // (1, 2]
	double _EliteRate = 0.5;
	
	// Random numbers settings
	private static final long SEED = 1L;
    private static final Random RANDOM = new Random(SEED);
	
	int _MinAisles, _MaxAisles;
	
	Set<Individual> _Population;
	List<Individual> _Parents, _Offspring;
	int _IndIdControl = 0;
	
	public GeneticAlgorithm(Instance inst, StopWatch stopWatch, long time_limit, int minAisles, int maxAisles) {
		super(inst, stopWatch, time_limit);
		_MinAisles = minAisles;
		_MaxAisles = maxAisles;
		_Population = new TreeSet<>();
	}
	
	public void optimize() {
		
		generateInitialPop();
	
		for (int gen = 1; gen < _NumGenerations; gen++) {
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
	
	private void matching() {
		
		// Ranking selection
		List<Double> cumProb = new ArrayList<>(_PopSize);
		List<Individual> popList = new ArrayList<>(_PopSize);
		double baseline = (2 - _RankPression) / _PopSize;
		double denominator = _PopSize * (_PopSize - 1);
		
		int i = 0;
		for (Individual ind : _Population) {
			popList.add(ind);
			double p = baseline + (2 * i * (_RankPression - 1))/denominator;
			if (i == 0) {
				cumProb.add(p);
				i++;
				continue;
			}
			cumProb.add(cumProb.get(i-1) + p);
			i++;
		}
		
		List<Integer> result = susRoulette(cumProb, _PopSize);
		Collections.shuffle(result);
		
		_Parents = new ArrayList<Individual>(_PopSize);
		for (int j = 0; j < result.size(); j++) {
			_Parents.add(popList.get(result.get(j)));
		}
		
	}
	
	private void recombination() {
		
		_Offspring = new ArrayList<Individual>(_PopSize / 2);
		
		for (int i = 0; i < _Parents.size(); i += 2) {
			
			Individual p1 = _Parents.get(i);
			Individual p2 = _Parents.get(i);
			
			if (p1._Id == p2._Id) { 
				// Apply mutation
				
			} else { 
				// Crossover
				BitSet orPs = (BitSet) p1._Cromossome.clone();
				orPs.or(p2._Cromossome);
				
				BitSet andPs = (BitSet) p1._Cromossome.clone();
				andPs.and(p2._Cromossome);
				
				BitSet xorRes = (BitSet) orPs.clone();
				xorRes.xor(andPs);
				
				int minCard = Math.min(p1._Cromossome.cardinality(), p2._Cromossome.cardinality());
				int maxCard = Math.max(p1._Cromossome.cardinality(), p2._Cromossome.cardinality());
				int sonCard = RANDOM.nextInt(minCard, maxCard + 1);
				
				int remove = sonCard - andPs.cardinality();
				List<Integer> positions = getOnePositions(xorRes);
				Collections.shuffle(positions);
				for (int j = 0; j < remove; j++) orPs.clear(positions.get(j));
				
				_Offspring.add(new Individual(++_IndIdControl, orPs));	
			}
		}
		
	}
	
	private void adaptation() {}
	
	private void mutation() {}
	
	private void selection() {}
	
	// ===== Auxiliary methods =====
	
	private List<Integer> susRoulette(List<Double> cumProb, int numSelections) {
		List<Integer> result = new ArrayList<Integer>(numSelections);
		int currSel = 0, i = 0;
		double r = RANDOM.nextDouble(1.0 / numSelections);
		while (currSel < numSelections) {
			while (r <= cumProb.get(i)) {
				result.set(currSel, i);
				r += 1.0 / numSelections; 
				currSel++;
				
				if(currSel == numSelections) break;
			}
			i++;
		}
		
		return result;
	}
	
	private List<Integer> getOnePositions(BitSet bitset) {
        List<Integer> positions = new ArrayList<>(bitset.cardinality());
        int i = bitset.nextSetBit(0);
        while (i != -1) {
            positions.add(i);
            i = bitset.nextSetBit(i + 1);
        }
        return positions;
    }
		
}
