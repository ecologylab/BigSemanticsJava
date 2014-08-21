package ecologylab.bigsemantics.metametadata.declarations;

import java.util.List;

import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.NestedMetaMetadataFieldTypesScope;
import ecologylab.bigsemantics.metametadata.mm_dont_inherit;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_wrap;

/**
 * Data structure for a mmd field.
 * 
 * @author quyin
 */
@SuppressWarnings("rawtypes")
public class MetaMetadataFieldDeclaration extends ElementState
{

  @simpl_scalar
  private String            name;

  @simpl_scalar
  @mm_dont_inherit
  private String            comment;

  @simpl_scalar
  private String            tag;

  @simpl_scalar
  private String            otherTags;

  @simpl_collection("xpath")
  @simpl_nowrap
  private List<String>      xpaths;

  @simpl_scalar
  private boolean           extractAsHtml;

  /**
   * Context node for xpath based extarction rules for this field. Default value is document root.
   */
  @simpl_scalar
  @mm_dont_inherit
  private String            contextNode;

  /**
   * Used in the field_parser mechanism, which takes a string as input and parse it into values
   * indexed by keys.
   * 
   * This field indicates what key this field uses to decide the value for it inside a field_parser.
   */
  @simpl_scalar
  private String            fieldParserKey;

  /**
   * schema.org microdata item_prop name.
   */
  @simpl_scalar
  private String            schemaOrgItemprop;

  /**
   * if this field should be indexed in database representation, used by the ORM component.
   */
  @simpl_scalar
  private boolean           indexed;

  /**
   * The name of natural id if this field is used as one.
   */
  @simpl_scalar
  private String            asNaturalId;

  /**
   * The format of this field. Used for normalization. Currently, only used with natural ids.
   */
  @simpl_scalar
  private String            format;

  /**
   * Indicate if this field is required for the upper level structure.
   */
  @simpl_scalar
  private boolean           required;

  /**
   * if this field should be serialized.
   */
  @simpl_scalar
  private boolean           dontSerialize;

  /**
   * if we should ignore this in the term vector.
   */
  @simpl_scalar
  private boolean           ignoreInTermVector;

  /**
   * if we should ignore this field completely (which will cause ignoring of this field for both
   * display and term vector).
   */
  @simpl_scalar
  private boolean           ignoreCompletely;

  /**
   * true if this field should not be displayed in interactive in-context metadata
   */
  @simpl_scalar
  private boolean           hide;

  /**
   * If true the field is shown even if its null or empty.
   */
  @simpl_scalar
  @mm_dont_inherit
  private boolean           alwaysShow;

  /**
   * name of a style.
   */
  @simpl_scalar
  private String            styleName;

  /**
   * Specifies the order in which a field is displayed in relation to other fields.
   */
  @simpl_scalar
  private float             layer;

  /**
   * Another field name that this field navigates to (e.g. from a label in in-context metadata)
   */
  @simpl_scalar
  private String            navigatesTo;

  /**
   * This MetaMetadataField shadows another field, so it is to be displayed instead of the other.It
   * is kind of over-riding a field.
   */
  @simpl_scalar
  private String            shadows;

  /**
   * The label to be used when visualizing this field. Name is used by default. This overrides name.
   */
  @simpl_scalar
  private String            label;

  /**
   * if this field is used as a facet.
   */
  @simpl_scalar
  private boolean           isFacet;

  /**
   * Hint for renderer to not label the extracted value in presentation
   */
  @simpl_scalar
  private boolean           hideLabel;

  /**
   * Another field name whose value can be used as label for this field
   */
  @simpl_scalar
  private String            useValueAsLabel;

  /**
   * Hint for renderer to concatenate this field to another
   */
  @simpl_scalar
  private String            concatenatesTo;

  /**
   * Hint for renderer how to position label w.r.t. value
   */
  @simpl_scalar
  private String            labelAt;

  @simpl_scalar
  @mm_dont_inherit
  private boolean           inheritDone;

  /**
   * From which field this one inherits. Could be null if this field is declared for the first time.
   */
  @simpl_composite
  @simpl_scope(NestedMetaMetadataFieldTypesScope.NAME)
  @simpl_wrap
  @mm_dont_inherit
  private MetaMetadataField superField            = null;

  /**
   * in which meta-metadata this field is declared.
   */
  @simpl_composite
  @mm_dont_inherit
  private MetaMetadata      declaringMmd          = null;

  /**
   * If this field is used to define inline meta-metadata types.
   * 
   * This flag is used by extraction module to determine the true root element for child fields
   * inside this field.
   */
  @simpl_scalar
  @mm_dont_inherit
  private boolean           usedToDefineInlineMmd = false;

  public MetaMetadataFieldDeclaration()
  {
    super();
  }

  public String getName()
  {
    return name;
  }

  public String getComment()
  {
    return comment;
  }

  public List<String> getXpaths()
  {
    return xpaths;
  }

  public String getXpath(int i)
  {
    if (xpaths == null || xpaths.size() == 0 || i > xpaths.size())
    {
      return null;
    }
    return xpaths.get(i);
  }

