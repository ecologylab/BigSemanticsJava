/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.collections.PrefixCollection;
import ecologylab.collections.PrefixPhrase;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.net.UserAgent;
import ecologylab.semantics.collecting.CookieProcessing;
import ecologylab.semantics.collecting.LinkedMetadataMonitor;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.collecting.SemanticsSiteMap;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarScalarType;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.textformat.NamedStyle;

/**
 * @author damaraju
 * 
 */

public class MetaMetadataRepository extends ElementState implements PackageSpecifier,
		DocumentParserTagNames
{
	private static final String																	FIREFOX_3_6_4_AGENT_STRING			= "Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.4) Gecko/20100513 Firefox/3.6.4";

	private static final String																	DEFAULT_STYLE_NAME							= "default";

	/**
	 * The name of the repository.
	 */
	@simpl_scalar
	private String																							name;

	/**
	 * The package in which the class files have to be generated.
	 */
	@xml_tag("package")
	@simpl_scalar
	private String																							packageAttribute;

	@simpl_map("user_agent")
	private HashMap<String, UserAgent>													userAgents;

	@simpl_composite
	private SearchEngines																				searchEngines;

	@simpl_map("named_style")
	private HashMap<String, NamedStyle>													namedStyles;

	@simpl_scalar
	private String																							defaultUserAgentName;

	private String																							defaultUserAgentString					= null;

	@simpl_nowrap
	@simpl_collection("cookie_processing")
	ArrayList<CookieProcessing>																	cookieProcessors;

	/**
	 * The keys for this hashmap are the values within TypeTagNames. This map is filled out
	 * automatically, by translateFromXML(). It contains bindings, for all subtypes.
	 */
	@simpl_map("meta_metadata")
	@simpl_nowrap
	private HashMapArrayList<String, MetaMetadata>							repositoryByTagName;

	@simpl_map("selector")
	@simpl_nowrap
	private HashMapArrayList<String, MetaMetadataSelector>			selectorsByName;

	private HashMap<String, MetaMetadata>												repositoryByClassName						= new HashMap<String, MetaMetadata>();

	/**
	 * Repository with noAnchorNoQuery URL string as key.
	 */
	private HashMap<String, MetaMetadata>												documentRepositoryByUrlStripped	= new HashMap<String, MetaMetadata>();

	/**
	 * Repository with domain as key.
	 */
	private HashMap<String, MetaMetadata>												documentRepositoryByDomain			= new HashMap<String, MetaMetadata>();

	/**
	 * Repository for ClippableDocument and its subclasses.
	 */
	private HashMap<String, MetaMetadata>												imageRepositoryByUrlStripped		= new HashMap<String, MetaMetadata>();

	private HashMap<String, ArrayList<RepositoryPatternEntry>>	documentRepositoryByPattern			= new HashMap<String, ArrayList<RepositoryPatternEntry>>();

	private HashMap<String, ArrayList<RepositoryPatternEntry>>	imageRepositoryByPattern				= new HashMap<String, ArrayList<RepositoryPatternEntry>>();

	/**
	 * We have only documents as direct binding will be used only in case of feeds and XML
	 */
	private HashMap<String, MetaMetadata>												repositoryByMime								= new HashMap<String, MetaMetadata>();

	private HashMap<String, MetaMetadata>												repositoryBySuffix							= new HashMap<String, MetaMetadata>();

	private PrefixCollection																		urlPrefixCollection							= new PrefixCollection(
																																																	'/');

	// /////////rm s00n private HashMap<String, MetaMetadataSelector> selectorsByName = new
	// HashMap<String, MetaMetadataSelector>();

	// public static final TranslationScope META_METADATA_TSCOPE =
	// MetaMetadataTranslationScope.get();

	private TranslationScope																		metadataTScope;

	// for debugging
	protected static File																				REPOSITORY_FILE;

	File																												file;

	@simpl_map("site")
	SemanticsSiteMap																						sites;

	public DownloadMonitor																			downloadMonitor;
	
	static MetaMetadata																					baseDocumentMM;

	static MetaMetadata																					baseImageMM;

	static
	{
		initializeTypes();
	}

	private static boolean																			initializedTypes;

	/**
	 * 
	 */
	public static void initializeTypes()
	{
		if (!initializedTypes)
		{
			initializedTypes = true;
			MetadataScalarScalarType.init(); // register metadata-specific scalar
			// types
			ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope.get();
		}
	}

	public static void main(String args[])
	{
		REPOSITORY_FILE = new File(
				/* PropertiesAndDirectories.thisApplicationDir(), */"C:\\abhinavThesisCode\\cf\\config\\semantics\\metametadata\\metaMetadataRepository.xml");
		MetaMetadataRepository metaMetaDataRepository = load(REPOSITORY_FILE);
		try
		{
			metaMetaDataRepository.serialize(System.out);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load MetaMetadata from repository files from the file directory. Loads the base level xml files
	 * first, then the xml files in the repositorySources folder and lastly the files in the powerUser
	 * folder. Does not build repository maps, because this requires a Metadata TranslationScope,
	 * which comes from ecologylabGeneratedSemantics.
	 * 
	 * @param file
	 *          directory
	 * @param metadataTScope
	 * @return
	 */
	public static MetaMetadataRepository load(File dir)
	{
		if (!dir.exists())
		{
			throw new RuntimeException("MetaMetadataRepository directory does not exist : " + dir.getAbsolutePath());
		}
		else
			println("MetaMetadataRepository directory : " + dir + "\n");

		MetaMetadataRepository result = null;

		FileFilter xmlFilter = new FileFilter()
		{
			public boolean accept(File dir)
			{
				return dir.getName().endsWith(".xml");
			}
		};

		File powerUserDir = new File(dir, "powerUser");
		File repositorySources = new File(dir, "repositorySources");

		TranslationScope metaMetadataTScope = MetaMetadataTranslationScope.get();

		for (File file : dir.listFiles(xmlFilter))
		{
			MetaMetadataRepository repos = readRepository(file, metaMetadataTScope);
			SearchEngines thoseEngines	= repos.searchEngines;
			if (result == null)
				result = repos;
			else if (thoseEngines != null)
			{
				result.searchEngines	= thoseEngines;
			}
			else
				result.integrateRepositoryWithThis(repos);
			// result.populateURLBaseMap();
			// // necessary to get, for example, fields for document into pdf...
			// result.populateInheritedValues();
			//
			// result.populateMimeMap();
			// For debug
			// this.metaMetaDataRepository.translateToXML(System.out);
		}

		if (repositorySources.exists())
		{
			for (File file : repositorySources.listFiles(xmlFilter))
				result.integrateRepositoryWithThis(file, metaMetadataTScope);
		}

		if (powerUserDir.exists())
		{
			for (File file : powerUserDir.listFiles(xmlFilter))
				result.integrateRepositoryWithThis(file, metaMetadataTScope);
		}

		// We might want to do this only if we have some policies worth enforcing.
		ParsedURL.cookieManager.setCookiePolicy(CookieProcessing.semanticsCookiePolicy);

		// FIXME -- get rid of this?!
		Metadata.setRepository(result);
		
		baseDocumentMM	= result.getByTagName(DOCUMENT_TAG);
		baseImageMM			= result.getByTagName(IMAGE_TAG);
		
		return result;
	}
	
	public static MetaMetadataRepository readRepository(File file)
	{
		return readRepository(file, MetaMetadataTranslationScope.get());
	}

	/**
	 * Load MetaMetadataRepository from one file.
	 * 
	 * @param file
	 * @param metadataTScope
	 * @return repository
	 */
	public static MetaMetadataRepository readRepository(File file, TranslationScope metaMetadataTScope)
	{
		MetaMetadataRepository repos = null;
		println("MetaMetadataRepository read:\t" + new File(file.getParent()).getName() + "/"
				+ file.getName());

		try
		{
			repos = (MetaMetadataRepository) metaMetadataTScope.deserialize(file);
			repos.file = file;
			repos.initializeSuffixAndMimeBasedMaps();
		}
		catch (SIMPLTranslationException e)
		{
			Debug.error("MetaMetadataRepository", "translating repository source file " + file.getAbsolutePath());
			e.printStackTrace();
		}

		return repos;
	}

	public void integrateRepositoryWithThis(File repositoryFile, TranslationScope metaMetadataTScope)
	{
		MetaMetadataRepository thatRepo = readRepository(repositoryFile, metaMetadataTScope);
		if (thatRepo == null)
			error("Could not integrate repository file:\t" + repositoryFile);
		else
			integrateRepositoryWithThis(thatRepo);
	}

	/**
	 * Combines the data stored in the parameter repository into this repository.
	 * 
	 * @param repository
	 * @return
	 */
	public void integrateRepositoryWithThis(MetaMetadataRepository repository)
	{
		// combine userAgents
		if (!combineMaps(repository.userAgents, this.userAgents))
			this.userAgents = repository.userAgents;

		// combine searchEngines
//		if (!combineMaps(repository.searchEngines, this.searchEngines))
//			this.searchEngines = repository.searchEngines;

		// combine namedStyles
		if (!combineMaps(repository.namedStyles, this.namedStyles))
			this.namedStyles = repository.namedStyles;

		// combine sites
		if (!combineMaps(repository.sites, this.sites))
			this.sites = repository.sites;

		if (!combineMaps(repository.repositoryByMime, this.repositoryByMime))
			this.repositoryByMime = repository.repositoryByMime;

		if (!combineMaps(repository.repositoryBySuffix, this.repositoryBySuffix))
			this.repositoryBySuffix = repository.repositoryBySuffix;

		combineMaps(repository.documentRepositoryByDomain, this.documentRepositoryByDomain);

		// set metaMetadata to have the correct parent repository
		HashMapArrayList<String, MetaMetadata> repositoryByTagName = repository.repositoryByTagName;
		if (repositoryByTagName != null)
		{
			for (MetaMetadata metaMetadata : repositoryByTagName)
			{
				metaMetadata.setParent(this);
				metaMetadata.file = repository.file;
				if (metaMetadata.getPackageAttribute() == null)
					metaMetadata.setPackageAttribute(repository.packageAttribute);
			}
		}

		// combine metaMetadata
		if (!combineMaps(repositoryByTagName, this.repositoryByTagName))
			this.repositoryByTagName = repositoryByTagName;
	}

	private boolean combineMaps(Map srcMap, Map destMap)
	{
		if (destMap == null)
			return false;
		if (srcMap != null)
		{
			for (Object sourceMmdName : srcMap.keySet())
			{
				if (destMap.containsKey(sourceMmdName))
				{
					error("META-METADATA DEFINED TWICE: " + sourceMmdName);
				}
				else
				{
					destMap.put(sourceMmdName, srcMap.get(sourceMmdName));
				}
			}
		}
		return true;
	}

	public TranslationScope traverseAndGenerateTranslationScope()
	{
		TranslationScope metadataBuiltInTScope = MetadataBuiltinsTranslationScope.get();
		TranslationScope ts = TranslationScope.get("meta-metadata-repository", new TranslationScope[] {metadataBuiltInTScope});
		for (MetaMetadata metaMetadata : repositoryByTagName)
		{
			if (!metaMetadata.isGenerateClass())
				metaMetadata.bindClassDescriptor(metaMetadata.getMetadataClass(ts), ts);
		}
		for (MetaMetadata metaMetadata : repositoryByTagName)
		{
			if (metaMetadata.isGenerateClass())
			{
				metaMetadata.setRepository(this);
				metaMetadata.inheritMetaMetadata(this);
				MetadataClassDescriptor cd = metaMetadata.generateMetadataClassDescriptor();
				ts.addTranslation(cd);
			}
		}
		return ts;
	}

	/**
	 * Recursively bind MetadataFieldDescriptors to all MetaMetadataFields. Perform other
	 * initialization.
	 * 
	 * @param metadataTScope
	 */
	public void bindMetadataClassDescriptorsToMetaMetadata(TranslationScope metadataTScope)
	{
		this.metadataTScope = metadataTScope;
		initializeDefaultUserAgent();

		// findAndDeclareNestedMetaMetadata(metadataTScope);

		for (MetaMetadata metaMetadata : repositoryByTagName)
		{
			metaMetadata.setRepository(this);
			metaMetadata.inheritMetaMetadata(this);
			metaMetadata.getClassAndBindDescriptors(metadataTScope);
			MetadataClassDescriptor metadataClassDescriptor = metaMetadata.getMetadataClassDescriptor();
			
			// don't put restatements of the same base type into *this* map
			if (metaMetadata.getType() == null && metadataClassDescriptor != null)
				repositoryByClassName.put(metadataClassDescriptor.getDescribedClass().getName(),
						metaMetadata);

			// set up <link_with>
			metaMetadata.setUpLinkWith(this);
		}

		initializeLocationBasedMaps();
	}

	/**
	 * 
	 */
	private void initializeDefaultUserAgent()
	{
		UserAgent userAgent = userAgents.get(defaultUserAgentName);
		if (userAgent != null)
			defaultUserAgentName = userAgent.userAgentString();
		else
			defaultUserAgentName = FIREFOX_3_6_4_AGENT_STRING;
	}

	private void findAndDeclareNestedMetaMetadata(TranslationScope metadataTScope)
	{
		ArrayList<MetaMetadata> nestedDeclarations = new ArrayList<MetaMetadata>();
		for (MetaMetadata metaMetadata : repositoryByTagName)
		{
			nestedDeclarations.addAll(generateNestedDeclarations(metaMetadata));
		}

		for (MetaMetadata metaMetadata : nestedDeclarations)
		{
			// if(!repositoryByTagName.containsKey(metaMetadata.getTag()))
			repositoryByTagName.put(metaMetadata.resolveTag(), metaMetadata);
		}
	}

	/**
	 * Recursively Copying MetadataFields from srcMetaMetadata to destMetaMetadata.
	 * 
	 * @param destMetaMetadata
	 * @param srcMetaMetadata
	 */
	protected void recursivePopulate(MetaMetadata destMetaMetadata)
	{
		// recursivePopulate(destMetaMetadata,
		// destMetaMetadata.getExtendsClass());
	}

	public MetaMetadata getMM(Class<? extends Metadata> thatClass)
	{
		String tag = metadataTScope.getTag(thatClass);

		return (tag == null) ? null : repositoryByTagName.get(tag);
	}

	public MetaMetadataCompositeField getMM(Class<? extends Metadata> parentClass, String fieldName)
	{
		MetaMetadataCompositeField result = null;
		MetaMetadata parentMM = getMM(parentClass);

		if (parentMM != null)
			result = (MetaMetadataCompositeField) parentMM.lookupChild(fieldName);

		return result;
	}

	private MetaMetadata domainAndPatternForMetadata(String domain, Pattern pattern)
	{
		MetaMetadata result = null;
		ArrayList<RepositoryPatternEntry> entries = documentRepositoryByPattern.get(domain);
		if (entries != null)
		{
			for (RepositoryPatternEntry entry : entries)
			{
				if (entry.getPattern().pattern().equals(pattern.pattern()))
				{
					result = entry.getMetaMetadata();
				}
			}
		}
		return result;
	}

	private void removeDomainAndPatternForMetadata(String domain, Pattern pattern)
	{
		MetaMetadata result = null;
		ArrayList<RepositoryPatternEntry> entries = documentRepositoryByPattern.get(domain);
		if (entries != null)
		{
			for (RepositoryPatternEntry entry : entries)
			{
				if (entry.getPattern().pattern().equals(pattern.pattern()))
				{
					// result = entry.getMetaMetadata();
					entries.remove(entry);
					return;
				}
			}
		}
	}

	/**
	 * Get MetaMetadata. First, try matching by url_base. If this fails, including if the attribute is
	 * null, then try by url_prefix. If this fails, including if the attribute is null, then try by
	 * url_pattern (regular expression).
	 * <p/>
	 * If that lookup fails, then lookup by tag name, to acquire the default.
	 * 
	 * @param purl
	 * @param tagName
	 * @return
	 */
	public MetaMetadata getDocumentMM(ParsedURL purl, String tagName)
	{
		MetaMetadata result = null;
		if (purl != null)
		{
			if (!purl.isFile())
			{
				String noAnchorNoQueryPageString = purl.noAnchorNoQueryPageString();
				result = documentRepositoryByUrlStripped.get(noAnchorNoQueryPageString);

				if (result == null)
				{
					PrefixPhrase matchingPrefix = urlPrefixCollection.getMatchingPrefix(purl);
					if (matchingPrefix != null)
					{
						result = (MetaMetadata) matchingPrefix.getMappedObject();
					}
				}

				if (result == null)
				{
					String domain = purl.domain();
					if (domain != null)
					{
						ArrayList<RepositoryPatternEntry> entries = documentRepositoryByPattern.get(domain);
						if (entries != null)
						{
							for (RepositoryPatternEntry entry : entries)
							{
								Matcher matcher = entry.getPattern().matcher(purl.toString());
								if (matcher.find())
								{
									result = entry.getMetaMetadata();
									break;
								}
							}
						}
						if (result == null)
						{
							result = documentRepositoryByDomain.get(domain);
							if (result != null)
								debug("Matched by domain = " + domain + "\t" + result);
						}
					}
				}
			}
			if (result == null)
			{
				String suffix = purl.suffix();

				if (suffix != null)
					result = getMMBySuffix(suffix);
			}
		}
		if (result == null)
			result = getByTagName(tagName);

		return result;
	}

	// TODO implement get by domain too
	/**
	 * Find the best matching MetaMetadata for the ParsedURL. Otherwise, return the default Document
	 * metadata.
	 * 
	 * @param purl
	 * @return appropriate MetaMetadata.
	 */
	public MetaMetadata getDocumentMM(ParsedURL purl)
	{
		return getDocumentMM(purl, DOCUMENT_TAG);
	}

	public MetaMetadata getCompoundDocumentMM(ParsedURL purl)
	{
		return getDocumentMM(purl, COMPOUND_DOCUMENT_TAG);
	}

	public MetaMetadata getMMBySuffix(String suffix)
	{
		return repositoryBySuffix.get(suffix);
	}

	public MetaMetadata getMMByMime(String mimeType)
	{
		return repositoryByMime.get(mimeType);
	}

	public MetaMetadata getDocumentMM(Document metadata)
	{
		return getDocumentMM(metadata.getLocation(), metadataTScope.getTag(metadata.getClass()));
	}

	public MetaMetadata getImageMM(ParsedURL purl)
	{
		return getClippableDocumentMM(purl, IMAGE_TAG);
	}

	public MetaMetadata getClippableDocumentMM(ParsedURL purl, String tagName)
	{
		MetaMetadata result = null;
		if (purl != null && !purl.isFile())
		{
			result = imageRepositoryByUrlStripped.get(purl.noAnchorNoQueryPageString());

			if (result == null)
			{
				String protocolStrippedURL = purl.toString().split("://")[1];

				String key = purl.url().getProtocol() + "://"
						+ urlPrefixCollection.getMatchingPhrase(protocolStrippedURL, '/');

				result = imageRepositoryByUrlStripped.get(key);

				if (result == null)
				{
					String domain = purl.domain();
					if (domain != null)
					{
						ArrayList<RepositoryPatternEntry> entries = imageRepositoryByPattern.get(domain);
						if (entries != null)
						{
							for (RepositoryPatternEntry entry : entries)
							{
								Matcher matcher = entry.getPattern().matcher(purl.toString());
								if (matcher.find())
								{
									result = entry.getMetaMetadata();
								}
							}
						}
					}
				}
			}
		}
		return (result != null) ? result : getByTagName(tagName);
	}

	/**
	 * Look-up MetaMetadata for this purl. If there is no special MetaMetadata, use Image. Construct
	 * Metadata of the correct subtype, base on the MetaMetadata.
	 * 
	 * @param purl
	 * @return A Metadata object, either of type Image, or a subclass. Never null!
	 */
	public Image constructImage(ParsedURL purl)
	{
		MetaMetadata metaMetadata = getImageMM(purl);
		Image result = null;
		if (metaMetadata != null)
		{
			result = (Image) metaMetadata.constructMetadata(metadataTScope);
			result.setLocation(purl);
		}
		return result;
	}

	/**
	 * Look-up MetaMetadata for this purl. If there is no special MetaMetadata, use Document.
	 * Construct Metadata of the correct subtype, base on the MetaMetadata. Set its location field to
	 * purl.
	 * 
	 * @param purl
	 * @return
	 */
	public Document constructDocument(ParsedURL purl)
	{
		MetaMetadata metaMetadata = getDocumentMM(purl);
		Document result = (Document) metaMetadata.constructMetadata(metadataTScope);
		result.setLocation(purl);
		return result;
	}

	public Document constructCompoundDocument(ParsedURL purl)
	{
		if (purl.isImg())
			return constructImage(purl);
		
		MetaMetadata metaMetadata = getCompoundDocumentMM(purl);
		Document result = (Document) metaMetadata.constructMetadata(metadataTScope);
		result.setLocation(purl);
		return result;
	}

	public Document constructDocumentBySuffix(String suffix)
	{
		MetaMetadata metaMetadata = this.getMMBySuffix(suffix);
		return metaMetadata == null ? null : (Document) metaMetadata.constructMetadata(metadataTScope);
	}

	public Document constructDocumentByMime(String mimeType)
	{
		MetaMetadata metaMetadata = this.getMMByMime(mimeType);
		return metaMetadata == null ? null : (Document) metaMetadata.constructMetadata(metadataTScope);
	}

	/**
	 * Initializes HashMaps for MetaMetadata selectors by URL or pattern. Uses the ClippableDocument
	 * and Document base classes to ensure that maps are only filled with appropriate matching
	 * MetaMetadata.
	 */
	private void initializeLocationBasedMaps()
	{
		for (MetaMetadata metaMetadata : repositoryByTagName)
		{
			// metaMetadata.inheritMetaMetadata(this);

			Class<? extends Metadata> metadataClass = metaMetadata.getMetadataClass(metadataTScope);
			if (metadataClass == null)
			{
				continue;
			}

			HashMap<String, MetaMetadata> repositoryByUrlStripped;
			HashMap<String, ArrayList<RepositoryPatternEntry>> repositoryByPattern;

			if (Image.class.isAssignableFrom(metadataClass))
			{
				repositoryByUrlStripped = imageRepositoryByUrlStripped;
				repositoryByPattern = imageRepositoryByPattern;
			}
			else if (Document.class.isAssignableFrom(metadataClass))
			{
				repositoryByUrlStripped = documentRepositoryByUrlStripped;
				repositoryByPattern = documentRepositoryByPattern;
			}
			else
				continue;

			// We need to check if something is there already
			// if something is there, then we need to check to see if it has its cf pref set
			// if not, then if I am null then I win

			ArrayList<MetaMetadataSelector> selectors = metaMetadata.getSelectors();
			for (MetaMetadataSelector selector : selectors)
			{
				String reselectMetaMetadataName = selector.getReselectMetaMetadataName();
				if (reselectMetaMetadataName != null)
				{
					MetaMetadata reselectMetaMetadata = repositoryByTagName.get(reselectMetaMetadataName);
					if (reselectMetaMetadata != null)
					{
						reselectMetaMetadata.addReselectEntry(selector, metaMetadata);
					}
					continue;
				}
				
				ParsedURL strippedPurl = selector.getUrlStripped();
				if (strippedPurl != null)
				{
					repositoryByUrlStripped.put(strippedPurl.noAnchorNoQueryPageString(), metaMetadata);
					metaMetadata.setMmSelectorType(MMSelectorType.LOCATION);
				}
				else
				{
					ParsedURL urlPathTree = selector.getUrlPathTree();
					if (urlPathTree != null)
					{
						PrefixPhrase pp = urlPrefixCollection.add(urlPathTree);
						pp.setMappedObject(metaMetadata);
	
						// TODO is this next line correct??? it looks wrong!
						repositoryByUrlStripped.put(urlPathTree.toString(), metaMetadata);
						metaMetadata.setMmSelectorType(MMSelectorType.LOCATION);
					}
					else
					{
						// use .pattern() for comparison
						String domain = selector.getDomain();
						if (domain != null)
						{
							Pattern urlPattern = selector.getUrlRegex();
							if (urlPattern != null)
							{
								ArrayList<RepositoryPatternEntry> bucket = repositoryByPattern.get(domain);
								if (bucket == null)
								{
									bucket = new ArrayList<RepositoryPatternEntry>(2);
									repositoryByPattern.put(domain, bucket);
								}
								bucket.add(new RepositoryPatternEntry(urlPattern, metaMetadata));
								metaMetadata.setMmSelectorType(MMSelectorType.LOCATION);
							}
							else
							{
								// domain only -- no pattern
								documentRepositoryByDomain.put(domain, metaMetadata);
								metaMetadata.setMmSelectorType(MMSelectorType.DOMAIN);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Recursively looks for nested declarations of meta-metadata by iterating over the fields of
	 * existing meta-metadata objects. Defines new MetaMetadata objects and adds them to the
	 * repository.
	 * 
	 * @param metaMetadata
	 * @return a collection of new meta-metadata objects to add to repository
	 */
	private ArrayList<MetaMetadata> generateNestedDeclarations(MetaMetadata metaMetadata)
	{
		ArrayList<MetaMetadata> result = new ArrayList<MetaMetadata>();
		for (MetaMetadataField metaMetadataField : metaMetadata)
		{
			if (metaMetadataField.isNewClass())
			{
				String mmName = metaMetadataField.getTagForTranslationScope();
				MetaMetadata newMetaMetadata = new MetaMetadata(metaMetadataField, mmName);
				// newMetaMetadata.setName(mmName);
				// newMetaMetadata.setChildMetaMetadata(metaMetadataField.childMetaMetadata);
				// repositoryByTagName.put(className, newMetaMetadata);
				result.add(newMetaMetadata);

				// recurse to find deeper nested declarations
				result.addAll(generateNestedDeclarations(newMetaMetadata));
			}
		}

		return result;
	}

	/**
	 * This initalizes the map based on mime type and suffix.
	 */
	private void initializeSuffixAndMimeBasedMaps()
	{
		if (repositoryByTagName == null)
			return;

		for (MetaMetadata metaMetadata : repositoryByTagName)
		{
			ArrayList<String> suffixes = metaMetadata.getSuffixes();
			if (suffixes != null)
			{
				for (String suffix : suffixes)
				{
					// FIXME-- Ask whether the suffix and mime should be
					// inherited or not
					if (!repositoryBySuffix.containsKey(suffix))
					{
						repositoryBySuffix.put(suffix, metaMetadata);
						metaMetadata.setMmSelectorType(MMSelectorType.SUFFIX_OR_MIME);
					}
				}
			}

			ArrayList<String> mimeTypes = metaMetadata.getMimeTypes();
			if (mimeTypes != null)
			{
				for (String mimeType : mimeTypes)
				{
					// FIXME -- Ask whether the suffix and mime should be
					// inherited or not
					if (!repositoryByMime.containsKey(mimeType))
					{
						repositoryByMime.put(mimeType, metaMetadata);
						metaMetadata.setMmSelectorType(MMSelectorType.SUFFIX_OR_MIME);
					}
				}
			}

		}
	}

	public MetaMetadata getByTagName(String tagName)
	{
		if (tagName == null)
			return null;
		return repositoryByTagName.get(tagName);
	}

	public MetaMetadata getByClass(Class<? extends Metadata> metadataClass)
	{
		if (metadataClass == null)
			return null;
		// String tag = metadataTScope.getTag(metadataClass);
		return repositoryByClassName.get(metadataClass.getName());
	}

	public Set<String> keySet()
	{
		return (repositoryByTagName == null) ? null : repositoryByTagName.keySet();
	}

	public Collection<MetaMetadata> values()
	{
		return (repositoryByTagName == null) ? null : repositoryByTagName.values();
	}

	public String packageName()
	{
		return packageAttribute;
	}

	public static String documentTag()
	{
		return DOCUMENT_TAG;
	}

	public MetaMetadata lookupByMime(String mimeType)
	{
		return null;
	}

	public MetaMetadata lookupBySuffix(String suffix)
	{
		return null;
	}

	public TranslationScope metadataTranslationScope()
	{
		return metadataTScope;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName()
	{
		return packageAttribute;
	}

	/**
	 * @param packageName
	 *          the packageName to set
	 */
	public void setPackageName(String packageName)
	{
		this.packageAttribute = packageName;
	}

	public NamedStyle lookupStyle(String styleName)
	{
		return namedStyles.get(styleName);
	}

	public NamedStyle getDefaultStyle()
	{
		return namedStyles.get(DEFAULT_STYLE_NAME);
	}

	public HashMap<String, UserAgent> userAgents()
	{
		if (userAgents == null)
			userAgents = new HashMap<String, UserAgent>();

		return userAgents;

	}

	public String getUserAgentString(String name)
	{
		return userAgents().get(name).userAgentString();
	}

	public String getDefaultUserAgentString()
	{
		if (defaultUserAgentString == null)
		{
			for (UserAgent userAgent : userAgents().values())
			{
				if (userAgent.isDefaultAgent())
				{
					defaultUserAgentString = userAgent.userAgentString();
					break;
				}
			}
		}

		return defaultUserAgentString;
	}

	public SearchEngine getSearchEngine(String engineName)
	{
		if (searchEngines != null)
		{
			return searchEngines.get(engineName);
		}
		return null;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public SemanticsSiteMap getSites()
	{
		return sites;
	}

	@Override
	public String toString()
	{
		String result = "MetaMetadataRepository";
		if (file != null)
			result += "[" + file + "]";
		return result;
	}

	/**
	 * This method initializes the mappings from selectors in the repository to selectors in meta
	 * metadata.
	 */
	private void initializeSelectors()
	{
		if (selectorsByName != null)
		{
			for (MetaMetadataSelector selector : selectorsByName)
			{
				String prefName = selector.getPrefName();
				prefName = Pref.lookupString(prefName);
				if (prefName == null)
				{
					prefName = selector.getDefaultPref();
				}
				int numberOfMetametaData = 0;
				if (repositoryByTagName != null)
				{
					ArrayList<MetaMetadataSelector> candidates = new ArrayList<MetaMetadataSelector>();
					MetaMetadata onlyCandidate = null;
					boolean prefNameMetaMetadataFound = false;
					for (MetaMetadata metaMetadata : repositoryByTagName)
					{
						onlyCandidate = metaMetadata;
						if (metaMetadata != null)
						{
							ArrayList<MetaMetadataSelector> mSelectors = metaMetadata.getSelectors();
							for(int i=0; i < mSelectors.size(); i++)
							{
								MetaMetadataSelector mSelector = mSelectors.get(i);
								if (mSelector != null)
								{
									String mSelectorName = mSelector.getName();
									if (mSelectorName != null)
									{
										if (mSelectorName.equals(selector.getName()))
										{
											numberOfMetametaData += 1;
											if (metaMetadata.getName().equals(prefName))
											{
												mSelectors.set(i, selector);
												prefNameMetaMetadataFound = true;
											}
										}
									}
								}
							}
						}
					}
					if (prefNameMetaMetadataFound == false)
					{
						if (numberOfMetametaData == 0)
						{
							Debug.warning(this, "Selector " + selector.getName()
									+ " does not appear to be used in any MetaMetadata.");
						}
						else if (numberOfMetametaData == 1)
						{
							onlyCandidate.addSelector(selector);
						}
						else if (numberOfMetametaData > 1)
						{
							Debug.error(this, "Selector " + selector.getName()
									+ " is ambiguous.  Set the pref_name or use a default_pref.");
						}
					}
				}
			}
		}
	}

	/**
	 * This method makes MetaMetadata objects with no user agent inherit from the repository they are inside of.
	 */
	private void initializeAgents()
	{
		if (repositoryByTagName != null)
		{
			for (MetaMetadata m : repositoryByTagName)
			{
				if (m != null)
				{
					if (m.getUserAgentName() == null)
					{
						m.userAgentName = this.defaultUserAgentName;
						m.userAgentString = this.defaultUserAgentString;
					}
				}
			}
		}
	}

	protected void deserializationPostHook()
	{
		initializeSelectors();
		initializeAgents();
	}

	LinkedMetadataMonitor	linkedMetadataMonitor	= new LinkedMetadataMonitor();

	public LinkedMetadataMonitor getLinkedMetadataMonitor()
	{
		return linkedMetadataMonitor;
	}

	/**
	 * @return the baseDocumentMM
	 */
	public static MetaMetadata getBaseDocumentMM()
	{
		return baseDocumentMM;
	}

	/**
	 * @return the baseImageMM
	 */
	public static MetaMetadata getBaseImageMM()
	{
		return baseImageMM;
	}

	public String getDefaultSearchEngine()
	{
		return searchEngines != null ? searchEngines.getDefaultEngine() : "bing";
	}
}
