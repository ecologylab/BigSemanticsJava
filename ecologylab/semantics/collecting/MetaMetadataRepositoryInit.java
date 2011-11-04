/**
 * 
 */
package ecologylab.semantics.collecting;

import java.io.File;
import java.net.URL;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.appframework.EnvironmentGeneric;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
import ecologylab.io.Files;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.ClippableDocument;
import ecologylab.semantics.metadata.builtins.Clipping;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;

/**
 * Initializes the MetaMetadataRepository, using its standard location in the /repository directory of the ecologylabSemantics project,
 * or its associated zip file in the Assets cache.
 * 
 * @author andruid
 */
public class MetaMetadataRepositoryInit extends Scope<Object>
implements DocumentParserTagNames, ApplicationProperties, SemanticsNames
{
	
	public static Format												DEFAULT_REPOSITORY_FORMAT		= Format.XML;

	public static MetaMetadataRepositoryLoader	DEFAULT_REPOSITORY_LOADER		= new MetaMetadataRepositoryLoader();

	private static final String									SEMANTICS										= "semantics/";
	
	private static final String									DEFAULT_REPOSITORY_LOCATION	= "../../MetaMetadataRepository/MmdRepository/mmdrepository";

	static
	{
		SimplTypesScope.graphSwitch	= GRAPH_SWITCH.ON;
		MetaMetadataRepository.initializeTypes();
	}
	
	private File																repositoryLocation;

	private MetaMetadataRepository							metaMetadataRepository;

	private SimplTypesScope											metadataTranslationScope;

	private final SimplTypesScope								generatedDocumentTranslations;

	private final SimplTypesScope								generatedMediaTranslations;

	private final SimplTypesScope								repositoryClippingTranslations;

	public MetaMetadata													DOCUMENT_META_METADATA;
	public MetaMetadata													PDF_META_METADATA;
	public MetaMetadata													SEARCH_META_METADATA;
	public MetaMetadata													IMAGE_META_METADATA;
	public MetaMetadata													DEBUG_META_METADATA;
	public MetaMetadata													IMAGE_CLIPPING_META_METADATA;

	protected MetaMetadataRepositoryInit(SimplTypesScope metadataTranslationScope)
	{
		this(null, metadataTranslationScope);
	}
	
	/**
	 * This constructor should only be called from SemanticsScope's constructor!
	 * 
	 * @param metadataTranslationScope
	 */
	protected MetaMetadataRepositoryInit(File repositoryLocation, SimplTypesScope metadataTranslationScope)
	{
		if (SingletonApplicationEnvironment.isInUse() && !SingletonApplicationEnvironment.runningInEclipse())
		{
			ParsedURL semanticsRelativeUrl = EnvironmentGeneric.configDir().getRelative(SEMANTICS);
			File repositoryDir = Files.newFile(PropertiesAndDirectories.thisApplicationDir(), SEMANTICS + "/repository");
			AssetsRoot mmAssetsRoot = new AssetsRoot(semanticsRelativeUrl, repositoryDir);
			repositoryLocation 	= Assets.getAsset(mmAssetsRoot, null, "repository", null, !USE_ASSETS_CACHE, SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
			Debug.debugT(this, "tentative meta-metadata repository location: " + repositoryLocation);
		}
		else
		{
			if (repositoryLocation == null)
				repositoryLocation = findRepositoryLocation();
		}
		this.repositoryLocation = repositoryLocation;
		
		Debug.println("\t\t-- Reading meta_metadata from " + repositoryLocation);
		metaMetadataRepository 					= DEFAULT_REPOSITORY_LOADER.loadFromDir(repositoryLocation, DEFAULT_REPOSITORY_FORMAT);
		if (metaMetadataRepository != null)
		{
			DOCUMENT_META_METADATA						= metaMetadataRepository.getMMByName(DOCUMENT_TAG);
			PDF_META_METADATA									= metaMetadataRepository.getMMByName(PDF_TAG);
			SEARCH_META_METADATA							= metaMetadataRepository.getMMByName(SEARCH_TAG);
			IMAGE_META_METADATA								= metaMetadataRepository.getMMByName(IMAGE_TAG);
			DEBUG_META_METADATA								= metaMetadataRepository.getMMByName(DEBUG_TAG);
			IMAGE_CLIPPING_META_METADATA			= metaMetadataRepository.getMMByName(IMAGE_CLIPPING_TAG);
			
			metaMetadataRepository.bindMetadataClassDescriptorsToMetaMetadata(metadataTranslationScope);
			
			this.metadataTranslationScope	= metadataTranslationScope;
			this.generatedDocumentTranslations	= metadataTranslationScope.getAssignableSubset(REPOSITORY_DOCUMENT_TRANSLATIONS, Document.class);
			this.generatedMediaTranslations	= metadataTranslationScope.getAssignableSubset(REPOSITORY_MEDIA_TRANSLATIONS, ClippableDocument.class);
			this.repositoryClippingTranslations = metadataTranslationScope.getAssignableSubset(REPOSITORY_CLIPPING_TRANSLATIONS, Clipping.class);
		}
		else
		{
			throw new RuntimeException("FAILURE TO LOAD META-METADATA REPOSITORY!!!");
		}
	}
	
	/**
	 * Find meta-metadata repository location. Look up the location by using Java resources. If failed, use the default location
	 * as specified by the crossPlatformSemantics project.
	 * 
	 * @return The repository location.
	 */
	public static File findRepositoryLocation()
	{
		File repositoryLocation = null;
		
		URL repositoryUrl = MetaMetadataRepositoryInit.class.getResource("/mmdrepository");
		String repositoryPath = repositoryUrl.getFile();
		if (repositoryPath != null)
		{
			repositoryLocation = new File(repositoryPath);
			if (!repositoryLocation.exists())
			{
				// repository not found by resource
				Debug.warning(MetaMetadataRepositoryInit.class, "Cannot locate meta-metadata repository by resource: is MetaMetadataRepository project in your development environment?");
				Debug.warning(MetaMetadataRepositoryInit.class, "Use default repository path.");
				repositoryLocation = new File(DEFAULT_REPOSITORY_LOCATION);
				if (!repositoryLocation.exists())
				{
					Debug.warning(MetaMetadataRepositoryInit.class, "Cannot locate meta-metadata repository by default: please upgrade to the new corss-platform semantics layout!!");
					Debug.warning(MetaMetadataRepositoryInit.class, "Fallback to the old default location -- THIS WILL NOT BE SUPPORTED SOON!!");
					repositoryLocation = new File("../ecologylabSemantics/repository");
					if (!repositoryLocation.exists())
					{
						Debug.error(MetaMetadataRepositoryInit.class, "Failure to locate meta-metadata repository!");
					}
				}
			}
		}
		
		return repositoryLocation;
	}
	
	/**
	 * @return The repository location.
	 */
	public File getRepositoryLocation()
	{
		return repositoryLocation;
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
	public SimplTypesScope getMetadataTranslationScope()
	{
		return metadataTranslationScope;
	}

	/**
	 * @return the generatedDocumentTranslations.
	 */
	public SimplTypesScope getGeneratedDocumentTranslations()
	{
		return generatedDocumentTranslations;
	}


	/**
	 * @return the generatedMediaTranslations.
	 */
	public SimplTypesScope getGeneratedMediaTranslations()
	{
		return generatedMediaTranslations;
	}
	
	/**
	 * @return the repositoryClippingTranslations.
	 */
	public SimplTypesScope getRepositoryClippingTranslations()
	{
		return repositoryClippingTranslations;
	}
	
}
