/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import ecologylab.collections.PrefixCollection;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.net.UserAgent;
import ecologylab.semantics.library.TypeTagNames;
import ecologylab.semantics.metadata.Document;
import ecologylab.semantics.metadata.Media;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.textformat.NamedStyle;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.types.element.ArrayListState;
import ecologylab.xml.types.element.HashMapState;

/**
 * @author damaraju
 * 
 */

public class MetaMetadataRepository extends ElementState implements PackageSpecifier, TypeTagNames
{
	private static final String	DEFAULT_STYLE_NAME	= "default";

	/**
	 * The name of the repository.
	 */
	@xml_attribute
	private String																	name;

	/**
	 * The package in which the class files have to be generated.
	 */
	@xml_tag("package")
	@xml_attribute
	private String																	packageName;

	@xml_nested 
	private HashMapState<String, UserAgent> 				userAgents; 
	
	@xml_nested
	private HashMapState<String,SearchEngine>				searchEngines;
	
	@xml_nested
	private HashMapState<String, NamedStyle> 				namedStyles;
	
	private String																	defaultUserAgentString = null;

	/**
	 * The keys for this hashmap are the values within TypeTagNames.
	 */
	@xml_map("meta_metadata")
	private HashMapArrayList<String, MetaMetadata>	repositoryByTagName;
	
	private HashMap<String, MetaMetadata>						repositoryByURL = null;
	
	private HashMap<String,MetaMetadata>						mediaRepositoryByURL=null;
	
	private PrefixCollection 												urlprefixCollection = new PrefixCollection('/');

	static final TranslationScope										META_METADATA_TSCOPE	= MetaMetadataTranslationScope.get();

	private TranslationScope												metadataTScope;

