package ecologylab.bigsemantics.actions;

import ecologylab.serialization.SimplTypesScope;

public class SemanticActionTranslationScope
{
	public static final String	SEMANTIC_ACTION_TRANSLATION_SCOPE	= "semantic_action_translation_scope";
	static final Class[] CLASSES = 
	{
		BackOffFromSiteSemanticAction.class,
		ChooseSemanticAction.class,
		ChooseSemanticAction.Otherwise.class,
		CreateAndVisualizeImgSurrogateSemanticAction.class,
		CreateAndVisualizeTextSurrogateSemanticAction.class,
		EvaluateRankWeight.class,
		ForEachSemanticAction.class,
		GetFieldSemanticAction.class,
		IfSemanticAction.class,
		ParseDocumentSemanticAction.class,
		SearchSemanticAction.class,
		SetFieldSemanticAction.class,
		SetMetadataSemanticAction.class,
		GetLinkedMetadataSemanticAction.class,
		ReselectAndExtractMetadataSemanticAction.class,
		AddMixinSemanticAction.class,
		FilterLocationAction.class,
		VisualizeClippings.class,
	};
	
	public static final SimplTypesScope get()
	{
		return SimplTypesScope.get(SEMANTIC_ACTION_TRANSLATION_SCOPE, CLASSES);
	}
}
