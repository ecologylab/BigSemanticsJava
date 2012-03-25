/**
 * 
 */
package ecologylab.semantics.collecting;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import ecologylab.semantics.metadata.builtins.Annotation;
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
 * Initializes the MetaMetadataRepository, using its standard location in the /repository directory
 * of the ecologylabSemantics project, or its associated zip file in the Assets cache.
 * 
 * @author andruid
 */
public class MetaMetadataRepositoryInit extends Scope<Object> implements DocumentParserTagNames,
		ApplicationProperties, SemanticsNames
{

	public static Format												DEFAULT_REPOSITORY_FORMAT		= Format.XML;

	public static MetaMetadataRepositoryLoader	DEFAULT_REPOSITORY_LOADER		= new MetaMetadataRepositoryLoader();

	private static final String									SEMANTICS										= "semantics/";

	public static final String									DEFAULT_REPOSITORY_LOCATION	= "../../MetaMetadataRepository/MmdRepository/mmdrepository";

	static
	{
		SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
		MetaMetadataRepository.initializeTypes();
	}

	private File												repositoryLocation;

	private MetaMetadataRepository							metaMetadataRepository;

	private SimplTypesScope											metadataTypesScope;

	private final SimplTypesScope								documentsScope;

	private final SimplTypesScope								mediaTypesScope;

	private final SimplTypesScope								clippingsTypeScope;

	private final SimplTypesScope								noAnnotationsScope;

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
		if (SingletonApplicationEnvironment.isInUse()
				&& !SingletonApplicationEnvironment.runningInEclipse())
		{
			ParsedURL semanticsRelativeUrl = EnvironmentGeneric.configDir().getRelative(SEMANTICS);
			File repositoryDir = Files.newFile(PropertiesAndDirectories.thisApplicationDir(), SEMANTICS
					+ "/repository");
			AssetsRoot mmAssetsRoot = new AssetsRoot(semanticsRelativeUrl, repositoryDir);
			repositoryLocation = Assets.getAsset(mmAssetsRoot, null, "repository", null,
					!USE_ASSETS_CACHE, SemanticsAssetVersions.METAMETADATA_ASSET_VERSION);
			Debug.debugT(this, "tentative meta-metadata repository location: " + repositoryLocation);
		}
		else
		{
			if (repositoryLocation == null)
				repositoryLocation = findRepositoryLocation();
		}
		this.repositoryLocation = repositoryLocation;

		Debug.println("\t\t-- Reading meta_metadata from " + repositoryLocation);
		metaMetadataRepository = DEFAULT_REPOSITORY_LOADER.loadFromDir(repositoryLocation,
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

			this.noAnnotationsScope = metadataTypesScope.getSubtractedSubset(
					REPOSITORY_NO_ANNOTATIONS_TYPE_SCOPE, Annotation.class);

			this.mediaTypesScope = metadataTypesScope.getAssignableSubset(
					REPOSITORY_MEDIA_TYPE_SCOPE, ClippableDocument.class);
			this.mediaTypesScope.addTranslation(Clipping.class);
			this.mediaTypesScope.addTranslation(Annotation.class);

			metaMetadataRepository.bindMetadataClassDescriptorsToMetaMetadata(metadataTypesScope);
		}
		else
		{
			throw new RuntimeException("FAILURE TO LOAD META-METADATA REPOSITORY!!!");
		}
	}

	/**
	 * Find meta-metadata repository location. Look up the location by using Java resources. If
	 * failed, use the default location as specified by the crossPlatformSemantics project.
	 * 
	 * @return The repository location.
	 */
	public static File findRepositoryLocation()
	{
		File repositoryLocation = null;

		if (PropertiesAndDirectories.os() == PropertiesAndDirectories.ANDROID)
			repositoryLocation = findRepositoryLocationForAndroid();
		else
			repositoryLocation = findRepositoryLocationForSun();

		return repositoryLocation;
	}

	private static File findRepositoryLocationForSun()
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
				Debug
						.warning(
								MetaMetadataRepositoryInit.class,
								"Cannot locate meta-metadata repository by resource: is MetaMetadataRepository project in your development environment?");
				Debug.warning(MetaMetadataRepositoryInit.class, "Use default repository path.");
				repositoryLocation = new File(DEFAULT_REPOSITORY_LOCATION);
				if (!repositoryLocation.exists())
				{
					Debug
							.warning(
									MetaMetadataRepositoryInit.class,
									"Cannot locate meta-metadata repository by default: please upgrade to the new corss-platform semantics layout!!");
					Debug.warning(MetaMetadataRepositoryInit.class,
							"Fallback to the old default location -- THIS WILL NOT BE SUPPORTED SOON!!");
					repositoryLocation = new File("../ecologylabSemantics/repository");
					if (!repositoryLocation.exists())
					{
						Debug.error(MetaMetadataRepositoryInit.class,
								"Failure to locate meta-metadata repository!");
					}
				}
			}
		}
		return repositoryLocation;
	}

	private static File findRepositoryLocationForAndroid()
	{
		Class environmentClass;
		try
		{
			environmentClass = Class.forName("android.os.Environment");
			if (environmentClass != null)
			{
				Method m = environmentClass.getMethod("getExternalStorageDirectory");
				File sdCard = (File) m.invoke(null, null);
				File ecologylabDir = new File(sdCard.getAbsolutePath()
						+ "/Android/data/com.ecologyAndroid.ecoDroidTest/files/");
				File mmdrepositoryDir = new File(ecologylabDir + "/mmdrepository/");
				return mmdrepositoryDir;
			}
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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

	public SimplTypesScope getNoAnnotationsScope()
	{
		return noAnnotationsScope;
	}

}
