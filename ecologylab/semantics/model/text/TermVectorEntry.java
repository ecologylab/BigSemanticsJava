package ecologylab.semantics.model.text;

import ecologylab.serialization.ElementState;

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
