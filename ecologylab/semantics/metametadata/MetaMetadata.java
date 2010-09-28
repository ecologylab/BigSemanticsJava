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
import ecologylab.semantics.actions.SemanticActionTranslationScope;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.ElementState.simpl_composite;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.types.element.Mappable;
import ecologylab.tests.FieldTagged;

/**
 * @author damaraju
 * 
 */
public class MetaMetadata extends MetaMetadataCompositeField implements Mappable<String>
{

	@simpl_composite
	MetaMetadataSelector			selector;

	@xml_tag("package")
	@simpl_scalar
	String										packageAttribute;

	@simpl_scalar
	protected boolean					dontGenerateClass	= false;

	/*
	 * @xml_collection("meta_metadata_field") private ArrayList<MetaMetadataField>
	 * metaMetadataFieldList;
	 */

	/**
	 * Mixins are needed so that we can have objects of multiple metadata classes in side a single
	 * metadata class. It basically provide us to simulate the functionality of multiple inheritance
	 * which is missing in java.
	 */
	@simpl_collection("mixins")
	@simpl_nowrap
	private ArrayList<String>	mixins;

	@simpl_scalar
	private String						collectionOf;

	// TranslationScope DEFAULT_METADATA_TRANSLATIONS = DefaultMetadataTranslationSpace.get();

	public MetaMetadata()
	{
		super();
	}

	protected MetaMetadata(MetaMetadataField copy, String name)
	{
		super(copy, name);
	}

