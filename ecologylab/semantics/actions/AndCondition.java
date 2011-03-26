package ecologylab.semantics.actions;

import java.util.ArrayList;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
@xml_tag("and")
public class AndCondition extends Condition
{

	@simpl_collection
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	@simpl_nowrap
	private ArrayList<Condition>	checks;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		boolean flag = true;
		if (checks != null)
		{
			for (Condition check : checks)
			{
				flag = flag && check.evaluate(handler);
				if (!flag)
					break;
			}
		}
		return flag;
	}

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<and><or><and /><or /></or><not_null /></and>";
		AndCondition and = (AndCondition) MetaMetadataTranslationScope.get().deserializeCharSequence(
				xml);
		System.out.println(and);
		System.out.println(and.checks);
		System.out.println(and.serialize());
	}

}
