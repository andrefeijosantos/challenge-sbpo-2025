package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.Objects;

public class Individual {

	int _Id;
	BitSet _Cromossome;
	double _Fitness;
	
	public Individual(int id, BitSet cromossome) {
		_Id = id;
		_Cromossome = cromossome;
		_Fitness = calcFitness();
	}
	
	private double calcFitness() {
		return 0.0;
	}
	
	public double getFitness() {
		return _Fitness;
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
