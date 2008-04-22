/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.xml.ElementState;

/**
 * @author damaraju
 *
 */

public class MetaMetadataRepository extends ElementState
{
	
	//@xml_collection("meta_metadata") private ArrayList<MetaMetadata> stuff; 
	@xml_map("meta_metadata") private HashMapArrayList<String, MetaMetadata> repository; 

	/**
	 * Metadata Transition: TODO
	 * Have to create the prefix collection of the url_bases and have to access from here. 
	 */
	private HashMapArrayList<ParsedURL, MetaMetadata> purlMapRepository = new HashMapArrayList<ParsedURL, MetaMetadata>(); 
	
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
			purlMapRepository.put(metaMetadata.getUrlBase(), metaMetadata);
		}
	}
	
	public MetaMetadata getMetaMetaData(ParsedURL parsedURL)
	{
		String purl = "http://www." + parsedURL.domain() + "/";
		MetaMetadata metaMetadata = purlMapRepository.get(ParsedURL.getAbsolute(purl));
		
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
//			//Metadata Transition TODO depends on the docType
//			String patternXMLFilepath = "C:/web_MMData/code/java/ecologylabSemantics/examplePatternFlickr.xml";
//			tempMetaMetadata = (MetaMetadata) ElementState.translateFromXML(patternXMLFilepath, TS);
//			tempMetaMetadata.setTS(TS);
//			super.put(docType, tempMetaMetadata);
//			//tempMetaMetadata.writePrettyXML(System.out);
//		}
//	}
	
	
}
