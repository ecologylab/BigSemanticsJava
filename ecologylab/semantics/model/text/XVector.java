package ecologylab.semantics.model.text;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class XVector<T> extends Observable implements VectorType<T>
{

	protected Hashtable<T, Double> values;
  private double norm;

	public XVector()
	{
		values = new Hashtable<T, Double>(20);
	}

	public XVector(int size)
	{
		values = new Hashtable<T, Double>(size);
	}

	public XVector(XVector<T> copyMe)
	{
		values = new Hashtable<T, Double>(copyMe.values);
	}

	public XVector<T> copy()
	{
		return new XVector<T>(this);
	}

	public double get(T term)
	{
		return values.get(term);
	}

	public void add(T term, double val)
	{
	  if (values.containsKey(term))
	    val += values.get(term);
		values.put(term, val);
		resetNorm();
	}

	/**
	 * Pairwise multiplies this Vector by another Vector, in-place.
	 * 
	 * @param v
	 *            Vector by which to multiply
	 */
	public void multiply(VectorType<T> v)
	{
		Hashtable<T,Double> other = v.map();
		if (other == null)
			return;
		this.values.keySet().retainAll(other.keySet());
		for (T term : this.values.keySet())
			this.values.put(term, other.get(term) * this.values.get(term));
		resetNorm();
	}

	/**
	 * Scalar multiplication of this vector by some constant
	 * 
	 * @param c
	 *            Constant to multiply this vector by.
	 */
	public void multiply(double c)
	{
		for (T term : this.values.keySet())
			this.values.put(term, c * this.values.get(term));
		resetNorm();
	}

	/**
	 * Pairwise addition of this vector by some other vector times some
	 * constant.<br>
	 * i.e. this + (c*v)<br>
	 * Vector v is not modified.
	 * 
	 * @param c
	 *            Constant which Vector v is multiplied by.
	 * @param v
	 *            Vector to add to this one
	 */
	public void add(double c, VectorType<T> v)
	{
		Hashtable<T,Double> other = v.map();
		if (other == null)
			return;
		for (T term : other.keySet())
			if (this.values.containsKey(term))
				this.values.put(term, c * other.get(term) + this.values.get(term));
			else
				this.values.put(term, c * other.get(term));
		resetNorm();
	}

	/**
	 * Adds another Vector to this Vector, in-place.
	 * @param v Vector to add to this
	 */	
	public void add(VectorType<T> v)
	{
		Hashtable<T,Double> other = v.map();
		if (other == null)
			return;
		for (T term : other.keySet())
			if (this.values.containsKey(term))
				this.values.put(term, other.get(term) + this.values.get(term));
			else
				this.values.put(term, other.get(term));
		resetNorm();
	}

	/**
	 * Calculates the dot product of this Vector with another Vector
	 * @param v Vector to dot this Vector with.
	 */	
	public double dot(VectorType<T> v)
	{
		Hashtable<T,Double> other = v.map();
		if (other == null)
			return 0;
		
		double dot = 0;
		Hashtable<T,Double> vector = this.values;
		for (T term : vector.keySet())
			if (other.containsKey(term))
				dot += other.get(term) * vector.get(term);
		dot /= this.norm() * v.norm();
		return dot;
	}

	public Set<T> elements()
	{
		return new HashSet<T>(values.keySet());
	}

	public Set<Double> values()
	{
		return new HashSet<Double>(values.values());
	}

	public Hashtable<T, Double> map()
	{
		return values;
	}

	public int size()
	{
		return values.size();
	}
	
	private void recalculateNorm()
	{
	  double norm = 0;
	  for(double d: this.values.values())
	  {
	    norm += Math.pow(d, 2);
	  }
	  this.norm = Math.sqrt(norm);
	}
	
	private void resetNorm() {
	  norm = -1;
	}
	
	public double norm()
	{
	  if (norm == -1)
	    recalculateNorm();
	  return norm;
	}
}
