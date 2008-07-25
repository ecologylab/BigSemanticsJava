package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class XVector<T> {
	
	protected HashMap<T, Double> values;
	
	public XVector() {
		values = new HashMap<T, Double>(20);
	}
	
	public XVector(int size) {
		values = new HashMap<T, Double>(size);
	}
	
	@SuppressWarnings("unchecked")
	public XVector(XVector<T> copyMe) {
		values = new HashMap<T,Double>(copyMe.values);
	}
	
	public double get(T term) {
		return values.get(term);
	}
	
	public void add(T term, double val) {
		values.put(term, val);
	}
	
	/**
	 * Pairwise multiplies this Vector by another Vector, in-place.
	 * @param v Vector by which to multiply
	 */
	public void multiply(XVector<T> v) {
		this.values.keySet().retainAll(v.values.keySet());
		for (T term : this.values.keySet())
			this.values.put(term, v.values.get(term) * this.values.get(term));
	}

	/**
	 * Adds another Vector to this Vector, in-place.
	 * @param v Vector to add to this
	 */	
	public void add(XVector<T> v) {
		for (T term : v.values.keySet())
			if (this.values.containsKey(term))
				this.values.put(term, v.values.get(term) + this.values.get(term));
			else
				this.values.put(term, v.values.get(term));
	}
	
	/**
	 * Calculates the dot product of this Vector with another Vector
	 * @param v Vector to dot this Vector with.
	 */	
	public double dot(XVector<T> v) {
		double dot = 0;
		for (T term : this.values.keySet())
			if (v.values.containsKey(term))
				dot += v.values.get(term) * this.values.get(term);
		return dot;
	}
	
	public Set<T> elements() {
		return new HashSet<T>(values.keySet());
	}
	
	public Set<Double> values() {
		return new HashSet<Double>(values.values());
	}
}
