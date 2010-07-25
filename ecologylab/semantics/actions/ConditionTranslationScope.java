package ecologylab.semantics.actions;

import ecologylab.serialization.TranslationScope;

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

	public static final TranslationScope get()
	{
		return TranslationScope.get(CONDITION_SCOPE, CLASSES);
	}
}
