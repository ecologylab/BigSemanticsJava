package ecologylab.bigsemantics.metametadata.declarations;

import java.util.ArrayList;

import ecologylab.bigsemantics.metametadata.DefVar;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.mm_dont_inherit;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

public abstract class MetaMetadataNestedFieldDeclaration extends MetaMetadataField
{

  @simpl_scalar
  @simpl_tag("package")
  private String            packageName;

  /**
   * The polymorphic scope (@simp_scope) for this field.
   */
  @simpl_scalar
  private String            polymorphicScope;

  /**
   * Comma-separated classes for polymorphic classes (@simpl_classes) for this field.
   */
  @simpl_scalar
  private String            polymorphicClasses;

  @simpl_scalar
  private String            schemaOrgItemtype;

  /**
   * Used as variables during the extraction and semantic action processes.
   */
  @simpl_collection("def_var")
  @simpl_nowrap
  private ArrayList<DefVar> defVars;

  /**
   * If children should be displayed at this level.
   */
  @simpl_scalar
  private boolean           promoteChildren;

  @simpl_scalar
  private boolean           showExpandedInitially;

  @simpl_scalar
  private boolean           showExpandedAlways;
  
  @simpl_scalar
  private boolean           dontShowExpandedInitially;

  @simpl_scalar
  private boolean           dontShowExpandedAlways;

  /**
   * Should we generate a metadata class descriptor for this field. used by the compiler.
   */
  @simpl_scalar
  @mm_dont_inherit
  private boolean           newMetadataClass;

  /**
   * The mmd that defined the type this nested field uses. could be a generated one for inline
   * definitions.
   * 
   * Corresponding attributes: (child_)type/extends.
   */
  @simpl_composite
  @mm_dont_inherit
  private MetaMetadata      typeMmd;
  
  @simpl_scalar
  @mm_dont_inherit
  private boolean           covariant;

  public MetaMetadataNestedFieldDeclaration()
  {
    super();
  }

  public MetaMetadataNestedFieldDeclaration(String name,
                                            HashMapArrayList<String, MetaMetadataField> childrenMap)
  {
    super(name, childrenMap);
  }

  public MetaMetadataNestedFieldDeclaration(MetaMetadataField copy, String name)
  {
    super(copy, name);
  }

  public String getPackageName()
  {
    return this.packageName;
  }

  public String getPolymorphicScope()
  {
    return this.polymorphicScope;
  }

  public String getPolymorphicClasses()
  {
    return this.polymorphicClasses;
  }

  @Override
  public String getSchemaOrgItemtype()
  {
    return schemaOrgItemtype;
  }

  public ArrayList<DefVar> getDefVars()
  {
    return defVars;
  }

  public boolean isPromoteChildren()
  {
    return promoteChildren;
  }

  public boolean isShowExpandedInitially()
  {
    return showExpandedInitially;
  }

  public boolean isShowExpandedAlways()
  {
    return showExpandedAlways;
  }

  public boolean isDontShowExpandedInitially() 
  {
	return dontShowExpandedInitially;
  }

  public boolean isDontShowExpandedAlways() 
  {
	return dontShowExpandedAlways;
  }

/**
   * Should we generate a metadata class descriptor for this field. Used by the compiler.
   * 
   * @return
   */
  public boolean isNewMetadataClass()
  {
    return newMetadataClass;
  }

  /**
   * @return The meta-metadata defining the type used by this field.
   */
  public MetaMetadata getTypeMmd()
  {
    return typeMmd;
  }

  public boolean isCovariant()
  {
    return covariant;
  }

  public void setPackageName(String packageName)
  {
    this.packageName = packageName;
  }

  public void setSchemaOrgItemtype(String schemaOrgItemtype)
  {
    this.schemaOrgItemtype = schemaOrgItemtype;
  }

  public void setPromoteChildren(boolean promoteChildren)
  {
    this.promoteChildren = promoteChildren;
  }

  /**
   * set the flag of generating (or not) metadata class descriptoer.
   * 
   * @param newMetadataClass
   * @see isGenerateClassDescriptor
   */
  public void setNewMetadataClass(boolean newMetadataClass)
  {
    this.newMetadataClass = newMetadataClass;
  }

  /**
   * Set the type meta-metadata of this field.
   * 
   * @param typeMmd
   */
  public void setTypeMmd(MetaMetadata typeMmd)
  {
    this.typeMmd = typeMmd;
  }

  public void setCovariant(boolean covariant)
  {
    this.covariant = covariant;
  }

}