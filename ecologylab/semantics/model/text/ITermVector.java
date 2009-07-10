package ecologylab.semantics.model.text;

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
}
