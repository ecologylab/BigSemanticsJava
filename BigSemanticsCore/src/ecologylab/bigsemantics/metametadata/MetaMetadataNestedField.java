package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ecologylab.bigsemantics.Utils;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.mm_name;
import ecologylab.bigsemantics.metadata.semantics_mixin;
import ecologylab.bigsemantics.metametadata.declarations.MetaMetadataNestedFieldDeclaration;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.MetaInformation;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * A nested field.
 * 
 * @author quyin
 */
@SuppressWarnings({ "rawtypes" })
@simpl_inherit
public abstract class MetaMetadataNestedField extends MetaMetadataNestedFieldDeclaration
    implements PackageSpecifier
{

  public static final String POLYMORPHIC_CLASSES_SEP = ",";

  @simpl_composite
  @simpl_tag("field_parser")
  private FieldParserElement fieldParserElement;

  @simpl_composite
  private MmdScope           scope;

  private boolean            mmdScopeTraversed;

  public MetaMetadataNestedField()
  {
    super();
  }

  public MetaMetadataNestedField(String name, HashMapArrayList<String, MetaMetadataField> set)
  {
    super(name, set);
  }

  public MetaMetadataNestedField(MetaMetadataField copy, String name)
  {
    super(copy, name);
  }

  public String packageName()
  {
    return this.getPackageName();
  }

  public FieldParserElement getFieldParserElement()
  {
    return fieldParserElement;
  }

  public MmdScope getScope()
  {
    return scope;
  }

  public MmdScope scope()
  {
    if (scope == null)
    {
      scope = new MmdScope(this.toString());

      // also put all local GTVs into the scope
      Map<String, MmdGenericTypeVar> gtvScope = getGenericTypeVars();
      if (gtvScope != null)
      {
        for (String localGtvName : gtvScope.keySet())
        {
          MmdGenericTypeVar gtv = gtvScope.get(localGtvName);
          scope.put(localGtvName, gtv);
          gtv.scope().addAncestor(scope);
        }
      }
    }
    return scope;
  }

  public void setScope(MmdScope scope)
  {
    this.scope = scope;
  }

  /**
   * Get the MetaMetadataCompositeField associated with this.
   * 
   * @return
   */
  public abstract MetaMetadataCompositeField metaMetadataCompositeField();

  @Override
  public String getTypeName()
  {
    String result = null;
    if (this instanceof MetaMetadataCompositeField)
    {
      MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) this;
      if (mmcf.getType() != null)
        result = mmcf.getType();
    }
    else if (this instanceof MetaMetadataCollectionField)
    {
      MetaMetadataCollectionField mmcf = (MetaMetadataCollectionField) this;
      if (mmcf.getChildType() != null)
        result = mmcf.getChildType();
      else if (mmcf.getChildScalarType() != null)
        result = mmcf.getChildScalarType().getJavaTypeName();
    }

    if (result == null)
    {
      MetaMetadataField inherited = getSuperField();
      if (inherited != null)
      {
        // use inherited field's type
        result = inherited.getTypeName();
      }
    }

    if (result == null)
    {
      // defining new type inline without using type= / child_type=
      result = getName();
    }

    return result;
  }

  protected String getMetadataClassName()
  {
    return this.getTypeMmd().getMetadataClassName();
  }

  /**
   * 
   * @return the corresponding Metadata class simple name.
   */
  protected String getMetadataClassSimpleName()
  {
    MetaMetadata inheritedMmd = this.getTypeMmd();
    return inheritedMmd.getMetadataClassSimpleName();
  }

  /**
   * to determine if this field is polymorphic inherently, that is, a field which we don't have
   * prior knowledge of its specific meta-metadata type when its encompassing meta-metadata is used.
   * <p />
   * NOTE THAT this is different from {@code isPolymorphicInDescendantFields()} which determines if
   * this field is used for extended types in descendant fields. in that case although the field is
   * polymorphic, too, but we can determine the specific meta-metadata type for this field if the
   * encompassing meta-metadata is used.
   * 
   * @return
   * @see isPolymorphicInDescendantFields
   */
  public boolean isPolymorphicInherently()
  {
    String polymorphicScope = getPolymorphicScope();
    String polymorphicClasses = getPolymorphicClasses();
    return (polymorphicScope != null && polymorphicScope.length() > 0)
           || (polymorphicClasses != null && polymorphicClasses.length() > 0);
  }

  /**
   * Determine if there is an inline meta-metadata defined by this field.
   * 
   * @return
   */
  abstract protected boolean isInlineDefinition();

  /**
   * bind metadata field descriptors to sub-fields of this nested field, with field names as keys,
   * but without mixins field.
   * <p>
   * sub-fields that lack corresponding field descriptors will be removed from this nested field.
   * <p>
   * note that this field no longer uses a boolean flag to prevent multiple invocation. this should
   * have been done by the bindClassDescriptor() method.
   * 
   * @param metadataTScope
   *          the translation scope of (generated) metadata classes.
   * @param metadataClassDescriptor
   *          the metadata class descriptor where field descriptors can be found.
   */
  protected void bindMetadataFieldDescriptors(SimplTypesScope metadataTScope,
                                              MetadataClassDescriptor metadataClassDescriptor)
  {
    // copy the kids collection first to prevent modification to the collection during iteration
    // (which may invalidate the iterator).
    List<MetaMetadataField> fields = new ArrayList<MetaMetadataField>(this.getChildren());
    for (MetaMetadataField thatChild : fields)
    {
      // look up by field name and bind
      MetadataFieldDescriptor metadataFieldDescriptor = thatChild
          .bindMetadataFieldDescriptor(metadataTScope, metadataClassDescriptor);
      if (metadataFieldDescriptor == null)
      {
        warning("Cannot bind metadata field descriptor for " + thatChild);
        this.getChildrenMap().remove(thatChild.getName());
        continue;
      }

      metadataFieldDescriptor.setDefiningMmdField(thatChild);

      // // process hide and shadows
      // HashSet<String> nonDisplayedFieldNames = nonDisplayedFieldNames();
      // if (thatChild.isHide() && !thatChild.isAlwaysShow())
      // nonDisplayedFieldNames.add(thatChild.getName());
      // if (thatChild.getShadows() != null)
      // nonDisplayedFieldNames.add(thatChild.getShadows());

      // recursively process sub-fields
      if (thatChild instanceof MetaMetadataScalarField)
      {
        // no! we can't add regex filters to field descriptors, because field descriptors
        // are shared between fields and inherited fields.
        // MetaMetadataScalarField scalar = (MetaMetadataScalarField) thatChild;
        // if (scalar.getRegexPattern() != null)
        // {
        // MetadataFieldDescriptor fd = scalar.getMetadataFieldDescriptor();
        // if (fd != null)
        // fd.setRegexFilter(scalar.getRegexPattern(), scalar.getRegexReplacement());
        // else
        // warning("Encountered null fd for scalar: " + scalar);
        // }
      }
      else if (thatChild instanceof MetaMetadataNestedField && thatChild.hasChildren())
      {
        // bind class descriptor for nested sub-fields
        MetaMetadataNestedField nested = (MetaMetadataNestedField) thatChild;
        MetadataClassDescriptor elementClassDescriptor =
            ((MetaMetadataNestedField) thatChild).bindMetadataClassDescriptor(metadataTScope);
        if (elementClassDescriptor != null)
        {
          MetaMetadata mmdForThatChild = nested.getTypeMmd();
          if (mmdForThatChild != null && mmdForThatChild.getMetadataClassDescriptor() == null)
            // mmdForThatChild.setMetadataClassDescriptor(elementClassDescriptor);
            mmdForThatChild.bindMetadataClassDescriptor(metadataTScope);
        }
        else
        {
          warning("Cannot determine elementClassDescriptor for " + thatChild);
          this.getChildrenMap().remove(thatChild.getName());
        }
        // }
      }

      if (this instanceof MetaMetadata)
      {
        MetaMetadata mmd = (MetaMetadata) this;
        String naturalId = thatChild.getAsNaturalId();
        if (naturalId != null)
        {
          mmd.addNaturalIdField(naturalId, thatChild);
        }
      }
    }
  }

  /**
   * bind metadata class descriptor to this nested field. bind field descriptors to nested
   * sub-fields inside this nested field, using the field names as key (but with mixins field
   * removed).
   * <p>
   * lazy evaluated. result cached.
   * 
   * @param metadataTScope
   *          the translation scope for (generated) metadata classes.
   * @return the bound metadata class descriptor.
   */
  protected MetadataClassDescriptor bindMetadataClassDescriptor(SimplTypesScope metadataTScope)
  {
    MetadataClassDescriptor metadataCd = this.getMetadataClassDescriptor();
    if (metadataCd == null)
    {
      metadataCd = metadataClassDescriptor(metadataTScope);
      if (metadataCd != null)
      {
        this.setMetadataClassDescriptor(metadataCd); // early assignment to prevent infinite loop
        this.bindMetadataFieldDescriptors(metadataTScope, metadataCd);
      }
    }
    return metadataCd;
  }

  @Override
  protected void customizeFieldDescriptor(SimplTypesScope metadataTScope,
                                          MetadataFieldDescriptorProxy fdProxy)
  {
    super.customizeFieldDescriptor(metadataTScope, fdProxy);

    MetaMetadata thisMmd = this.getTypeMmd();
    if (thisMmd == null)
      return; // could be collection of scalars

    MetaMetadataNestedField inheritedField = (MetaMetadataNestedField) this.getSuperField();
    if (inheritedField != null)
    {
      MetaMetadata superMmd = inheritedField.getTypeMmd();
      // if (thisMmd == superMmd)
      // return;
      if (thisMmd == superMmd || thisMmd.isDerivedFrom(superMmd))
      {
        // extending type!
        // MetadataClassDescriptor metadataClassDescriptor =
        // thisMmd.bindMetadataClassDescriptor(metadataTScope);
        MetadataClassDescriptor elementMetadataCD = thisMmd.metadataClassDescriptor(metadataTScope);
        fdProxy.setElementClassDescriptor(elementMetadataCD);
      }
      else
      {
        throw new MetaMetadataException("incompatible types: " + inheritedField + " => " + this);
      }
    }
  }

  protected MetadataClassDescriptor metadataClassDescriptor(SimplTypesScope metadataTScope)
  {
    MetadataClassDescriptor metadataCd = this.getMetadataClassDescriptor();
    if (metadataCd == null)
    {
      // this.inheritMetaMetadata(inheritanceHandler);

      String metadataClassSimpleName = this.getMetadataClassSimpleName();
      // first look up by simple name, since package names for some built-ins are wrong
      metadataCd = (MetadataClassDescriptor) metadataTScope
          .getClassDescriptorBySimpleName(metadataClassSimpleName);
      if (metadataCd == null)
      {
        String metadataClassName = this.getMetadataClassName();
        metadataCd = (MetadataClassDescriptor) metadataTScope
            .getClassDescriptorByClassName(metadataClassName);
        if (metadataCd == null)
        {
          try
          {
            Class metadataClass = Class.forName(metadataClassName);
            this.setMetadataClass(metadataClass);
            metadataCd = (MetadataClassDescriptor) ClassDescriptor
                .getClassDescriptor(metadataClass);
            metadataTScope.addTranslation(metadataClass);
          }
          catch (ClassNotFoundException e)
          {
            // e.printStackTrace();
            // throw new MetaMetadataException("Cannot find metadata class: " + metadataClassName);
            error("Cannot find metadata class: " + metadataClassName);
          }
        }
      }
    }
    return metadataCd;
  }

  void findOrGenerateMetadataClassDescriptor(SimplTypesScope tscope)
  {
    if (mmdScopeTraversed)
      return;

    mmdScopeTraversed = true;

    if (this.getScope() != null)
    {
      for (Object obj : this.getScope().values())
      {
        if (obj instanceof MetaMetadata)
        {
          MetaMetadata inlineMmd = (MetaMetadata) obj;
          inlineMmd.findOrGenerateMetadataClassDescriptor(tscope);
        }
      }
    }

    if (this.getChildrenMap() != null)
      for (MetaMetadataField f : this.getChildrenMap())
      {
        if (f instanceof MetaMetadataNestedField)
        {
          MetaMetadataNestedField nested = (MetaMetadataNestedField) f;
          nested.findOrGenerateMetadataClassDescriptor(tscope);
        }
      }
  }

  @Override
  public void addAdditionalMetaInformation(List<MetaInformation> metaInfoBuf,
                                           MmdCompilerService compiler)
  {
    metaInfoBuf.add(new MetaInformation(mm_name.class, false, getName()));
    if (this.getName().equals("mixins") && this.getDeclaringMmd().getName().equals("metadata"))
      metaInfoBuf.add(new MetaInformation(semantics_mixin.class));
  }

  public void recursivelyRestoreChildComposite()
  {
    for (MetaMetadataField field : this.getChildrenMap())
    {
      if (field instanceof MetaMetadataNestedField)
        ((MetaMetadataNestedField) field).recursivelyRestoreChildComposite();
    }
  }

  @Override
  protected String getFingerprintString()
  {
    StringBuilder sb = StringBuilderUtils.acquire();
    sb.append(super.getFingerprintString());
    addToFp(sb, Utils.serializeToString(fieldParserElement, StringFormat.XML));
    addToFp(sb, getPolymorphicScope());
    addToFp(sb, getPolymorphicClasses());
    addToFp(sb, getSchemaOrgItemtype());
    addCollectionToFp(sb, getDefVars());
    String fp = sb.toString();
    StringBuilderUtils.release(sb);
    return fp;
  }

}
