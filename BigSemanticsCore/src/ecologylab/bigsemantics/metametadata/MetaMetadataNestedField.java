package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

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
@SuppressWarnings(
{ "rawtypes" })
@simpl_inherit
public abstract class MetaMetadataNestedField extends MetaMetadataNestedFieldDeclaration
    implements PackageSpecifier
{

  public static interface InheritFinishEventListener
  {
    void inheritFinish(Object... eventArgs);
  }

  protected static interface InheritCommand
  {
    void doInherit(Object... eventArgs);
  }

  public static final String               POLYMORPHIC_CLASSES_SEP = ",";

  @simpl_composite
  @simpl_tag("field_parser")
  private FieldParserElement               fieldParserElement;

  // ////////////// Inheritance Related ////////////////

  /**
   * The (local) scope of visible meta-metadata for this nested field.
   */
  @simpl_composite
  @mm_dont_inherit
  private MmdScope                         mmdScope;

  private boolean                          mmdScopeTraversed;

  private List<InheritFinishEventListener> inheritFinishEventListeners;

  /**
   * this lock is currently useless, because all inheritance stuff happens in one thread. it is here
   * just for possible future extensions.
   */
  protected Object                         lockInheritCommands;

  /*
   * how we handle cross-references in inheritance:
   * 
   * - each nested field has two possible sources to inherit from: the meta-metadata defining its
   * type, and the inherited field that it is overriding. for some cases, e.g. fields defining
   * inline meta-metadata, both sources are used.
   * 
   * - it is possible that when we are inheriting for a nested field, one or more than one of its
   * inheriting sources is not ready (inheritInProgress == true). when this is the case, two things
   * will be done: 1) a event listener will be added to the source structure; 2) a command will be
   * added to this.waitForFinish.
   * 
   * - when a source structure finishes inheriting, it calls the event listener, which pops one
   * command from waitForFinish and pushes it to waitForExecute. if waitForFinish is empty, it pops
   * commands in waitForExecute one by one and execute them (do inheritance). in this way, we ensure
   * that: 1) inheritance is done in the same order as they are supposed to be in the code; 2) it
   * doesn't matter which structure is done first, or if it is done before the following ones are
   * pushed into the stack, etc.
   */

  protected Stack<InheritCommand>          waitForFinish;

  protected Stack<InheritCommand>          waitForExecute;

  public MetaMetadataNestedField()
  {
    inheritFinishEventListeners = new ArrayList<InheritFinishEventListener>();
    lockInheritCommands = new Object();
  }

  public MetaMetadataNestedField(String name, HashMapArrayList<String, MetaMetadataField> set)
  {
    super(name, set);
    inheritFinishEventListeners = new ArrayList<InheritFinishEventListener>();
    lockInheritCommands = new Object();
  }

  public MetaMetadataNestedField(MetaMetadataField copy, String name)
  {
    super(copy, name);
    inheritFinishEventListeners = new ArrayList<InheritFinishEventListener>();
    lockInheritCommands = new Object();
  }

  public String packageName()
  {
    return this.getPackageName();
  }

  public FieldParserElement getFieldParserElement()
  {
    return fieldParserElement;
  }

  public MmdScope getMmdScope()
  {
    return mmdScope;
  }

  protected void setMmdScope(MmdScope mmdScope)
  {
    this.mmdScope = mmdScope;
  }

  public void addInheritFinishEventListener(InheritFinishEventListener listener)
  {
    synchronized (inheritFinishEventListeners)
    {
      inheritFinishEventListeners.add(listener);
    }
  }

  protected void clearInheritFinishedOrInProgressFlag()
  {
    this.setInheritDone(false);
    this.setInheritOngoing(false);
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

  abstract protected String getMetaMetadataTagToInheritFrom();

  /**
   * The main entrance to the inheritance process. Also ensures that the inheritance process will
   * not be carried out on the same field / meta-metadata twice.
   * <p>
   * Note that this inheritance process is used by both the compiler and the run-time repository
   * loading process, thus there should be nothing about generating Class/FieldDescriptors inside.
   * <p>
   * prerequisites:<br>
   * <ul>
   * <li>for meta-metadata: name & type/extends set;</li>
   * <li>for fields: attributes inherited & declaringMmd set;</li>
   * <li>for both: parent element inheritedMmd set.</li>
   * </ul>
   * <p>
   * consequences:<br>
   * <ul>
   * <li>if this is a MetaMetadata object, attributes inherited from inheritedMmd;</li>
   * <li>inheritedMmd set;</li>
   * <li>(first-level) fields inherited from inheritedMmd;</li>
   * <li>inheritedField and declaringMmd set for all (first-level) fields;</li>
   * <li>for all (first-level) fields, attributes inherited from their inheritedField. (enabling
   * recursion)</li>
   * </ul>
   * 
   * @param inheritanceHandler
   *          TODO
   * @param mmdScope
   *          a scope used for looking up meta-metadata & perhaps adding new meta-metadata.
   * @return true if inheritance was done successfully. false if it needs to be delayed.
   */
  public boolean inheritMetaMetadata(InheritanceHandler inheritanceHandler)
  {
    if (!isInheritDone() && !isInheritOngoing())
    {
      setInheritOngoing(true);
      boolean result = this.inheritMetaMetadataHelper(inheritanceHandler);
      if (result)
      {
        finishInheritance();
      }
      return result;
    }
    return true;
  }

  protected void finishInheritance()
  {
    this.sortForDisplay();
    setInheritOngoing(false);
    setInheritDone(true);
    handleInheritFinishEventListeners();
  }

  private void handleInheritFinishEventListeners()
  {
    if (inheritFinishEventListeners != null)
    {
      synchronized (inheritFinishEventListeners)
      {
        for (InheritFinishEventListener listener : inheritFinishEventListeners)
          listener.inheritFinish();
        inheritFinishEventListeners.clear();
      }
    }
  }

  /**
   * Helper method that actually does the inheritance process. This should be overridden in
   * sub-classes to fine-control the inheritance process.
   * 
   * @param inheritanceHandler
   *          TODO
   * @param mmdScope
   *          a scope used for looking up meta-metadata & perhaps adding new meta-metadata.
   * @return true if inheritance was successful. false if inheritance needs to be delayed.
   */
  abstract protected boolean inheritMetaMetadataHelper(InheritanceHandler inheritanceHandler);

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

      // process hide and shadows
      HashSet<String> nonDisplayedFieldNames = nonDisplayedFieldNames();
      if (thatChild.isHide() && !thatChild.isAlwaysShow())
        nonDisplayedFieldNames.add(thatChild.getName());
      if (thatChild.getShadows() != null)
        nonDisplayedFieldNames.add(thatChild.getShadows());

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
        MetadataFieldDescriptor fd = nested.getMetadataFieldDescriptor();
        // if (fd.isPolymorphic())
        // {
        // do not bind descriptor for truely polymorphic fields
        // debug("Polymorphic field: " + nested + ", not binding an element class descriptor.");
        // }
        // else
        // {
        MetadataClassDescriptor elementClassDescriptor = ((MetaMetadataNestedField) thatChild)
            .bindMetadataClassDescriptor(metadataTScope);
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

    if (this.getMmdScope() != null)
    {
      for (MetaMetadata inlineMmd : this.getMmdScope().values())
      {
        inlineMmd.findOrGenerateMetadataClassDescriptor(tscope);
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
