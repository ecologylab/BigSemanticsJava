package ecologylab.bigsemantics.actions;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestNotCondition
{

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
