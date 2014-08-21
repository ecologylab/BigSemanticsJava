package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.Collection;

import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataParsedURLScalarType;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField.AttributeChangeListener;
import ecologylab.bigsemantics.metametadata.declarations.MetaMetadataFieldDeclaration;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringTools;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.GenericTypeVar;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.types.ScalarType;

@SuppressWarnings("rawtypes")
@simpl_inherit
@simpl_tag("collection")
public class MetaMetadataCollectionField extends MetaMetadataNestedField
{

  public static final String UNRESOLVED_NAME = "&UNRESOLVED_NAME";

  /**
   * Each object in a collection of metadata require a specific MMdata composite object to be
   * associated with them. This is unavailable in the MMD XML, and must be generated when the XML is
   * read in.
   */
  @Override
  public void deserializationPostHook(TranslationContext translationContext, Object object)
  {
    if (this.isInheritDone())
    {
      return;
    }

    FieldType typeCode = this.getFieldType();
    if (typeCode == FieldType.COLLECTION_SCALAR)
    {
      return;
    }

    HashMapArrayList<String, MetaMetadataField> kids = super.getChildrenMap();
    String childType = getChildType();
    String childCompositeName = childType != null ? childType : UNRESOLVED_NAME;
    final MetaMetadataCollectionField thisField = this;
    MetaMetadataCompositeField composite = new MetaMetadataCompositeField(childCompositeName, kids);
    composite.setAttributeChangeListener(new AttributeChangeListener()
    {
      @Override
      public void typeChanged(String newType)
      {
        thisField.childType = newType;
      }

      @Override
      public void extendsChanged(String newExtends)
      {
        thisField.childExtends = newExtends;
      }

      @Override
      public void tagChanged(String newTag)
      {
        if (thisField.childTag == null)
          thisField.childTag = newTag;
      }

      @Override
      public void usedForInlineMmdDefChanged(boolean usedForInlineMmdDef)
      {
        thisField.setUsedToDefineInlineMmd(usedForInlineMmdDef);
      }
    });
    composite.setParent(this);
    composite.setType(childType);
    composite.setExtendsAttribute(this.childExtends);
    kids.clear();
    kids.put(composite.getName(), composite);
    composite.setPromoteChildren(this.shouldPromoteChildren());
  }

  @simpl_scalar
  private String             childTag;

  /**
   * The type for collection children.
   */
  @simpl_scalar
  private String             childType;

  @mm_dont_inherit
  @simpl_scalar
  private String             childExtends;

  @simpl_scalar
  private MetadataScalarType childScalarType;

  /**
   * Specifies adding @simpl_nowrap to the collection object in cases where items in the collection
   * are not wrapped inside a tag.
   */
  @simpl_scalar
  private boolean            noWrap;

  /**
   * for caching getTypeNameInJava().
   */
  private String             typeNameInJava = null;

  /**
   * Another field name whose value can be used as label for each child
   */
  @simpl_scalar
  private String             childUseValueAsLabel;

  /**
   * to show composite children expanded
   */
  @simpl_scalar
  private boolean            childShowExpandedInitially;

  @simpl_scalar
  private boolean            childShowExpandedAlways;

  public MetaMetadataCollectionField()
  {
    // no op
  }

  public String getChildTag()
  {
    if (childTag != null)
      return childTag;
    if (childType != null)
      return childType;
    return null;
  }

  public String getChildType()
  {
    return childType;
  }

  public String getChildExtends()
  {
    return childExtends;
  }

  public ScalarType getChildScalarType()
  {
    return childScalarType;
  }

  public boolean isNoWrap()
  {
    return noWrap;
  }

  @Override
  public String getType()
  {
    return getChildType();
  }

  @Override
  public String getExtendsAttribute()
  {
    return getChildExtends();
  }

  @Override
  public HashMapArrayList<String, MetaMetadataField> getChildrenMap()
  {
    HashMapArrayList<String, MetaMetadataField> kids = super.getChildrenMap();
    return (kids != null && kids.size() > 0) ? kids.get(0).getChildrenMap() : null;
  }

  public MetaMetadataCompositeField getChildComposite()
  {
    HashMapArrayList<String, MetaMetadataField> kids = super.getChildrenMap();
    return (kids != null && kids.size() > 0) ? (MetaMetadataCompositeField) kids.get(0) : null;
  }

  public String getChildUseValueAsLabel()
  {
    return childUseValueAsLabel;
  }

  public boolean isChildShowExpandedInitially()
  {
    return childShowExpandedInitially;
  }

  public boolean isChildShowExpandedAlways()
  {
    return childShowExpandedAlways;
  }

  public boolean isCollectionOfScalars()
  {
    if (childScalarType != null)
    {
      return true;
    }
    MetaMetadataFieldDeclaration superField = getSuperField();
    if (superField != null)
    {
      return ((MetaMetadataCollectionField) superField).isCollectionOfScalars();
    }
    return false;
  }

