package ecologylab.semantics.actions;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
@xml_tag("not")
public class NotCondition extends Condition
{

	@simpl_composite
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	private Condition	check;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		return !check.evaluate(handler);
	}

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<not><and><or><and /><or /></or><not_null /></and></not>";
		NotCondition not = (NotCondition) MetaMetadataTranslationScope.get().deserializeCharSequence(
				xml);
		System.out.println(not);
		System.out.println(not.check);
		System.out.println(not.serialize());
	}

}
