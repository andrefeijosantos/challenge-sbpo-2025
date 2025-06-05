package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.Objects;
import java.util.Random;

public class Individual implements Comparable<Individual> {

	int _Id;
	BitSet _Cromossome;
	double _Fitness;
	
	// Temporary
	private static final long SEED = 1L;
    private static final Random RANDOM = new Random(SEED);
	
	public Individual(int id, BitSet cromossome) {
		_Id = id;
		_Cromossome = (BitSet) cromossome.clone();
		_Fitness = calcFitness();
	}
	
	private double calcFitness() {
		return RANDOM.nextDouble() * 100;
	}
	
	public int getId() {
		return _Id;
	}
	
	@Override
	public int compareTo(Individual other) {
	    int result = Double.compare(this._Fitness, other._Fitness);
	    return result != 0 ? result : Integer.compare(this._Id, other._Id);
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Individual)) return false;
        Individual that = (Individual) o;
        return _Id == that._Id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_Id);
    }
}