  @Override
  protected String getTypeNameInJava()
  {
    String rst = typeNameInJava;
    if (rst == null)
    {
      String className = null;
      if (this.getFieldType() == FieldType.COLLECTION_SCALAR)
      {
        className = this.getChildScalarType().getJavaClass().getSimpleName();
      }
      else
      {
        String typeName = getTypeName();
        MetaMetadata mmd = getRepository().getMMByName(typeName);
        if (mmd != null && mmd.getType() != null && mmd.getExtendsAttribute() == null)
        {
          typeName = mmd.getType();
        }
        className = XMLTools.classNameFromElementName(typeName);
      }
      rst = "List<" + className + ">";
      typeNameInJava = rst;
    }
    return typeNameInJava;
  }

  public void setChildType(String childType)
  {
    this.childType = childType;
  }

  protected void setChildScalarType(MetadataParsedURLScalarType childScalarType)
  {
    this.childScalarType = childScalarType;
  }

  @Override
  public String resolveTag()
  {
    if (isNoWrap())
    {
      return (childTag != null) ? childTag : childType;
    }
    else
    {
      return (getTag() != null) ? getTag() : getName();
    }
  }

  @Override
  public String getTagForTypesScope()
  {
    // FIXME: seems broken when rewriting collection xpath without re-indicating child_type
    return childType != null ? childType : getTag() != null ? getTag() : getName();
  }

  @Override
  protected String getMetaMetadataTagToInheritFrom()
  {
    return childType != null ? childType : null;
  }

  /**
   * Get the MetaMetadataCompositeField associated with this.
   * 
   * @return this, because it is a composite itself.
   */
  @Override
  public MetaMetadataCompositeField metaMetadataCompositeField()
  {
    return getChildComposite();
  }

  @Override
  protected boolean inheritMetaMetadataHelper(InheritanceHandler inheritanceHandler)
  {
    boolean result = false;

    /*
     * the childComposite should hide all complexity between collection fields and composite fields,
     * through hooks when necessary.
     */
    FieldType typeCode = this.getFieldType();
    switch (typeCode)
    {
    case COLLECTION_ELEMENT:
    {
      // prepare childComposite: possibly new name, type, extends, tag and inheritedField
      MetaMetadataCompositeField childComposite = this.getChildComposite();
      if (childComposite.getName().equals(UNRESOLVED_NAME))
        childComposite.setName(this.childType == null ? this.getName() : this.childType);
      childComposite.setType(this.childType); // here not using setter to reduce unnecessary
      // re-assignment of this.childType
      childComposite.setExtendsAttribute(this.childExtends);
      childComposite.setTag(this.childTag);
      childComposite.setRepository(this.getRepository());
      childComposite.setPackageName(this.packageName());

      MetaMetadataCollectionField inheritedField =
          (MetaMetadataCollectionField) this.getSuperField();
      if (inheritedField != null)
      {
        childComposite.setSuperField(inheritedField.getChildComposite());
      }
      childComposite.setDeclaringMmd(this.getDeclaringMmd());
      childComposite.setMmdScope(this.getMmdScope());

      // inheritedMmd might be inferred from type/extends
      result = childComposite.inheritMetaMetadata(inheritanceHandler);

      this.setTypeMmd(childComposite.getTypeMmd());
      this.setMmdScope(childComposite.getMmdScope());
      break;
    }
    case COLLECTION_SCALAR:
    {
      MetaMetadataField inheritedField = this.getSuperField();
      if (inheritedField != null)
        this.inheritAttributes(inheritedField, false);
      result = true;
      break;
    }
    }

    return result;
  }

  @Override
  protected void clearInheritFinishedOrInProgressFlag()
  {
    super.clearInheritFinishedOrInProgressFlag();
    if (this.getChildComposite() != null)
    {
      this.getChildComposite().clearInheritFinishedOrInProgressFlag();
    }
  }

  @Override
  protected MetadataClassDescriptor bindMetadataClassDescriptor(SimplTypesScope metadataTScope)
  {
    FieldType fieldType = getFieldType();
    if (fieldType == FieldType.COLLECTION_ELEMENT)
    {
      MetaMetadataCompositeField childComposite = getChildComposite();
      if (childComposite != null)
        return childComposite.bindMetadataClassDescriptor(metadataTScope);
    }
    return null;
  }

  @Override
  protected void customizeFieldDescriptor(SimplTypesScope metadataTScope,
                                          MetadataFieldDescriptorProxy fdProxy)
  {
    super.customizeFieldDescriptor(metadataTScope, fdProxy);

    fdProxy.setCollectionOrMapTagName(this.childTag);
    fdProxy.setWrapped(!this.isNoWrap());
  }

