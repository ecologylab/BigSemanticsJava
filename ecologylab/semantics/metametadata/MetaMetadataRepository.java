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
	
	private PrefixCollection 												urlprefixCollection = new PrefixCollection('/');

	static final TranslationScope										TS	= MetaMetadataTranslationScope.get();

	private TranslationScope												metadataTScope;

	// for debugging
	protected static File														REPOSITORY_FILE;

	
	public static void main(String args[])
	{
		REPOSITORY_FILE = new File(
				/* PropertiesAndDirectories.thisApplicationDir(), */"H:\\web\\code\\java\\cf\\config\\semantics\\metametadata\\defaultRepository.xml");
		MetaMetadataRepository metaMetaDataRepository = load(REPOSITORY_FILE);
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

	public static MetaMetadataRepository load(File file)
	{
		MetaMetadataRepository result = null;
		try
		{
			result = (MetaMetadataRepository) ElementState.translateFromXML(file, TS);
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

	/**
	 * Get MetaMetadata by ParsedURL if possible. If that lookup fails, then lookup by tag name, to
	 * acquire some default.
	 * 
	 * @param purl
	 * @param tagName
	 * @return
	 */
	public MetaMetadata get(ParsedURL purl, String tagName)
	{
		MetaMetadata result = getByPURL(purl);
		return (result != null) ? result : getByTagName(tagName);
	}

	/**
	 * Find the best matching MetaMetadata for the ParsedURL -- if there is a match.
	 * Right now implemented a No Query YRL based pattern matcher.
	 * TODO implement  get by domain too
	 * @param parsedURL
	 * @return appropriate MetaMetadata, or null.
	 */
	public MetaMetadata getByPURL(ParsedURL parsedURL)
	{
		MetaMetadata returnValue=null;
		if (repositoryByURL == null)
		{
			repositoryByURL = new HashMap<String, MetaMetadata>();
			
			for (MetaMetadata metaMetadata : repositoryByTagName)
			{
				ParsedURL purl = metaMetadata.getUrlBase();
				if (purl != null)
					repositoryByURL.put(purl.noAnchorNoQueryPageString(), metaMetadata);
				ParsedURL urlPrefix = metaMetadata.getUrlPrefix();
				if(urlPrefix != null)
				{
						urlprefixCollection.add(urlPrefix);
						repositoryByURL.put(urlPrefix.toString(), metaMetadata);
				}
			}
		}
		if(parsedURL!=null)
		{
			returnValue = repositoryByURL.get(parsedURL.noAnchorNoQueryPageString());
			
			if(returnValue == null)
			{
				//if(urlprefixCollection.match(parsedURL))
				{
					String protocolStrippedURL = parsedURL.toString().split("://")[1];
					
					String key = parsedURL.url().getProtocol()+"://"+urlprefixCollection.getMatchingPhrase(protocolStrippedURL, '/');
					
					returnValue = repositoryByURL.get(key);
				}
			}
		}
		
		return returnValue;
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

	public MetaMetadata getByClass(Class<? extends Metadata> thatClass)
	{
		String tag = metadataTScope.lookupTag(thatClass);

		return (tag == null) ? null : repositoryByTagName.get(tag);
	}

	public MetaMetadata getByMetadata(Metadata metadata)
	{
		ParsedURL purl = metadata.getLocation();

		MetaMetadata result = getByPURL(purl);
		if (result == null)
			result = getByClass(metadata.getClass());
		return result;
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
		return TS;
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
