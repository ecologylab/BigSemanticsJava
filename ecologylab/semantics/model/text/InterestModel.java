package ecologylab.semantics.model.text;

import java.util.Map.Entry;

import ecologylab.generic.IFeatureVector;
import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.StringDoubleMap;

public class InterestModel
{
	
	public static class InterestModelState extends ElementState {
		protected @xml_nested @xml_tag("terms") StringDoubleMap	values = new StringDoubleMap();
		public InterestModelState()
		{
			synchronized(participantInterest)
			{
				for (Entry<Term,Double> e : participantInterest.map().entrySet())
					values.put(e.getKey().getWord(), e.getValue());
			}
		}
		
		public void loadInterestModelFromState()
		{
			TermVector tv = new TermVector();
			for (Entry<String,Double> e : values.entrySet())
			{
				Term term = TermDictionary.getTermForWord(e.getKey());
				double val = e.getValue();
				tv.add(term, val);
			}
			participantInterest.clear();
			participantInterest.add(tv);
		}
		
	}
	// participant interest vector

	private static TermVector	participantInterest		= new TermVector();

	private static TermVector	unitParticipantInterest	= new TermVector();

	private static long			timestamp;

	public static double		INTEREST_TIME_CONSTANT	= 120;

	public static void expressInterest ( IFeatureVector<Term> interestingTermVector, short magnitude )
	{
		timeScaleInterest();
		TermVector xtv = new TermVector(interestingTermVector);
		xtv.multiply(magnitude);
		xtv.clamp(magnitude);
		participantInterest.add(1, xtv);
		unitize();
	}

	public static void expressInterest ( Term term, short magnitude )
	{
		magnitude /= 2;
		timeScaleInterest();
		participantInterest.add(term, magnitude);
		unitize();
	}

	private static void timeScaleInterest ( )
	{
		long delta_t = System.nanoTime() - timestamp;
		double delta_t_in_seconds = delta_t / 1e9;
		participantInterest.multiply(Math.exp(-delta_t_in_seconds / INTEREST_TIME_CONSTANT));
		timestamp = System.nanoTime();
	}

	public static TermVector getPIV ( )
	{
		return participantInterest;
	}

	public static double getAbsoluteInterestOfTermVector ( IFeatureVector<Term> tv )
	{
		if (tv == null)
			return 0;
		return unitParticipantInterest.idfDotSimplex(tv);
	}

	public static short getInterestExpressedInTermVector ( IFeatureVector<Term> termVector )
	{
		if (termVector == null)
			return 0;
		double retVal = unitParticipantInterest.dotSimplex(termVector);
		retVal /= unitParticipantInterest.commonDimensions(termVector);
		retVal *= 10;
		return (short) retVal;
	}

	public static short getInterestExpressedInXTerm ( Term term )
	{
		return (short) (2 * participantInterest.get(term));
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
		unitParticipantInterest = participantInterest.unit();
	}

	public static void setTermInterest ( Term term, short newValue )
	{
		participantInterest.set(term, newValue);
		unitize();
	}

	public static void expressInterest ( String query, short i )
	{
		expressInterest(new TermVector(query), i);
	}

	public static boolean interestIsExpressed()
	{
		return participantInterest.size() > 0;
	}

}
