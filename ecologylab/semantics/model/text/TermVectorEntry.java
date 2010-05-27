package ecologylab.semantics.model.text;

import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_nested;

public class TermVectorEntry extends ElementState
{
	public @xml_nested Term term;
	public @xml_attribute double freq;
	
	public TermVectorEntry() { }
	
	public TermVectorEntry(Term term, double freq)
	{
		this.term = term;
		this.freq = freq;
	}
}
