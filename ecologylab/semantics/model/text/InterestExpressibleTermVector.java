package ecologylab.semantics.model.text;

import java.util.Hashtable;
import java.util.Observable;
import java.util.Set;



import ecologylab.generic.VectorType;
import ecologylab.gui.ScaledValueObserver;
import ecologylab.semantics.model.text.XCompositeTermVector;
import ecologylab.semantics.model.text.XTerm;

public class InterestExpressibleTermVector extends XTermVector implements ScaledValueObserver
{	

	public InterestExpressibleTermVector(VectorType<XTerm> tv) {
		super(tv);
	}
	
	public short getScaledValue()
	{
		// TODO Auto-generated method stub
		return (short)InterestModel.dotShort(this);
	}
	public void update(Observable o, Object arg)
	{
		if (arg instanceof Short)
		{
			short intensity = ((Short) arg).shortValue();
			InterestModel.expressInterest(this, intensity);
		}
		
	}

}
