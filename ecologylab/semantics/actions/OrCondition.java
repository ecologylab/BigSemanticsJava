package ecologylab.semantics.actions;

import java.util.ArrayList;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("or")
public class OrCondition extends Condition
{

	@simpl_collection
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	@simpl_nowrap
	private ArrayList<Condition>	checks;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		boolean flag = false;
		if (checks != null)
		{
			for (Condition check : checks)
			{
				flag = flag || check.evaluate(handler);
				if (flag)
					break;
			}
		}
		return flag;
	}

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<or><or /><not_null /></or>";
		OrCondition or = (OrCondition) MetaMetadataTranslationScope.get().deserializeCharSequence(xml);
		System.out.println(or);
		System.out.println(or.checks);
		System.out.println(or.serialize());
	}

}
