package ecologylab.bigsemantics.metametadata;

import ecologylab.bigsemantics.actions.ConditionTranslationScope;
import ecologylab.bigsemantics.actions.NestedSemanticAction;
import ecologylab.bigsemantics.actions.SemanticAction;
import ecologylab.bigsemantics.actions.SemanticActionTranslationScope;
import ecologylab.bigsemantics.collecting.CookieProcessing;
import ecologylab.bigsemantics.collecting.SemanticsSite;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.concurrent.BasicSite;
import ecologylab.generic.Debug;
import ecologylab.net.UserAgent;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.textformat.NamedStyle;

public class MetaMetadataTranslationScope extends Debug
{
	public static final String		NAME						= "meta_metadata";

	public static final String		BASE_NAME				= "meta_metadata_base";

	@SuppressWarnings("rawtypes")
	protected static final Class	BASE_CLASSES[]	=
	{
		MetadataClassDescriptor.class,
		MetadataFieldDescriptor.class,
		MmdGenericTypeVar.class,
		MetaMetadataNestedField.class,
		MetaMetadata.class,
		SearchEngines.class,
		SearchEngine.class,
		UserAgent.class, 
		NamedStyle.class, 
		SemanticAction.class,
		NestedSemanticAction.class,
		
		SemanticsSite.class,
		BasicSite.class,
		Argument.class,
		
		RegexFilter.class,
		FieldParserElement.class,
		
		CookieProcessing.class,
		
		MetaMetadataRepository.class, 
		
		MetaMetadataSelector.class,
		MetaMetadataSelectorReselectField.class,
		
		DefVar.class,	// cannot be in NestedSemanticActionsTranslationScope because these are collected separtely in MetaMetadataField
		
		UrlGenerator.class,
		LinkWith.class,
	};

	public static final SimplTypesScope BASE_TRANSLATIONS	= SimplTypesScope.get(BASE_NAME, BASE_CLASSES);

	public static final SimplTypesScope[]	SCOPE_SET	= 
	{
		BASE_TRANSLATIONS, 
		NestedMetaMetadataFieldTypesScope.get(),
		SemanticActionTranslationScope.get(),
		ConditionTranslationScope.get(),
	};

	public static SimplTypesScope get()
	{
		return SimplTypesScope.get(NAME, SCOPE_SET);
	}
}
