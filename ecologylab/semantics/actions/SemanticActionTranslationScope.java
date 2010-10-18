package ecologylab.semantics.actions;

import ecologylab.serialization.TranslationScope;

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
		CreateSemanticAnchorSemanticAction.class,
		EvaluateRankWeight.class,
		ForEachSemanticAction.class,
		GetFieldSemanticAction.class,
		IfSemanticAction.class,
		ParseDocumentSemanticAction.class,
		SearchSemanticAction.class,
		SetFieldSemanticAction.class,
		SetMetadataSemanticAction.class,
		
		ParseBibtexSemanticAction.class,
	};
	
	public static final TranslationScope get()
	{
		return TranslationScope.get(SEMANTIC_ACTION_TRANSLATION_SCOPE, CLASSES);
	}
}
