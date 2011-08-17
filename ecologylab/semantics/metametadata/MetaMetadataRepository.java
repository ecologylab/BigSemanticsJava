/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.collections.MultiAncestorScope;
import ecologylab.collections.PrefixCollection;
import ecologylab.collections.PrefixPhrase;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.net.UserAgent;
import ecologylab.semantics.collecting.CookieProcessing;
import ecologylab.semantics.collecting.LinkedMetadataMonitor;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.semantics.collecting.SemanticsSiteMap;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.simpl_inherit;
import ecologylab.textformat.NamedStyle;

/**
 * The repository of meta-metadata wrappers.
 * 
 * Wrapper definitions can scatter over multiple files, while loadFromXXX() methods will collect
 * information and assemble a unified representation.
 * 
 * @author damaraju
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@simpl_inherit
public class MetaMetadataRepository extends ElementState
implements PackageSpecifier, DocumentParserTagNames
{

	private static final String																	DEFAULT_STYLE_NAME							= "default";

	protected static final int																	BUFFER_SIZE											= 4096;

	private static MetaMetadata																	baseDocumentMM;

	private static MetaMetadata																	baseImageMM;

	// [region] de/serializable data fields.

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

	/**
	 * user agent strings.
	 */
	@simpl_map("user_agent")
	private Map<String, UserAgent>															userAgents;

	/**
	 * default user agent string name.
	 */
	@simpl_scalar
	private String																							defaultUserAgentName;

	/**
	 * definition of search engines.
	 */
	@simpl_composite
	private SearchEngines																				searchEngines;

	/**
	 * definition of sites.
	 */
	@simpl_map("site")
	private SemanticsSiteMap																		sites;

	/**
	 * definition of name styles.
	 */
	@simpl_map("named_style")
	private Map<String, NamedStyle>															namedStyles;

	/**
	 * definition of cookie processors.
	 */
	@simpl_nowrap
	@simpl_collection("cookie_processing")
	private ArrayList<CookieProcessing>													cookieProcessors;

	/**
	 * definition of selectors. selectors are used to select a meta-metadata based on URL pattern or
	 * MIME type / suffix.
	 */
	@simpl_map("selector")
	@simpl_nowrap
	private HashMapArrayList<String, MetaMetadataSelector>			selectorsByName;

	/**
	 * The map from meta-metadata name (currently simple name, but might be extended to fully
	 * qualified name in the future) to meta-metadata objects. This collection is filled during the
	 * loading process.
	 * 
	 * @see {@code MetaMetadata}, {@code Mappable}
	 */
	@simpl_map("meta_metadata")
	@simpl_nowrap
	private HashMapArrayList<String, MetaMetadata>							repositoryByName;

	// [endregion]

	/**
	 * package mmd scopes.
	 */
	private Map<String, MultiAncestorScope<MetaMetadata>>				packageMmdScopes;

	// [region] repository maps generated from repositoryByName. used for look-up.

	/**
	 * meta-metadata sorted by metadata class name.
	 */
	private HashMap<String, MetaMetadata>												repositoryByClassName						= new HashMap<String, MetaMetadata>();

	/**
	 * Repository by MIME type.
	 */
	private HashMap<String, MetaMetadata>												repositoryByMime								= new HashMap<String, MetaMetadata>();

	/**
	 * Repository by suffix.
	 */
	private HashMap<String, MetaMetadata>												repositoryBySuffix							= new HashMap<String, MetaMetadata>();

	/**
	 * Collection of URL prefixes.
	 */
	private PrefixCollection																		urlPrefixCollection							= new PrefixCollection('/');

	/**
	 * Repository of documents with domain as key.
	 */
	private HashMap<String, MetaMetadata>												documentRepositoryByDomain			= new HashMap<String, MetaMetadata>();

	/**
	 * Repository of documents with noAnchorNoQuery URL string as key.
	 */
	private HashMap<String, MetaMetadata>												documentRepositoryByUrlStripped	= new HashMap<String, MetaMetadata>();

	/**
	 * Repository of documents with URL pattern as key.
	 */
	private HashMap<String, ArrayList<RepositoryPatternEntry>>	documentRepositoryByPattern			= new HashMap<String, ArrayList<RepositoryPatternEntry>>();

	/**
	 * Repository of images with noAnchroNoQuery URL string as key.
	 */
//	private HashMap<String, MetaMetadata>												imageRepositoryByUrlStripped		= new HashMap<String, MetaMetadata>();

	/**
	 * Repository of images with URL pattern as key.
	 */
