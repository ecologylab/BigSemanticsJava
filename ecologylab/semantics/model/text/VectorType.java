package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.Set;

public interface VectorType<T> {

	public double get(T term);

	/**
	 * Calculates the dot product of this Vector with another Vector
	 * @param v Vector to dot this Vector with.
	 */
	public double dot(VectorType<T> v);

	public Set<T> elements();

	public Set<Double> values();
	
	public HashMap<T,Double> map();

}