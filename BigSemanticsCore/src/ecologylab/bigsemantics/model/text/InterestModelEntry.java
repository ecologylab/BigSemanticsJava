package ecologylab.bigsemantics.model.text;

import java.util.Map.Entry;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

public class InterestModelEntry extends ElementState
implements IMappable<String>
{
	@simpl_scalar	protected String	string;
	@simpl_scalar	protected double 	value;
	
	public InterestModelEntry()
	{
	}
	
	public InterestModelEntry(Entry<Term,Double> entry)
	{
		this.string	= entry.getKey().getWord();
		this.value	= entry.getValue();
	}
	
	public String getKey()
	{
		return string;
	}

	public Double getValue()
	{
		return value;
	}

	public String key()
	{
		return string;
	}
	
	
}
