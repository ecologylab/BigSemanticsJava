package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Observer;
import java.util.Set;

import ecologylab.generic.VectorType;

public class NullTermVector extends VectorType<Term>
{

	public static NullTermVector	ntv	= new NullTermVector();

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

	public double dot(VectorType<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public Set<Term> elements()
	{
		// TODO Auto-generated method stub
		return new HashSet<Term>();
	}

	public double get(Term term)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public HashMap<Term, Double> map()
	{
		// TODO Auto-generated method stub
		return new HashMap();
	}

	public Set<Double> values()
	{
		// TODO Auto-generated method stub
		return new HashSet<Double>();
	}

	public double norm()
	{
		return 0;
	}

	@Override
	public double idfDot(VectorType<Term> v)
	{
		return 0;
	}

	public String toString()
	{
		return "NullTV";
	}

	@Override
	public VectorType<Term> unit()
	{
		return this;
	}
	
	@Override
	public VectorType<Term> simplex()
	{
		return this;
	}

	@Override
	public int commonDimensions(VectorType<Term> v)
	{
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public double dotSimplex(VectorType<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double idfDotNoTF(VectorType<Term> v)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
