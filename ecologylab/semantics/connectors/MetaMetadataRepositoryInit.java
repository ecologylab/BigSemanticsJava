/**
 * 
 */
package ecologylab.semantics.connectors;

import java.io.File;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.appframework.ApplicationProperties;
import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.Debug;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
import ecologylab.io.Files;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class MetaMetadataRepositoryInit extends Debug
implements DocumentParserTagNames, ApplicationProperties
{
	static final String	META_METADATA_REPOSITORY_DIR		= "repository";
	
	protected static File												METAMETADATA_REPOSITORY_DIR_FILE;
	protected static File												METAMETADATA_SITES_FILE;


	public static final String 	SEMANTICS											= "semantics/";
	
	/**
	 * 
	 * The repository has the metaMetadatas of the document types. The repository is populated as the
	 * documents are processed.
	 */
	protected static final MetaMetadataRepository		META_METADATA_REPOSITORY;
	
	public static final MetaMetadata								DOCUMENT_META_METADATA;
	public static final MetaMetadata								PDF_META_METADATA;
	public static final MetaMetadata								SEARCH_META_METADATA;
	public static final MetaMetadata								IMAGE_META_METADATA;
	
	static
	{
		if (ApplicationEnvironment.isInUse() && !ApplicationEnvironment.runningInEclipse())
		{
			AssetsRoot mmAssetsRoot = new AssetsRoot(EnvironmentGeneric.configDir().getRelative(SEMANTICS), 
					 Files.newFile(PropertiesAndDirectories.thisApplicationDir(), SEMANTICS + "/repository"));
	
			METAMETADATA_REPOSITORY_DIR_FILE 	= Assets.getAsset(mmAssetsRoot, null, "repository", null, !USE_ASSETS_CACHE, SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
		}
		else
			METAMETADATA_REPOSITORY_DIR_FILE 	= new File("../ecologylabSemantics/repository");
		
		println("\t\t-- Reading meta_metadata from " + METAMETADATA_REPOSITORY_DIR_FILE);

		META_METADATA_REPOSITORY 					= MetaMetadataRepository.load(METAMETADATA_REPOSITORY_DIR_FILE);
		
		DOCUMENT_META_METADATA						= META_METADATA_REPOSITORY.getByTagName(DOCUMENT_TAG);
		PDF_META_METADATA									= META_METADATA_REPOSITORY.getByTagName(PDF_TAG);
		SEARCH_META_METADATA							= META_METADATA_REPOSITORY.getByTagName(SEARCH_TAG);
		IMAGE_META_METADATA								= META_METADATA_REPOSITORY.getByTagName(IMAGE_TAG);
	}

	
	private MetaMetadataRepository															metaMetadataRepository;
	
	private TranslationScope																		metadataTranslationScope;
	
	public MetaMetadataRepositoryInit(TranslationScope metadataTranslationScope)
	{
		META_METADATA_REPOSITORY.bindMetadataClassDescriptorsToMetaMetadata(metadataTranslationScope);
		this.metadataTranslationScope	= metadataTranslationScope;
		this.metaMetadataRepository		= META_METADATA_REPOSITORY;
	}
	
  
	public MetaMetadataRepository getMetaMetadataRepository()
	{
		return metaMetadataRepository;
	}

	public TranslationScope getMetadataTranslationScope()
	{
		return metadataTranslationScope;
	}
}
