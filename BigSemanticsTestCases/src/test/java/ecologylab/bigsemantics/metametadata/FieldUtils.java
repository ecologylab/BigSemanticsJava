package ecologylab.bigsemantics.metametadata;

import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;

/**
 * Utils related to meta-metadata fields.
 * 
 * @author quyin
 */
public class FieldUtils
{

  public static void setMetadataFieldDescriptor(MetaMetadataField field,
                                                MetadataFieldDescriptor descriptor)
  {
    field.setMetadataFieldDescriptor(descriptor);
  }

  public static void setMetadataClassDescriptor(MetaMetadataField field,
                                                MetadataClassDescriptor descriptor)
  {
    field.setMetadataClassDescriptor(descriptor);
  }

}
