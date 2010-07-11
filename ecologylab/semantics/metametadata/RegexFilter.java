package ecologylab.semantics.metametadata;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.Hint;

public class RegexFilter extends ElementState
{
	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected String	regex;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected String	replace;
}
