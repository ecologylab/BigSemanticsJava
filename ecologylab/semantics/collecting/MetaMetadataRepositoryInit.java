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
import ecologylab.semantics.metadata.builtins.Clipping;
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
	public static String																			DEFAULT_REPOSITORY_LOCATION			= "../ecologylabSemantics/repository";

	public static String																			DEFAULT_REPOSITORY_FILE_SUFFIX	= ".xml";

	public static MetaMetadataRepository.RepositoryFileLoader	DEFAULT_REPOSITORY_FILE_LOADER	= MetaMetadataRepository.XML_FILE_LOADER;

	public static final String																SEMANTICS												= "semantics/";

	protected static File																			METAMETADATA_REPOSITORY_DIR_FILE;

	protected static File																			METAMETADATA_SITES_FILE;

	/**
	 * 
	 * The repository has the metaMetadatas of the document types. The repository is populated as the
	 * documents are processed.
	 */
	protected static MetaMetadataRepository										META_METADATA_REPOSITORY;

	public static MetaMetadata																DOCUMENT_META_METADATA;
	public static MetaMetadata																PDF_META_METADATA;
	public static MetaMetadata																SEARCH_META_METADATA;
	public static MetaMetadata																IMAGE_META_METADATA;
	public static MetaMetadata																DEBUG_META_METADATA;
	public static MetaMetadata																IMAGE_CLIPPING_META_METADATA;

	static
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		
		MetaMetadataRepository.initializeTypes();

	}
	
	public static MetaMetadataRepository getRepository()
	{
		return META_METADATA_REPOSITORY;
	}
	
	private MetaMetadataRepository	metaMetadataRepository;

	private TranslationScope				metadataTranslationScope;

	private final TranslationScope	generatedDocumentTranslations;

	private final TranslationScope	generatedMediaTranslations;

	private final TranslationScope	repositoryClippingTranslations;
	
	/**
	 * This constructor should only be called from SemanticsScope's constructor!
	 * 
	 * @param metadataTranslationScope
	 */
	protected MetaMetadataRepositoryInit(TranslationScope metadataTranslationScope)
	{
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

		
		META_METADATA_REPOSITORY 					= MetaMetadataRepository.loadFromDir(METAMETADATA_REPOSITORY_DIR_FILE, DEFAULT_REPOSITORY_FILE_SUFFIX, DEFAULT_REPOSITORY_FILE_LOADER);
		
		DOCUMENT_META_METADATA						= META_METADATA_REPOSITORY.getMMByName(DOCUMENT_TAG);
		PDF_META_METADATA									= META_METADATA_REPOSITORY.getMMByName(PDF_TAG);
		SEARCH_META_METADATA							= META_METADATA_REPOSITORY.getMMByName(SEARCH_TAG);
		IMAGE_META_METADATA								= META_METADATA_REPOSITORY.getMMByName(IMAGE_TAG);
		DEBUG_META_METADATA								= META_METADATA_REPOSITORY.getMMByName(DEBUG_TAG);
		IMAGE_CLIPPING_META_METADATA			= META_METADATA_REPOSITORY.getMMByName(IMAGE_CLIPPING_TAG);
		
		META_METADATA_REPOSITORY.bindMetadataClassDescriptorsToMetaMetadata(metadataTranslationScope);
		this.metadataTranslationScope	= metadataTranslationScope;
		this.metaMetadataRepository		= META_METADATA_REPOSITORY;
		this.generatedDocumentTranslations	= 
			metadataTranslationScope.getAssignableSubset(REPOSITORY_DOCUMENT_TRANSLATIONS, Document.class);
		this.generatedMediaTranslations	=
			metadataTranslationScope.getAssignableSubset(REPOSITORY_MEDIA_TRANSLATIONS, ClippableDocument.class);
		this.repositoryClippingTranslations =
			metadataTranslationScope.getAssignableSubset(REPOSITORY_CLIPPING_TRANSLATIONS, Clipping.class);
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
