package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
 * 
 * @author quyin
 */
public class MetaMetadataAltNameEntry implements IMappable<String>
{

  @simpl_scalar
  String       name;

  @simpl_composite
  MetaMetadata mmd;

  public MetaMetadataAltNameEntry()
  {
    this(null, null);
  }

  public MetaMetadataAltNameEntry(String name, MetaMetadata mmd)
  {
    super();
    this.name = name;
    this.mmd = mmd;
  }

  @Override
  public String key()
  {
    return name;
  }

}
