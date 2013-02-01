package ecologylab.bigsemantics.model.text;

import java.util.Observer;

import ecologylab.generic.IFeatureVector;

public interface ITermVector extends IFeatureVector<Term>
{
	/**
	 * The idf-weighted dot product of this vector with the passed in vector. <br /><br />
	 * 
	 *   Sums the product this[term] * v[term] * term.idf for each term common to both vectors. 
	 * @param v the vector to take the IDF dot product with
	 * @return IDF weighted dot product
	 */
	public double idfDot(IFeatureVector<Term> v);
	
	/**
	 * The idf-weighted dot product of this and the passed in vector's simplex.<br/><br/>
	 * 
	 * This method is equivalent to idfDot(v.simplex()) but more efficient.
	 * @param v the vector to take the simplex IDF dot product with.
	 * @return simplex IDF weighted dot product
	 */
	public double idfDotSimplex(IFeatureVector<Term> v);
	
	public void addObserver(Observer observer);
	
	public void deleteObserver(Observer observer);
	
	public void recycle();

	public boolean hasObservers();

	/**
	 * Pairwise addition of this vector by some other vector times some constant.<br>
	 * i.e. this + (c*v)<br>
	 * Vector v is not modified.
	 * 
	 * @param c
	 *            Constant which Vector v is multiplied by.
	 * @param v
	 *            Vector to add to this one
	 */
	public void add ( double c, ITermVector v );
	
	/**
	 * Adds another Vector to this Vector, in-place.
	 * 
	 * @param v
	 *            Vector to add to this
	 */
	public void add (ITermVector v );

}
