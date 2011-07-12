/**
 * 
 */
package ecologylab.semantics.collecting;

import java.io.File;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.appframework.ApplicationProperties;
import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
import ecologylab.io.Files;
import ecologylab.semantics.metadata.builtins.ClippableDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

/**
 * Initializes the MetaMetadataRepository, using its standard location in the /repository directory of the ecologylabSemantics project,
 * or its associated zip file in the Assets cache.
 * 
 * @author andruid
 */
public class MetaMetadataRepositoryInit extends Scope<Object>
implements DocumentParserTagNames, ApplicationProperties, SemanticsNames
{
	public static final String										DEFAULT_REPOSITORY_LOCATION	= "../ecologylabSemantics/repository";

	public static final String										SEMANTICS										= "semantics/";

	protected static File													METAMETADATA_REPOSITORY_DIR_FILE;

	protected static File													METAMETADATA_SITES_FILE;

	/**
	 * 
	 * The repository has the metaMetadatas of the document types. The repository is populated as the
	 * documents are processed.
	 */
	protected static final MetaMetadataRepository	META_METADATA_REPOSITORY;

	public static final MetaMetadata							DOCUMENT_META_METADATA;
	public static final MetaMetadata							PDF_META_METADATA;
	public static final MetaMetadata							SEARCH_META_METADATA;
	public static final MetaMetadata							IMAGE_META_METADATA;
	public static final MetaMetadata							DEBUG_META_METADATA;

	static
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;

		if (ApplicationEnvironment.isInUse() && !ApplicationEnvironment.runningInEclipse())
		{
			AssetsRoot mmAssetsRoot = new AssetsRoot(EnvironmentGeneric.configDir().getRelative(SEMANTICS), 
					 Files.newFile(PropertiesAndDirectories.thisApplicationDir(), SEMANTICS + "/repository"));
	
			METAMETADATA_REPOSITORY_DIR_FILE 	= Assets.getAsset(mmAssetsRoot, null, "repository", null, !USE_ASSETS_CACHE, SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
		}
		else
		{
//			METAMETADATA_REPOSITORY_DIR_FILE 	= new File("../ecologylabSemantics/repository");
			METAMETADATA_REPOSITORY_DIR_FILE 	= new File(DEFAULT_REPOSITORY_LOCATION);
		}
		
		Debug.println("\t\t-- Reading meta_metadata from " + METAMETADATA_REPOSITORY_DIR_FILE);

		META_METADATA_REPOSITORY 					= MetaMetadataRepository.load(METAMETADATA_REPOSITORY_DIR_FILE);
		
		DOCUMENT_META_METADATA						= META_METADATA_REPOSITORY.getByTagName(DOCUMENT_TAG);
		PDF_META_METADATA									= META_METADATA_REPOSITORY.getByTagName(PDF_TAG);
		SEARCH_META_METADATA							= META_METADATA_REPOSITORY.getByTagName(SEARCH_TAG);
		IMAGE_META_METADATA								= META_METADATA_REPOSITORY.getByTagName(IMAGE_TAG);
		DEBUG_META_METADATA								= META_METADATA_REPOSITORY.getByTagName(DEBUG_TAG);
	}
	
	public static MetaMetadataRepository getRepository()
	{
		return META_METADATA_REPOSITORY;
	}
	
	private MetaMetadataRepository									metaMetadataRepository;
	
	private TranslationScope												metadataTranslationScope;
	
	private final TranslationScope									generatedDocumentTranslations;

	private final TranslationScope									generatedMediaTranslations;
	
	/**
	 * This constructor should only be called from SemanticsScope's constructor!
	 * 
	 * @param metadataTranslationScope
	 */
	protected MetaMetadataRepositoryInit(TranslationScope metadataTranslationScope)
	{
		META_METADATA_REPOSITORY.bindMetadataClassDescriptorsToMetaMetadata(metadataTranslationScope);
		this.metadataTranslationScope	= metadataTranslationScope;
		this.metaMetadataRepository		= META_METADATA_REPOSITORY;
		this.generatedDocumentTranslations	= 
			metadataTranslationScope.getAssignableSubset(GENERATED_DOCUMENT_TRANSLATIONS, Document.class);
		this.generatedMediaTranslations	=
			metadataTranslationScope.getAssignableSubset(GENERATED_MEDIA_TRANSLATIONS, ClippableDocument.class);
	}
	
	public MetaMetadataRepository getMetaMetadataRepository()
	{
		return metaMetadataRepository;
	}

	public TranslationScope getMetadataTranslationScope()
	{
		return metadataTranslationScope;
	}


	/**
	 * @return the generatedDocumentTranslations
	 */
	public TranslationScope getGeneratedDocumentTranslations()
	{
		return generatedDocumentTranslations;
	}


	/**
	 * @return the generatedMediaTranslations
	 */
	public TranslationScope getGeneratedMediaTranslations()
	{
		return generatedMediaTranslations;
	}
}
