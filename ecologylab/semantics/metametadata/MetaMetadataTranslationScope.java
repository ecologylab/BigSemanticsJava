package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.net.UserAgent;
import ecologylab.semantics.actions.GetXPathNodeSemanticAction;
import ecologylab.semantics.actions.BackOffFromSite;
import ecologylab.semantics.actions.CreateAndVisualizeImgSurrogateSemanticAction;
import ecologylab.semantics.actions.CreateAndVisualizeTextSurrogateSemanticAction;
import ecologylab.semantics.actions.CreateContainerSemanticAction;
import ecologylab.semantics.actions.CreateSemanticAnchorSemanticAction;
import ecologylab.semantics.actions.ParseDocumentLaterSemanticAction;
import ecologylab.semantics.actions.ParseDocumentNowSemanticAction;
import ecologylab.semantics.actions.EvaluateRankWeight;
import ecologylab.semantics.actions.FlagCheck;
import ecologylab.semantics.actions.ForEachSemanticAction;
import ecologylab.semantics.actions.GeneralSemanticAction;
import ecologylab.semantics.actions.GetFieldSemanticAction;
import ecologylab.semantics.actions.IfSemanticAction;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SetFieldSemanticAction;
import ecologylab.semantics.actions.SetMetadataSemanticAction;
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
			ParseDocumentNowSemanticAction.class, 
			SetMetadataSemanticAction.class,
			GetFieldSemanticAction.class,
			SetFieldSemanticAction.class,
			SearchEngines.class,
			SearchEngine.class,
			CreateSemanticAnchorSemanticAction.class,
			ParseDocumentLaterSemanticAction.class,
			GetXPathNodeSemanticAction.class,
			DefVar.class,
			IfSemanticAction.class,
			CreateAndVisualizeTextSurrogateSemanticAction.class,
			EvaluateRankWeight.class,
			BackOffFromSite.class
			};

	public static TranslationScope get()
	{
		return TranslationScope.get(PACKAGE_NAME, TRANSLATIONS);
	}
}
