package ecologylab.bigsemantics.actions;

import java.util.ArrayList;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag("or")
public class OrCondition extends Condition
{

	@simpl_collection
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	@simpl_nowrap
	ArrayList<Condition>	checks;

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

}
