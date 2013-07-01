package ecologylab.bigsemantics.actions;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestAndCondition
{

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<and><or><and /><or /></or><not_null /></and>";
		AndCondition and = (AndCondition) MetaMetadataTranslationScope.get().deserialize(
				xml, StringFormat.XML);
		System.out.println(and);
		System.out.println(and.checks);
		System.out.println(SimplTypesScope.serialize(and, StringFormat.XML));
	}

}
