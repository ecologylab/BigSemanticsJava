/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.util.Collection;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author damaraju
 *
 */

public class MetaMetadataRepository extends ElementState
implements PackageSpecifier
{
	
	//@xml_collection("meta_metadata") private ArrayList<MetaMetadata> stuff; 
	@xml_map("meta_metadata") private HashMapArrayList<String, MetaMetadata> repository; 

	@xml_attribute 		String 						name;
	
	@xml_tag("package") 
	@xml_attribute 		String						packageName;
	
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
	
	public void populatePurlMapRepository()
	{
		Iterator<MetaMetadata> iterator = repository.iterator();
		while(iterator.hasNext())
		{
			MetaMetadata metaMetadata = iterator.next();
			ParsedURL purl = metaMetadata.getUrlBase();
			if(purl != null)
				purlMapRepository.put(purl.domain(), metaMetadata);
		}
	}
	
	public MetaMetadata getMetaMetaData(ParsedURL parsedURL)
	{
		MetaMetadata metaMetadata = purlMapRepository.get(parsedURL.domain());
		/**
		 * returns null if there is no url_base.
		 */
		return metaMetadata;
	}
	
	//Bharat:
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
	
	
}
