package ecologylab.bigsemantics.model.text;

import ecologylab.generic.IFeatureVector;
import ecologylab.generic.ScaledValueObserver;
import ecologylab.serialization.ObservableElementState;

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
	public void update(ObservableElementState o, Object arg)
	{
		if (arg instanceof Short)
		{
			short newValue = ((Short) arg).shortValue();	// the value from the slider!
			short magnitude = (short) (newValue - getScaledValue());
			InterestModel.expressInterest(this, magnitude);
		}
		
	}

}
