package ecologylab.semantics.metametadata;

import ecologylab.generic.Debug;
import ecologylab.net.UserAgent;
import ecologylab.semantics.actions.ApplyXPathSemanticAction;
import ecologylab.semantics.actions.BackOffFromSite;
import ecologylab.semantics.actions.CreateAndVisualizeImgSurrogateSemanticAction;
import ecologylab.semantics.actions.CreateAndVisualizeTextSurrogateSemanticAction;
import ecologylab.semantics.actions.CreateContainerSemanticAction;
import ecologylab.semantics.actions.CreateSemanticAnchorSemanticAction;
import ecologylab.semantics.actions.DownloadLaterSemanticAction;
import ecologylab.semantics.actions.DownloadNowSemanticAction;
import ecologylab.semantics.actions.EvaluateRankWeight;
import ecologylab.semantics.actions.FlagCheck;
import ecologylab.semantics.actions.ForEachSemanticAction;
import ecologylab.semantics.actions.GeneralSemanticAction;
import ecologylab.semantics.actions.GetFieldSemanticAction;
import ecologylab.semantics.actions.IfSemanticAction;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SetMetadataSemanticAction;
import ecologylab.semantics.actions.SetterSemanticAction;
import ecologylab.semantics.actions.TrySyncNestedMetadata;
import ecologylab.textformat.NamedStyle;
import ecologylab.xml.TranslationScope;

public class MetaMetadataTranslationScope extends Debug
{
	public static final String		NAME						= "metaMetadata";

	public static final String		PACKAGE_NAME		= "metaMetadata";

	protected static final Class	TRANSLATIONS[]	=
																								{
			MetaMetadata.class,
			MetaMetadataField.class,
			MetaMetadataRepository.class, 
			UserAgent.class, 
			NamedStyle.class, 
			SemanticAction.class,
			Argument.class,
			Check.class,
			FlagCheck.class,
			CreateAndVisualizeImgSurrogateSemanticAction.class,
			CreateContainerSemanticAction.class,
			ForEachSemanticAction.class,
			GeneralSemanticAction.class,
			DownloadNowSemanticAction.class, 
			SetMetadataSemanticAction.class,
			GetFieldSemanticAction.class,
			SetterSemanticAction.class,
			SearchEngines.class,
			SearchEngine.class,
			CreateSemanticAnchorSemanticAction.class,
			DownloadLaterSemanticAction.class,
			ApplyXPathSemanticAction.class,
			DefVar.class,
			IfSemanticAction.class,
			CreateAndVisualizeTextSurrogateSemanticAction.class,
			TrySyncNestedMetadata.class,
			EvaluateRankWeight.class,
			BackOffFromSite.class
			};

	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
}