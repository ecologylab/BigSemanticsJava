/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.tools.MetadataCompilerConstants;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.types.element.Mappable;

/**
 * @author damaraju
 * 
 */
public class MetaMetadata extends MetaMetadataField implements Mappable<String>
{
	@xml_attribute
	private String						name;

	@xml_tag("extends")
	@xml_attribute
	private String						extendsAttribute;

	@xml_attribute
	private ParsedURL					urlBase;

	@xml_attribute
	private String						userAgentName;

	@xml_tag("package")
	@xml_attribute
	String										packageAttribute;

	@xml_attribute
	private String						comment;

	@xml_attribute
	private boolean						generateClass	= true;

	@xml_attribute
	private String						userAgentString;

	/*
	 * @xml_collection("meta_metadata_field") private ArrayList<MetaMetadataField>
	 * metaMetadataFieldList;
	 */

	/**
	 * Mixins are needed so that we can have objects of multiple metadata classes in side a single
	 * metadata class. It basically provide us to simulate the functionality of multiple inheritance
	 * which is missing in java.
	 */
	@xml_collection("mixins")
	private ArrayList<String>	mixins;

	@xml_collection("mime_type")
	private ArrayList<String>	mimeTypes;

	@xml_collection("suffix")
	private ArrayList<String>	suffixes;

	// TranslationScope DEFAULT_METADATA_TRANSLATIONS = DefaultMetadataTranslationSpace.get();

	private boolean						inheritedMetaMetadata = false;
	
	public MetaMetadata()
	{
		super();
	}

	public boolean isSupported(ParsedURL purl)
	{
		return purl.toString().startsWith(urlBase.toString());
	}

	public String getUserAgent()
	{
		return userAgentName;
	}

	public ParsedURL getUrlBase()
	{
		return urlBase;
	}

	public void setUrlBase(ParsedURL urlBase)
	{
		this.urlBase = urlBase;
	}

	public void setUrlBase(String urlBase)
	{
		this.urlBase = ParsedURL.getAbsolute(urlBase);
	}

	/**
	 * Lookup the Metadata class object that corresponds to the tag_name in this.
	 * 
	 * @return
	 */
	public Class<? extends Metadata> getMetadataClass(TranslationScope ts)
	{
		return getMetadataClass(name,ts);
	}

	private Class<? extends Metadata> getMetadataClass(String name,TranslationScope ts)
	{
		return (Class<? extends Metadata>) ts.getClassByTag(name);
	}

