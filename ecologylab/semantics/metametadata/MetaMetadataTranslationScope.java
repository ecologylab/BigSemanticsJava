package ecologylab.semantics.metametadata;

import ecologylab.generic.Debug;
import ecologylab.net.UserAgent;
import ecologylab.semantics.actions.FlagCheck;
import ecologylab.semantics.actions.NestedSemanticActionsTranslationScope;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.textformat.NamedStyle;
import ecologylab.xml.TranslationScope;

public class MetaMetadataTranslationScope extends Debug
{
	public static final String		NAME						= "meta_metadata";

	public static final String		BASE_NAME				= "meta_metadata_base";

	protected static final Class	BASE_CLASSES[]	=
	{
		MetadataClassDescriptor.class,
		MetadataFieldDescriptor.class,
		MetaMetadataField.class,
		MetaMetadataScalarField.class,
		MetaMetadataNestedField.class,
		MetaMetadataCollectionField.class,
		MetaMetadata.class,
		SearchEngines.class,
		SearchEngine.class,
		UserAgent.class, 
		NamedStyle.class, 
		SemanticAction.class,
		Argument.class,
		Check.class,
		FlagCheck.class,
		MetaMetadataRepository.class, 
		
		DefVar.class,	// cannot be in NestedSemanticActionsTranslationScope because these are collected separtely in MetaMetadataField
	};

	public static final TranslationScope BASE_TRANSLATIONS	= TranslationScope.get(BASE_NAME, BASE_CLASSES);

	public static final TranslationScope[]	SCOPE_SET	= 
	{
		BASE_TRANSLATIONS, 
		NestedSemanticActionsTranslationScope.get(),
	};

	public static TranslationScope get()
	{
		return TranslationScope.get(NAME, SCOPE_SET);
	}
}
