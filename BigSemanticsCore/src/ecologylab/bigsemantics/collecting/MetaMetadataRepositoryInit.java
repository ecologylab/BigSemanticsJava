/**
 * 
 */
package ecologylab.bigsemantics.collecting;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.bigsemantics.metadata.builtins.ClippableDocument;
import ecologylab.bigsemantics.metadata.builtins.Clipping;
import ecologylab.bigsemantics.metadata.builtins.CreativeAct;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.RichArtifact;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.bigsemantics.namesandnums.DocumentParserTagNames;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;

/**
 * Initializes the MetaMetadataRepository, using its standard location in the /repository directory
 * of the ecologylabSemantics project, or its associated zip file in the Assets cache.
 * 
 * @author andruid
 */
public class MetaMetadataRepositoryInit extends Scope<Object>
implements DocumentParserTagNames, ApplicationProperties, SemanticsNames
{

	public static Format												DEFAULT_REPOSITORY_FORMAT		= Format.XML;

	static
	{
		SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
		MetaMetadataRepository.initializeTypes();
	}
	
	private MetaMetadataRepositoryLocator       repositoryLocator;

	private MetaMetadataRepositoryLoader        repositoryLoader;
	
	private MetaMetadataRepository							metaMetadataRepository;

	private SimplTypesScope											metadataTypesScope;

	private final SimplTypesScope								documentsScope;

	private final SimplTypesScope								mediaTypesScope;

	private final SimplTypesScope								clippingsTypeScope;
	
	private final SimplTypesScope               richArtifactsTypeScope;

	private final SimplTypesScope				creativeActsTypesScope;
	
	public MetaMetadata													DOCUMENT_META_METADATA;

	public MetaMetadata													PDF_META_METADATA;

	public MetaMetadata													SEARCH_META_METADATA;

	public MetaMetadata													IMAGE_META_METADATA;

	public MetaMetadata													DEBUG_META_METADATA;

	public MetaMetadata													IMAGE_CLIPPING_META_METADATA;

	protected MetaMetadataRepositoryInit(SimplTypesScope metadataTypesScope)
	{
		this(null, metadataTypesScope);
	}

	protected MetaMetadataRepositoryInit(File repositoryLocation,
			SimplTypesScope metadataTypesScope)
	{
		this(repositoryLocation, DEFAULT_REPOSITORY_FORMAT, metadataTypesScope);
	}

	/**
	 * This constructor should only be called from SemanticsScope's constructor!
	 * 
	 * @param metadataTranslationScope
	 */
	protected MetaMetadataRepositoryInit(File repositoryLocation, Format repositoryFormat,
			SimplTypesScope metadataTypesScope)
	{
	  repositoryLocator = new MetaMetadataRepositoryLocator(USE_ASSETS_CACHE);
	  repositoryLoader = new MetaMetadataRepositoryLoader();
	  
		List<InputStream> repositoryIStreams =
		    repositoryLocator.locateRepositoryAndOpenStreams(repositoryLocation, repositoryFormat);
    metaMetadataRepository = repositoryLoader.loadFromInputStreams(repositoryIStreams,
                                                                   repositoryFormat);
		if (metaMetadataRepository != null)
		{
			DOCUMENT_META_METADATA = metaMetadataRepository.getMMByName(DOCUMENT_TAG);
			PDF_META_METADATA = metaMetadataRepository.getMMByName(PDF_TAG);
			SEARCH_META_METADATA = metaMetadataRepository.getMMByName(SEARCH_TAG);
			IMAGE_META_METADATA = metaMetadataRepository.getMMByName(IMAGE_TAG);
			DEBUG_META_METADATA = metaMetadataRepository.getMMByName(DEBUG_TAG);
			IMAGE_CLIPPING_META_METADATA = metaMetadataRepository.getMMByName(IMAGE_CLIPPING_TAG);

			this.metadataTypesScope = metadataTypesScope;
			this.documentsScope = metadataTypesScope.getAssignableSubset(
					REPOSITORY_DOCUMENTS_TYPE_SCOPE, Document.class);
			this.clippingsTypeScope = metadataTypesScope.getAssignableSubset(
					REPOSITORY_CLIPPINGS_TYPE_SCOPE, Clipping.class);
			this.richArtifactsTypeScope = metadataTypesScope.getAssignableSubset(
					REPOSITORY_RICH_ARTIFACTS_TYPE_SCOPE, RichArtifact.class);
			this.creativeActsTypesScope = metadataTypesScope.getAssignableSubset(
					REPOSITORY_CREATIVE_ACTS_TYPE_SCOPE, CreativeAct.class);
			
			this.mediaTypesScope = metadataTypesScope.getAssignableSubset(
					REPOSITORY_MEDIA_TYPE_SCOPE, ClippableDocument.class);
			this.mediaTypesScope.addTranslation(Clipping.class);

			metaMetadataRepository.bindMetadataClassDescriptorsToMetaMetadata(metadataTypesScope);
		}
		else
		{
			throw new RuntimeException("FAILURE TO LOAD META-METADATA REPOSITORY!!!");
		}
	}

	/**
	 * @return The repository.
	 */
	public MetaMetadataRepository getMetaMetadataRepository()
	{
		return metaMetadataRepository;
	}

	/**
	 * @return The SimplTypesScope for (generated) metadata semantics.
	 */
	public SimplTypesScope getMetadataTypesScope()
	{
		return metadataTypesScope;
	}

	/**
	 * @return the generatedDocumentTranslations.
	 */
	public SimplTypesScope getDocumentsTypeScope()
	{
		return documentsScope;
	}

	/**
	 * @return the generatedMediaTranslations.
	 */
	public SimplTypesScope getMediaTypesScope()
	{
		return mediaTypesScope;
	}

	/**
	 * @return the repositoryClippingTranslations.
	 */
	public SimplTypesScope getClippingsTypesScope()
	{
		return clippingsTypeScope;
	}

}
