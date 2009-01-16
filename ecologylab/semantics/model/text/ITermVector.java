package ecologylab.semantics.model.text;

import java.util.Observer;

import ecologylab.generic.IFeatureVector;

public interface ITermVector extends IFeatureVector<Term>
{
	public double idfDot(IFeatureVector<Term> v);
	
	public double idfDotNoTF(IFeatureVector<Term> v);
	
	public void addObserver(Observer observer);
	
	public void deleteObserver(Observer observer);
	
}
