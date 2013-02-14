package ecologylab.bigsemantics.actions;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestOrCondition
{

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<or><or /><not_null /></or>";
		OrCondition or = (OrCondition) MetaMetadataTranslationScope.get().deserialize(xml, StringFormat.XML);
		System.out.println(or);
		System.out.println(or.checks);
		System.out.println(SimplTypesScope.serialize(or, StringFormat.XML));
	}

}
