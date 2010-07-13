package ecologylab.semantics.metametadata;

import org.junit.Test;

import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("not_null")
public class NotNullCheck extends Check
{

	@Override
	public String getCondition()
	{
		return SemanticActionsKeyWords.NOT_NULL_CHECK;
	}
	
	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<not_null name=\"foo\" />";
		NotNullCheck nn = (NotNullCheck) MetaMetadataTranslationScope.get().deserializeCharSequence(xml);
		System.out.println(nn.getCondition());
		System.out.println(nn.getName());
	}
}
