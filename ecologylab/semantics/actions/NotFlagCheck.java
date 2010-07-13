package ecologylab.semantics.actions;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("not")
public class NotFlagCheck extends FlagCheckBase
{

	@simpl_composite
	@simpl_classes(
	{ FlagCheck.class, OrFlagCheck.class, AndFlagCheck.class, NotFlagCheck.class })
	private FlagCheckBase	check;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		return !check.evaluate(handler);
	}

	@Test
	public void testDeserialization() throws SIMPLTranslationException
	{
		String xml = "<not><and><or><and /><or /></or><flag_check /></and></not>";
		NotFlagCheck not = (NotFlagCheck) MetaMetadataTranslationScope.get().deserializeCharSequence(
				xml);
		System.out.println(not);
	}

}
