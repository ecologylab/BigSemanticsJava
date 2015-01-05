package ecologylab.bigsemantics.metametadata.declarations;

import ecologylab.bigsemantics.metametadata.MetaMetadataCollectionField;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataNestedField;
import ecologylab.bigsemantics.metametadata.mm_dont_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

public abstract class MetaMetadataCompositeFieldDeclaration extends MetaMetadataNestedField
{

  /**
   * The type/class of metadata object.
   */
  @simpl_scalar
  private String                      type;

  /**
   * the extends attribute of a composite field / meta-metadata.
   */
  @simpl_tag("extends")
  @simpl_scalar
  @mm_dont_inherit
  private String                      extendsAttribute;

  @simpl_scalar
  private String                      userAgentName;

  @simpl_scalar
  private String                      userAgentString;

  /**
   * if this composite should be wrapped.
   */
  @simpl_scalar
  private boolean                     wrap;

  private MetaMetadataCollectionField enclosingCollectionField;

  public MetaMetadataCompositeFieldDeclaration()
  {
    super();
  }

  public MetaMetadataCompositeFieldDeclaration(MetaMetadataField copy, String name)
  {
    super(copy, name);
  }

  public MetaMetadataCollectionField getEnclosingCollectionField()
  {
    return enclosingCollectionField;
  }

  public String getExtendsAttribute()
  {
    return extendsAttribute;
  }

  @Override
  public String getType()
  {
    return type;
  }

  public String getUserAgentName()
  {
    return userAgentName;
  }

  public String getUserAgentString()
  {
    if (userAgentString == null)
    {
      userAgentString = (userAgentName == null)
          ? getRepository().getDefaultUserAgentString()
          : getRepository().getUserAgentString(userAgentName);
    }
    return userAgentString;
  }

  public boolean isWrap()
  {
    return wrap;
  }

  public void setEnclosingCollectionField(MetaMetadataCollectionField enclosingCollectionField)
  {
    this.enclosingCollectionField = enclosingCollectionField;
  }

  public void setExtendsAttribute(String extendsAttribute)
  {
    this.extendsAttribute = extendsAttribute;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public void setUserAgentName(String userAgentName)
  {
    this.userAgentName = userAgentName;
  }

  public void setUserAgentString(String userAgentString)
  {
    this.userAgentString = userAgentString;
  }

  public void setWrap(boolean wrap)
  {
    this.wrap = wrap;
  }

}