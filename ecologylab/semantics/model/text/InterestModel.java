package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.Hashtable;

import ecologylab.collections.VectorSetElement;
import ecologylab.collections.VectorWeightStrategy;
import ecologylab.collections.WeightSet;
import ecologylab.collections.WeightingStrategy;
import ecologylab.generic.VectorType;
import ecologylab.semantics.model.text.XCompositeTermVector;
import ecologylab.semantics.model.text.XTerm;
import ecologylab.semantics.model.text.XTermVector;

public class InterestModel
{
	// participant interest vector

	private static XVector<XTerm>				participantInterest	= new XTermVector();
	private static HashMap<XTerm, Short>	termInterest = new HashMap<XTerm, Short>();

	public static void expressInterest(VectorType<XTerm> interestingTermVector, short magnitude)
	{
		
		participantInterest.add(magnitude, interestingTermVector);
//		HashMap<XTerm, Double> values = interestingTermVector.map();
//		synchronized(values)
//		{
//			for (XTerm term : values.keySet())
//			{
//				if ( termInterest.containsKey(term) )
//					values.remove(term);
//			}
//		}
//		participantInterest.add(magnitude, interestingTermVector);
		fixInterest();
	}

	public static void expressInterest(XTerm term, short magnitude)
	{
		magnitude /= 2;
//		participantInterest.add(term, magnitude);
		participantInterest.set(term, magnitude);
//		termInterest.put(term, magnitude);
		fixInterest();
	}
	
	private static void fixInterest() {
		participantInterest.clamp(5);
	}

	public static XVector<XTerm> getPIV()
	{
		return participantInterest;
	}

	
	public static short dotShort(VectorType<XTerm> termVector)
	{
		short d = (short) termVector.dot(participantInterest);
		if (d>5)  d=5;
		if (d<-5) d=-5;
		return d;
	}
	
	public static double dot(VectorType<XTerm> tv)
	{
		return tv.idfDot(participantInterest);
	}

	public static double getTermInterest(XTerm term)
	{
		return participantInterest.get(term);
	}

	public static short getTermInterestShort(XTerm term)
	{
		short d = (short) getTermInterest(term);
		if (d>5)  d=5;
		if (d<-5) d=-5;
		return d;
	}

}
