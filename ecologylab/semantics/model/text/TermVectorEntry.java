package ecologylab.semantics.model.text;

import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.simpl_scalar;
import ecologylab.xml.ElementState.simpl_composite;

public class TermVectorEntry extends ElementState
{
	public @simpl_composite Term term;
	public @simpl_scalar double freq;
	
	public TermVectorEntry() { }
	
	public TermVectorEntry(Term term, double freq)
	{
		this.term = term;
		this.freq = freq;
	}
}