	// for debugging
	protected static File														REPOSITORY_FILE;

	
	public static void main(String args[])
	{
		REPOSITORY_FILE = new File(
				/* PropertiesAndDirectories.thisApplicationDir(), */"C:\\abhinavCode\\cf\\config\\semantics\\metametadata\\metaMetadataRepository.xml");
		MetaMetadataRepository metaMetaDataRepository = load(REPOSITORY_FILE, null);
		try
		{
			metaMetaDataRepository.writePrettyXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static MetaMetadataRepository load(File file, TranslationScope metadataTScope)
	{
		MetaMetadataRepository result = null;
		try
		{
			result = (MetaMetadataRepository) ElementState.translateFromXML(file, META_METADATA_TSCOPE);
			// result.populateURLBaseMap();
			// // necessary to get, for example, fields for document into pdf...
			// result.populateInheritedValues();
			//			
			// result.populateMimeMap();
			// For debug
			// this.metaMetaDataRepository.writePrettyXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			e.printStackTrace();
		}
		result.metadataTScope	= metadataTScope;
		return result;
	}

	/**
	 * Recursively Copying MetadataFields from srcMetaMetadata to destMetaMetadata.
	 * 
	 * @param destMetaMetadata
	 * @param srcMetaMetadata
	 */
	protected void recursivePopulate(MetaMetadata destMetaMetadata)
	{
		// recursivePopulate(destMetaMetadata, destMetaMetadata.getExtendsClass());
	}


	public MetaMetadata getMM(Class<? extends Metadata> thatClass)
	{
		String tag = metadataTScope.lookupTag(thatClass);

		return (tag == null) ? null : repositoryByTagName.get(tag);
	}

	/**
	 * Get MetaMetadata by ParsedURL if possible. If that lookup fails, then lookup by tag name, to
	 * acquire some default.
	 * 
	 * @param purl
	 * @param tagName
	 * @return
	 */
	public MetaMetadata getDocumentMM(ParsedURL purl, String tagName)
	{
		MetaMetadata result = null;
		repositoryByURL = initializeRepository(repositoryByURL);
		if(purl!=null && !purl.isFile())
		{
			result = repositoryByURL.get(purl.noAnchorNoQueryPageString());
			
			if(result == null)
			{
				String protocolStrippedURL = purl.toString().split("://")[1];					
				String matchingPhrase = urlprefixCollection.getMatchingPhrase(protocolStrippedURL, '/');
				//FIXME -- andruid needs abhinav to explain this code better and make more clear!!!
				if(matchingPhrase != null)
				{
					String key = purl.url().getProtocol()+"://"+matchingPhrase;
					
					result = repositoryByURL.get(key);
				}
			}
		}
		return (result != null) ? result : getByTagName(tagName);
	}
//TODO implement  get by domain too
	/**
	 * Find the best matching MetaMetadata for the ParsedURL -- if there is a match.
	 * Right now implemented a No Query YRL based pattern matcher.
	 * <p/>
	 * Usually, the preferred method to use is the one that takes a tagName as the 2nd argument,
	 * in case the lookup fails.
	 * 
	 * @param purl
	 * @return appropriate MetaMetadata, or null.
	 */
	public MetaMetadata getDocumentMM(ParsedURL purl)
	{
		return getDocumentMM(purl, DOCUMENT_TAG);
	}

	public MetaMetadata getDocumentMM(Document metadata)
	{
		return getDocumentMM(metadata.getLocation(), metadataTScope.lookupTag(metadata.getClass()));
	}

	public MetaMetadata getImageMM(ParsedURL purl)
	{
		return getMediaMM(purl, IMAGE_TAG);
	}
	public MetaMetadata getMediaMM(ParsedURL purl, String tagName)
	{
		MetaMetadata result		= null;
		mediaRepositoryByURL 	= initializeRepository(mediaRepositoryByURL);
		if(purl!=null && !purl.isFile())
		{
			result = mediaRepositoryByURL.get(purl.noAnchorNoQueryPageString());
			
			if(result == null)
			{
				//if(urlprefixCollection.match(parsedURL))
				{
					String protocolStrippedURL = purl.toString().split("://")[1];
					
					String key = purl.url().getProtocol()+"://"+urlprefixCollection.getMatchingPhrase(protocolStrippedURL, '/');
					
					result = mediaRepositoryByURL.get(key);
				}
			}
		}
		return (result != null) ? result : getByTagName(tagName);
	}
	
	/**
	 * Look-up MetaMetadata for this purl.
	 * If there is no special MetaMetadata, use Image.
	 * Construct Metadata of the correct subtype, base on the MetaMetadata.
	 * 
	 * @param purl
	 * @return		A Metadata object, either of type Image, or a subclass.
	 * 						Never null!
	 */
	public Media constructImage(ParsedURL purl)
	{
		MetaMetadata metaMetadata	= getImageMM(purl);
		Media result							= null;
		if (metaMetadata != null)
		{
			result									= (Media) metaMetadata.constructMetadata(metadataTScope);
		}
		return result;
	}
	
	/**
	 * Look-up MetaMetadata for this purl.
	 * If there is no special MetaMetadata, use Document.
	 * Construct Metadata of the correct subtype, base on the MetaMetadata.
	 * Set its location field to purl.
	 * 
	 * @param purl
	 * @return
	 */
	public Document constructDocument(ParsedURL purl)
	{
		MetaMetadata metaMetadata	= getDocumentMM(purl);
		Document result							= null;
		if (metaMetadata != null)
		{
			result									= (Document) metaMetadata.constructMetadata(metadataTScope);
			result.setLocation(purl);
		}
		return result;
	}
	
	private HashMap<String, MetaMetadata> initializeRepository(HashMap<String, MetaMetadata>	repository)
	{
		if (repository == null)
		{
			repository = new HashMap<String, MetaMetadata>();
			
			for (MetaMetadata metaMetadata : repositoryByTagName)
			{
				ParsedURL purl = metaMetadata.getUrlBase();
				if (purl != null)
					repository.put(purl.noAnchorNoQueryPageString(), metaMetadata);
				ParsedURL urlPrefix = metaMetadata.getUrlPrefix();
				if(urlPrefix != null)
				{
						urlprefixCollection.add(urlPrefix);
						repository.put(urlPrefix.toString(), metaMetadata);
				}
			}
		}
		return repository;
	}
	public MetaMetadata getByTagName(String tagName)
	{

		return (tagName == null) ? null : repositoryByTagName.get(tagName);
	}

	public Collection<MetaMetadata> values()
	{
		return (repositoryByTagName == null) ? null : repositoryByTagName.values();
	}

	public String packageName()
	{
		return packageName;
	}

	@Override
	protected void postTranslationProcessingHook()
	{

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

	public TranslationScope translationScope()
	{
		return META_METADATA_TSCOPE;
	}

	public void setMetadataTranslationScope(TranslationScope metadataTScope)
	{
		this.metadataTScope = metadataTScope;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName()
	{
		return packageName;
	}

	/**
	 * @param packageName
	 *          the packageName to set
	 */
	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}
	
	public NamedStyle lookupStyle(String styleName)
	{
		return namedStyles.get(styleName);
	}
	
	public NamedStyle getDefaultStyle()
	{
		return namedStyles.get(DEFAULT_STYLE_NAME);
	}
	
	public HashMapState<String, UserAgent> userAgents()
	{
		if (userAgents == null)
			userAgents = new HashMapState<String, UserAgent>();
		
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
			for(UserAgent userAgent : userAgents().values())
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

	public String getSearchURL(String searchEngine)
	{
		if(searchEngines!=null)
		{
			return searchEngines.get(searchEngine).getUrlPrefix();
		}
		return null;
	}
	
	public String getSearchURLSufix(String searchEngine)
	{
		String returnVal="";
		if(searchEngines!=null)
		{
			return searchEngines.get(searchEngine).getUrlSuffix();
		}
		return returnVal;
	}
	
	public String getNumResultString(String searchEngine)
	{
		String returnVal="";
		if(searchEngine!=null)
			return searchEngines.get(searchEngine).getNumResultString();
		return returnVal;
	}
	
	public String getStartString(String searchEngine)
	{
		String returnVal="";
		if(searchEngine!=null)
			return searchEngines.get(searchEngine).getStartString();
		return returnVal;
	}
}
