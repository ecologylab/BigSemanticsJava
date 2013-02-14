package ecologylab.bigsemantics.actions;

import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag("not")
public class NotCondition extends Condition
{

	@simpl_composite
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	Condition	check;

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		return !check.evaluate(handler);
	}

}
