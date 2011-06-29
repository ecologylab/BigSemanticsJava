/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xml.internal.serialize.ElementState;

import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.LinkedMetadataMonitor;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.types.element.Mappable;

/**
 * @author damaraju
 * 
 */
public class MetaMetadata extends MetaMetadataCompositeField implements Mappable<String>
{

	@simpl_collection("selector")
	@simpl_nowrap
	ArrayList<MetaMetadataSelector>					selectors;

	@xml_tag("package")
	@simpl_scalar
	String																	packageAttribute;

	@simpl_scalar
	@mm_dont_inherit
	protected boolean												dontGenerateClass	= false;

	@simpl_scalar
	@mm_dont_inherit
	protected boolean												builtIn;

	@simpl_scalar
	protected RedirectHandling							redirectHandling;

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
	private ArrayList<String>								mixins;

	@simpl_scalar
	private String													collectionOf;

	@simpl_collection("url_generator")
	@simpl_nowrap
	private ArrayList<UrlGenerator>					urlGenerators;

	private Map<String, MetaMetadataField>	naturalIds				= new HashMap<String, MetaMetadataField>();

	@simpl_map("link_with")
	@simpl_nowrap
	private HashMap<String, LinkWith>				linkWiths;
	
	@simpl_scalar
	protected String												schemaOrgItemtype;

	// TranslationScope DEFAULT_METADATA_TRANSLATIONS = DefaultMetadataTranslationSpace.get();
	
	private Map<MetaMetadataSelector, MetaMetadata> reselectMap;

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
			Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
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
	 * @return the mimeTypes
	 */
	public ArrayList<String> getMimeTypes()
	{
		ArrayList<String> result = null;

		for (MetaMetadataSelector selector : getSelectors())
		{
			if (result == null)
				result = new ArrayList<String>();

			ArrayList<String> mimeTypes = selector.getMimeTypes();
			if (mimeTypes != null)
				result.addAll(mimeTypes);
		}

		return result;
	}

	public ArrayList<String> getSuffixes()
	{
		ArrayList<String> result = null;

		for (MetaMetadataSelector selector : getSelectors())
		{
			if (result == null)
				result = new ArrayList<String>();

			ArrayList<String> suffixes = selector.getSuffixes();
			if (suffixes != null)
				result.addAll(suffixes);
		}

		return result;
	}

	@Override
	public String key()
	{
		return getName();
	}

	public boolean isGenerateClass()
	{
		// we r not using getType as by default getType will give meta-metadata name

		boolean compositeMmdWithTypeDecl = isCompositeMmdWithTypeDecl();
		boolean dontGenerateOrBuiltin = dontGenerateClass || builtIn;
		boolean hasExtends	= extendsAttribute != null;
		
//		boolean result = compositeMmdWithTypeDecl && !dontGenerateOrBuiltin;
		boolean result = hasExtends && !builtIn && !dontGenerateClass;
		return result;
	}

	/**
	 * @return
	 */
	public boolean isCompositeMmdWithTypeDecl()
	{
		return (this instanceof MetaMetadataCompositeField)
				&& ((MetaMetadataCompositeField) this).type != null;
	}

	@Override
	public boolean isBuiltIn()
	{
		return builtIn;
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

		// File outputRoot = PropertiesAndDirectories.userDir();
		//
		// for (MetaMetadata metaMetadata : test.values())
		// {
		// // metaMetadata.translateToMetadataClass();
		// System.out.println('\n');
		// }
	}

	public ArrayList<MetaMetadataSelector> getSelectors()
	{
		if (selectors == null)
			return MetaMetadataSelector.NULL_SELECTOR;
		return selectors;
	}

	public void addSelector(MetaMetadataSelector s)
	{
		if (selectors == null)
			selectors = new ArrayList<MetaMetadataSelector>();

		selectors.add(s);
	}

	@Override
	public String getTypeName()
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
	public MetaMetadataField getInheritedField()
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

	/**
	 * @return the redirectHandling
	 */
	public RedirectHandling getRedirectHandling()
	{
		return redirectHandling;
	}

	public void addNaturalIdField(String naturalId, MetaMetadataField childField)
	{
		naturalIds.put(naturalId, childField);
	}

	public Map<String, MetaMetadataField> getNaturalIdFields()
	{
		return naturalIds;
	}

	public ParsedURL generateUrl(String naturalId, String value)
	{
		if (urlGenerators != null)
		{
			for (UrlGenerator ug : urlGenerators)
			{
				if (ug.canGenerate(naturalId))
				{
					return ug.generate(getRepository(), naturalId, value);
				}
			}
		}
		return null;
	}

	public Map<String, LinkWith> getLinkWiths()
	{
		return linkWiths;
	}

	public void addLinkWith(LinkWith lw)
	{
		if (linkWiths == null)
		{
			linkWiths = new HashMap<String, LinkWith>();
		}
		linkWiths.put(lw.key(), lw);
	}
	
	void setUpLinkWith(MetaMetadataRepository repository)
	{
		LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
		if (linkWiths != null)
		{
			for (String lwName : linkWiths.keySet())
			{
				LinkWith lw = linkWiths.get(lwName);
				MetaMetadata targetMmd = this.getRepository().getByTagName(lw.getName());
				if (targetMmd != null)
				{
					monitor.registerName(lw.getName());
					monitor.registerName(name);
	
					if (targetMmd.getLinkWiths() != null && targetMmd.getLinkWiths().containsKey(name))
					{
						// if there is already a reverse link, just make sure the reverse link reference is set
						LinkWith r = targetMmd.getLinkWiths().get(name);
						if (!r.isReverse())
						{
							// warning("not encouraging explicitly defining reverse links!");
							r.setReverse(true);
							lw.setReverseLink(r);
						}
					}
					else
					{
						// if there isn't, create a new one
						LinkWith r = lw.createReverseLink(name);
						targetMmd.addLinkWith(r);
					}
				}
				else
				{
					error("link_with: meta-metadata not found: " + lw.getName());
				}
			}
		}
	}

	public Map<MetaMetadataSelector, MetaMetadata> getReselectMap()
	{
		return reselectMap;
	}

	public void addReselectEntry(MetaMetadataSelector selector, MetaMetadata mmd)
	{
		if (reselectMap == null)
		{
			reselectMap = new HashMap<MetaMetadataSelector, MetaMetadata>();
		}
		reselectMap.put(selector, mmd);
	}
	
	public MetadataClassDescriptor createMetadataClassDescriptor()
	{
		MetadataClassDescriptor mcd = new MetadataClassDescriptor();
		// TODO
		return mcd;
	}
	
	public MetadataClassDescriptor generateMetadataClassDescriptor()
	{
		MetadataClassDescriptor cd = this.getMetadataClassDescriptor();
		if (cd == null)
		{
			ClassDescriptor superCd = this.getInheritedMmd().generateMetadataClassDescriptor();
			cd = new MetadataClassDescriptor(
					this.getName(),
					this.getComment(),
					this.packageName(),
					XMLTools.classNameFromElementName(this.getName()),
					superCd,
					null);
			this.setMetadataClassDescriptor(cd);
			
			for (MetaMetadataField f : this.getChildMetaMetadata())
			{
				if (f.parent() == this)
				{
					MetadataFieldDescriptor fd = f.generateMetadataFieldDescriptor(cd);
					cd.addMetadataFieldDescriptor(fd);
				}
			}
		}
		return cd;
	}

}
