/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
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
	 * 
	 */
	public MetaMetadataRepository()
	{
		
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
