/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.net.UserAgent;
import ecologylab.semantics.library.TypeTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.types.element.HashMapState;

/**
 * @author damaraju
 *
 */

public class MetaMetadataRepository extends ElementState
implements PackageSpecifier, TypeTagNames
{
	
	//@xml_collection("meta_metadata") private ArrayList<MetaMetadata> stuff;
	/**
	 * The keys for this hashmap are the values within TypeTagNames.
	 */
	@xml_map("meta_metadata") private HashMapArrayList<String, MetaMetadata> repositoryByTagName; 

	@xml_attribute 		String 						name;
	
	@xml_tag("package") 
	@xml_attribute 		String						packageName;
	
	static final TranslationScope TS = MetaMetadataTranslationScope.get();

	@xml_tag("user_agents") @xml_map("user_agent") private HashMapState<String, UserAgent> userAgentCollection;
	
	private String  defaultUserAgentString;
	
	/**
	 * Prefix collection of the url_bases and have to access from here. 
	 * For now we are using the domain as the key.
	 */
	private HashMapArrayList<String, MetaMetadata>	urlBaseMap 	= new HashMapArrayList<String, MetaMetadata>(); 

	private HashMapArrayList<String, MetaMetadata>	mimeMap 		= new HashMapArrayList<String, MetaMetadata>(); 
	
	private HashMapArrayList<String, MetaMetadata>	suffixMap 	= new HashMapArrayList<String, MetaMetadata>(); 
	
	private TranslationScope						metadataTScope;
	
	//for debugging
	protected static File	REPOSITORY_FILE;
	
	/**
	 * 
	 */
	public MetaMetadataRepository()
	{
		
	}
	
	public static void main(String args[])
	{
		REPOSITORY_FILE = new File(/*PropertiesAndDirectories.thisApplicationDir(), */ "c:/web/code/java/cf/config/semantics/metametadata/debugRepository.xml");
		MetaMetadataRepository 		metaMetaDataRepository = load(REPOSITORY_FILE);
	}
	
	public static MetaMetadataRepository load(File file)
	{
		MetaMetadataRepository result	= null;
		try
		{
			result = (MetaMetadataRepository) ElementState.translateFromXML(file, TS);
//			result.populateURLBaseMap();
//			// necessary to get, for example, fields for document into pdf...
//			result.populateInheritedValues();
//			
//			result.populateMimeMap();
			//For debug
			//this.metaMetaDataRepository.writePrettyXML(System.out);
		} catch (XMLTranslationException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	private void populateMimeMap()
	{
		for (MetaMetadata mm: repositoryByTagName.values())
		{
			if (mm != null)
			{
				ArrayList<String> mimeTypes = mm.mimeTypes;
				if (mimeTypes != null)
				{
					for (String mimeType : mimeTypes)
						mimeMap.put(mimeType, mm);
				}
			}
		}
	}

	private void populateSuffixMap()
	{
		for (MetaMetadata mm: repositoryByTagName.values())
		{
			if (mm != null)
			{
				ArrayList<String> suffixes = mm.suffixes;
				if (suffixes != null)
				{
					for (String suffix : suffixes)
						suffixMap.put(suffix, mm);
				}
			}
		}
	}

	private void populateInheritedValues()
	{
		for (MetaMetadata mm: repositoryByTagName.values())
		{
			if (mm != null)
				recursivePopulate(mm);
		}
	}

	/**
	 * Recursively Copying MetadataFields from srcMetaMetadata to destMetaMetadata.
	 * @param destMetaMetadata
	 * @param srcMetaMetadata
	 */
	protected void recursivePopulate(MetaMetadata destMetaMetadata)
	{
		recursivePopulate(destMetaMetadata, destMetaMetadata.extendsClass);		
	}

	private void recursivePopulate(MetaMetadata destMetaMetadata,
			String superClassName)
	{
		if (superClassName == null || METADATA_TAG.equals(superClassName))
			return;
		
		MetaMetadata superMetaMetadata	= repositoryByTagName.get(superClassName);
		if (superMetaMetadata == null)
			return;

		for (MetaMetadataField superChildMetaMetadataField : superMetaMetadata)
		{
			destMetaMetadata.addChild(superChildMetaMetadataField);
		}
		
		recursivePopulate(destMetaMetadata, superMetaMetadata.extendsClass);
	}
	
	protected void populateURLBaseMap()
	{
		for (MetaMetadata metaMetadata: repositoryByTagName)
		{
			ParsedURL purl = metaMetadata.getUrlBase();
			if(purl != null)
				urlBaseMap.put(purl.host(), metaMetadata);
		}
	}
	
	/**
	 * Get MetaMetadata by ParsedURL if possible.
	 * If that lookup fails, then lookup by tag name, to acquire some default.
	 * 
	 * @param purl
	 * @param tagName
	 * @return
	 */
	public MetaMetadata get(ParsedURL purl, String tagName)
	{
		MetaMetadata result	= getByPURL(purl);
		return (result != null) ? result : getByTagName(tagName);
	}
	
	/**
	 * Find the best matching MetaMetadata for the ParsedURL -- if there is a match.
	 * 
	 * @param parsedURL
	 * @return		appropriate MetaMetadata, or null.
	 */
	public MetaMetadata getByPURL(ParsedURL parsedURL)
	{
		return (parsedURL == null) ? null : urlBaseMap.get(parsedURL.host());
	}
	
	public MetaMetadata getByTagName(String tagName)
	{

		return (tagName == null) ? null : repositoryByTagName.get(tagName);
//		MetaMetadata tempMetaMetadata;
//		try 
//		{
//			if (!repository.containsKey(docType))
//			{
//				setMetaMetaData(docType); //create a new metametadata
//			}
//			else
//			{
//				System.out.println("Already created "+docType);
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		return get(docType);
	}

//	public MetaMetadata getMetaMetaData(docType docType)
//	{
//		
//	}
//	public void setMetaMetaData(String docType) throws XMLTranslationException
//	{
//		MetaMetadata tempMetaMetadata;
//		if (!repository.containsKey(docType))
//		{
//			final TranslationSpace TS = MetaMetadataTranslationSpace.get();
//			System.out.println("Creating MetaMetadata for doctype: "+docType);
//			//TODO depends on the docType
//			String patternXMLFilepath = "C:/web_MMData/code/java/ecologylabSemantics/examplePatternFlickr.xml";
//			tempMetaMetadata = (MetaMetadata) ElementState.translateFromXML(patternXMLFilepath, TS);
//			tempMetaMetadata.setTS(TS);
//			super.put(docType, tempMetaMetadata);
//			//tempMetaMetadata.writePrettyXML(System.out);
//		}
//	}
	
	public Collection<MetaMetadata> values()
	{
		return (repositoryByTagName == null) ? null : repositoryByTagName.values();
	}

	public String packageName()
	{
		return packageName;
	}
	
	public HashMapState<String, UserAgent> userAgents()
	{
		if (userAgentCollection == null)
			userAgentCollection = new HashMapState<String, UserAgent>();
		
		return userAgentCollection;
		
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
	
	@Override
    protected void postTranslationProcessingHook()
    {
		populateURLBaseMap();
		// necessary to get, for example, fields for document into pdf...
		populateInheritedValues();
		
		populateMimeMap();

    }

	public static String documentTag()
	{
		return DOCUMENT_TAG;
	}
	
	public MetaMetadata getByClass(Class<? extends Metadata> thatClass)
	{
		String tag	= metadataTScope.lookupTag(thatClass);
		
		return (tag == null) ? null : repositoryByTagName.get(tag);
	}
	
	public MetaMetadata getByMetadata(Metadata metadata)
	{
		ParsedURL purl	= metadata.getLocation();
	
		MetaMetadata result	= getByPURL(purl);
		if (result == null)
			result 	= getByClass(metadata.getClass());
		return result;
	}
	
	public MetaMetadata lookupByMime(String mimeType)
	{
		return mimeMap.get(mimeType);
	}
	
	public MetaMetadata lookupBySuffix(String suffix)
	{
		return suffixMap.get(suffix);
	}
	
	public TranslationScope translationScope()
	{
		return TS;
	}

	public void setMetadataTranslationScope(TranslationScope metadataTScope)
	{
		this.metadataTScope = metadataTScope;
	}
}
