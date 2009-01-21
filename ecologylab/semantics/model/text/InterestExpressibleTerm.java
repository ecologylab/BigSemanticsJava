package ecologylab.semantics.model.text;

import java.util.Observable;

import ecologylab.gui.ScaledValueObserver;

public class InterestExpressibleTerm implements ScaledValueObserver
{
	private Term term;
	
	public InterestExpressibleTerm(String s)
	{
		term = TermDictionary.getTermForUnsafeWord(s);
		term.setWord(s);
	}
	
	public InterestExpressibleTerm(Term t)
	{
		term = t;
	}

	public short getScaledValue()
	{
		return InterestModel.getInterestExpressedInXTerm(term);
	}

	public void update(Observable o, Object arg)
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
