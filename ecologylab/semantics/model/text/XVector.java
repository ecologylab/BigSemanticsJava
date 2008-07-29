package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class XVector<T> extends Observable implements VectorType<T> {
	
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
	
	public XVector<T> copy() {
		return new XVector<T>(this);
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
	public void multiply(VectorType<T> v) {
		HashMap<T,Double> other = v.map();
		this.values.keySet().retainAll(other.keySet());
		for (T term : this.values.keySet())
			this.values.put(term, other.get(term) * this.values.get(term));
	}
	
	/**
	 * Scalar multiplication of this vector by some constant
	 * @param c Constant to multiply this vector by.
	 */
	public void multiply(double c) {
		for (T term : this.values.keySet())
			this.values.put(term, c * this.values.get(term));
	}
	
	/**
	 * Pairwise addition of this vector by some other vector times some constant.<br>
	 * i.e. this + (c*v)<br>
	 * Vector v is not modified.
	 * @param c Constant which Vector v is multiplied by.
	 * @param v Vector to add to this one
	 */
	public void add(double c, VectorType<T> v) {
		HashMap<T,Double> other = v.map();
		for (T term : other.keySet())
			if (this.values.containsKey(term))
				this.values.put(term, c*other.get(term) + this.values.get(term));
			else
				this.values.put(term, c*other.get(term));
	}

	/**
	 * Adds another Vector to this Vector, in-place.
	 * @param v Vector to add to this
	 */	
	public void add(VectorType<T> v) {
		HashMap<T,Double> other = v.map();
		for (T term : other.keySet())
			if (this.values.containsKey(term))
				this.values.put(term, other.get(term) + this.values.get(term));
			else
				this.values.put(term, other.get(term));
	}
	
	/**
	 * Calculates the dot product of this Vector with another Vector
	 * @param v Vector to dot this Vector with.
	 */	
	public double dot(VectorType<T> v) {
		HashMap<T,Double> other = v.map();
		double dot = 0;
		for (T term : this.values.keySet())
			if (other.containsKey(term))
				dot += other.get(term) * this.values.get(term);
		return dot;
	}
	
	public Set<T> elements() {
		return new HashSet<T>(values.keySet());
	}
	
	public Set<Double> values() {
		return new HashSet<Double>(values.values());
	}

	public HashMap<T, Double> map() {
		return values;
	}
	
	public int size() {
		return values.size();
	}
}
