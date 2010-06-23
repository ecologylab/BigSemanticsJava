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
import ecologylab.semantics.actions.NestedSemanticActionsTranslationScope;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.types.element.Mappable;

/**
 * @author damaraju
 * 
 */
public class MetaMetadata extends MetaMetadataNestedField 
implements Mappable<String>
{
	@xml_attribute
	private ParsedURL					urlStripped;
	
	@xml_attribute
	private ParsedURL 				urlPathTree;

	/**
	 * Regular expression. Must be paired with domain.
	 * This is the least efficient form of matcher, so it should be used only when url_base & url_prefix cannot be used.
	 */
	@xml_attribute
	private Pattern						urlRegex;
	
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
	private String						userAgentString;

	@xml_attribute
	private String						parser=null;
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
	@xml_nowrap 
	private ArrayList<String>	mixins;

	@xml_collection("mime_type")
	@xml_nowrap 
	private ArrayList<String>	mimeTypes;

	@xml_collection("suffix")
	@xml_nowrap 
	private ArrayList<String>	suffixes;

	@xml_collection
	@xml_scope(NestedSemanticActionsTranslationScope.NESTED_SEMANTIC_ACTIONS_SCOPE)
	private ArrayList<SemanticAction>	semanticActions;
	
	@xml_collection("def_var")
	@xml_nowrap 
	private ArrayList<DefVar> defVars;

	
	@xml_attribute
	private String 					collectionOf;
	


	// TranslationScope DEFAULT_METADATA_TRANSLATIONS = DefaultMetadataTranslationSpace.get();
	
	public MetaMetadata()
	{
		super();
	}
	
	protected MetaMetadata(MetaMetadataField copy, String name)
	{
		super(copy, name);
	}

	/**
	 * @param purl
	 * @param mimeType TODO
	 * @return
	 */
	public boolean isSupported(ParsedURL purl, String mimeType)
	{
		if(urlStripped!=null)
			return purl.toString().startsWith(urlStripped.toString());
		Pattern pattern = null;
		if(urlPathTree!=null)
			 pattern = Pattern.compile(urlPathTree.toString());
		if(urlRegex!=null)
			pattern = Pattern.compile(urlRegex.toString());
		
		if(pattern != null)
		{
			// create a matcher based on input string
			Matcher matcher = pattern.matcher(purl.toString());
			
			boolean result = matcher.find();
			//System.out.println(result);
			return result;
		}
		if(suffixes!=null)
		{
			for(String suffix : suffixes)
			{
				if(purl.hasSuffix(suffix))
					return true;
			}				
		}
		if(mimeTypes!=null)
		{
			for(String mime: mimeTypes)
			{
				if(mime.equals(mimeType))
					return true;
			}
		}
		return false;
	}

	public Metadata constructMetadata()
	{
		return constructMetadata(this.repository().metadataTranslationScope());
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
		Class<? extends Metadata> metadataClass = getMetadataClassDescriptor().describedClass();
		
		if (metadataClass != null)
		{
			Class[] argClasses 	= new Class[] { this.getClass() };
			Object[] argObjects = new Object[] { this };
			result = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
			if (mixins != null && mixins.size() > 0)
			{
				for (String mixinName : mixins)
				{
					MetaMetadata mixinMM	= repository().getByTagName(mixinName);
					if (mixinMM != null)
					{
						Metadata mixinMetadata	= mixinMM.constructMetadata(ts);
						if (mixinMetadata != null)
							result.addMixin(mixinMetadata);
					}
					// andruid & andrew 11/2/09 changed from below to above
//					Class<? extends Metadata> mixinClass = (Class<? extends Metadata>) ts.getClassByTag(mixinName);
//					if (mixinClass != null)
//					{
//						result.addMixin(ReflectionTools.getInstance(mixinClass));
//					}
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
		String generationPath = MetadataCompilerUtils.getGenerationPath(packageName);

		// create a file writer to write the JAVA files.
		File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
		File file = new File(directoryPath, XMLTools.classNameFromElementName(getName()) + ".java");
		FileWriter fileWriter = new FileWriter(file);
		PrintWriter p = new PrintWriter(fileWriter);
		
		//update the translation class.

		// Write the package
		p.println(MetadataCompilerUtils.PACKAGE + " " + packageName + ";");

		// write java doc comment
		p.println(MetadataCompilerUtils.COMMENT);

		// Write the import statements
//		p.println(MetadataCompiler.getImportStatement());
		MetadataCompiler.printImports(p);
		// Write java-doc comments
		MetadataCompilerUtils.writeJavaDocComment(getComment(), fileWriter);

		//write @xml_inherit
		p.println("@xml_inherit");
		
//		p.println("@xml_tag(\""+getName()+"\")");

		p.println(getTagDecl());
		
		// Write class declaration
		String className = XMLTools.classNameFromElementName(getName());
		System.out.println("#######################################"+getName());
		p.println("public class  " + className + "\nextends  "
				+ XMLTools.classNameFromElementName(extendsAttribute) + "\n{\n");

		
		// loop to write the class definition
		HashMapArrayList<String, MetaMetadataField> metaMetadataFieldList = getChildMetaMetadata();
		if(metaMetadataFieldList != null)
		{
			for (MetaMetadataField metaMetadataField : metaMetadataFieldList)
			{
				metaMetadataField.setExtendsField(extendsAttribute);
				metaMetadataField.setMmdRepository(mmdRepository);
				try
				{
					// translate the field into for metadata class.
					metaMetadataField.translateToMetadataClass(packageName, p,MetadataCompilerUtils.GENERATE_FIELDS_PASS,false);
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
			
			// write the constructors
			MetadataCompilerUtils.appendBlankConstructor(p, className);
			MetadataCompilerUtils.appendConstructor(p, className);
			for (int i = 0; i < metaMetadataFieldList.size(); i++)
			{
				// get the metadata field.
				MetaMetadataField f = (MetaMetadataField) metaMetadataFieldList.get(i);
				f.setExtendsField(extendsAttribute);
				f.setMmdRepository(mmdRepository);
				try
				{
					// translate the field into for metadata class.
					f.translateToMetadataClass(packageName, p,MetadataCompilerUtils.GENERATE_METHODS_PASS,true);
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
	
	/**
	 * 
	 */
	protected void inheritSemanticActionsFromMM(MetaMetadata inheritedMetaMetadata)
	{
		if(semanticActions == null)
		{
			semanticActions = inheritedMetaMetadata.getSemanticActions();
		}
	}
	
	@Override
	protected String getMetaMetadataTagToInheritFrom()
	{
		return (extendsAttribute != null) ? extendsAttribute : super.getMetaMetadataTagToInheritFrom();
	}
	/**
	 * @return the semanticActions
	 */
	public ArrayList<SemanticAction> getSemanticActions()
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

	public String getParser()
	{
		// TODO Auto-generated method stub
		return parser;
	}

	/**
	 * @return the defVars
	 */
	public final ArrayList<DefVar> getDefVars()
	{
		return defVars;
	}

	/**
	 * @return the urlPattern
	 */
	public Pattern getUrlRegex()
	{
		return urlRegex;
	}

	/**
	 * @param urlPattern the urlPattern to set
	 */
	public void setUrlRegex(Pattern urlPattern)
	{
		this.urlRegex = urlPattern;
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
	
	public String getUserAgent()
	{
		return userAgentName;
	}

	public ParsedURL getUrlBase()
	{
		return urlStripped;
	}

	public void setUrlBase(ParsedURL urlBase)
	{
		this.urlStripped = urlBase;
	}

	public void setUrlBase(String urlBase)
	{
		this.urlStripped = ParsedURL.getAbsolute(urlBase);
	}

	public ParsedURL getUrlPrefix()
	{
		return urlPathTree;
	}
	
	public void setUrlPrefix(ParsedURL urlPrefix)
	{
		this.urlPathTree = urlPrefix;
	}
	
	public void setUrlPrefix(String urlPrefix)
	{
		this.urlPathTree = ParsedURL.getAbsolute(urlPrefix);
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

	@Override
	public String key()
	{
		return getName();
	}
	
	public MetadataFieldDescriptor getFieldDescriptorByTagName(String tagName)
	{
		return metadataClassDescriptor.getFieldDescriptorByTag(tagName, metaMetadataRepository().metadataTranslationScope());
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
	 * @return the metadataClassDescriptor
	 */
	public MetadataClassDescriptor getMetadataClassDescriptor()
	{
		return metadataClassDescriptor;
	}

}
