package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.Collection;

import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField.AttributeChangeListener;
import ecologylab.bigsemantics.metametadata.declarations.MetaMetadataCollectionFieldDeclaration;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringTools;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.GenericTypeVar;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.types.ScalarType;

/**
 * A collection field.
 * 
 * @author quyin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@simpl_inherit
@simpl_tag("collection")


public class MetaMetadataCollectionField extends MetaMetadataCollectionFieldDeclaration
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

    createElementComposite();
  }

  /**
   * for caching getTypeNameInJava().
   */
  private String typeNameInJava;

  public MetaMetadataCollectionField()
  {
    // no op
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

  public String getChildTag()
  {
    if (super.getChildTag() != null)
      return super.getChildTag();
    if (super.getChildType() != null)
      return super.getChildType();
    return null;
  }

  @Override
  public HashMapArrayList<String, MetaMetadataField> getChildrenMap()
  {
    HashMapArrayList<String, MetaMetadataField> kids = super.getChildrenMap();
    return (kids != null && kids.size() > 0) ? kids.get(0).getChildrenMap() : null;
  }

  public MetaMetadataCompositeField getElementComposite()
  {
    HashMapArrayList<String, MetaMetadataField> kids = super.getChildrenMap();
    return (kids != null && kids.size() > 0) ? (MetaMetadataCompositeField) kids.get(0) : null;
  }

  public MetaMetadataCompositeField getPreparedElementComposite()
  {
    MetaMetadataCompositeField elementComposite = getElementComposite();
    prepareElementComposite(elementComposite);
    return elementComposite;
  }

  public MetaMetadataCompositeField createElementComposite()
  {
    HashMapArrayList<String, MetaMetadataField> kids = super.getChildrenMap();
    String childType = getChildType();
    String elementCompositeName = childType != null ? childType : UNRESOLVED_NAME;
    MetaMetadataCompositeField composite =
        new MetaMetadataCompositeField(elementCompositeName, kids);
    composite.setParent(this);
    composite.setEnclosingCollectionField(this);

    final MetaMetadataCollectionField thisField = this;
    composite.setAttributeChangeListener(new AttributeChangeListener()
    {
      @Override
      public void typeChanged(String newType)
      {
        thisField.setChildType(newType);
      }

      @Override
      public void extendsChanged(String newExtends)
      {
        thisField.setChildExtends(newExtends);
      }

      @Override
      public void tagChanged(String newTag)
      {
        if (thisField.getChildTag() == null)
        {
          thisField.setChildTag(newTag);
        }
      }

      @Override
      public void inlineMmdChanged(MetaMetadata newInlineMmd)
      {
        thisField.setInlineMmd(newInlineMmd);
      }

      @Override
      public void typeMmdChanged(MetaMetadata newTypeMmd)
      {
        thisField.setTypeMmd(newTypeMmd);
      }

      @Override
      public void scopeChanged(MmdScope newScope)
      {
        thisField.setScope(newScope);
      }
    });

    kids.clear();
    kids.put(composite.getName(), composite);

    prepareElementComposite(composite);

    return composite;
  }

  private void prepareElementComposite(MetaMetadataCompositeField composite)
  {
    if (composite.getName().equals(UNRESOLVED_NAME))
      composite.setName(this.getChildType() == null ? this.getName() : this.getChildType());

    composite.setRepository(this.getRepository());
    composite.setPackageName(this.packageName());
    composite.setDeclaringMmd(this.getDeclaringMmd());

    composite.setPromoteChildren(this.isPromoteChildren());

    // use set*Directly() to reduce unnecessary trigger of AttributeChangeListener
    composite.setTypeDirectly(this.getChildType());
    composite.setExtendsAttributeDirectly(this.getChildExtends());
    composite.setTagDirectly(this.getChildTag());
    composite.setScopeDirectly(this.scope());

    MetaMetadataCollectionField superField = (MetaMetadataCollectionField) this.getSuperField();
    if (superField != null)
    {
      composite.setSuperField(superField.getElementComposite());
    }
  }

  public boolean isCollectionOfScalars()
  {
    if (getChildScalarType() != null)
    {
      return true;
    }
    MetaMetadataField superField = getSuperField();
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

  @Override
  public String getTagForTypesScope()
  {
    // FIXME: seems broken when rewriting collection xpath without re-indicating child_type
    return getChildType() != null ? getChildType() : getTag() != null ? getTag() : getName();
  }

  @Override
  public String resolveTag()
  {
    if (isNoWrap())
    {
      return (getChildTag() != null) ? getChildTag() : getChildType();
    }
    else
    {
      return (getTag() != null) ? getTag() : getName();
    }
  }

  /**
   * Get the MetaMetadataCompositeField associated with this.
   * 
   * @return this, because it is a composite itself.
   */
  @Override
  public MetaMetadataCompositeField metaMetadataCompositeField()
  {
    return getPreparedElementComposite();
  }

  @Override
  protected boolean isInlineDefinition()
  {
    return getElementComposite().isInlineDefinition();
  }

  @Override
  protected MetadataClassDescriptor bindMetadataClassDescriptor(SimplTypesScope metadataTScope)
  {
    FieldType fieldType = getFieldType();
    if (fieldType == FieldType.COLLECTION_ELEMENT)
    {
      MetaMetadataCompositeField childComposite = getElementComposite();
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

    fdProxy.setCollectionOrMapTagName(this.getChildTag());
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
        fd = new MetadataFieldDescriptor(this,
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
        if (StringTools.isUpperCase(getChildType()))
        {
          genericTypeVar.setName(getChildType());
        }
        else
        {
          genericTypeVar.setClassDescriptor(fieldCd);
        }
        Collection<MmdGenericTypeVar> metaMetadataGenericTypeVars =
            this.getGenericTypeVarsList();
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
        if (scalarType != null && getChildTag() == null)
        {
          String name = this.getName();
          setChildTag(name.endsWith("s") ? name.substring(0, name.length() - 1) : name);
          warning("child_tag is necessary when using collection of scalars! will use a default child_tag: "
                  + getChildTag());
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
        ClassDescriptor<? extends FieldDescriptor> cd =
            ClassDescriptor.getClassDescriptor(scalarType.getJavaClass());
        genericTypeVar.setClassDescriptor(cd);
        break;
      }
      default:
      {
        error("Unrecognized field type " + typeCode + " for " + this);
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
      MetaMetadataCompositeField childComposite = getElementComposite();
      if (childComposite != null)
      {
        HashMapArrayList<String, MetaMetadataField> childsKids = childComposite.getChildrenMap();
        this.setChildrenMap(childsKids);
        if (childsKids != null)
          for (MetaMetadataField field : childsKids)
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
    addToFp(sb, getChildTag());
    addToFp(sb, getChildType());
    addToFp(sb, getChildExtends());
    addToFp(sb, getChildScalarType());
    addToFp(sb, isNoWrap());
    String fp = sb.toString();
    StringBuilderUtils.release(sb);
    return fp;
  }

}
