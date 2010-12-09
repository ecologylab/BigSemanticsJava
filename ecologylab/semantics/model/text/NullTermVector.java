package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Observer;
import java.util.Set;

import ecologylab.generic.IFeatureVector;


/**
 * A TermVector that has no content.  We use these as placeholders for metadata
 * elements which do not inherently have a TermVector associated with them, such
 * as a MetadataParsedURL
 * @author jmole
 *
 */
public class NullTermVector implements ITermVector
{

	public static NullTermVector	ntv	= new NullTermVector();

	private static final HashSet<Term> HASH_SET = new HashSet<Term>();
	
	public static NullTermVector singleton()
	{
		return ntv;
	}

	public void addObserver(Observer o)
	{
		// TODO Auto-generated method stub

	}

	public void deleteObserver(Observer o)
	{
		// TODO Auto-generated method stub

	}

	public double dot(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public Set<Term> elements()
	{
		// TODO Auto-generated method stub
		return HASH_SET;
	}

	public double get(Term term)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	//FIXME -- this is wasteful crap code -- sez andruid
	public HashMap<Term, Double> map()
	{
		// TODO Auto-generated method stub
		return null;
	}

	//FIXME -- this is wasteful crap code -- sez andruid
	public Set<Double> values()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public double norm()
	{
		return 0;
	}

	public double idfDot(IFeatureVector<Term> v)
	{
		return 0;
	}

	public String toString()
	{
		return "NullTV";
	}

	public IFeatureVector<Term> unit()
	{
		return this;
	}
	
	public IFeatureVector<Term> simplex()
	{
		return this;
	}

	public int commonDimensions(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 1;
	}

	public double dotSimplex(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public double idfDotSimplex(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public double max ( )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void recycle()
	{
		// TODO Auto-generated method stub
		
	}
	
	public boolean hasObservers()
	{
		return true;
	}

	public void add(double c, ITermVector v)
	{
		// TODO Auto-generated method stub
		
	}

	public void add(ITermVector v)
	{
		// TODO Auto-generated method stub
		
	}
}
