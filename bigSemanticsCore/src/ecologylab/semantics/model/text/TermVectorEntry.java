package ecologylab.semantics.model.text;

import ecologylab.serialization.ElementStateOrmBase;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class TermVectorEntry extends ElementStateOrmBase
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
