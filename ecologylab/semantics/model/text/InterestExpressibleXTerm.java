package ecologylab.semantics.model.text;

import java.util.Observable;

import ecologylab.gui.ScaledValueObserver;

public class InterestExpressibleXTerm implements ScaledValueObserver
{
	private XTerm term;
	
	public InterestExpressibleXTerm(String s)
	{
		term = XTermDictionary.getTermForWord(s);
	}
	
	public InterestExpressibleXTerm(XTerm t)
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
		}

	}

}
