package ecologylab.semantics.actions;

import java.util.ArrayList;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag("and")
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
		AndCondition and = (AndCondition) MetaMetadataTranslationScope.get().deserialize(
				xml, StringFormat.XML);
		System.out.println(and);
		System.out.println(and.checks);
		System.out.println(SimplTypesScope.serialize(and, StringFormat.XML));
	}

}
