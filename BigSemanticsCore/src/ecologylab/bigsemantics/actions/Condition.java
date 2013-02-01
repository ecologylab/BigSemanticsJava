package ecologylab.bigsemantics.actions;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;

@simpl_inherit
public class Condition extends ElementState
{
	public boolean evaluate(SemanticActionHandler handler)
	{
		warning("Condition.evaluate() gets called: this should never happen.");
		return false;
	}
}
