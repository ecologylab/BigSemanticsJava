package ecologylab.semantics.model.text;

import java.util.Observable;

import ecologylab.generic.IFeatureVector;
import ecologylab.gui.ScaledValueObserver;

public class InterestExpressibleTermVector extends TermVector implements ScaledValueObserver
{	

	public InterestExpressibleTermVector(IFeatureVector<Term> tv) {
		super(tv);
	}
	
	public short getScaledValue()
	{
		// TODO Auto-generated method stub
		return (short)InterestModel.getInterestExpressedInTermVector(this);
	}
	public void update(Observable o, Object arg)
	{
		if (arg instanceof Short)
		{
			short newValue = ((Short) arg).shortValue();	// the value from the slider!
			short magnitude = (short) (newValue - getScaledValue());
			InterestModel.expressInterest(this, magnitude);
		}
		
	}

}
