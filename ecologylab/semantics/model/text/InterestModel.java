package ecologylab.semantics.model.text;

import java.util.HashMap;

import ecologylab.generic.VectorType;
import ecologylab.semantics.model.text.XTerm;
import ecologylab.semantics.model.text.XTermVector;

public class InterestModel
{
	// participant interest vector

	private static XTermVector	participantInterest		= new XTermVector();

	private static XTermVector	unitParticipantInterest	= new XTermVector();

	private static long			timestamp;

	public static double		INTEREST_TIME_CONSTANT	= 120;

	private static Object		PI_LOCK					= "";

	public static void expressInterest ( VectorType<XTerm> interestingTermVector, short magnitude )
	{
		 timeScaleInterest();
		XTermVector xtv = new XTermVector(interestingTermVector);
		xtv.multiply(magnitude);
		xtv.clamp(magnitude);
		participantInterest.add(1, xtv);
		unitize();

		// HashMap<XTerm, Double> values = interestingTermVector.map();
		// synchronized(values)
		// {
		// for (XTerm term : values.keySet())
		// {
		// if ( termInterest.containsKey(term) )
		// values.remove(term);
		// }
		// }
		// participantInterest.add(magnitude, interestingTermVector);
	}

	public static void expressInterest ( XTerm term, short magnitude )
	{
		magnitude /= 2;
		 timeScaleInterest();
		participantInterest.add(term, magnitude);
		unitize();
		// participantInterest.add(term, magnitude);
		// termInterest.put(term, magnitude);
	}

	private static void timeScaleInterest ( )
	{
		long delta_t = System.nanoTime() - timestamp;
		double delta_t_in_seconds = delta_t / 1e9;
		participantInterest.multiply(Math.exp(-delta_t_in_seconds/INTEREST_TIME_CONSTANT));
		timestamp = System.nanoTime();
	}

	public static XVector<XTerm> getPIV ( )
	{
		return participantInterest;
	}

	public static double getAbsoluteInterestOfTermVector ( VectorType<XTerm> tv )
	{
		return tv.idfDot(unitParticipantInterest);
	}

	public static short getInterestExpressedInTermVector ( VectorType<XTerm> termVector )
	{
		return (short) (10 * unitParticipantInterest.dotSimplex(termVector));
	}

	public static short getInterestExpressedInXTerm ( XTerm term )
	{
		return (short) ( 2*participantInterest.get(term));
	}

	public static void expressInterest ( InterestExpressibleElement element, short magnitude )
	{
		expressInterest(element.getInterestExpressionTermVector(), magnitude);
	}

	public static short getInterestExpressedInElement ( InterestExpressibleElement element )
	{
		return getInterestExpressedInTermVector(element.getInterestExpressionTermVector());
	}

	private static void unitize ( )
	{
		synchronized (PI_LOCK)
		{
			unitParticipantInterest = participantInterest.unit();
		}
	}

	public static void setTermInterest ( XTerm term, short newValue )
	{
		participantInterest.set(term, newValue);
		unitize();
	}

	public static void expressInterest ( String query, short i )
	{
		expressInterest(new XTermVector(query), i);
		
	}

}
