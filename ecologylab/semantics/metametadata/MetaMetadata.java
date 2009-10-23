/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.TypeTagNames;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.semantics.tools.MetadataCompilerConstants;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.types.element.ArrayListState;
import ecologylab.xml.types.element.Mappable;

/**
 * @author damaraju
 * 
 */
public class MetaMetadata extends MetaMetadataField implements Mappable<String>
{
	


	
	@xml_tag("extends")
	@xml_attribute
	private String						extendsAttribute;

	@xml_attribute
	private ParsedURL					urlBase;
	
	@xml_attribute
	private ParsedURL 				urlPrefix;

	/**
	 * Regular expression. Must be paired with domain.
	 * This is the least efficient form of matcher, so it should be used only when url_base & url_prefix cannot be used.
	 */
	@xml_attribute
	private Pattern						urlPattern;
	
	/**
	 * This key is *required* for urlPatterns, so that we can organize them efficiently.
	 */
	@xml_attribute
	private String						domain;
	
	@xml_attribute
	private String						userAgentName;

	@xml_tag("package")
	@xml_attribute
	String										packageAttribute;

	@xml_attribute
	private String						comment;



	@xml_attribute
	private String						userAgentString;

	@xml_attribute
	private String						binding=null;
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

	@xml_tag("semantic_actions")
	@xml_collection("semantic_actions")
	private ArrayListState<? extends SemanticAction>	semanticActions;
	
	@xml_tag("def_vars")
	@xml_collection("def_vars")
	private ArrayListState<DefVar> defVars;

	
	@xml_attribute
	private String 					collectionOf;
	


	// TranslationScope DEFAULT_METADATA_TRANSLATIONS = DefaultMetadataTranslationSpace.get();
	
	public MetaMetadata()
	{
		super();
	}

