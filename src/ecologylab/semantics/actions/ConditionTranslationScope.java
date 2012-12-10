package ecologylab.semantics.actions;

import ecologylab.serialization.SimplTypesScope;

public class ConditionTranslationScope
{
	public static final String	CONDITION_SCOPE	= "condition_scope";

	static final Class[]				CLASSES					= {
		Condition.class,
		AndCondition.class,
		OrCondition.class,
		NotCondition.class,
		
		NotNull.class,
		Null.class,
		
	};

	public static final SimplTypesScope get()
	{
		return SimplTypesScope.get(CONDITION_SCOPE, CLASSES);
	}
}
