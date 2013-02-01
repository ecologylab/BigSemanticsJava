package ecologylab.semantics.metametadata;

/**
 * 
 * @author quyin
 *
 */
public class StrippedUrlEntry
{

  private MetaMetadata         metaMetadata;

  private MetaMetadataSelector selector;

  public StrippedUrlEntry(MetaMetadata metaMetadata, MetaMetadataSelector selector)
  {
    this.metaMetadata = metaMetadata;
    this.selector = selector;
  }

  public MetaMetadata getMetaMetadata()
  {
    return metaMetadata;
  }

  public void setMetaMetadata(MetaMetadata metaMetadata)
  {
    this.metaMetadata = metaMetadata;
  }

  public MetaMetadataSelector getSelector()
  {
    return selector;
  }

  public void setSelector(MetaMetadataSelector selector)
  {
    this.selector = selector;
  }

}