	/**
	 * @param purl
	 * @return
	 */
	public boolean isSupported(ParsedURL purl)
	{
		if(urlBase!=null)
			return purl.toString().startsWith(urlBase.toString());
		if(urlPrefix!=null)
		{
			Pattern pattern = Pattern.compile(urlPrefix.toString());
		
			// create a matcher based on input string
			Matcher matcher = pattern.matcher(purl.toString());
			
			boolean result = matcher.find();
			System.out.println(result);
			return result;
		}
		return false;
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

	public ParsedURL getUrlPrefix()
	{
		return urlPrefix;
	}
	
	public void setUrlPrefix(ParsedURL urlPrefix)
	{
		this.urlPrefix = urlPrefix;
	}
	
	public void setUrlPrefix(String urlPrefix)
	{
		this.urlPrefix = ParsedURL.getAbsolute(urlPrefix);
	}
	/**
	 * Lookup the Metadata class object that corresponds to the tag_name in this.
	 * 
	 * @return
	 */
	public Class<? extends Metadata> getMetadataClass(TranslationScope ts)
	{
		Class<? extends Metadata> result = getMetadataClass(getType(),ts);
		if(result ==null)
		{
			// there is no class for this tag we can use class of meta-metadata it extends
			result = getMetadataClass(extendsAttribute, ts);
		}
		return result;
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
	 * @param test TODO
	 * @throws IOException
	 */
	public void translateToMetadataClass(String packageName, MetaMetadataRepository mmdRepository) throws IOException
	{
		// get the generation path from the package name.
		if (this.packageAttribute != null)
		{
			packageName = this.packageAttribute;
		}
		String generationPath = MetadataCompilerConstants.getGenerationPath(packageName);

		// create a file writer to write the JAVA files.
		File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
		File file = new File(directoryPath, XMLTools.classNameFromElementName(getName()) + ".java");
		FileWriter fileWriter = new FileWriter(file);
		PrintWriter p = new PrintWriter(fileWriter);
		
		//update the translation class.

		// Write the package
		p.println(MetadataCompilerConstants.PACKAGE + " " + packageName + ";");

		// write java doc comment
		p.println(MetadataCompilerConstants.COMMENT);

		// Write the import statements
		p.println(MetadataCompiler.getImportStatement());

		// Write java-doc comments
		MetadataCompilerConstants.writeJavaDocComment(comment, fileWriter);

		//write @xml_inherit
		p.println("@xml_inherit");
		p.println("@xml_tag(\""+getName()+"\")");
		
		// Write class declaration
		String className = XMLTools.classNameFromElementName(getName());
		System.out.println("#######################################"+getName());
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
			f.setExtendsField(extendsAttribute);
			f.setMmdRepository(mmdRepository);
			try
			{
				// translate the field into for metadata class.
				f.translateToMetadataClass(packageName, p,MetadataCompilerConstants.GENERATE_FIELDS_PASS,false);
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
		for (int i = 0; i < metaMetadataFieldList.size(); i++)
		{
			// get the metadata field.
			MetaMetadataField f = (MetaMetadataField) metaMetadataFieldList.get(i);
			f.setExtendsField(extendsAttribute);
			f.setMmdRepository(mmdRepository);
			try
			{
				// translate the field into for metadata class.
				f.translateToMetadataClass(packageName, p,MetadataCompilerConstants.GENERATE_METHODS_PASS,true);
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
			userAgentString = (userAgentName == null) ? repository().getDefaultUserAgentString() :
				repository().getUserAgentString(userAgentName);
		}

		return userAgentString;
	}

	public static void main(String args[]) throws XMLTranslationException
	{
		final TranslationScope TS = MetaMetadataTranslationScope.get();
		String patternXMLFilepath = "../cf/config/semantics/metametadata/metaMetadataRepository.xml";

		// ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository test = (MetaMetadataRepository) ElementState.translateFromXML(
				patternXMLFilepath, TS);

	  test.writePrettyXML(System.out);

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


	@Override
	public String key()
	{
		return getName();
	}

	/**
	 * Bind nested child and collection meta-metadata.
	 */
	public void bindNonScalarChildren()
	{
		if (childMetaMetadata == null)
			return;
		
		final MetaMetadataRepository repository	= repository();
		
		for (MetaMetadataField childField : childMetaMetadata)
		{
			if (childField.getScalarType() == null && childField.childMetaMetadata == null)
			{
				if (isNested())
				{
					repository.bindChildren(childField, childField.getName());
				}
				else if (childField.getCollectionChildType() != null)
				{
					if (childField.isEntity())
					{
						repository.bindChildren(childField, TypeTagNames.ENTITY);
					}
					else
					{
						repository.bindChildren(childField, childField.getCollectionChildType());
					}
				}
			}
		}
	}
	/**
	 * @return the semanticActions
	 */
	public ArrayListState<? extends SemanticAction> getSemanticActions()
	{
		return semanticActions;
	}

	/**
	 * @return the collectionOf
	 */
	public String getCollectionOf()
	{
		return collectionOf;
	}

	/**
	 * @return the extendsAttribute
	 */
	public String getExtendsAttribute()
	{
		return extendsAttribute;
	}

	public String getBinding()
	{
		// TODO Auto-generated method stub
		return binding;
	}

	/**
	 * @return the defVars
	 */
	public final ArrayListState<DefVar> getDefVars()
	{
		return defVars;
	}

	/**
	 * @return the urlPattern
	 */
	public Pattern getUrlPattern()
	{
		return urlPattern;
	}

	/**
	 * @param urlPattern the urlPattern to set
	 */
	public void setUrlPattern(Pattern urlPattern)
	{
		this.urlPattern = urlPattern;
	}

	/**
	 * @return the domain
	 */
	public String getDomain()
	{
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	/**
	 * @return the packageAttribute
	 */
	public final String getPackageAttribute()
	{
		return packageAttribute;
	}
	
	@Override
	protected String getMetaMetadataTagToInheritFrom()
	{
		return (extendsAttribute != null) ? extendsAttribute : super.getMetaMetadataTagToInheritFrom();
	}
}
