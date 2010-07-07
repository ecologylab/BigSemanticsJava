package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.Map.Entry;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.IFeatureVector;
import ecologylab.semantics.connectors.CFPrefNames;
import ecologylab.semantics.seeding.SemanticsPrefs;
import ecologylab.serialization.ElementState;

public class InterestModel implements SemanticsPrefs
{
	
	public static class InterestModelState extends ElementState 
	{
		protected @simpl_map("interest_model_entry") HashMap<String, InterestModelEntry>	values = new HashMap<String, InterestModelEntry>();
		
		public InterestModelState()
		{
		}
		
		public static InterestModelState build()
		{
			InterestModelState result	= new InterestModelState();
			result.enterValues();
			return result;
		}
		private void enterValues()
		{
			synchronized(participantInterest)
			{
				for (Entry<Term,Double> e : participantInterest.map().entrySet())
				{
					InterestModelEntry	modelEntry	= new InterestModelEntry(e);
					values.put(modelEntry.getKey(), modelEntry);					
				}
			}
		}
		
		public void loadInterestModelFromState()
		{
			TermVector tv = new TermVector();
			for (Entry<String,InterestModelEntry> e : values.entrySet())
			{
				Term term = TermDictionary.getTermForWord(e.getKey());
				double val = e.getValue().getValue();
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
		double max			= interestingTermVector.max();
		double factor		= max * magnitude < magnitude ? (double) magnitude : ((double) magnitude) / max;
		participantInterest.add(factor, interestingTermVector);
		unitize();
	}

	public static void expressInterest ( Term term, short magnitude )
	{
		magnitude /= 2;
		timeScaleInterest();
		participantInterest.add(term, new Double(magnitude));
		unitize();
	}

	private static void timeScaleInterest ( )
	{
		if (INTEREST_DECAY_PREF.value())
		{
			long delta_t = System.nanoTime() - timestamp;
			double delta_t_in_seconds = delta_t / 1e9;
			participantInterest.multiply(Math.exp(-delta_t_in_seconds / INTEREST_TIME_CONSTANT));
		}
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
		participantInterest.set(term, new Double(newValue));
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
