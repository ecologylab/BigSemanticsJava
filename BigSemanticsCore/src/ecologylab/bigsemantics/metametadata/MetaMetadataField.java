package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.Utils;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.MetaInformation;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_descriptor_classes;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_map_key_field;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_wrap;
import ecologylab.serialization.types.ScalarType;
import ecologylab.serialization.types.element.IMappable;
import ecologylab.textformat.NamedStyle;

/**
 * The basic meta-metadata field class. Encapsulate common attributes and methods for all types of
 * meta-metadata fields.
 * 
 * @author quyin
 * @author damaraju
 */
@SuppressWarnings("rawtypes")
@simpl_inherit
@simpl_descriptor_classes(
{ MetaMetadataClassDescriptor.class, MetaMetadataFieldDescriptor.class })
public abstract class MetaMetadataField extends ElementState
    implements IMappable<String>, Iterable<MetaMetadataField>, MMDConstants, Cloneable
{

  /**
   * The Comparator for conveniently sort fields.
   */
  static class LayerComparator implements Comparator<MetaMetadataField>
  {

    @Override
    public int compare(MetaMetadataField o1, MetaMetadataField o2)
    {
      // return negation for descending ordering in sort
      return -Float.compare(o1.layer, o2.layer);
    }

  }

  /**
   * The Iterator class of fields inside this one.
   */
  class MetaMetadataFieldIterator implements Iterator<MetaMetadataField>
  {

    int currentIndex = 0;

    @Override
    public boolean hasNext()
    {
      int size = kids.size();
      boolean result = currentIndex < size;

      if (result)
      {
        for (int i = currentIndex; i < size; i++)
        {
          MetaMetadataField nextField = kids.get(i);
          if (nextField.isIgnoreCompletely())
          {
            currentIndex++;
          }
          else
          {
            break;
          }
        }

        if (currentIndex == size)
        {
          result = false;
        }
      }

      return result;
    }

    @Override
    public MetaMetadataField next()
    {
      return kids.get(currentIndex++);
    }

    @Override
    public void remove()
    {
      throw new MetaMetadataException("Removing child field through iterator is not yet supported");
    }

  }

  /**
   * This class encapsulate the clone-on-write behavior of metadata field descriptor associated with
   * this field.
   * 
   * @author quyin
   *
   */
  protected class MetadataFieldDescriptorProxy
  {

    boolean fieldDescriptorCloned = false;

    private void cloneFieldDescriptorOnWrite()
    {
      if (!fieldDescriptorCloned)
      {
        MetaMetadataField.this.metadataFieldDescriptor = MetaMetadataField.this.metadataFieldDescriptor
            .clone();
        fieldDescriptorCloned = true;
      }
    }

    public void setTagName(String newTagName)
    {
      if (newTagName != null
          && !newTagName.equals(MetaMetadataField.this.metadataFieldDescriptor.getTagName()))
      {
        cloneFieldDescriptorOnWrite();
        MetaMetadataField.this.metadataFieldDescriptor.setTagName(newTagName);
      }
    }

    public void setElementClassDescriptor(MetadataClassDescriptor metadataClassDescriptor)
    {
      if (metadataClassDescriptor != MetaMetadataField.this.metadataFieldDescriptor
          .getElementClassDescriptor())
      {
        cloneFieldDescriptorOnWrite();
        MetaMetadataField.this.metadataFieldDescriptor
            .setElementClassDescriptor(metadataClassDescriptor);
      }
    }

    public void setCollectionOrMapTagName(String childTag)
    {
      if (childTag != null
          && !childTag.equals(MetaMetadataField.this.metadataFieldDescriptor
              .getCollectionOrMapTagName()))
      {
        cloneFieldDescriptorOnWrite();
        MetaMetadataField.this.metadataFieldDescriptor.setCollectionOrMapTagName(childTag);
      }
    }

    public void setWrapped(boolean wrapped)
    {
      if (wrapped != MetaMetadataField.this.metadataFieldDescriptor.isWrapped())
      {
        cloneFieldDescriptorOnWrite();
        MetaMetadataField.this.metadataFieldDescriptor.setWrapped(wrapped);
      }
    }

  }

  static LayerComparator                              LAYER_COMPARATOR;

  static ArrayList<MetaMetadataField>                 EMPTY_COLLECTION;

  static Iterator<MetaMetadataField>                  EMPTY_ITERATOR;

  static Collection<MmdGenericTypeVar>                EMPTY_GENERIC_TYPE_VAR_COLLECTION;

  static Logger                                       logger;

  static
  {
    LAYER_COMPARATOR = new LayerComparator();
    EMPTY_COLLECTION = new ArrayList<MetaMetadataField>(0);
    EMPTY_ITERATOR = EMPTY_COLLECTION.iterator();
    EMPTY_GENERIC_TYPE_VAR_COLLECTION = new ArrayList<MmdGenericTypeVar>();
    logger = LoggerFactory.getLogger(MetaMetadataField.class);
  }

  @simpl_scalar
  private String                                      name;

  @simpl_scalar
  @mm_dont_inherit
  private String                                      comment;

  /**
   * The nested child fields inside this field.
   */
  @simpl_map
  @simpl_scope(NestedMetaMetadataFieldTypesScope.NAME)
  @simpl_nowrap
  private HashMapArrayList<String, MetaMetadataField> kids;

  @simpl_scalar
  private String                                      tag;

  @simpl_scalar
  private String                                      otherTags;

  @simpl_collection("xpath")
  @simpl_nowrap
  private List<String>                                xpaths;

  @simpl_scalar
  private boolean                                     extractAsHtml;

  /**
   * Context node for xpath based extarction rules for this field. Default value is document root.
   */
  @simpl_scalar
  @mm_dont_inherit
  private String                                      contextNode;

  /**
   * Used in the field_parser mechanism, which takes a string as input and parse it into values
   * indexed by keys.
   * 
   * This field indicates what key this field uses to decide the value for it inside a field_parser.
   */
  @simpl_scalar
  private String                                      fieldParserKey;

  /**
   * schema.org microdata item_prop name.
   */
  @simpl_scalar
  private String                                      schemaOrgItemprop;

  /**
   * if this field should be indexed in database representation, used by the ORM component.
   */
  @simpl_scalar
  private boolean                                     indexed;

  /**
   * The name of natural id if this field is used as one.
   */
  @simpl_scalar
  private String                                      asNaturalId;

  /**
   * The format of this field. Used for normalization. Currently, only used with natural ids.
   */
  @simpl_scalar
  private String                                      format;

  /**
   * Indicate if this field is required for the upper level structure.
   */
  @simpl_scalar
  private boolean                                     required;

  /**
   * if this field should be serialized.
   */
  @simpl_scalar
  private boolean                                     dontSerialize;

  /**
   * if we should ignore this in the term vector.
   */
  @simpl_scalar
  private boolean                                     ignoreInTermVector;

  /**
   * if we should ignore this field completely (which will cause ignoring of this field for both
   * display and term vector).
   */
  @simpl_scalar
  private boolean                                     ignoreCompletely;

  @simpl_collection
  @simpl_scope(FieldOpScope.NAME)
  @simpl_nowrap
  private List<FieldOp>                               fieldOps;

  // ////////////// Presentation Semantics Below ////////////////

  /**
   * true if this field should not be displayed in interactive in-context metadata
   */
  @simpl_scalar
  private boolean                                     hide;

  /**
   * If true the field is shown even if its null or empty.
   */
  @simpl_scalar
  @mm_dont_inherit
  private boolean                                     alwaysShow;

  /**
   * name of a style.
   */
  @simpl_scalar
  private String                                      styleName;

  /**
   * Collection of styles
   */
  @simpl_collection("style")
  @simpl_nowrap
  private List<MetaMetadataStyle>                     styles;

  /**
   * Specifies the order in which a field is displayed in relation to other fields.
   */
  @simpl_scalar
  private float                                       layer;

  /**
   * Another field name that this field navigates to (e.g. from a label in in-context metadata)
   */
  @simpl_scalar
  private String                                      navigatesTo;

  /**
   * This MetaMetadataField shadows another field, so it is to be displayed instead of the other.It
   * is kind of over-riding a field.
   */
  @simpl_scalar
  private String                                      shadows;

  /**
   * The label to be used when visualizing this field. Name is used by default. This overrides name.
   */
  @simpl_scalar
  private String                                      label;

  /**
   * if this field is used as a facet.
   */
  @simpl_scalar
  private boolean                                     isFacet;

  /**
   * Hint for renderer to not label the extracted value in presentation
   */
  @simpl_scalar
  private boolean                                     hideLabel;

  /**
   * Another field name whose value can be used as label for this field
   */
  @simpl_scalar
  private String                                      useValueAsLabel;

  /**
   * Hint for renderer to concatenate this field to another
   */
  @simpl_scalar
  private String                                      concatenatesTo;

  /**
   * Hint for renderer how to position label w.r.t. value
   */
  @simpl_scalar
  private String                                      labelAt;

  // ////////////// Members Below ////////////////

  private MetaMetadataRepository                      repository;

  @simpl_scalar
  @mm_dont_inherit
  private boolean                                     inheritDone;

  private boolean                                     inheritOngoing;

  /**
   * From which field this one inherits. Could be null if this field is declared for the first time.
   */
  @simpl_composite
  @simpl_scope(NestedMetaMetadataFieldTypesScope.NAME)
  @simpl_wrap
  @mm_dont_inherit
  private MetaMetadataField                           superField            = null;

  /**
   * in which meta-metadata this field is declared.
   */
  @simpl_composite
  @mm_dont_inherit
  private MetaMetadata                                declaringMmd          = null;

  /**
   * If this field is used to define inline meta-metadata types.
   * 
   * This flag is used by extraction module to determine the true root element for child fields
   * inside this field.
   */
  @simpl_scalar
  @mm_dont_inherit
  private boolean                                     usedToDefineInlineMmd = false;

  @simpl_map("generic_type_var")
  @simpl_map_key_field("name")
  @simpl_nowrap
  private MmdGenericTypeVarScope                      genericTypeVars;

  private String                                      fieldNameInJava;

  private String                                      capFieldNameInJava;

  private MetadataFieldDescriptor                     metadataFieldDescriptor;

  private MetadataFieldDescriptorProxy                fieldDescriptorProxy;

  /**
   * Class descriptor for the Metadata object that corresponds to this. Non-null for nested and
   * collection fields. Null for scalar fields.
   */
  private MetadataClassDescriptor                     metadataClassDescriptor;

  /**
   * Class of the Metadata object that corresponds to this. Non-null for nested and collection
   * fields. Null for scalar fields.
   */
  private Class<? extends Metadata>                   metadataClass;

  private HashSet<String>                             nonDisplayedFieldNames;

  private boolean                                     fieldsSortedForDisplay;

  /**
   * (For caching toString())
   */
  private String                                      toString;

  private String                                      hashForExtraction;

  private boolean                                     hashForExtractionOngoing;

  public MetaMetadataField()
  {
    kids = new HashMapArrayList<String, MetaMetadataField>();
  }

  public MetaMetadataField(String name, HashMapArrayList<String, MetaMetadataField> children)
  {
    this.name = name;
    this.kids = children;
  }

  protected MetaMetadataField(MetaMetadataField copy, String name)
  {
    this();
    this.name = name;
    this.tag = copy.tag;
    this.kids = copy.kids;
  }

  public String getName()
  {
    return name;
  }

  public String getType()
  {
    return null;
  }

  public String getExtendsAttribute()
  {
    return null;
  }

  public String getComment()
  {
    return comment;
  }

  /**
   * @return The nested fields inside of this one as a collection.
   */
  public Collection<MetaMetadataField> getChildren()
  {
    return kids == null ? null : kids.values();
  }

  /**
   * @return The nested fields inside of this one as a map.
   */
  public HashMapArrayList<String, MetaMetadataField> getChildrenMap()
  {
    return kids;
  }
  
  public HashMapArrayList<String, MetaMetadataField> childrenMap()
  {
    if (kids == null)
    {
      kids = new HashMapArrayList<String, MetaMetadataField>();
    }
    return kids;
  }

  public boolean hasChildren()
  {
    return kids != null && kids.size() > 0;
  }

  public int getChildrenSize()
  {
    return kids == null ? 0 : kids.size();
  }

  public MetaMetadataField lookupChild(String name)
  {
    return kids == null ? null : kids.get(name);
  }

  public MetaMetadataField lookupChild(MetadataFieldDescriptor metadataFieldDescriptor)
  {
    return lookupChild(XMLTools.getXmlTagName(metadataFieldDescriptor.getName(), null));
  }

  public boolean isAuthoredChildOf(MetaMetadataField parentField)
  {
    if (parentField instanceof MetaMetadataCompositeField && this.parent() == parentField)
      return true;
    if (parentField instanceof MetaMetadataCollectionField
        && this.parent() == ((MetaMetadataCollectionField) parentField).getChildComposite())
      return true;
    return false;
  }

  public boolean hasAuthoredChild(MetaMetadataField childField)
  {
    return childField.isAuthoredChildOf(this);
  }

  /**
   * The (first) xpath of this field.
   * 
   * @return
   */
  public String getXpath()
  {
    return getXpath(0);
  }

  public List<String> getXpaths()
  {
    return xpaths;
  }

  public int getXpathsSize()
  {
    return xpaths == null ? 0 : xpaths.size();
  }

  public String getXpath(int i)
  {
    if (xpaths == null || xpaths.size() == 0 || i > xpaths.size())
    {
      return null;
    }
    return xpaths.get(i);
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

  public String getSchemaOrgItemtype()
  {
    return null;
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

  public List<FieldOp> getFieldOps()
  {
    return fieldOps;
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

  public List<MetaMetadataStyle> getStyles()
  {
    return styles;
  }

  public NamedStyle lookupStyle()
  {
    NamedStyle result = null;
    if (styleName != null)
      result = getRepository().lookupStyle(styleName);
    return (result != null) ? result : getRepository().getDefaultStyle();
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

  public MetaMetadataRepository getRepository()
  {
    if (repository == null)
      repository = findRepository();
    return repository;
  }

  private MetaMetadataRepository findRepository()
  {
    ElementState parent = parent();
    while (parent != null && !(parent instanceof MetaMetadataRepository))
    {
      parent = parent.parent();
    }
    if (parent == null)
    {
      logger.error("can't find repository for " + this);
      return null;
    }
    return (MetaMetadataRepository) parent;
  }

  public boolean isInheritDone()
  {
    return inheritDone;
  }

  public boolean isInheritOngoing()
  {
    return inheritOngoing;
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

  public MmdGenericTypeVarScope getGenericTypeVars()
  {
    return genericTypeVars;
  }

  public Collection<MmdGenericTypeVar> getGenericTypeVarsCollection()
  {
    return genericTypeVars == null ? EMPTY_GENERIC_TYPE_VAR_COLLECTION : genericTypeVars.values();
  }

  /**
   * Get the field name in java.
   * 
   * @param capitalized
   * @return
   */
  public String getFieldNameInJava(boolean capitalized)
  {
    if (capitalized)
      return getCapFieldNameInJava();

    String rst = fieldNameInJava;
    if (rst == null)
    {
      rst = XMLTools.fieldNameFromElementName(getName());
      fieldNameInJava = rst;
    }
    return fieldNameInJava;
  }

  /**
   * Get the capitalized field name in java (could be used in method names).
   * 
   * @return
   */
  private String getCapFieldNameInJava()
  {
    String rst = capFieldNameInJava;
    if (rst == null)
    {
      rst = XMLTools.javaNameFromElementName(getName(), true);
      capFieldNameInJava = rst;
    }
    return capFieldNameInJava;
  }

  /**
   * @return The corresponding metadataFieldDescriptor.
   */
  public MetadataFieldDescriptor getMetadataFieldDescriptor()
  {
    return metadataFieldDescriptor;
  }

  public MetadataFieldDescriptorProxy getFieldDescriptorProxy()
  {
    return fieldDescriptorProxy;
  }

  /**
   * @return The corresponding metadataClassDescriptor. Null for scalars.
   * 
   *         Note that this class descriptor might be incomplete during the compilation process,
   *         e.g. lacking the actual Class object.
   */
  public MetadataClassDescriptor getMetadataClassDescriptor()
  {
    return metadataClassDescriptor;
  }

  /**
   * @return The corresponding metadata class. Null for scalars.
   * 
   *         Should be used after inheritance and binding process.
   */
  public Class<? extends Metadata> getMetadataClass()
  {
    Class<? extends Metadata> metadataClass = this.metadataClass;
    if (metadataClass == null)
    {
      MetadataClassDescriptor metadataClassDescriptor = this.getMetadataClassDescriptor();
      if (metadataClassDescriptor != null)
      {
        metadataClass = (Class<? extends Metadata>) metadataClassDescriptor.getDescribedClass();
      }
      this.metadataClass = metadataClass;
    }
    return metadataClass;
  }

  public HashSet<String> getNonDisplayedFieldNames()
  {
    return nonDisplayedFieldNames;
  }

  protected HashSet<String> nonDisplayedFieldNames()
  {
    HashSet<String> result = this.nonDisplayedFieldNames;
    if (result == null)
    {
      result = new HashSet<String>();
      this.nonDisplayedFieldNames = result;
    }
    return result;
  }

  public boolean isChildFieldDisplayed(String childName)
  {
    return nonDisplayedFieldNames == null ? true : !nonDisplayedFieldNames.contains(childName);
  }

  public int getNonDisplayedFieldNamesSize()
  {
    return nonDisplayedFieldNames == null ? 0 : nonDisplayedFieldNames.size();
  }

  public boolean isFieldsSortedForDisplay()
  {
    return fieldsSortedForDisplay;
  }

  /**
   * NOTE: This is not Object.getHashCode()!
   * 
   * @return A hash that is used for versioning.
   */
  public String getHashForExtraction()
  {
    if (hashForExtraction == null)
    {
      synchronized (this)
      {
        if (hashForExtraction == null)
        {
          hashForExtractionOngoing = true;
          String fp = getFingerprintString();
          byte[] bytes = Utils.secureHashBytes(fp);
          hashForExtraction = Utils.base64urlEncode(bytes);
          hashForExtractionOngoing = false;
        }
      }
    }

    return hashForExtraction;
  }

  protected String getFingerprintString()
  {
    StringBuilder sb = StringBuilderUtils.acquire();
    addToFp(sb, name);
    addToFp(sb, required);
    addToFp(sb, tag);
    addToFp(sb, schemaOrgItemprop);
    addToFp(sb, contextNode);
    addToFp(sb, extractAsHtml);
    addToFp(sb, fieldParserKey);
    addToFp(sb, format);
    addCollectionToFp(sb, xpaths);
    addCollectionToFp(sb, fieldOps);
    for (MetaMetadataField field : kids)
    {
      if (field.hashForExtractionOngoing)
      {
        addToFp(sb, "self ref to " + field.name);
      }
      else
      {
        sb.append(field.name).append(" : ");
        addToFp(sb, field.getHashForExtraction());
      }
    }

    String fp = sb.toString();
    StringBuilderUtils.release(sb);
    return fp;
  }

  protected void addToFp(StringBuilder fpBuilder, Object obj)
  {
    fpBuilder.append(obj).append("\n");
  }

  protected void addCollectionToFp(StringBuilder fpBuilder, Collection collection)
  {
    if (collection != null)
    {
      for (Object obj : collection)
      {
        addToFp(fpBuilder, obj);
      }
    }
  }

  public String parentString()
  {
    StringBuilder result = new StringBuilder();

    ElementState parent = parent();
    while (parent instanceof MetaMetadataField)
    {
      MetaMetadataField pf = (MetaMetadataField) parent;
      result.insert(0, "<" + pf.name + ">");
      parent = parent.parent();
    }
    return result.toString();
  }

  @Override
  public String toString()
  {
    String result = toString;
    if (result == null)
    {
      result = getClassSimpleName() + parentString() + "<" + name + ">";
      toString = result;
    }
    return result;
  }

  public boolean isHashForExtractionOngoing()
  {
    return hashForExtractionOngoing;
  }

  protected void setName(String name)
  {
    this.name = name;
  }

  protected void setComment(String comment)
  {
    this.comment = comment;
  }

  protected void setTag(String tag)
  {
    this.tag = tag;
  }

  protected void setOtherTags(String otherTags)
  {
    this.otherTags = otherTags;
  }

  protected void setChildrenMap(HashMapArrayList<String, MetaMetadataField> childMetaMetadata)
  {
    this.kids = childMetaMetadata;
  }

  protected HashMapArrayList<String, MetaMetadataField> initializeChildrenMap()
  {
    return null;
  }

  protected void setRepository(MetaMetadataRepository repository)
  {
    this.repository = repository;
  }

  protected void setExtractAsHtml(boolean extractAsHtml)
  {
    this.extractAsHtml = extractAsHtml;
  }

  protected void setContextNode(String contextNode)
  {
    this.contextNode = contextNode;
  }

  protected void setFieldParserKey(String fieldParserKey)
  {
    this.fieldParserKey = fieldParserKey;
  }

  protected void setSchemaOrgItemprop(String schemaOrgItemprop)
  {
    this.schemaOrgItemprop = schemaOrgItemprop;
  }

  protected void setIndexed(boolean indexed)
  {
    this.indexed = indexed;
  }

  protected void setAsNaturalId(String asNaturalId)
  {
    this.asNaturalId = asNaturalId;
  }

  protected void setFormat(String format)
  {
    this.format = format;
  }

  protected void setRequired(boolean required)
  {
    this.required = required;
  }

  protected void setDontSerialize(boolean dontSerialize)
  {
    this.dontSerialize = dontSerialize;
  }

  protected void setIgnoreInTermVector(boolean ignoreInTermVector)
  {
    this.ignoreInTermVector = ignoreInTermVector;
  }

  protected void setIgnoreCompletely(boolean ignoreCompletely)
  {
    this.ignoreCompletely = ignoreCompletely;
  }

  protected void setHide(boolean hide)
  {
    this.hide = hide;
  }

  protected void setAlwaysShow(boolean alwaysShow)
  {
    this.alwaysShow = alwaysShow;
  }

  protected void setStyleName(String styleName)
  {
    this.styleName = styleName;
  }

  protected void setLayer(float layer)
  {
    this.layer = layer;
  }

  protected void setNavigatesTo(String navigatesTo)
  {
    this.navigatesTo = navigatesTo;
  }

  protected void setShadows(String shadows)
  {
    this.shadows = shadows;
  }

  protected void setLabel(String label)
  {
    this.label = label;
  }

  protected void setFacet(boolean isFacet)
  {
    this.isFacet = isFacet;
  }

  protected void setHideLabel(boolean hideLabel)
  {
    this.hideLabel = hideLabel;
  }

  protected void setUseValueAsLabel(String useValueAsLabel)
  {
    this.useValueAsLabel = useValueAsLabel;
  }

  protected void setConcatenatesTo(String concatenatesTo)
  {
    this.concatenatesTo = concatenatesTo;
  }

  protected void setLabelAt(String labelAt)
  {
    this.labelAt = labelAt;
  }

  protected void setInheritDone(boolean inheritDone)
  {
    this.inheritDone = inheritDone;
  }

  protected void setInheritOngoing(boolean inheritOngoing)
  {
    this.inheritOngoing = inheritOngoing;
  }

  protected void setSuperField(MetaMetadataField superField)
  {
    this.superField = superField;
  }

  protected void setDeclaringMmd(MetaMetadata declaringMmd)
  {
    this.declaringMmd = declaringMmd;
  }

  protected void setUsedToDefineInlineMmd(boolean usedToDefineInlineMmd)
  {
    this.usedToDefineInlineMmd = usedToDefineInlineMmd;
  }

  protected void setGenericTypeVars(MmdGenericTypeVarScope genericTypeVars)
  {
    this.genericTypeVars = genericTypeVars;
  }

  protected void setMetadataFieldDescriptor(MetadataFieldDescriptor metadataFieldDescriptor)
  {
    this.metadataFieldDescriptor = metadataFieldDescriptor;
  }

  protected void setMetadataClassDescriptor(MetadataClassDescriptor metadataClassDescriptor)
  {
    this.metadataClassDescriptor = metadataClassDescriptor;
  }

  protected void setMetadataClass(Class metadataClass)
  {
    this.metadataClass = metadataClass;
  }

  protected void setNonDisplayedFieldNames(HashSet<String> nonDisplayedFieldNames)
  {
    this.nonDisplayedFieldNames = nonDisplayedFieldNames;
  }

  protected void setFieldsSortedForDisplay(boolean fieldsSortedForDisplay)
  {
    this.fieldsSortedForDisplay = fieldsSortedForDisplay;
  }

  protected void resetToString()
  {
    this.toString = null;
  }

  protected void setFieldDescriptorProxy(MetadataFieldDescriptorProxy fieldDescriptorProxy)
  {
    this.fieldDescriptorProxy = fieldDescriptorProxy;
  }

  /**
   * @return The file in which this field is declared.
   */
  public File getFile()
  {
    Object parent = parent();
    if (parent != null && parent instanceof MetaMetadataField)
    {
      return ((MetaMetadataField) parent).getFile();
    }
    return null;
  }

  public void sortForDisplay()
  {
    if (!fieldsSortedForDisplay)
    {

      HashMapArrayList<String, MetaMetadataField> childMetaMetadata = getChildrenMap();
      if (childMetaMetadata != null)
        Collections.sort((ArrayList<MetaMetadataField>) childMetaMetadata.values(),
                         LAYER_COMPARATOR);
      fieldsSortedForDisplay = true;
    }
  }

  /**
   * Test if two fields are the same one or inherited from the same origin.
   */
  public boolean equalOrSameOrigin(Object obj)
  {
    if (obj instanceof MetaMetadataField)
    {
      MetaMetadataField f = (MetaMetadataField) obj;
      if (f.getName().equals(this.getName()) && f.getDeclaringMmd() == this.getDeclaringMmd())
      {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @return The tag if existed, or the name of this field. this method is different from
   *         getTagForTypesScope() which is overridden in MetaMetadataCollectionField. they have
   *         different purposes.
   */
  @Deprecated
  public String getTagOrName()
  {
    return tag != null ? tag : name;
  }

  /**
   * 
   * @return The tag used to look up metadata from MetadataTypesScope.
   */
  @Deprecated
  public String getTagForTypesScope()
  {
    return tag != null ? tag : name;
  }

  public String resolveTag()
  {
    return (tag != null) ? tag : name;
  }

  @Override
  public Iterator<MetaMetadataField> iterator()
  {
    HashMapArrayList<String, MetaMetadataField> childMetaMetadata = getChildrenMap();
    return (childMetaMetadata != null) ? new MetaMetadataFieldIterator() : EMPTY_ITERATOR;
  }

  @Override
  public String key()
  {
    return name;
  }

  public FieldType getFieldType()
  {
    if (this.metadataFieldDescriptor != null)
      return metadataFieldDescriptor.getType();
    else
    {
      if (this instanceof MetaMetadataCompositeField)
        return FieldType.COMPOSITE_ELEMENT;
      else if (this instanceof MetaMetadataCollectionField)
      {
        MetaMetadataCollectionField coll = (MetaMetadataCollectionField) this;
        if (coll.getChildScalarType() != null)
        {
          return FieldType.COLLECTION_SCALAR;
        }
        else
        {
          return FieldType.COLLECTION_ELEMENT;
        }
      }
      else
      {
        return FieldType.SCALAR;
      }
    }
  }

  public void inheritAttributes(MetaMetadataField inheritFrom, boolean overwrite)
  {
    MetaMetadataClassDescriptor classDescriptor = (MetaMetadataClassDescriptor) ClassDescriptor
        .getClassDescriptor(this);
    ;

    for (MetaMetadataFieldDescriptor fieldDescriptor : classDescriptor)
    {
      if (fieldDescriptor.isInheritable())
      {
        String fieldName = fieldDescriptor.getName();
        Field field = fieldDescriptor.getField();
        ScalarType scalarType = fieldDescriptor.getScalarType();

        try
        {
          // If it is one of the following cases we should stop actually inheriting attribute:
          // 1. scalarType is null.
          // 2. the attribute is a scalar or composite value, and we already have a value specified
          // on the sub-field, and overwrite is false.
          // 3. the value from the super-field is a default value.
          if (scalarType != null
              && (overwrite || fieldDescriptor.isCollection() || scalarType.isDefaultValue(field,
                                                                                           this))
              && !scalarType.isDefaultValue(field, inheritFrom))
          {
            Object value = field.get(inheritFrom);
            if (fieldDescriptor.isCollection())
            {
              // Append elements from inheritFrom to this field's list
              List list = (List) value;
              if (list != null && list.size() > 0)
              {
                List localList = (List) field.get(this);
                if (localList == null)
                {
                  localList = new ArrayList();
                  field.set(this, localList);
                }
                for (Object element : list)
                {
                  if (!localList.contains(element))
                  {
                    localList.add(element);
                  }
                }
              }
            }
            else
            {
              // Just a scalar value
              fieldDescriptor.setField(this, value);
            }

            Object localValue = field.get(this);
            logger.debug("Field attribute inherited: {}.{} = {}, from {}",
                         this,
                         fieldName,
                         localValue,
                         inheritFrom);
          }
        }
        catch (Exception e)
        {
          logger.error("Attribute inheritance failed: {}.{} from {}", this, fieldName, inheritFrom);
          logger.error("Exception details: ", e);
        }
      }
    }
  }

  /**
   * bind the corresponding metadata field descriptor to this field, matched by field name.
   * customize the field descriptor when needed.
   * <p>
   * note that the customization assumes that the field descriptor is copied from super class to
   * this class. if this changes in the future, the customization process should do this copy. see
   * {@code customizeFieldDescriptor()}.
   * <p>
   * lazy evaluation. result cached.
   * 
   * @param metadataTScope
   *          the translation scope of (generated) metadata classes.
   * @param metadataClassDescriptor
   *          the current metadata class descriptor.
   * @return the bound metadata field descriptor if any.
   * 
   * @see {@code customizeFieldDescriptor()}
   */
  protected MetadataFieldDescriptor bindMetadataFieldDescriptor(SimplTypesScope metadataTScope,
                                                                MetadataClassDescriptor metadataClassDescriptor)
  {
    MetadataFieldDescriptor metadataFieldDescriptor = this.metadataFieldDescriptor;
    if (metadataFieldDescriptor == null)
    {
      synchronized (this)
      {
        metadataFieldDescriptor = this.metadataFieldDescriptor;
        String fieldName = this.getFieldNameInJava(false);
        if (metadataFieldDescriptor == null)
        {
          metadataFieldDescriptor = (MetadataFieldDescriptor) metadataClassDescriptor
              .getFieldDescriptorByFieldName(fieldName);
          if (metadataFieldDescriptor != null)
          {
            // if we don't have a field, then this is a wrapped collection, so we need to get the
            // wrapped field descriptor
            if (metadataFieldDescriptor.getField() == null)
              metadataFieldDescriptor = (MetadataFieldDescriptor) metadataFieldDescriptor
                  .getWrappedFD();

            this.metadataFieldDescriptor = metadataFieldDescriptor;

            // this method handles polymorphic type / changing tags
            if (this.metadataFieldDescriptor != null)
            {
              fieldDescriptorProxy = new MetadataFieldDescriptorProxy();
              customizeFieldDescriptor(metadataTScope, fieldDescriptorProxy);
            }
            if (this.metadataFieldDescriptor != metadataFieldDescriptor)
            {
              // the field descriptor has been modified in customizeFieldDescriptor()!
              // we need to update it in the class descriptor so that deserialization of metadata
              // objects can work correctly, e.g. using the right classDescriptor for a composite
              // field or a right elementClassDescriptor for a collection field.
              customizeFieldDescriptorInClass(metadataTScope, metadataClassDescriptor);
            }
          }
        }
        else
        {
          warning("Ignoring <"
                  + fieldName
                  + "> because no corresponding MetadataFieldDescriptor can be found.");
        }
      }
    }
    return metadataFieldDescriptor;
  }

  private void customizeFieldDescriptorInClass(SimplTypesScope metadataTScope,
                                               MetadataClassDescriptor metadataClassDescriptor)
  {
    MetadataFieldDescriptor oldFD = metadataClassDescriptor.getFieldDescriptorByFieldName(this
        .getFieldNameInJava(false)); // oldFD is the non-wrapper one
    String newTagName = this.metadataFieldDescriptor.getTagName();

    metadataClassDescriptor.replace(oldFD, this.metadataFieldDescriptor);

    MetadataFieldDescriptor wrapperFD = (MetadataFieldDescriptor) this.metadataFieldDescriptor
        .getWrapper();
    if (wrapperFD != null)
    {
      MetadataFieldDescriptor clonedWrapperFD = wrapperFD.clone();
      clonedWrapperFD.setTagName(newTagName);
      clonedWrapperFD.setWrappedFD(this.metadataFieldDescriptor);
      metadataClassDescriptor.replace(wrapperFD, clonedWrapperFD);
    }

    FieldType fieldType = this.metadataFieldDescriptor.getType();
    if (fieldType == FieldType.COLLECTION_ELEMENT || fieldType == FieldType.MAP_ELEMENT)
    {
      if (!this.metadataFieldDescriptor.isWrapped())
      {
        String childTagName = this.metadataFieldDescriptor.getCollectionOrMapTagName();
        oldFD = metadataClassDescriptor.getFieldDescriptorByTag(childTagName, metadataTScope);
        metadataClassDescriptor.replace(oldFD, this.metadataFieldDescriptor);
      }
    }
  }

  /**
   * this method customizes field descriptor for this field, e.g. specific type or tag.
   * 
   * @param metadataTScope
   *          the translation scope of (generated) metadata classes.
   * @param fdProxy
   *          the current metadata field descriptor.
   */
  protected void customizeFieldDescriptor(SimplTypesScope metadataTScope,
                                          MetadataFieldDescriptorProxy fdProxy)
  {
    fdProxy.setTagName(this.getTagOrName());
  }

  /**
   * 
   * @param deserializationMM
   * @return true if binding succeeds
   */
  public boolean validateMetaMetadataToMetadataBinding(MetaMetadataField deserializationMM)
  {
    if (deserializationMM != null) // should be always
    {
      MetadataClassDescriptor originalClassDescriptor = this.getMetadataClassDescriptor();
      MetadataClassDescriptor deserializationClassDescriptor = deserializationMM
          .getMetadataClassDescriptor();

      // quick fix for a NullPointerException for RSS. originalClassDescriptor can be null because
      // it might be a meta-metadata that does not generate metadata class, e.g. xml
      if (originalClassDescriptor == null)
        return true; // use the one from deserialization

      boolean sameMetadataSubclass = originalClassDescriptor.equals(deserializationClassDescriptor);
      // if they have the same metadataClassDescriptor, they can be of the same type, or one
      // of them is using "type=" attribute.
      boolean useMmdFromDeserialization = sameMetadataSubclass
                                          && (deserializationMM.getType() != null);
      if (!useMmdFromDeserialization && !sameMetadataSubclass)
        // if they have different metadataClassDescriptor, need to choose the more specific one
        useMmdFromDeserialization = originalClassDescriptor.getDescribedClass()
            .isAssignableFrom(
                              deserializationClassDescriptor.getDescribedClass());
      return useMmdFromDeserialization;
    }
    else
    {
      error("No meta-metadata in root after direct binding :-(");
      return false;
    }
  }

  protected String generateNewClassName()
  {
    String javaClassName = null;

    if (this instanceof MetaMetadataCollectionField)
    {
      javaClassName = ((MetaMetadataCollectionField) this).getChildType();
    }
    else
    {
      javaClassName = ((MetaMetadataCompositeField) this).getTypeOrName();
    }

    return javaClassName;
  }

  /**
   * get the type name of this field, in terms of meta-metadata.
   * 
   * TODO redefining this.
   * 
   * @return the type name.
   */
  abstract protected String getTypeName();

  /**
   * generate java type name string. since type name will be used for several times (both in member
   * definition and methods), it should be cached.
   * 
   * note that this could be different from changing getTypeName() into camel case: consider Entity
   * for composite fields or ArrayList for collection fields.
   */
  abstract protected String getTypeNameInJava();

  abstract public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(SimplTypesScope tscope,
                                                                                MetadataClassDescriptor contextCd);

  /**
   * Add additional meta information about the field to an existing meta information list.
   * 
   * @param metaInfoBuf
   *          The existing meta information list. Additional meta information will be added to this
   *          list. Cannot be null.
   * @param compiler
   *          Providing compiler services such as dependency handling, in case needed in this
   *          method. This is not used right now; just for extensibility.
   */
  abstract public void addAdditionalMetaInformation(List<MetaInformation> metaInfoBuf,
                                                    MmdCompilerService compiler);

}
