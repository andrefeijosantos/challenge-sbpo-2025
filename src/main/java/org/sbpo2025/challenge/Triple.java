package org.sbpo2025.challenge;

import org.apache.commons.lang3.tuple.Pair;

public class Triple {
	public int i, j, k;
	
	public Triple(int _i, int _j, int _k) {
		this.i = _i;
		this.j = _j;
		this.k = _k;
	}
	
	public static Triple of(int _i, int _j, int _k) {
		return new Triple(_i, _j, _k);
	}
	
    @Override
    public int hashCode() {
        return Pair.of(Pair.of(i, j), Pair.of(j, k)).hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	Triple other = (Triple) obj;
        return other.i == this.i && other.j == this.j && other.k == this.k;
    }
    
    @Override
    public String toString() {
        return "(" + i + ", " + j + ", " + k + ")";
    }
}