	/**
	 * Lookup the Metadata class that corresponds to the (tag) name of this, using the
	 * DefaultMetadataTranslationSpace. Assuming that is found, use reflection to instantiate it.
	 * 
	 * @return An instance of the Metadata subclass that corresponds to this, or null, if there is
	 *         none.
	 */
	public Metadata constructMetadata(TranslationScope ts)
	{
		Metadata result = null;
		Class<? extends Metadata> metadataClass = getMetadataClass(ts);

		if (metadataClass != null)
		{
			result = ReflectionTools.getInstance(metadataClass);
			result.setMetaMetadata(this);
			if (mixins != null && mixins.size() > 0)
			{
				for (String mixinName : mixins)
				{
					Class<? extends Metadata> mixinClass = getMetadataClass(mixinName,ts);
					if (mixinClass != null)
					{
						result.addMixin(ReflectionTools.getInstance(mixinClass));
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method translates the MetaMetaDeclaration into a metadata class.
	 * 
	 * @param packageName
	 *          The package in which the generated metadata class is to be placed.
	 * @throws IOException
	 */
	public void translateToMetadataClass(String packageName) throws IOException
	{
		// get the generation path from the package name.
		if (this.packageAttribute != null)
		{
			packageName = this.packageAttribute;
		}
		String generationPath = MetadataCompilerConstants.getGenerationPath(packageName);

		// create a file writer to write the JAVA files.
		File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
		File file = new File(directoryPath, XMLTools.classNameFromElementName(name) + ".java");
		FileWriter fileWriter = new FileWriter(file);
		PrintWriter p = new PrintWriter(fileWriter);
		
		//update the translation class.

		// Write the package
		p.println(MetadataCompilerConstants.PACKAGE + " " + packageName + ";");

		// write java doc comment
		p.println(MetadataCompilerConstants.COMMENT);

		// Write the import statements
		p.println(MetadataCompilerConstants.IMPORTS);

		// Write java-doc comments
		MetadataCompilerConstants.writeJavaDocComment(comment, fileWriter);

		//write @xml_inherit
		p.println("@xml_inherit");
		
		// Write class declaration
		String className = XMLTools.classNameFromElementName(name);
		p.println("public class  " + className + "\nextends  "
				+ XMLTools.classNameFromElementName(extendsAttribute) + "\n{\n");

		// write the constructors
		MetadataCompilerConstants.appendBlankConstructor(p, className);
		MetadataCompilerConstants.appendConstructor(p, className);

		// loop to write the class defination
		HashMapArrayList metaMetadataFieldList = getChildMetaMetadata();
		for (int i = 0; i < metaMetadataFieldList.size(); i++)
		{
			// get the metadata field.
			MetaMetadataField f = (MetaMetadataField) metaMetadataFieldList.get(i);
			try
			{
				// translate the field into for metadata class.
				f.translateToMetadataClass(packageName, p);
			}
			catch (XMLTranslationException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		// end the class declaration
		p.println("\n}\n");
		p.flush();
	}

	public MetaMetadataRepository repository()
	{
		return (MetaMetadataRepository) parent();
	}

	public String getUserAgentString()
	{
		if (userAgentString == null)
		{
			userAgentString = "";
		}

		return userAgentString;
	}

	public static void main(String args[]) throws XMLTranslationException
	{
		final TranslationScope TS = MetaMetadataTranslationScope.get();
		String patternXMLFilepath = "../cf/config/semantics/metametadata/defaultRepository.xml";

		// ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository test = (MetaMetadataRepository) ElementState.translateFromXML(
				patternXMLFilepath, TS);

		// test.writePrettyXML(System.out);

		File outputRoot = PropertiesAndDirectories.userDir();

		for (MetaMetadata metaMetadata : test.values())
		{
			// metaMetadata.translateToMetadataClass();
			System.out.println('\n');
		}
	}

	/**
	 * @param mimeTypes
	 *          the mimeTypes to set
	 */
	public void setMimeTypes(ArrayList<String> mimeTypes)
	{
		this.mimeTypes = mimeTypes;
	}

	/**
	 * @return the mimeTypes
	 */
	public ArrayList<String> getMimeTypes()
	{
		return mimeTypes;
	}

	/**
	 * @param suffixes
	 *          the suffixes to set
	 */
	public void setSuffixes(ArrayList<String> suffixes)
	{
		this.suffixes = suffixes;
	}

	/**
	 * @return the suffixes
	 */
	public ArrayList<String> getSuffixes()
	{
		return suffixes;
	}

	/**
	 * @param comment
	 *          the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param generateClass
	 *          the generateClass to set
	 */
	public void setGenerateClass(boolean generateClass)
	{
		this.generateClass = generateClass;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the generateClass
	 */
	public boolean isGenerateClass()
	{
		return generateClass;
	}

	@Override
	public String key()
	{
		return name;
	}
	
	@Override
	public MetaMetadataField lookupChild(String name)
	{
		if(!inheritedMetaMetadata)
			inheritMetaMetadata();
		
		return super.lookupChild(name);
	}
	

	private void inheritMetaMetadata()
	{
		if(!inheritedMetaMetadata && extendsAttribute != null)
		{
			MetaMetadata extendedMetaMetadata = repository().getByTagName(extendsAttribute);
			if(extendedMetaMetadata != null)
			{
				extendedMetaMetadata.inheritMetaMetadata();
				for(MetaMetadataField extendedField : extendedMetaMetadata.getChildMetaMetadata())
					addChild(extendedField);
			}
		}
		inheritedMetaMetadata = true;
	}

}
