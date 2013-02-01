package ecologylab.bigsemantics.actions;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.formatenums.StringFormat;

@simpl_inherit
@simpl_tag("not")
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
		NotCondition not = (NotCondition) MetaMetadataTranslationScope.get().deserialize(xml,
				StringFormat.XML);
		System.out.println(not);
		System.out.println(not.check);
		System.out.println(SimplTypesScope.serialize(not, StringFormat.XML));
	}

}
