package ecologylab.semantics.metametadata;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionTranslationScope;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
@xml_tag("composite")
public class MetaMetadataCompositeField extends MetaMetadataNestedField
{

	/**
	 * The type/class of metadata object.
	 */
	@simpl_scalar
	protected String									type;

	@simpl_scalar
	protected String									userAgentName;

	@simpl_scalar
	protected String									userAgentString;

	@simpl_scalar
	private String										parser					= null;

	@simpl_collection
	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
	private ArrayList<SemanticAction>	semanticActions;

	@simpl_collection("def_var")
	@simpl_nowrap
	private ArrayList<DefVar>					defVars;

	@simpl_scalar
	protected String									schemaOrgItemType;

	
	private MMSelectorType						mmSelectorType	= MMSelectorType.DEFAULT;

	/**
	 * for caching getTypeNameInJava().
	 */
	private String										typeNameInJava	= null;

	public MetaMetadataCompositeField()
	{
		
	}

	public MetaMetadataCompositeField(String name, HashMapArrayList<String, MetaMetadataField> kids)
	{
		this.name = name;
		this.kids = new HashMapArrayList<String, MetaMetadataField>();
		if (kids != null)
			this.kids.putAll(kids);
	}

	public MetaMetadataCompositeField(MetaMetadataField mmf)
	{
		this.name = mmf.name;
		this.extendsAttribute = mmf.extendsAttribute;
		this.hide = mmf.hide;
		this.alwaysShow = mmf.alwaysShow;
		this.style = mmf.style;
		this.layer = mmf.layer;
		this.xpath = mmf.xpath;
		this.navigatesTo = mmf.navigatesTo;
		this.shadows = mmf.shadows;
		this.isFacet = mmf.isFacet;
		this.ignoreInTermVector = mmf.ignoreInTermVector;
		this.comment = mmf.comment;
		this.contextNode = mmf.contextNode;
		this.tag = mmf.tag;
		this.kids = mmf.kids;
	}

	public MetaMetadataCompositeField(MetaMetadataField copy, String name)
	{
		super(copy, name);
	}

	@Override
	public String getType()
	{
		return type;
	}

	public String getTypeOrName()
	{
		if (type != null)
			return type;
		else
			return getName();
	}

	public String getTagForTranslationScope()
	{
		return tag != null ? tag : name;
	}

	protected String getMetaMetadataTagToInheritFrom()
	{
		return type != null ? type : null;
	}

	/**
	 * Get the MetaMetadataCompositeField associated with this.
	 * 
	 * @return this, because it is a composite itself.
	 */
	public MetaMetadataCompositeField metaMetadataCompositeField()
	{
		return this;
	}

	public String getParser()
	{
		return parser;
	}

	public String getUserAgentString()
	{
		if (userAgentString == null)
		{
			userAgentString = (userAgentName == null) ? getRepository().getDefaultUserAgentString() :
					getRepository().getUserAgentString(userAgentName);
		}

		return userAgentString;
	}

	/**
	 * 
	 */
	protected void inheritSemanticActionsFromMM(MetaMetadata inheritedMetaMetadata)
	{
		if (semanticActions == null)
		{
			semanticActions = inheritedMetaMetadata.getSemanticActions();
		}
	}

	/**
	 * @return the defVars
	 */
	public final ArrayList<DefVar> getDefVars()
	{
		return defVars;
	}

	/**
	 * @return the semanticActions
	 */
	public ArrayList<SemanticAction> getSemanticActions()
	{
		return semanticActions;
	}

	@Deprecated
	@Override
	public String getAnnotationsInJava()
	{
		String tagDecl = getTagDecl();
		return "@simpl_composite" + (tagDecl.length() > 0 ? (" " + tagDecl) : "")
					 + " @mm_name(\"" + getName() + "\")";
	}

	@Override
	public String getAdditionalAnnotationsInJava()
	{
		return " @mm_name(\"" + getName() + "\")";
	}

	@Override
	protected String getTypeNameInJava()
	{
		String rst = typeNameInJava;
		if (rst == null)
		{
			rst = XMLTools.classNameFromElementName(getTypeOrName());
			typeNameInJava = rst;
		}
		return typeNameInJava;
	}

	/**
	 * @return the mmSelectorType
	 */
	public MMSelectorType getMmSelectorType()
	{
		return mmSelectorType;
	}

	/**
	 * @param mmSelectorType the mmSelectorType to set
	 */
	public void setMmSelectorType(MMSelectorType mmSelectorType)
	{
		this.mmSelectorType = mmSelectorType;
	}

	public boolean isGenericMetadata()
	{
		return mmSelectorType == MMSelectorType.SUFFIX_OR_MIME || isBuiltIn();
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public boolean isBuiltIn()
	{
		return false;
	}

	/**
	 * @return the schemaOrgItemType
	 */
	public String getSchemaOrgItemType()
	{
		return schemaOrgItemType;
	}

}
