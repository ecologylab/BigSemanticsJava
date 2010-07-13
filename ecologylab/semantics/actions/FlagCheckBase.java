package ecologylab.semantics.actions;

import ecologylab.serialization.ElementState;

public class FlagCheckBase extends ElementState
{
	public boolean evaluate(SemanticActionHandler handler)
	{
		warning("FlagCheckBase.evaluate() gets called: this should never happen.");
		return false;
	}
}
