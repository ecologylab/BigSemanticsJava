/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.types.element.Mappable;

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
			Class[] argClasses	= new Class[]  { MetaMetadataCompositeField.class };
			Object[] argObjects = new Object[] { this };
			
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
				}
			}
		}
		return result;
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

//		File outputRoot = PropertiesAndDirectories.userDir();
//
//		for (MetaMetadata metaMetadata : test.values())
//		{
//			// metaMetadata.translateToMetadataClass();
//			System.out.println('\n');
//		}
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
		if (getType() != null)
			return getType();
		return getName();
	}

	/**
	 * @return for meta-metadata defining a new mmd type, return the super meta-metadata type name
	 *         (extends= or "metadata"); for meta-metadata decorating an existent mmd type, return the
	 *         decorated meta-metadata type name (type=).
	 */
	public String getSuperMmdTypeName()
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

	/**
	 * this will always return null since meta-metadata doesn't inherit from any fields. 
	 */
	@Override
	protected MetaMetadataField getInheritedField()
	{
		return null;
	}
	
	@Override
	protected boolean checkForErrors()
	{
		MetaMetadata superMmd = (MetaMetadata) getInheritedField();
		
		return assertNotNull(getTypeName(), "meta-metadata type name must be specified.")
				&& assertNotNull(getSuperMmdTypeName(), "can't resolve parent meta-metadata.")
				&& assertNotNull(superMmd, "meta-metadata '%s' not found.", getSuperMmdTypeName());
	}

	/**
	 * meta_metadata can define their own package attribute. otherwise is the same as meta-metadata
	 * field.
	 */
	@Override
	public void compileToMetadataClass(String packageName) throws IOException
	{
		String packageAttr = getPackageAttribute();
		if (packageAttr != null)
			super.compileToMetadataClass(packageAttr);
		else
			super.compileToMetadataClass(packageName);
	}

}
