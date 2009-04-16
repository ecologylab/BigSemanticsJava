package ecologylab.semantics.model.text;

import java.util.Map.Entry;

import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.types.scalar.ScalarType;

public class InterestModelEntry extends ElementState
implements Mappable<String>
{
	@xml_attribute	protected String	string;
	@xml_attribute	protected double 	value;
	
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
