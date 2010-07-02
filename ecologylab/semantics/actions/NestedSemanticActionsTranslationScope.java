package ecologylab.semantics.actions;

import ecologylab.semantics.metametadata.DefVar;
import ecologylab.xml.TranslationScope;

public class NestedSemanticActionsTranslationScope
{
	public static final String	NESTED_SEMANTIC_ACTIONS_SCOPE	= "nested_semantic_actions_scope";
	static final Class[] CLASSES = 
	{
		BackOffFromSite.class,
		CreateAndVisualizeImgSurrogateSemanticAction.class,
		CreateAndVisualizeTextSurrogateSemanticAction.class,
	  CreateContainerSemanticAction.class,
		CreateSemanticAnchorSemanticAction.class,
		EvaluateRankWeight.class,
		ForEachSemanticAction.class,
		GeneralSemanticAction.class,
		GetFieldSemanticAction.class,
		GetXPathNodeSemanticAction.class,
		IfSemanticAction.class,
		ParseDocumentLaterSemanticAction.class,
		ParseDocumentNowSemanticAction.class,
		SetFieldSemanticAction.class,
		SetMetadataSemanticAction.class,

		StringOperationsSemanticAction.class,
		SearchSemanticAction.class,
	};
	
	public static final TranslationScope get()
	{
		return TranslationScope.get(NESTED_SEMANTIC_ACTIONS_SCOPE, CLASSES);
	}
}
