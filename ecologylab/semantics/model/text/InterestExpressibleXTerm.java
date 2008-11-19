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
		return InterestModel.getTermInterestShort(term);
	}

	public void update(Observable o, Object arg)
	{
		if (arg instanceof Short)
		{
			short intensity = ((Short) arg).shortValue();
			InterestModel.expressInterest(term, intensity);
		}

	}

}
