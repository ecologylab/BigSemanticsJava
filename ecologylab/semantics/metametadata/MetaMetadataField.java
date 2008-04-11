package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_collection;
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
	@xml_attribute protected String	name;
	@xml_attribute private String xpath;
	
	// no idea what this is for....added while parsing for acmportal
	@xml_attribute private String stringPrefix; 
	
	//@xml_attribute @xml_tag("scalar_type") private ScalarType metadataType;
	@xml_attribute private ScalarType scalarType;
	
	@xml_map("meta_metadata_field") private HashMapArrayList<String, MetaMetadataField>	childMetaMetadata;

	/**
	 * For adding @xml_other_tag translations to a Metadata definition, for backwards compatability.
	 */
	@xml_collection("other_tag")		ArrayList<String>		otherTags;
	
//	@xml_collection("mixin")		ArrayList<String>		mixin;
	
	//For acmportal
	@xml_attribute private boolean 			isLink;
	@xml_attribute private boolean 			isList;
	@xml_attribute private boolean 			isFacet;
	
	@xml_attribute private boolean 			isNested;
	@xml_attribute private String 			key;
	@xml_attribute private boolean			isMap;

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
		final TranslationScope TS = MetaMetadataTranslationScope.get();
		String patternXMLFilepath = "config/examplePatternFlickr.xml";

//		ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository test = (MetaMetadataRepository) ElementState.translateFromXML(patternXMLFilepath, TS);
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
	
	public String getXpath()
	{
		return xpath;
	}
	
	public boolean isList()
	{
		return isList;
	}
	
	public boolean isNested()
	{
		return isNested;
	}
	
	public boolean isMap()
	{
		return isMap;
	}
	
	public String getStringPrefix()
	{
		return stringPrefix;
	}

	/**
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	/**
	 * @return the scalarType
	 */
	public ScalarType getScalarType()
	{
		return scalarType;
	}
}
