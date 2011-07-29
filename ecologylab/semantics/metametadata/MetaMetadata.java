/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ecologylab.collections.Scope;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionTranslationScope;
import ecologylab.semantics.collecting.LinkedMetadataMonitor;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.types.element.Mappable;

/**
 * @author damaraju
 * 
 */
public class MetaMetadata extends MetaMetadataCompositeField implements Mappable<String>
{

	@simpl_scalar
	protected String																ormInheritanceStrategy;

	@simpl_collection("selector")
	@simpl_nowrap
	ArrayList<MetaMetadataSelector>									selectors;

	@simpl_scalar
	private String																	parser						= null;

	@simpl_collection
	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
	private ArrayList<SemanticAction>								semanticActions;

	@simpl_scalar
	@mm_dont_inherit
	protected boolean																builtIn;

	@simpl_scalar
	protected RedirectHandling											redirectHandling;

	/**
	 * Mixins are needed so that we can have objects of multiple metadata classes in side a single
	 * metadata class. It basically provide us to simulate the functionality of multiple inheritance
	 * which is missing in java.
	 */
	@simpl_collection("mixins")
	@simpl_nowrap
	private ArrayList<String>												mixins;

	@simpl_scalar
	private String																	collectionOf;

	@simpl_collection("url_generator")
	@simpl_nowrap
	private ArrayList<UrlGenerator>									urlGenerators;

	private Map<String, MetaMetadataField>					naturalIds				= new HashMap<String, MetaMetadataField>();

	@simpl_map("link_with")
	@simpl_nowrap
	private HashMap<String, LinkWith>								linkWiths;

	@simpl_scalar
	protected String																schemaOrgItemtype;

	private Map<MetaMetadataSelector, MetaMetadata>	reselectMap;

	private Scope<MetaMetadata>											inlineMmds				= null;

	/**
	 * MMDs derived from this one.
	 */
	private ArrayList<MetaMetadata>									derivedMmds;
	
	public MetaMetadata()
	{
		super();
	}

	protected MetaMetadata(MetaMetadataField copy, String name)
	{
		super(copy, name);
	}

	public String getParser()
	{
		return parser;
	}

