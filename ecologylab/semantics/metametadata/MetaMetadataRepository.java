/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationSpace;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.types.element.HashMapState;

/**
 * @author damaraju
 *
 */
public class MetaMetadataRepository extends HashMapState<String, MetaMetadata>
{
	
	

	/**
	 * 
	 */
	public MetaMetadataRepository()
	{
		
	}
	//Bharat:
	public MetaMetadata getMetaMetaData(String docType)
	{
		MetaMetadata tempMetaMetadata;
		try 
		{
			if (!this.containsKey(docType))
			{
				setMetaMetaData(docType); //create a new metametadata
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return get(docType);
	}

//	public MetaMetadata getMetaMetaData(docType docType)
//	{
//		
//	}
	public void setMetaMetaData(String docType) throws XMLTranslationException
	{
		MetaMetadata tempMetaMetadata;
		if (!this.containsKey(docType))
		{
			final TranslationSpace TS = MetaMetadataTranslationSpace.get();
			System.out.println("creating for doctype: "+docType);
			//Bharat: TODO depends on the docType switch case
			String patternXMLFilepath = "C:/web_MMData/code/java/ecologylabSemantics/examplePatternFlickr.xml";
			tempMetaMetadata = (MetaMetadata) ElementState.translateFromXML(patternXMLFilepath, TS);
			tempMetaMetadata.setTS(TS);
			super.put(docType, tempMetaMetadata);
			//tempMetaMetadata.writePrettyXML(System.out);
		}
	}
	
	
}
