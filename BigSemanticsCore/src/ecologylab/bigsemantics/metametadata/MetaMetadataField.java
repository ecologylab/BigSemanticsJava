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
import ecologylab.bigsemantics.metametadata.declarations.MetaMetadataFieldDeclaration;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.MetaInformation;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_descriptor_classes;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_map_key_field;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scope;
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
public abstract class MetaMetadataField extends MetaMetadataFieldDeclaration
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
      return -Float.compare(o1.getLayer(), o2.getLayer());
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

  /**
   * The nested child fields inside this field.
   */
  @simpl_map
  @simpl_scope(NestedMetaMetadataFieldTypesScope.NAME)
  @simpl_nowrap
  @mm_dont_inherit
  private HashMapArrayList<String, MetaMetadataField> kids;

  @simpl_collection
  @simpl_scope(FieldOpScope.NAME)
  @simpl_nowrap
  List<FieldOp>                               fieldOps;

  // ////////////// Presentation Semantics Below ////////////////

  /**
   * Collection of styles
   */
  @simpl_collection("style")
  @simpl_nowrap
  private List<MetaMetadataStyle>                     styles;

  

  // ////////////// Members Below ////////////////

  private MetaMetadataRepository                      repository;

  private boolean                                     inheritOngoing;

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

  public MetaMetadataField(String name, HashMapArrayList<String, MetaMetadataField> childrenMap)
  {
    this.setName(name);
    this.setChildrenMap(childrenMap);
  }

  protected MetaMetadataField(MetaMetadataField copy, String name)
  {
    this.setName(name);
    this.setTag(copy.getTag());
    this.setChildrenMap(copy.getChildrenMap());
  }

  public String getType()
  {
    return null;
  }

  public String getExtendsAttribute()
  {
    return null;
  }

  /**
   * @return The nested fields inside of this one as a collection.
   */
  public Collection<MetaMetadataField> getChildren()
  {
    return kids == null ? null : kids.values();
  }

  public int getChildrenSize()
  {
    return kids == null ? 0 : kids.size();
  }

  public boolean hasChildren()
  {
    return kids != null && kids.size() > 0;
  }
  
  public MetaMetadataField getChild(int i)
  {
    return kids == null ? null : kids.get(i);
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

  public MetaMetadataField lookupChild(String name)
  {
    HashMapArrayList<String, MetaMetadataField> childrenMap = getChildrenMap();
    return childrenMap == null ? null : childrenMap.get(name);
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
        && this.parent() == ((MetaMetadataCollectionField) parentField).getElementComposite())
      return true;
    return false;
  }

  public boolean hasAuthoredChild(MetaMetadataField childField)
  {
    return childField.isAuthoredChildOf(this);
  }

  public List<FieldOp> getFieldOps()
  {
    return fieldOps;
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

  public String getSchemaOrgItemtype()
  {
    return null;
  }

  public List<MetaMetadataStyle> getStyles()
  {
    return styles;
  }

  public NamedStyle lookupStyle()
  {
    NamedStyle result = null;
    String styleName = getStyleName();
    if (styleName != null)
      result = getRepository().lookupStyle(styleName);
    return (result != null) ? result : getRepository().getDefaultStyle();
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

  public boolean isInheritOngoing()
  {
    return inheritOngoing;
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
    addToFp(sb, getName());
    addToFp(sb, isRequired());
    addToFp(sb, getTag());
    addToFp(sb, getSchemaOrgItemprop());
    addToFp(sb, getContextNode());
    addToFp(sb, isExtractAsHtml());
    addToFp(sb, getFieldParserKey());
    addToFp(sb, getFormat());
    addCollectionToFp(sb, getXpaths());
    addCollectionToFp(sb, fieldOps);
    for (MetaMetadataField field : kids)
    {
      if (field.hashForExtractionOngoing)
      {
        addToFp(sb, "self ref to " + field.getName());
      }
      else
      {
        sb.append(field.getName()).append(" : ");
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
      result.insert(0, "<" + pf.getName() + ">");
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
      result = getClassSimpleName() + parentString() + "<" + getName() + ">";
      toString = result;
    }
    return result;
  }

  public boolean isHashForExtractionOngoing()
  {
    return hashForExtractionOngoing;
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

  protected void setInheritOngoing(boolean inheritOngoing)
  {
    this.inheritOngoing = inheritOngoing;
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

  public MetaMetadata getTypeMmd()
  {
    return null;
  }

  public void setTypeMmd(MetaMetadata result)
  {
    // no op
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
    return getTag() != null ? getTag() : getName();
  }

  /**
   * 
   * @return The tag used to look up metadata from MetadataTypesScope.
   */
  @Deprecated
  public String getTagForTypesScope()
  {
    return getTag() != null ? getTag() : getName();
  }

  public String resolveTag()
  {
    return getTag() != null ? getTag() : getName();
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
    return getName();
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
              && (overwrite
                  || fieldDescriptor.isCollection()
                  || scalarType.isDefaultValue(field, this))
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
