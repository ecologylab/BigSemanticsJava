package ecologylab.bigsemantics.actions;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestChooseSemanticAction
{

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<choose><case><not_null /><get_field /><for_each /></case><case><not_null /><set_metadata /></case><otherwise><get_field /></otherwise></choose>";
		ChooseSemanticAction choose = (ChooseSemanticAction) MetaMetadataTranslationScope.get()
				.deserialize(xml, StringFormat.XML);
		System.out.println(choose);
		System.out.println(choose.cases);
		System.out.println(choose.otherwise);
		System.out.println(SimplTypesScope.serialize(choose, StringFormat.XML));
	}

}