  @Override
  public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(SimplTypesScope tscope,
                                                                       MetadataClassDescriptor contextCd)
  {
    MetadataFieldDescriptor fd = this.getMetadataFieldDescriptor();
    if (fd == null)
    {
      String tagName = this.resolveTag();
      String fieldName = this.getFieldNameInJava(false);
      String javaTypeName = this.getTypeNameInJava();
      boolean wrapped = !this.isNoWrap();
      if (!wrapped)
      {
        tagName = null;
      }

      String genericTypeName = null;
      GenericTypeVar genericTypeVar = new GenericTypeVar();
      FieldType typeCode = this.getFieldType();
      switch (typeCode)
      {
      case COLLECTION_ELEMENT:
      {
        MetaMetadata inheritedMmd = this.getTypeMmd();
        assert inheritedMmd != null : "IMPOSSIBLE: inheritedMmd == null: something wrong in the inheritance process!";
        inheritedMmd.findOrGenerateMetadataClassDescriptor(tscope);
        MetadataClassDescriptor fieldCd = inheritedMmd.getMetadataClassDescriptor();
        assert fieldCd != null : "IMPOSSIBLE: fieldCd == null: something wrong in the inheritance process!";
        fd = new MetadataFieldDescriptor(
                                         this,
                                         tagName,
                                         this.getComment(),
                                         typeCode,
                                         fieldCd,
                                         contextCd,
                                         fieldName,
                                         null,
                                         null,
                                         javaTypeName);
        genericTypeName = fieldCd.getDescribedClassSimpleName();
        if (StringTools.isUpperCase(childType))
        {
          genericTypeVar.setName(childType);
        }
        else
        {
          genericTypeVar.setClassDescriptor(fieldCd);
        }
        Collection<MmdGenericTypeVar> metaMetadataGenericTypeVars =
            this.getGenericTypeVarsCollection();
        if (metaMetadataGenericTypeVars != null && metaMetadataGenericTypeVars.size() > 0)
        {
          for (MmdGenericTypeVar mmdgtv : metaMetadataGenericTypeVars)
          {
            GenericTypeVar gtv = new GenericTypeVar();
            gtv.setName(mmdgtv.getArg()); // FIXME this is just one case.
            genericTypeVar.addGenericTypeVarArg(gtv);
          }
        }
        break;
      }
      case COLLECTION_SCALAR:
      {
        if (this.getChildrenSize() > 0)
          warning("Ignoring nested fields inside "
                  + this
                  + " because child_scalar_type specified ...");

        ScalarType scalarType = this.getChildScalarType();
        if (scalarType != null && childTag == null)
        {
          String name = this.getName();
          childTag = name.endsWith("s") ? name.substring(0, name.length() - 1) : name;
          warning("child_tag is necessary when using collection of scalars! will use a default child_tag: "
                  + childTag);
        }
        fd = new MetadataFieldDescriptor(
                                         this,
                                         tagName,
                                         this.getComment(),
                                         typeCode,
                                         null,
                                         contextCd,
                                         fieldName,
                                         scalarType,
                                         null,
                                         javaTypeName);
        genericTypeName = scalarType.getCSharpTypeName();
        genericTypeVar.setClassDescriptor(ClassDescriptor.getClassDescriptor(scalarType
            .getJavaClass()));
        break;
      }
      }
      fd.setWrapped(wrapped);
      fd.setGeneric("<" + genericTypeName + ">");
      ArrayList<GenericTypeVar> derivedGenericTypeVariables = new ArrayList<GenericTypeVar>();
      derivedGenericTypeVariables.add(genericTypeVar);
      fd.setGenericTypeVars(derivedGenericTypeVariables);
    }
    this.setMetadataFieldDescriptor(fd);
    return fd;
  }

  @Override
  public void recursivelyRestoreChildComposite()
  {
    FieldType typeCode = this.getFieldType();
    if (typeCode == FieldType.COLLECTION_SCALAR)
      return;

    if (getChildrenSize() == 1)
    {
      MetaMetadataCompositeField childComposite = getChildComposite();
      if (childComposite != null)
      {
        HashMapArrayList<String, MetaMetadataField> childsKids = childComposite.getChildrenMap();
        this.setChildrenMap(childsKids);
        if (childsKids != null)
          for (MetaMetadataFieldDeclaration field : childsKids)
          {
            field.setParent(this);
            if (field instanceof MetaMetadataNestedField)
              ((MetaMetadataNestedField) field).recursivelyRestoreChildComposite();
          }
      }
      return;
    }
    warning("collection field without a (correct) child composite: " + this);
  }

  @Override
  protected String getFingerprintString()
  {
    StringBuilder sb = StringBuilderUtils.acquire();
    sb.append(super.getFingerprintString());
    addToFp(sb, childTag);
    addToFp(sb, childType);
    addToFp(sb, childExtends);
    addToFp(sb, childScalarType);
    addToFp(sb, noWrap);
    String fp = sb.toString();
    StringBuilderUtils.release(sb);
    return fp;
  }

}