	public Metadata constructMetadata()
	{
		return constructMetadata(this.getRepository().metadataTranslationScope());
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
			Class[] argClasses = new Class[]
			{ MetaMetadataCompositeField.class };
			Object[] argObjects = new Object[]
			{ this };
			result = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
			if (mixins != null && mixins.size() > 0)
			{
				for (String mixinName : mixins)
				{
					MetaMetadata mixinMM = getRepository().getByTagName(mixinName);
					if (mixinMM != null)
					{
						Metadata mixinMetadata = mixinMM.constructMetadata(ts);
						if (mixinMetadata != null)
							result.addMixin(mixinMetadata);
					}
					// andruid & andrew 11/2/09 changed from below to above
					// Class<? extends Metadata> mixinClass = (Class<? extends Metadata>)
					// ts.getClassByTag(mixinName);
					// if (mixinClass != null)
					// {
					// result.addMixin(ReflectionTools.getInstance(mixinClass));
					// }
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
	 * @param test
	 *          TODO
	 * @throws IOException
	 */
	public void compileToMetadataClass(String packageName, MetaMetadataRepository mmdRepository)
			throws IOException
	{
		if (!checkForErrors())
			return;
		
		// get the generation path from the package name.
		if (this.packageAttribute != null)
		{
			packageName = this.packageAttribute;
		}
		String generationPath = MetadataCompilerUtils.getGenerationPath(packageName);

		// create a file writer to write the JAVA files.
		File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
		File file = new File(directoryPath, XMLTools.classNameFromElementName(getName()) + ".java");

		// write to console
		System.out.print(this.file + "\n\t\t -> " + file);

		FileWriter fileWriter = new FileWriter(file);
		PrintWriter p = new PrintWriter(fileWriter);

		// update the translation class.

		// Write the package
		p.println(MetadataCompilerUtils.PACKAGE + " " + packageName + ";");

		// write java doc comment
		p.println(MetadataCompilerUtils.COMMENT);

		// Write the import statements
		// p.println(MetadataCompiler.getImportStatement());
		MetadataCompiler.printImports(p);
		// Write java-doc comments
		MetadataCompilerUtils.writeJavaDocComment(getComment(), fileWriter);

		// write @simpl_inherit
		p.println("@simpl_inherit");

		// p.println("@xml_tag(\""+getName()+"\")");

		p.println(getTagDecl());

		// Write class declaration
		String className = XMLTools.classNameFromElementName(getName());
		// System.out.println("#######################################"+getName());
		p.println("public class  " + className + "\nextends  "
				+ XMLTools.classNameFromElementName(extendsAttribute) + "\n{\n");

		// loop to write the class definition
		HashMapArrayList<String, MetaMetadataField> metaMetadataFieldList = getChildMetaMetadata();
		if (metaMetadataFieldList != null)
		{
			for (MetaMetadataField metaMetadataField : metaMetadataFieldList)
			{
				metaMetadataField.setExtendsField(extendsAttribute);
				metaMetadataField.setRepository(mmdRepository);
				try
				{
					// translate the field into for metadata class.
					metaMetadataField.compileToMetadataClass(packageName, p,
							MetadataCompilerUtils.GENERATE_FIELDS_PASS, false);
				}
				catch (SIMPLTranslationException e)
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
				f.setRepository(mmdRepository);
				try
				{
					// translate the field into for metadata class.
					f.compileToMetadataClass(packageName, p, MetadataCompilerUtils.GENERATE_METHODS_PASS,
							true);
				}
				catch (SIMPLTranslationException e)
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

	@Override
	protected String getMetaMetadataTagToInheritFrom()
	{
		return (extendsAttribute != null) ? extendsAttribute : super.getMetaMetadataTagToInheritFrom();
	}

	/**
	 * @return the collectionOf
	 */
	public String getCollectionOf()
	{
		return collectionOf;
	}

	/**
	 * @return the urlPattern
	 */
	public Pattern getUrlRegex()
	{
		return getSelector().getUrlRegex();
	}

	/**
	 * @param urlPattern
	 *          the urlPattern to set
	 */
	public void setUrlRegex(Pattern urlPattern)
	{
		getSelector().setUrlRegex(urlPattern);
	}

	/**
	 * @return the domain
	 */
	public String getDomain()
	{
		return getSelector().getDomain();
	}

	/**
	 * @param domain
	 *          the domain to set
	 */
	public void setDomain(String domain)
	{
		getSelector().setDomain(domain);
	}

	/**
	 * @return the packageAttribute
	 */
	public final String getPackageAttribute()
	{
		return packageAttribute;
	}

	final void setPackageAttribute(String pa)
	{
		packageAttribute = pa;
	}

	public String getUserAgentName()
	{
		return userAgentName;
	}

	public ParsedURL getUrlBase()
	{
		return getSelector().getUrlStripped();
	}

	public void setUrlBase(ParsedURL urlBase)
	{
		getSelector().setUrlBase(urlBase);
	}

	public void setUrlBase(String urlBase)
	{
		getSelector().setUrlBase(ParsedURL.getAbsolute(urlBase));
	}

	public ParsedURL getUrlPrefix()
	{
		return getSelector().getUrlPathTree();
	}

	public void setUrlPrefix(ParsedURL urlPrefix)
	{
		getSelector().setUrlPrefix(urlPrefix);
	}

	public void setUrlPrefix(String urlPrefix)
	{
		getSelector().setUrlPrefix(ParsedURL.getAbsolute(urlPrefix));
	}

	/**
	 * @param mimeTypes
	 *          the mimeTypes to set
	 */
	public void setMimeTypes(ArrayList<String> mimeTypes)
	{
		getSelector().setMimeTypes(mimeTypes);
	}

	/**
	 * @return the mimeTypes
	 */
	/**
	 * @return
	 */
	public ArrayList<String> getMimeTypes()
	{
		return getSelector().getMimeTypes();
	}

	/**
	 * @param suffixes
	 *          the suffixes to set
	 */
	public void setSuffixes(ArrayList<String> suffixes)
	{
		getSelector().setSuffixes(suffixes);
	}

	/**
	 * @return the suffixes
	 */
	public ArrayList<String> getSuffixes()
	{
		return getSelector().getSuffixes();
	}

	@Override
	public String key()
	{
		return getName();
	}

	public boolean isGenerateClass()
	{
		// we r not using getType as by default getType will give meta-metadata name
		if ((this instanceof MetaMetadataCompositeField)
				&& ((MetaMetadataCompositeField) this).type != null)
		{
			return false;
		}
		return !dontGenerateClass;
	}

	public void setGenerateClass(boolean generateClass)
	{
		this.dontGenerateClass = !generateClass;
	}

	public MetadataFieldDescriptor getFieldDescriptorByTagName(String tagName)
	{
		return metadataClassDescriptor.getFieldDescriptorByTag(tagName, getRepository()
				.metadataTranslationScope());
	}

	public static void main(String args[]) throws SIMPLTranslationException
	{
		final TranslationScope TS = MetaMetadataTranslationScope.get();
		String patternXMLFilepath = "../cf/config/semantics/metametadata/metaMetadataRepository.xml";

		// ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository test = (MetaMetadataRepository) TS.deserialize(patternXMLFilepath);

		test.serialize(System.out);

		File outputRoot = PropertiesAndDirectories.userDir();

		for (MetaMetadata metaMetadata : test.values())
		{
			// metaMetadata.translateToMetadataClass();
			System.out.println('\n');
		}
	}

	public MetaMetadataSelector getSelector()
	{
		if (selector == null)
			return MetaMetadataSelector.NULL_SELECTOR;
		return selector;
	}

	public void setSelector(MetaMetadataSelector s)
	{
		selector = s;
	}

	@Override
	protected String getTypeName()
	{
		return getName();
	}

	/**
	 * for definitive meta-metadata, return the super meta-metadata type name (extends= or "metadat");
	 * for decorative meta-metadata, return the decorated meta-metadata type name (type=).
	 */
	@Override
	protected String getSuperTypeName()
	{
		// decorative
		if (getType() != null)
			return getType();
		
		// definitive
		if (getExtendsAttribute() != null)
			return getExtendsAttribute();
		else
			return "metadata";
	}

	@Override
	protected MetaMetadataField getInheritedField()
	{
		String inheritedMmdName = getSuperTypeName();
		return getRepository().getByTagName(inheritedMmdName);
	}
	
	@Override
	protected boolean checkForErrors()
	{
		MetaMetadata superMmd = (MetaMetadata) getInheritedField();
		
		return assertNotNull(getTypeName(), "meta-metadata type name must be specified.")
				&& assertNotNull(getSuperTypeName(), "can't resolve parent meta-metadata.")
				&& assertNotNull(superMmd, "meta-metadata '%s' not found.", getSuperTypeName());
	}

}
