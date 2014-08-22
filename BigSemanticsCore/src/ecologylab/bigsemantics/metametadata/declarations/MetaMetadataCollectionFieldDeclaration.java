package ecologylab.bigsemantics.metametadata.declarations;

import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataNestedField;
import ecologylab.bigsemantics.metametadata.mm_dont_inherit;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.ScalarType;

public abstract class MetaMetadataCollectionFieldDeclaration extends MetaMetadataNestedField
{

  @simpl_scalar
  private String             childTag;

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

  public MetaMetadataCollectionFieldDeclaration()
  {
    super();
  }

  public MetaMetadataCollectionFieldDeclaration(MetaMetadataField copy, String name)
  {
    super(copy, name);
  }

  public MetaMetadataCollectionFieldDeclaration(String name,
                                                HashMapArrayList<String, MetaMetadataField> set)
  {
    super(name, set);
  }

  public String getChildExtends()
  {
    return childExtends;
  }

  public ScalarType getChildScalarType()
  {
    return childScalarType;
  }

  public String getChildTag()
  {
    return childTag;
  }

  public String getChildType()
  {
    return childType;
  }

  public String getChildUseValueAsLabel()
  {
    return childUseValueAsLabel;
  }

  public boolean isChildShowExpandedAlways()
  {
    return childShowExpandedAlways;
  }

  public boolean isChildShowExpandedInitially()
  {
    return childShowExpandedInitially;
  }

  public boolean isNoWrap()
  {
    return noWrap;
  }

  public void setChildExtends(String childExtends)
  {
    this.childExtends = childExtends;
  }

  public void setChildScalarType(MetadataScalarType childScalarType)
  {
    this.childScalarType = childScalarType;
  }

  public void setChildShowExpandedAlways(boolean childShowExpandedAlways)
  {
    this.childShowExpandedAlways = childShowExpandedAlways;
  }

  public void setChildShowExpandedInitially(boolean childShowExpandedInitially)
  {
    this.childShowExpandedInitially = childShowExpandedInitially;
  }

  public void setChildTag(String childTag)
  {
    this.childTag = childTag;
  }

  public void setChildType(String childType)
  {
    this.childType = childType;
  }

  public void setChildUseValueAsLabel(String childUseValueAsLabel)
  {
    this.childUseValueAsLabel = childUseValueAsLabel;
  }

  public void setNoWrap(boolean noWrap)
  {
    this.noWrap = noWrap;
  }

}