package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationSpace;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class MetaMetadataField extends ElementState implements Mappable<String>
{
	@xml_attribute private String	name;

	//@xml_attribute @xml_tag("scalar_type") private ScalarType metadataType;
	@xml_attribute private ScalarType scalarType;
	
	@xml_map("meta_metadata_field") private HashMapArrayList<String, MetaMetadataField>	childMetaMetadata;

	public MetaMetadataField()
	{
		
	}
	
	public MetaMetadataField(String name, ScalarType metadataType,HashMapArrayList<String, MetaMetadataField> set)
	{
		this.name = name;
		//this.metadataType = metadataType;
		this.childMetaMetadata = set;
	}
	
	public static void main(String args[]) throws XMLTranslationException
	{
		final TranslationSpace TS = MetaMetadataTranslationSpace.get();
		String patternXMLFilepath = "exampleMetaMetadata2.xml";

//		ElementState.setUseDOMForTranslateTo(true);
		MetaMetadata test = (MetaMetadata) ElementState.translateFromXML(patternXMLFilepath, TS);
		println("Stop");
		
		test.writePrettyXML(System.out);
	}
	
	public int size()
	{
		return childMetaMetadata == null ? 0 : childMetaMetadata.size();
	}

	public HashMapArrayList<String, MetaMetadataField> getSet()
	{
		return childMetaMetadata;
	}

	public String getName()
	{
		return name;
	}

	public String key()
	{
		return name;
	}

	public ScalarType getMetadataType()
	{
		return scalarType;
	}

	public MetaMetadataField lookupChild(String name)
	{
		return childMetaMetadata.get(name);
	}
}
