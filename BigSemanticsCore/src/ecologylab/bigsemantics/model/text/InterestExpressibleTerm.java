package ecologylab.bigsemantics.model.text;

import ecologylab.generic.ScaledValueObserver;
import ecologylab.serialization.ObservableElementState;

public class InterestExpressibleTerm implements ScaledValueObserver
{
	private Term term;
	
	public InterestExpressibleTerm(String s)
	{
		term = TermDictionary.getTermForUnsafeWord(s);
	}
	
	public InterestExpressibleTerm(Term t)
	{
		term = t;
	}

	public short getScaledValue()
	{
		return InterestModel.getInterestExpressedInXTerm(term);
	}

	public void update(ObservableElementState o, Object arg)
	{
		if (arg instanceof Short)
		{
			short newValue = ((Short) arg).shortValue();	// the value from the slider!
			short magnitude = (short) (newValue - getScaledValue());
			InterestModel.expressInterest(term, magnitude);
			//InterestModel.setTermInterest(term,newValue);
		}

	}

}