  public int getXpathsSize()
  {
    return xpaths == null ? 0 : xpaths.size();
  }

  public String getTag()
  {
    return tag;
  }

  public String getOtherTags()
  {
    return otherTags;
  }

  public boolean isExtractAsHtml()
  {
    return extractAsHtml;
  }

  public String getContextNode()
  {
    return contextNode;
  }

  public String getFieldParserKey()
  {
    return fieldParserKey;
  }

  public String getSchemaOrgItemprop()
  {
    return schemaOrgItemprop;
  }

  public boolean isIndexed()
  {
    return indexed;
  }

  public String getAsNaturalId()
  {
    return asNaturalId;
  }

  public String getFormat()
  {
    return format;
  }

  public boolean isRequired()
  {
    return required;
  }

  public boolean isDontSerialize()
  {
    return dontSerialize;
  }

  public boolean isIgnoreInTermVector()
  {
    return ignoreInTermVector || isIgnoreCompletely();
  }

  public boolean isIgnoreCompletely()
  {
    return ignoreCompletely;
  }

  public boolean isHide()
  {
    return hide || isIgnoreCompletely();
  }

  public boolean isAlwaysShow()
  {
    return alwaysShow;
  }

  public String getStyleName()
  {
    return styleName;
  }

  public float getLayer()
  {
    return layer;
  }

  public String getNavigatesTo()
  {
    return navigatesTo;
  }

  public String getShadows()
  {
    return shadows;
  }

  public String getLabel()
  {
    return label;
  }

  public boolean isFacet()
  {
    return isFacet;
  }

  public boolean isHideLabel()
  {
    return hideLabel;
  }

  public String getUseValueAsLabel()
  {
    return useValueAsLabel;
  }

  public String getConcatenatesTo()
  {
    return concatenatesTo;
  }

  public String getLabelAt()
  {
    return labelAt;
  }

  public boolean isInheritDone()
  {
    return inheritDone;
  }

  public MetaMetadataField getSuperField()
  {
    return superField;
  }

  public MetaMetadata getDeclaringMmd()
  {
    return declaringMmd;
  }

  public boolean isUsedToDefineInlineMmd()
  {
    return usedToDefineInlineMmd;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public void setTag(String tag)
  {
    this.tag = tag;
  }

  public void setOtherTags(String otherTags)
  {
    this.otherTags = otherTags;
  }

  public void setExtractAsHtml(boolean extractAsHtml)
  {
    this.extractAsHtml = extractAsHtml;
  }

  public void setContextNode(String contextNode)
  {
    this.contextNode = contextNode;
  }

  public void setFieldParserKey(String fieldParserKey)
  {
    this.fieldParserKey = fieldParserKey;
  }

  public void setSchemaOrgItemprop(String schemaOrgItemprop)
  {
    this.schemaOrgItemprop = schemaOrgItemprop;
  }

  public void setIndexed(boolean indexed)
  {
    this.indexed = indexed;
  }

  public void setAsNaturalId(String asNaturalId)
  {
    this.asNaturalId = asNaturalId;
  }

  public void setFormat(String format)
  {
    this.format = format;
  }

  public void setRequired(boolean required)
  {
    this.required = required;
  }

  public void setDontSerialize(boolean dontSerialize)
  {
    this.dontSerialize = dontSerialize;
  }

  public void setIgnoreInTermVector(boolean ignoreInTermVector)
  {
    this.ignoreInTermVector = ignoreInTermVector;
  }

  public void setIgnoreCompletely(boolean ignoreCompletely)
  {
    this.ignoreCompletely = ignoreCompletely;
  }

  public void setHide(boolean hide)
  {
    this.hide = hide;
  }

  public void setAlwaysShow(boolean alwaysShow)
  {
    this.alwaysShow = alwaysShow;
  }

  public void setStyleName(String styleName)
  {
    this.styleName = styleName;
  }

  public void setLayer(float layer)
  {
    this.layer = layer;
  }

  public void setNavigatesTo(String navigatesTo)
  {
    this.navigatesTo = navigatesTo;
  }

  public void setShadows(String shadows)
  {
    this.shadows = shadows;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public void setFacet(boolean isFacet)
  {
    this.isFacet = isFacet;
  }

  public void setHideLabel(boolean hideLabel)
  {
    this.hideLabel = hideLabel;
  }

  public void setUseValueAsLabel(String useValueAsLabel)
  {
    this.useValueAsLabel = useValueAsLabel;
  }

  public void setConcatenatesTo(String concatenatesTo)
  {
    this.concatenatesTo = concatenatesTo;
  }

  public void setLabelAt(String labelAt)
  {
    this.labelAt = labelAt;
  }

  public void setInheritDone(boolean inheritDone)
  {
    this.inheritDone = inheritDone;
  }

  public void setSuperField(MetaMetadataField superField)
  {
    this.superField = superField;
  }

  public void setDeclaringMmd(MetaMetadata declaringMmd)
  {
    this.declaringMmd = declaringMmd;
  }

  public void setUsedToDefineInlineMmd(boolean usedToDefineInlineMmd)
  {
    this.usedToDefineInlineMmd = usedToDefineInlineMmd;
  }

}