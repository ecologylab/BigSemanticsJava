package ecologylab.semantics.actions;

import java.util.ArrayList;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("and")
public class AndFlagCheck extends FlagCheckBase
{

	@simpl_collection
	@simpl_classes(
	{ FlagCheck.class, OrFlagCheck.class, AndFlagCheck.class, NotFlagCheck.class })
	@simpl_nowrap
	private ArrayList<FlagCheckBase>	checks;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		boolean flag = true;
		if (checks != null)
		{
			for (FlagCheckBase check : checks)
			{
				flag = flag && check.evaluate(handler);
				if (!flag)
					break;
			}
		}
		return flag;
	}

	@Test
	public void testDeserialization() throws SIMPLTranslationException
	{
		String xml = "<and><or><and /><or /></or><flag_check /></and>";
		AndFlagCheck and = (AndFlagCheck) MetaMetadataTranslationScope.get().deserializeCharSequence(
				xml);
		System.out.println(and);
	}

}