	/**
	 * @return the semanticActions
	 */
	public ArrayList<SemanticAction> getSemanticActions()
	{
		return semanticActions;
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
//		Class<? extends Metadata> metadataClass = getMetadataClass(ts);
		Class<? extends Metadata> metadataClass = this.getMetadataClassDescriptor().getDescribedClass();

		if (metadataClass != null)
		{
			Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
			Object[] argObjects = new Object[] { this };

			result = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
			if (mixins != null && mixins.size() > 0)
			{
				for (String mixinName : mixins)
				{
					MetaMetadata mixinMM = getRepository().getByName(mixinName);
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

	public MetadataFieldDescriptor getFieldDescriptorByTagName(String tagName)
	{
		return metadataClassDescriptor.getFieldDescriptorByTag(tagName, getRepository()
				.metadataTranslationScope());
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
				MetaMetadata targetMmd = this.getRepository().getByName(lw.getName());
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
	
	/**
	 * Hook overrided by MetaMetadata class
	 * 
	 * @param inheritedMmd
	 */
	protected void inheritNonFieldComponents(MetaMetadata inheritedMmd)
	{
		inheritSemanticActions(inheritedMmd);
	}
	
	protected void inheritSemanticActions(MetaMetadata inheritedMmd)
	{
		if (semanticActions == null)
		{
			semanticActions = inheritedMmd.getSemanticActions();
		}
	}

	@Override
	protected boolean isInlineDefinition()
	{
		return false;
	}

	public Scope<MetaMetadata> getInlineMmds()
	{
		return inlineMmds;
	}
	
	void setInlineMmds(Scope<MetaMetadata> inlineMmds)
	{
		this.inlineMmds = inlineMmds;
	}
	
	public MetaMetadata getInlineMmd(String name)
	{
		if (inlineMmds != null)
			return inlineMmds.get(name);
		return null;
	}

	void addInlineMmd(String name, MetaMetadata generatedMmd)
	{
		if (inlineMmds == null)
			inlineMmds = new Scope<MetaMetadata>();
		inlineMmds.put(name, generatedMmd);
	}
	
	public ArrayList<MetaMetadata> getDerivedMmds()
	{
		return derivedMmds;
	}
	
	public void addDerivedMmd(MetaMetadata mmd)
	{
		if (derivedMmds == null)
			derivedMmds = new ArrayList<MetaMetadata>();
		derivedMmds.add(mmd);
	}

	void inheritInlineMmds(MetaMetadata mmd)
	{
		if (this.getInlineMmds() == null)
			this.setInlineMmds(new Scope<MetaMetadata>());
		if (mmd.getInlineMmds() != null)
			this.getInlineMmds().setParent(mmd.getInlineMmds());
	}
	
	@Override
	protected void inheritMetaMetadataHelper()
	{
		debug("processing mmd: " + this);
		// init
		for (MetaMetadataField field : this.getChildMetaMetadata())
		{
			// init each field's declaringMmd to this (some of them may change during inheritance)
			field.setDeclaringMmd(this);
			if (field instanceof MetaMetadataNestedField)
				((MetaMetadataNestedField) field).setPackageName(this.packageName());
		}

		// a terminating point of recursion: do not inherit for root mmd
		if (MetaMetadata.isRootMetaMetadata(this))
			return;

		super.inheritMetaMetadataHelper();

		// inherit other stuffs
		this.inheritNonFieldComponents(this.getInheritedMmd());
	}
	
	protected void inheritFromInheritedMmd(MetaMetadata inheritedMmd)
	{
		this.inheritAttributes(inheritedMmd);
		this.inheritInlineMmds(inheritedMmd);
	}
	
	protected MetaMetadata getScopingMmd()
	{
		return this;
	}
	
	void findOrGenerateMetadataClassDescriptor(TranslationScope tscope)
	{
		if (this.metadataClassDescriptor == null)
		{
			this.inheritMetaMetadata();
			MetaMetadata inheritedMmd = this.getInheritedMmd();
			inheritedMmd.findOrGenerateMetadataClassDescriptor(tscope);
			MetadataClassDescriptor superCd = inheritedMmd.getMetadataClassDescriptor();
			if (this.isNewMetadataClass())
			{
				String tagOrName = this.getTagOrName();
				MetadataClassDescriptor cd = new MetadataClassDescriptor(
						this,
						tagOrName,
						this.getComment(),
						this.packageName(),
						XMLTools.classNameFromElementName(this.getName()),
						superCd,
						null);
				// setting this early allows referring to the same class in fields
				this.metadataClassDescriptor = cd;

				for (MetaMetadataField f : this.getChildMetaMetadata())
				{
					if (f.getDeclaringMmd() == this && f.getInheritedField() == null)
					{
						MetadataFieldDescriptor fd = f.findOrGenerateMetadataFieldDescriptor(tscope, cd);
						cd.addMetadataFieldDescriptor(fd);
					}
				}
				
				MetadataClassDescriptor existingCdWithThisTag = (MetadataClassDescriptor) tscope.getClassDescriptorByTag(tagOrName);
				if (existingCdWithThisTag != null)
				{
					warning("Class descriptor exists for tag [" + tagOrName + "]: " + existingCdWithThisTag);
				}
				tscope.addTranslation(cd);
			}
			else
			{
				this.metadataClassDescriptor = superCd;
			}
			
			if (this.getInlineMmds() != null)
			{
				for (MetaMetadata inlineMmd : this.getInlineMmds().values())
				{
					inlineMmd.findOrGenerateMetadataClassDescriptor(tscope);
				}
			}
		}
	}
	
	public boolean isDerivedFrom(MetaMetadata base)
	{
		MetaMetadata mmd = this;
		while (mmd != null)
		{
			if (mmd == base)
				return true;
			mmd = mmd.getInheritedMmd();
		}
		return false;
	}
	
	public static boolean isRootMetaMetadata(MetaMetadata mmd)
	{
		return mmd.getName().equals(ROOT_MMD_NAME);
	}

	/**
	 * 
	 * @return the corresponding Metadata class simple name.
	 */
	protected String getMetadataClassSimpleName()
	{
		if (this.isBuiltIn() || this.isNewMetadataClass())
		{
			// new definition
			return XMLTools.classNameFromElementName(this.getName());
		}
		else
		{
			// re-using existing type
			// do not use this.type directly because we don't know if that is a definition or just re-using exsiting type
			MetaMetadata inheritedMmd = this.getInheritedMmd();
			if (inheritedMmd == null)
				this.inheritMetaMetadata(); // currently, this should never happend because we call this method after inheritance process.
			return inheritedMmd == null ? null : inheritedMmd.getMetadataClassSimpleName();
		}
	}
	
	@Override
	public boolean isNewMetadataClass()
	{
		// for meta-metadata, except for looking at its contents, we should also look at its built_in
		// attribute to determine if it is a new type
		return super.isNewMetadataClass() && !this.isBuiltIn();
	}
	
}
