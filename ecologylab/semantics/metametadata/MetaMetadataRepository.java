/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.util.Collection;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.net.UserAgent;
import ecologylab.semantics.library.TypeTagNames;
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
	@xml_map("meta_metadata") private HashMapArrayList<String, MetaMetadata> repository; 

	@xml_attribute 		String 						name;
	
	@xml_tag("package") 
	@xml_attribute 		String						packageName;
	
	static final TranslationScope TS = MetaMetadataTranslationScope.get();

	@xml_tag("user_agents") @xml_map("user_agent") private HashMapState<String, UserAgent> userAgentCollection;
	
	private String  defaultUserAgentString;
	
	/**
	 * TODO
	 * Have to create the prefix collection of the url_bases and have to access from here. 
	 * For now we are using the domain as the key.
	 */
	private HashMapArrayList<String, MetaMetadata>	purlMapRepository = new HashMapArrayList<String, MetaMetadata>(); 
	
	/**
	 * 
	 */
	public MetaMetadataRepository()
	{
		
	}
	
	public static MetaMetadataRepository load(File file)
	{
		MetaMetadataRepository result	= null;
		try
		{
			result = (MetaMetadataRepository) ElementState.translateFromXML(file, TS);
			result.populatePurlMapRepository();
			result.populateInheritedValues();
			//For debug
			//this.metaMetaDataRepository.writePrettyXML(System.out);
		} catch (XMLTranslationException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	private void populateInheritedValues()
	{
		for (MetaMetadata mm: repository.values())
		{
			String superClassName	= mm.extendsClass;
			if (superClassName != null)
			{
				MetaMetadata superInstance	= repository.get(superClassName);
				if (superInstance != null)
				{
					propagateInheritedValues(superInstance);
//					String className = mm.getName();
//					metaMetadataPopulate(className,superClassName);
				}
			}
		}
	}

	/**
	 * 
	 * @param thisClassName
	 * @param superClassName
	 */
	protected void metaMetadataPopulate(String thisClassName, String superClassName)
	{
		MetaMetadata metaMetadata = this.repository.get(thisClassName);
		MetaMetadata superMetaMetadata = this.repository.get(superClassName);
		if(metaMetadata != null && superMetaMetadata != null)
			recursivePopulate(metaMetadata, superMetaMetadata);
	}
	
	/**
	 * Copying MetadataFields from srcMetaMetadata to destMetaMetadata.
	 * @param destMetaMetadata
	 * @param srcMetaMetadata
	 */
	protected void recursivePopulate(MetaMetadata destMetaMetadata, MetaMetadata srcMetaMetadata)
	{
		if(destMetaMetadata == null || srcMetaMetadata == null)
		{
			return;
		}
		for(MetaMetadataField metaMetadataField : srcMetaMetadata.getChildMetaMetadata())
		{
			destMetaMetadata.getChildMetaMetadata().put(metaMetadataField.getName(), metaMetadataField);
		}
		String superClassName	= srcMetaMetadata.extendsClass;
		if(superClassName == null || METADATA_TAG.equals(superClassName))
		{
			return;
		}
		
		MetaMetadata superInstance	= repository.get(superClassName);
		recursivePopulate(destMetaMetadata, superInstance);		
	}
	
	protected void populatePurlMapRepository()
	{
		for (MetaMetadata metaMetadata: repository)
		{
			ParsedURL purl = metaMetadata.getUrlBase();
			if(purl != null)
				purlMapRepository.put(purl.host(), metaMetadata);
		}
	}
	
	/**
	 * Find the best matching MetaMetadata for the ParsedURL -- if there is a match.
	 * 
	 * @param parsedURL
	 * @return		appropriate MetaMetadata, or null.
	 */
	public MetaMetadata getMetaMetaData(ParsedURL parsedURL)
	{
		MetaMetadata metaMetadata = purlMapRepository.get(parsedURL.host());
		/**
		 * returns null if there is no url_base.
		 */
		return metaMetadata;
	}
	
	public MetaMetadata getMetaMetaData(String docType)
	{

		return repository.get(docType);
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
		return (repository == null) ? null : repository.values();
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
	
	public static String documentTag()
	{
		return DOCUMENT_TAG;
	}
	
}