//	private HashMap<String, ArrayList<RepositoryPatternEntry>>	imageRepositoryByPattern				= new HashMap<String, ArrayList<RepositoryPatternEntry>>();

	// [endregion]

	/**
	 * The metadata translation scope used by this repository.
	 */
	private TranslationScope																		metadataTScope;

	private LinkedMetadataMonitor																linkedMetadataMonitor						= new LinkedMetadataMonitor();

	/**
	 * for debug.
	 */
	private File																								file;

	private static boolean																			initializedTypes;

	static
	{
		initializeTypes();
	}

	public static synchronized void initializeTypes()
	{
		if (!initializedTypes)
		{
			initializedTypes = true;
			MetadataScalarType.init(); // register metadata-specific scalar types
			ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope.get();
		}
	}

	// [region] basic methods (getter/setters, etc.).
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String packageName()
	{
		return packageAttribute;
	}

	public NamedStyle getDefaultStyle()
	{
		return lookupStyle(DEFAULT_STYLE_NAME);
	}

	public NamedStyle lookupStyle(String styleName)
	{
		return namedStyles == null ? null : namedStyles.get(styleName);
	}
	
	public String getDefaultUserAgentString()
	{
		if (defaultUserAgentName == null)
		{
			for (UserAgent userAgent : getUserAgents().values())
			{
				if (userAgent.isDefaultAgent())
				{
					defaultUserAgentName = userAgent.name();
					break;
				}
			}
		}
		return getUserAgentString(defaultUserAgentName);
	}

	public String getUserAgentString(String name)
	{
		return getUserAgents().get(name).userAgentString();
	}

	private Map<String, UserAgent> getUserAgents()
	{
		if (userAgents == null)
			userAgents = new HashMap<String, UserAgent>();
		return userAgents;
	}

	public String getDefaultSearchEngine()
	{
		return searchEngines != null ? searchEngines.getDefaultEngine() : "bing";
	}

	public SearchEngine getSearchEngine(String engineName)
	{
		if (searchEngines != null)
		{
			return searchEngines.getEngine(engineName);
		}
		return null;
	}

	public SemanticsSiteMap getSites()
	{
		return sites;
	}

	public SemanticsSite getSite(Document document, SemanticsGlobalScope semanticsSessionScope)
	{
		return sites.getOrConstruct(document, semanticsSessionScope);
	}

	public TranslationScope metadataTranslationScope()
	{
		return metadataTScope;
	}

	public Set<String> keySet()
	{
		return (repositoryByName == null) ? null : repositoryByName.keySet();
	}

	public Collection<MetaMetadata> values()
	{
		return (repositoryByName == null) ? null : repositoryByName.values();
	}
	
	public Map<String, MultiAncestorScope<MetaMetadata>> getPackageMmdScopes()
	{
		return this.packageMmdScopes;
	}
	
	/**
	 * 
	 * @return the monitor used for linking metadata.
	 */
	public LinkedMetadataMonitor getLinkedMetadataMonitor()
	{
		return linkedMetadataMonitor;
	}
	
	// [endregion]
	
	public static interface RepositoryFileLoader
	{
		MetaMetadataRepository loadRepositoryFile(File file) throws SIMPLTranslationException, IOException;
	}
	
	public static RepositoryFileLoader	XML_FILE_LOADER	= new RepositoryFileLoader() {
		@Override
		public MetaMetadataRepository loadRepositoryFile( File file) throws SIMPLTranslationException
		{
			return (MetaMetadataRepository) MetaMetadataTranslationScope .get().deserialize(file);
		}
	};
	
	public static RepositoryFileLoader JSON_FILE_LOADER = new RepositoryFileLoader() {
		@Override
		public MetaMetadataRepository loadRepositoryFile(File file) throws SIMPLTranslationException, IOException
		{
			StringBuilder json = new StringBuilder();
			char[] buffer = new char[BUFFER_SIZE];
			FileReader reader = new FileReader(file);
			while (true)
			{
				int n = reader.read(buffer, 0, BUFFER_SIZE);
				if (n < 0)
					break;
				json.append(buffer, 0, n);
			}
			reader.close();
			return (MetaMetadataRepository) MetaMetadataTranslationScope.get().deserializeCharSequence(json, FORMAT.JSON);
		}
	};
	
	protected static MetaMetadataRepository loadFromFiles(List<File> files, RepositoryFileLoader fileLoader)
	{
		MetaMetadataRepository result = new MetaMetadataRepository();
		result.repositoryByName = new HashMapArrayList<String, MetaMetadata>();
		result.packageMmdScopes = new HashMap<String, MultiAncestorScope<MetaMetadata>>();
		
		for (File file : files)
		{
			if (file == null || !file.exists())
			{
				result.warning("ignoring " + file);
				continue;
			}
			
			println("MetaMetadataRepository read:\t" + file.getPath());
			
			try
			{
				MetaMetadataRepository repoData = fileLoader.loadRepositoryFile(file);
				if (repoData != null)
				{
					repoData.file = file;
					
					// sort meta-metadata into result.repositoryByName and mmd scope for that package.
					if (repoData.repositoryByName != null)
					{
						for (String mmdName : repoData.repositoryByName.keySet())
						{
							MetaMetadata mmd = repoData.repositoryByName.get(mmdName);
							mmd.setFile(file);
							mmd.setParent(result);
							mmd.setRepository(result);

							String packageName = mmd.packageName();
							if (packageName == null)
							{
								packageName = repoData.packageName();
								if (packageName == null)
									throw new MetaMetadataException("no package name specified for " + mmd);
								mmd.setPackageName(packageName);
							}
							MultiAncestorScope<MetaMetadata> packageMmdScope = result.packageMmdScopes
									.get(packageName);
							if (packageMmdScope == null)
							{
								packageMmdScope = new MultiAncestorScope<MetaMetadata>(result.repositoryByName);
								result.packageMmdScopes.put(packageName, packageMmdScope);
							}

							switch (mmd.visibility)
							{
							case GLOBAL:
							{
								MetaMetadata existingMmd = result.repositoryByName.get(mmdName);
								if (existingMmd != null && existingMmd != mmd)
									throw new MetaMetadataException("meta-metadata already exists: " + mmdName
											+ " in " + file);
								result.repositoryByName.put(mmdName, mmd);
								break;
							}
							case PACKAGE:
							{
								MetaMetadata existingMmd = packageMmdScope.get(mmdName);
								if (existingMmd != null && existingMmd != mmd)
									throw new MetaMetadataException("meta-metadata already exists: " + mmdName
											+ " in " + file);
								packageMmdScope.put(mmdName, mmd);
								break;
							}
							}
						}

						for (MetaMetadata mmd : repoData.repositoryByName.values())
						{
							MultiAncestorScope<MetaMetadata> packageMmdScope = result.packageMmdScopes.get(mmd
									.packageName());
							mmd.setMmdScope(packageMmdScope);
						}
					}

					// combine other parts
					result.integrateRepositoryWithThis(repoData);
				}
			}
			catch (SIMPLTranslationException e)
			{
				Debug.error("MetaMetadataRepository", "translating repository source file " + file.getAbsolutePath());
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// initialize meta-metadata look-up maps
//		result.initializeLocationBasedMaps(); // cannot do this since it needs the metadata TScope.
		result.initializeSuffixAndMimeBasedMaps();
		
		// We might want to do this only if we have some policies worth enforcing.
		ParsedURL.cookieManager.setCookiePolicy(CookieProcessing.semanticsCookiePolicy);
	
		// FIXME -- get rid of this?!
		Metadata.setRepository(result);
		
		baseDocumentMM	= result.getMMByName(DOCUMENT_TAG);
		baseImageMM			= result.getMMByName(IMAGE_TAG);
		
		return result;
	}

	/**
	 * Load meta-metadata from repository files from a directly. Loads the base level xml files first,
	 * then the xml files in the repositorySources folder and lastly the files in the powerUser
	 * folder. Does not build repository maps, because this requires a Metadata TranslationScope,
	 * which comes from ecologylabGeneratedSemantics.
	 * 
	 * @param dir
	 *          the repository directory.
	 * @param fileNameSuffix
	 * @param fileLoader
	 * @return
	 */
	public static MetaMetadataRepository loadFromDir(File dir, final String fileNameSuffix, RepositoryFileLoader fileLoader)
	{
		if (!dir.exists())
		{
			throw new MetaMetadataException("MetaMetadataRepository directory does not exist : " + dir.getAbsolutePath());
		}
		
		println("MetaMetadataRepository directory : " + dir + "\n");
	
		FileFilter xmlFilter = new FileFilter()
		{
			public boolean accept(File dir)
			{
				return dir.getName().endsWith(fileNameSuffix);
			}
		};
		
		File repositorySources = new File(dir, "repositorySources");
		File powerUserDir = new File(dir, "powerUser");
		
		List<File> allFiles = new ArrayList<File>();
		addFilesInDirToList(dir, xmlFilter, allFiles);
		addFilesInDirToList(repositorySources, xmlFilter, allFiles);
		addFilesInDirToList(powerUserDir, xmlFilter, allFiles);
		
		return loadFromFiles(allFiles, fileLoader);
	}

	/**
	 * Load meta-metadata from repository files from a set of files. Does not build location-based
	 * repository maps, because this requires a Metadata TranslationScope, which comes from
	 * ecologylabGeneratedSemantics.
	 * 
	 * @param files
	 * @return
	 */
	public static MetaMetadataRepository loadXmlFromFiles(File... files)
	{
		return loadFromFiles(Arrays.asList(files), XML_FILE_LOADER);
	}

	public static MetaMetadataRepository loadXmlFromDir(File dir)
	{
		return loadFromDir(dir, ".xml", XML_FILE_LOADER);
	}
	
	public static MetaMetadataRepository loadJsonFromFiles(File... files)
	{
		return loadFromFiles(Arrays.asList(files), JSON_FILE_LOADER);
	}

	public static MetaMetadataRepository loadJsonFromDir(File dir)
	{
		return loadFromDir(dir, ".json", JSON_FILE_LOADER);
	}
	
	private static void addFilesInDirToList(File dir, FileFilter filter, List<File> buf)
	{
		if (dir == null || !dir.exists())
			return;
		for (File f : dir.listFiles(filter))
			buf.add(f);
	}

	/**
	 * Combines the data stored in the parameter repository into this repository, except for
	 * repositoryByName.
	 * 
	 * @param theOtherRepository
	 * @return
	 */
	private void integrateRepositoryWithThis(MetaMetadataRepository theOtherRepository)
	{
		this.userAgents = combineMap(this.userAgents, theOtherRepository.userAgents);

		if (this.searchEngines != null && theOtherRepository.searchEngines != null)
		{
			String theOtherDefaultEngine = theOtherRepository.searchEngines.getDefaultEngine();
			if (theOtherDefaultEngine != null)
			{
				if (this.searchEngines.getDefaultEngine() == null)
					this.searchEngines.setDefaultEngine(theOtherDefaultEngine);
				else
					warning("default engine already defined, ignoring the one defined in " + theOtherRepository);
			}
			this.searchEngines.setSearchEngines(combineMap(this.searchEngines.getSearchEngines(), theOtherRepository.searchEngines.getSearchEngines()));
		}

		this.namedStyles = combineMap(this.namedStyles, theOtherRepository.namedStyles);

		this.sites = combineMap(this.sites, theOtherRepository.sites);
	}
	
	/**
	 * copy all items in srcMap to destMap, and report duplicate elements.
	 * 
	 * @param destMap
	 * @param srcMap
	 * @param controller
	 *          the filter used to tune the combining process.
	 * @return the combined map. if destMap != null, this is destMap; or this is srcMap.
	 */
	private <KT, VT, MT extends Map<KT, VT>> MT combineMap(MT destMap, MT srcMap)
	{
		if (destMap == null)
			return srcMap;
		
		if (srcMap != null)
		{
			for (KT key : srcMap.keySet())
			{
				VT value = srcMap.get(key);
				if (destMap.containsKey(key))
				{
					error(value.getClass().getSimpleName() + " DEFINED TWICE: " + key);
				}
				else
				{
					destMap.put(key, value);
				}
			}
		}
		return destMap;
	}
	
	/**
	 * traverse the repository and generate a translation scope from it. note that the graph switch
	 * should be turned on because there probably will be type graphs in the meta-metadata type
	 * system.
	 * 
	 * @param TSName
	 *          the name of the resulted translation scope.
	 * @return
	 */
	public TranslationScope traverseAndGenerateTranslationScope(String TSName)
	{
		// init the TScope with built-ins, and bind descriptors for built-ins.
		TranslationScope metadataBuiltInTScope = MetadataBuiltinsTranslationScope.get();
		TranslationScope ts = TranslationScope.get(TSName, new TranslationScope[] {metadataBuiltInTScope});
		for (MetaMetadata metaMetadata : repositoryByName)
		{
			if (metaMetadata.isBuiltIn())
				metaMetadata.bindMetadataClassDescriptor(ts);
		}
		
		// inheritance.
		traverseAndInheritMetaMetadata();
		
		// generate translation scopes.
		for (MetaMetadata metaMetadata : new ArrayList<MetaMetadata>(repositoryByName.values()))
			metaMetadata.findOrGenerateMetadataClassDescriptor(ts);
		
		return ts;
	}

	/**
	 * Recursively bind MetadataClassDescriptors to all MetaMetadata. Perform other initialization.
	 * 
	 * @param metadataTScope
	 *          the (global) metadata translation scope used for binding.
	 */
	public void bindMetadataClassDescriptorsToMetaMetadata(TranslationScope metadataTScope)
	{
		this.metadataTScope = metadataTScope;
		
		traverseAndInheritMetaMetadata();
		
		// use another copy because we may modify the scope during the process
		ArrayList<MetaMetadata> mmds = new ArrayList<MetaMetadata>(this.repositoryByName.values());
		for (MetaMetadata mmd : mmds)
		{
			MetadataClassDescriptor mcd = mmd.bindMetadataClassDescriptor(metadataTScope);
			if (mcd == null)
			{
				warning("Cannot bind metadata class descriptor for " + mmd);
				this.repositoryByName.remove(mmd.getName());
			}
		}
		
		// other initialization stuffs
		for (MetaMetadata mmd : repositoryByName.values())
		{
			MetadataClassDescriptor mcd = mmd.getMetadataClassDescriptor();
			if (mcd != null)
				repositoryByClassName.put(mcd.getDescribedClass().getName(), mmd);
			
			mmd.setUpLinkWith(this);
		}

		initializeLocationBasedMaps();
	}
	
	/**
	 * traverse the repository and do inheritance on each meta-metadata.
	 */
	public void traverseAndInheritMetaMetadata()
	{
		if (this.repositoryByName != null && this.repositoryByName.size() > 0)
		{
			// make another copy because we may modify the collection (e.g. for adding inline definitions)
			ArrayList<MetaMetadata> mmds = new ArrayList<MetaMetadata>(repositoryByName.values());
			for (MetaMetadata metaMetadata : mmds)
			{
				metaMetadata.setRepository(this);
				metaMetadata.inheritMetaMetadata();
			}
		}
	}

	public MetaMetadata getMMByName(String name)
	{
		if (name == null)
			return null;
		return repositoryByName.get(name);
	}

	public MetaMetadata getMMByClass(Class<? extends Metadata> metadataClass)
	{
		if (metadataClass == null)
			return null;
		return repositoryByClassName.get(metadataClass.getName());
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
			result = getMMByName(tagName);

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

	public MetaMetadata getDocumentMM(Document metadata)
	{
		return getDocumentMM(metadata.getLocation(), metadataTScope.getTag(metadata.getClass()));
	}

	public MetaMetadata getCompoundDocumentMM(ParsedURL purl)
	{
		return getDocumentMM(purl, COMPOUND_DOCUMENT_TAG);
	}

	public MetaMetadata getClippableDocumentMM(ParsedURL purl, String tagName)
	{
		return getDocumentMM(purl, tagName);
	}
	/*
	public MetaMetadata getOldClippableDocumentMM(ParsedURL purl, String tagName)
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
		return (result != null) ? result : getMMByName(tagName);
	}
*/
	public MetaMetadata getImageMM(ParsedURL purl)
	{
		return getClippableDocumentMM(purl, IMAGE_TAG);
	}

	public MetaMetadata getMMBySuffix(String suffix)
	{
		return repositoryBySuffix.get(suffix);
	}

	public MetaMetadata getMMByMime(String mimeType)
	{
		return repositoryByMime.get(mimeType);
	}

	public Metadata constructByName(String name)
	{
		Metadata result						= null;
		MetaMetadata metaMetadata	= getMMByName(name);
		if (metaMetadata != null)
		{
			result									= metaMetadata.constructMetadata(metadataTScope);
		}
		return result;
	}

	public Metadata constructBySuffix(String suffix)
	{
		MetaMetadata metaMetadata = this.getMMBySuffix(suffix);
		return metaMetadata == null ? null : metaMetadata.constructMetadata(metadataTScope);
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

	public Document constructDocumentBySuffix(String suffix)
	{
		Metadata result = constructBySuffix(suffix);
		return result instanceof Document ? (Document) result : null;
	}

	public Document constructDocumentByMime(String mimeType)
	{
		MetaMetadata metaMetadata = this.getMMByMime(mimeType);
		return metaMetadata == null ? null : (Document) metaMetadata.constructMetadata(metadataTScope);
	}

	/**
	 * Construct a document by location.
	 * If nothing particular turns up, make it either a CompoundDocument, or, if isImage, an Image.
	 * 
	 * @param purl
	 * @param isImage
	 * @return
	 */
	public Document constructDocument(ParsedURL purl, boolean isImage)
	{
		if (purl.isImg() || isImage)
			return constructImage(purl);
		
		MetaMetadata metaMetadata = getCompoundDocumentMM(purl);
		Document result = (Document) metaMetadata.constructMetadata(metadataTScope);
		result.setLocation(purl);
		return result;
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
	 * Initializes HashMaps for MetaMetadata selectors by URL or pattern. Uses the ClippableDocument
	 * and Document base classes to ensure that maps are only filled with appropriate matching
	 * MetaMetadata.
	 */
	private void initializeLocationBasedMaps()
	{
		for (MetaMetadata metaMetadata : repositoryByName)
		{
			// metaMetadata.inheritMetaMetadata(this);

			// Class<? extends Metadata> metadataClass = metaMetadata.getMetadataClass(metadataTScope);
			Class<? extends Metadata> metadataClass = metaMetadata.getMetadataClassDescriptor().getDescribedClass();
			if (metadataClass == null)
			{
				continue;
			}

			HashMap<String, MetaMetadata> repositoryByUrlStripped;
			HashMap<String, ArrayList<RepositoryPatternEntry>> repositoryByPattern;

//			if (Image.class.isAssignableFrom(metadataClass))
//			{
//				repositoryByUrlStripped = imageRepositoryByUrlStripped;
//				repositoryByPattern = imageRepositoryByPattern;
//			}
//			else if (Document.class.isAssignableFrom(metadataClass))
//			{
//				repositoryByUrlStripped = documentRepositoryByUrlStripped;
//				repositoryByPattern = documentRepositoryByPattern;
//			}
//			else
//				continue;

			repositoryByUrlStripped = documentRepositoryByUrlStripped;
			repositoryByPattern = documentRepositoryByPattern;

			// We need to check if something is there already
			// if something is there, then we need to check to see if it has its cf pref set
			// if not, then if I am null then I win

			ArrayList<MetaMetadataSelector> selectors = metaMetadata.getSelectors();
			for (MetaMetadataSelector selector : selectors)
			{
				String reselectMetaMetadataName = selector.getReselectMetaMetadataName();
				if (reselectMetaMetadataName != null)
				{
					MetaMetadata reselectMetaMetadata = repositoryByName.get(reselectMetaMetadataName);
					if (reselectMetaMetadata != null)
					{
						reselectMetaMetadata.addReselectEntry(selector, metaMetadata);
					}
					continue;
				}

				ParsedURL strippedPurl = selector.getUrlStripped();
				if (strippedPurl != null)
				{
					String noAnchorNoQueryPageString = strippedPurl.noAnchorNoQueryPageString();
					repositoryByUrlStripped.put(noAnchorNoQueryPageString, metaMetadata);
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
//						repositoryByUrlStripped.put(urlPathTree.toString(), metaMetadata);
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
	 * This initalizes the map based on mime type and suffix.
	 */
	private void initializeSuffixAndMimeBasedMaps()
	{
		if (repositoryByName == null)
			return;

		for (MetaMetadata metaMetadata : repositoryByName)
		{
			ArrayList<String> suffixes = metaMetadata.getSuffixes();
			if (suffixes != null)
			{
				for (String suffix : suffixes)
				{
					// FIXME-- Ask whether the suffix and mime should be inherited or not
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
					// FIXME -- Ask whether the suffix and mime should be inherited or not
					if (!repositoryByMime.containsKey(mimeType))
					{
						repositoryByMime.put(mimeType, metaMetadata);
						metaMetadata.setMmSelectorType(MMSelectorType.SUFFIX_OR_MIME);
					}
				}
			}
		}
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
				if (repositoryByName != null)
				{
					MetaMetadata onlyCandidate = null;
					boolean prefNameMetaMetadataFound = false;
					for (MetaMetadata metaMetadata : repositoryByName)
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
							Debug.warning(this, "Selector " + selector.getName() + " does not appear to be used in any MetaMetadata.");
						}
						else if (numberOfMetametaData == 1)
						{
							onlyCandidate.addSelector(selector);
						}
						else if (numberOfMetametaData > 1)
						{
							Debug.error(this, "Selector " + selector.getName() + " is ambiguous.  Set the pref_name or use a default_pref.");
						}
					}
				}
			}
		}
	}

	protected void deserializationPostHook()
	{
		initializeSelectors();
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
	
}
