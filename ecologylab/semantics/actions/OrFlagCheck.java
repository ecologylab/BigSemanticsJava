package ecologylab.semantics.actions;

import java.util.ArrayList;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("or")
public class OrFlagCheck extends FlagCheckBase
{

	@simpl_collection
	@simpl_classes(
	{ FlagCheck.class, OrFlagCheck.class, AndFlagCheck.class, NotFlagCheck.class })
	@simpl_nowrap
	private ArrayList<FlagCheckBase>	checks;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		boolean flag = false;
		if (checks != null)
		{
			for (FlagCheckBase check : checks)
			{
				flag = flag || check.evaluate(handler);
				if (flag)
					break;
			}
		}
		return flag;
	}

	@Test
	public void testDeserialization() throws SIMPLTranslationException
	{
		String xml = "<or><or /><flag_check /></or>";
		OrFlagCheck or = (OrFlagCheck) MetaMetadataTranslationScope.get().deserializeCharSequence(xml);
		System.out.println(or);
	}

}
