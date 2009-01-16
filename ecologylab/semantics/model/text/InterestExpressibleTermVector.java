package ecologylab.semantics.model.text;

import java.util.Hashtable;
import java.util.Observable;
import java.util.Set;



import ecologylab.generic.VectorType;
import ecologylab.gui.ScaledValueObserver;
import ecologylab.semantics.model.text.CompositeTermVector;
import ecologylab.semantics.model.text.Term;

public class InterestExpressibleTermVector extends TermVector implements ScaledValueObserver
{	

	public InterestExpressibleTermVector(VectorType<Term> tv) {
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
