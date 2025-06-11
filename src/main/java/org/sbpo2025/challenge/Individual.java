package org.sbpo2025.challenge;

import java.util.BitSet;
import java.util.Objects;
import java.util.Random;

public class Individual implements Comparable<Individual> {

	private int _Id;
	private BitSet _Cromossome;
	private double _Fitness;
	private boolean _Parthenogenesis;
	
	// Temporary
	private static final long SEED = 1L;
    private static final Random RANDOM = new Random(SEED);
	
	public Individual(int id, BitSet cromossome, boolean parthenogenesis) {
		_Id = id;
		_Cromossome = (BitSet) cromossome.clone();
		_Parthenogenesis = parthenogenesis;
		_Fitness = 0.0;
	}
	
	public int getId() {
		return _Id;
	}
	
	public BitSet getCromossome() {
		return (BitSet) _Cromossome.clone();
	}
	
	public void setCromossome(BitSet cromossome) {
		_Cromossome = (BitSet) cromossome.clone();
	}
	
	public void setFitness(double fitness) {
		_Fitness = fitness;
	}
	
	public double getFitness() {
		return _Fitness;
	}
	
	public boolean getParthenogenesis() {
		return _Parthenogenesis;
	}
	
	public void print() {
		System.out.println("(" +_Id + "): " + _Cromossome);
	}
	
	@Override
	public int compareTo(Individual other) {
	    int result = Double.compare(this._Fitness, other.getFitness());
	    return result != 0 ? result : Integer.compare(this._Id, other.getId());
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Individual)) return false;
        Individual that = (Individual) o;
        return _Id == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(_Id);
    }
}
