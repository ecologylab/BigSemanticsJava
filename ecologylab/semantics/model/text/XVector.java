package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import ecologylab.generic.VectorType;

public class XVector<T> extends VectorType<T>
{

	protected HashMap<T, Double> values;
	private double norm;

	public XVector()
	{
		values = new HashMap<T, Double>(20);
	}

	public XVector(int size)
	{
		values = new HashMap<T, Double>(size);
	}

	public XVector(VectorType<T> copyMe)
	{
		values = new HashMap<T, Double>(copyMe.map());
	}

	public XVector<T> copy()
	{
		return new XVector<T>(this);
	}

	public double get(T term)
	{
		Double d = values.get(term);
		if (d == null)
			return 0;
		return d;
	}

	public void add(T term, double val)
	{
		synchronized(values) {
			if (values.containsKey(term))
				val += values.get(term);
			values.put(term, val);
			resetNorm();
		}
	}

	public void set(T term, double val)
	{
		synchronized(values)
		{
			values.put(term, val);
			resetNorm();
		}
	}

	/**
	 * Pairwise multiplies this Vector by another Vector, in-place.
	 * 
	 * @param v
	 *            Vector by which to multiply
	 */
	public void multiply(VectorType<T> v)
	{
		HashMap<T,Double> other = v.map();
		if (other == null)
			return;
		synchronized(values) {
			this.values.keySet().retainAll(other.keySet());
			for (T term : this.values.keySet())
				this.values.put(term, other.get(term) * this.values.get(term));
		}
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
		synchronized(values) {
			for (T term : this.values.keySet())
				this.values.put(term, c * this.values.get(term));
		}
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
		HashMap<T,Double> other = v.map();
		if (other == null)
			return;
		synchronized(other) {
			synchronized(values) {
				for (T term : other.keySet())
					if (this.values.containsKey(term))
						this.values.put(term, c * other.get(term) + this.values.get(term));
					else
						this.values.put(term, c * other.get(term));
			}
		}
		resetNorm();
	}

	/**
	 * Adds another Vector to this Vector, in-place.
	 * @param v Vector to add to this
	 */	
	public void add(VectorType<T> v)
	{
		add(1,v);
	}

	/**
	 * Calculates the dot product of this Vector with another Vector
	 * @param v Vector to dot this Vector with.
	 */	
	public double dot(VectorType<T> v)
	{
		HashMap<T,Double> other = v.map();
		if (other == null || v.norm() == 0 || this.norm() == 0)
			return 0;

		double dot = 0;
		int num = 0;
		HashMap<T,Double> vector = this.values;
		synchronized(values) {
			for (T term : vector.keySet())
				if (other.containsKey(term)) {
					dot += other.get(term) * vector.get(term);
					num++;
				}
			if (num != 0)
				dot /= num;
		}
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

	public HashMap<T, Double> map()
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

	@Override
	public void addObserver(Observer o) {}

	@Override
	public void deleteObserver(Observer o) {}

	@Override
	public double idfDot(VectorType<T> v)
	{
		return dot(v);
	}
}
