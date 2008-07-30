package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ecologylab.generic.HashMapArrayList;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.Optimizations;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_collection;
import ecologylab.xml.ElementState.xml_tag;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class MetaMetadataField extends ElementState 
implements Mappable<String>, PackageSpecifier
{
	@xml_tag("package") 
	@xml_attribute protected 			String			packageName;

	/**
	 * The element name style spelling of the field name for this Metadata scalar value or nested element.
	 * <p/>
	 * Also, for true nested elements, the element name style spelling of the class of this Metadata.
	 */
	@xml_attribute protected 			String			name;

	/**
	 * This is used for generation of Metadata class declaration.
	 */
	@xml_attribute @xml_tag("extends")	String			extendsClass;

	@xml_attribute private 				String 			xpath;
	
	//FIXME -- talk to bharat, eliminate this declaration
	// no idea what this is for....added while parsing for acmportal
	@xml_attribute private				String 			stringPrefix; 
	
	//@xml_attribute @xml_tag("scalar_type") private ScalarType metadataType;
	@xml_attribute private 				ScalarType	 	scalarType;
	
	@xml_map("meta_metadata_field") 
	private HashMapArrayList<String, MetaMetadataField>	childMetaMetadata;


	@xml_attribute private 				boolean 		isLink;
	@xml_attribute private 				boolean 		isList;
	@xml_attribute private 				boolean			isMap;
	@xml_attribute private 				boolean 		isFacet;
	
	@xml_attribute private 				boolean 		ignoreInTermVector;

	/**
	 * Enables hand coding a few Metadata classes, but still providing MetaMetadata to
	 * control operations on them.
	 */
	@xml_attribute private				boolean		dontCompile;
	
	/**
	 * true if this field should not be displayed in interactive in-context metadata
	 */
	@xml_attribute private 				boolean 		hide;
	
	@xml_attribute private				boolean		alwaysShow;
	
	public boolean isAlwaysShow()
	{
		return alwaysShow;
	}

	@xml_attribute private 				boolean 		isNested;
	@xml_attribute private 				String 			key;
	
	HashMap<String, String>				childPackagesMap	= new HashMap<String, String>(2);
	

	public MetaMetadataField()
	{
		
	}
	
	public MetaMetadataField(String name, ScalarType metadataType,HashMapArrayList<String, MetaMetadataField> set)
	{
		this.name = name;
		//this.metadataType = metadataType;
		this.childMetaMetadata = set;
	}
	
	public String packageName()
	{
		String result	= packageName;
		if (result != null)
			return result;
		ElementState parent	= parent();
		if ((parent != null) && (parent instanceof PackageSpecifier))
		{
			return ((PackageSpecifier) parent).packageName();
		}
		else
			return null;
	}
	
	//TODO import paths
	
	//TODO track generated classes for TranslationScope declaration
	
	public void translateToMetadataClass(Appendable appendable, File outputRoot)
	throws XMLTranslationException, IOException
	{
		String packageName = packageName();
		File outFile	= new File(outputRoot, packageName.replace('.', '/'));
		appendable.append(outFile+"").append('\n');
		// write package declaration
		appendable.append("package ").append(packageName).append(';').append('\n').append('\n');
		
		// write import statements
		appendImport(appendable, "ecologylab.semantics.library.scalar.*");
		
		appendable.append('\n');
		// write class declaration
		
		String extendsClassName	= (extendsClass == null) ? "Metadata" : XMLTools.classNameFromElementName(extendsClass);
		appendable.append("public class " + XMLTools.classNameFromElementName(name) +"\nextends ").append(extendsClassName);
		appendable.append('\n').append('{').append('\n');

		Set<String> metaMetadataKeys	= childMetaMetadata.keySet();
		
		for (String key : metaMetadataKeys)
		{
			MetaMetadataField	mmf		= childMetaMetadata.get(key);
			
			if (mmf.scalarType != null)
			{
				// field declaration
				mmf.appendScalarNested(appendable);
			}
			else if (mmf.isNested)
			{
				// reference to nested class
				
				// generate referring field in this metadata
				mmf.appendNested(appendable);
				
				//TODO -- if this could be in a different package, would need forward reference to import this here.
				
				// generate nested class definition
				mmf.translateToMetadataClass(appendable, outputRoot);
			}
			else if (mmf.isList)
			{
				// collection of nested elements
				//TODO -- can these be scalars? if so, how can we tell?
				
				mmf.appendCollection(appendable);
				
				// generate nested class definition
				mmf.translateToMetadataClass(appendable, outputRoot);
			}
		}

		// end the class declaration
		appendable.append('}').append('\n');

	}
	
	protected void appendImport(Appendable appendable, String importDecl) 
	throws IOException
	{
		appendable.append("import ").append(importDecl).append(';').append('\n');
	}

	/**
	 * Append an @xml_nested declaration to appendable, using the name of this to directly form both
	 * the class name and the field name.
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	private void appendNested(Appendable appendable)
	throws IOException
	{
		String elementName		= getName();
		appendNested(appendable, "", XMLTools.classNameFromElementName(elementName), XMLTools.fieldNameFromElementName(elementName));
	} 

	
	private void appendScalarNested(Appendable appendable)
	throws IOException
	{
		String fieldTypeName = scalarType.fieldTypeName();
		appendNested(appendable, "Metadata", fieldTypeName, XMLTools.fieldNameFromElementName(getName()));
	} 

	private void appendNested(Appendable appendable, String classNamePrefix, String className, String fieldName)
	throws IOException
	{
		appendMetalanguageDecl(appendable, "@xml_nested", classNamePrefix, className, fieldName);
	} 

	private void appendCollection(Appendable appendable)
	throws IOException
	{
		String elementName	= this.name;
		String className	= XMLTools.classNameFromElementName(elementName);
		String fieldName	= XMLTools.fieldNameFromElementName(elementName)+ "s";
		appendMetalanguageDecl(appendable, "@xml_collection", "ArrayList<", className, ">", fieldName);
	} 

	private void appendMetalanguageDecl(Appendable appendable, String metalanguage, String classNamePrefix, String className, String fieldName)
	throws IOException
	{
		appendMetalanguageDecl( appendable, metalanguage, classNamePrefix, className, "", fieldName);
	}
	private void appendMetalanguageDecl(Appendable appendable, String metalanguage, 
			String classNamePrefix, String className,  String classNameSuffix, String fieldName)
	throws IOException
	{
		appendable.append('\t').append(metalanguage).append(' ').append(classNamePrefix).append(className).append(classNameSuffix).append('\t');
		appendable.append(fieldName).append(';').append('\n');
	} 

	
	public static void main(String args[]) throws XMLTranslationException
	{
		final TranslationScope TS = MetaMetadataTranslationScope.get();
		String patternXMLFilepath = "../cf/config/semantics/metametadata/defaultRepository.xml";

//		ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository test = (MetaMetadataRepository) ElementState.translateFromXML(patternXMLFilepath, TS);
		println("Stop");
		
		test.writePrettyXML(System.out);
		
		for (MetaMetadata metaMetadata : test.values())
		{
			
		}
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
	
	public MetaMetadataField lookupChild(FieldAccessor fieldAccessor)
	{
		return childMetaMetadata.get(fieldAccessor.getTagName());
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

	public boolean isHide()
	{
		return hide;
	}
	
	protected Collection<String> importPackages()
	{
		Collection<String> result	= null;
		if ((childMetaMetadata == null) || (childMetaMetadata.size() == 0))
		{
			result		= new ArrayList<String>();
			result.add(packageName());
		}
		
		return result;
	}
	
	public boolean isIgnoreInTermVector()
	{
		return ignoreInTermVector;
	}

}
