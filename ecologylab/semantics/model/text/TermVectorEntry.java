package ecologylab.semantics.model.text;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.ElementState.simpl_composite;
import ecologylab.serialization.ElementState.simpl_scalar;

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
