package org.sbpo2025.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
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
	int _PopSize = 1000;
	int _NumGenerations = 1;
	double _MutationRate = 0.2;
	double _RankPression = 1.5; // (1, 2]
	double _EliteRate = 0.5;
	
	// Random numbers settings
	private static final long SEED = 1L;
    private static final Random RANDOM = new Random(SEED);
	
	int _MinAisles, _MaxAisles;
	
	Set<Individual> _Population;
	List<Individual> _Parents, _Offspring,  _Mutated;
	int _IndIdControl = 0;
	
	public GeneticAlgorithm(Instance inst, StopWatch stopWatch, long time_limit, int minAisles, int maxAisles) {
		super(inst, stopWatch, time_limit);
		_MinAisles = minAisles;
		_MaxAisles = maxAisles;
		_Population = new TreeSet<>();
	}
	
	public void optimize() {
		
		generateInitialPop();
	
		for (int gen = 1; gen <= _NumGenerations; gen++) {
			matching();
			recombination();
			mutation();
			adaptation();
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
			
			
			Individual ind_ = new Individual(++_IndIdControl, cromossome, false);
			_Population.add(ind_);
		}
		
	}
	
	private void matching() {
		
		_Parents = new ArrayList<Individual>(_PopSize);
		
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
		Collections.shuffle(result, RANDOM);
		
		for (int j = 0; j < result.size(); j++) {
			_Parents.add(popList.get(result.get(j)));
		}
		
	}
	
	private void recombination() {
		
		_Offspring = new ArrayList<Individual>(_PopSize / 2);
		
		for (int i = 0; i < _Parents.size(); i += 2) {
			
			Individual p1 = _Parents.get(i);
			Individual p2 = _Parents.get(i+1);
			
			BitSet crom1 = p1.getCromossome();
			BitSet crom2 = p2.getCromossome();
			
			if (p1.getId() == p2.getId()) {
				// Mutation will be applied
				_Offspring.add(new Individual(++_IndIdControl, p1.getCromossome(), true));
				
			} else { 
				// Crossover
				BitSet orPs = (BitSet) crom1.clone();
				orPs.or(crom2);
				
				BitSet andPs = (BitSet) crom1.clone();
				andPs.and(crom2);
				
				BitSet xorRes = (BitSet) orPs.clone();
				xorRes.xor(andPs);
				
				int minCard = Math.min(crom1.cardinality(), crom2.cardinality());
				int maxCard = Math.max(crom1.cardinality(), crom2.cardinality());
				int sonCard = RANDOM.nextInt(minCard, maxCard + 1);
				
				int remove = xorRes.cardinality() - (sonCard - andPs.cardinality());
				List<Integer> positions = getOnePositions(xorRes);
				Collections.shuffle(positions, RANDOM);
				for (int j = 0; j < remove; j++) orPs.clear(positions.get(j));
				
				_Offspring.add(new Individual(++_IndIdControl, orPs, false));
				
				if (sonCard != orPs.cardinality()) System.out.println("ERRO NA RECOMBINACAO");
			}
		}
		
	}
	
	private void mutation() {
		
		_Mutated = new ArrayList<Individual>((int) Math.ceil((_MutationRate + 0.2) * _Offspring.size()));
		
		for (int i = 0; i < _Offspring.size(); i++) {
			if (_Offspring.get(i).getPathenogenesis() || RANDOM.nextDouble() <= _MutationRate) {
				BitSet crom = _Offspring.get(i).getCromossome();
				mutateCromossome(crom);
				_Mutated.add(new Individual(++_IndIdControl, crom, false));
			}
		}	
	}
	
	private void mutateCromossome(BitSet cromossome) {
		List<Integer> onePos = getOnePositions(cromossome);
		List<Integer> zeroPos = getZeroPositions(cromossome);
		
		if (onePos.size() > 0 && zeroPos.size() > 0) {
			int randOne = RANDOM.nextInt(0, onePos.size());
			int randZero = RANDOM.nextInt(0, zeroPos.size());
			
			cromossome.clear(onePos.get(randOne));
			cromossome.set(zeroPos.get(randZero));
		}
	}
	
	private void adaptation() {}
	
	private void selection() {}
	
	// ===== Auxiliary methods =====
	
	private List<Integer> susRoulette(List<Double> cumProb, int numSelections) {
		List<Integer> result = new ArrayList<Integer>(Collections.nCopies(numSelections, null));
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
	
	private List<Integer> getZeroPositions(BitSet bitset) {
	    List<Integer> positions = new ArrayList<>(inst.aisles.size());
	    for (int i = 0; i < inst.aisles.size(); i++) {
	        if (!bitset.get(i)) {
	            positions.add(i);
	        }
	    }
	    return positions;
	}
		
}
