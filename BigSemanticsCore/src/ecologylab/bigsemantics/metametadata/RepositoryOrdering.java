package ecologylab.bigsemantics.metametadata;

import java.util.List;

/**
 * 
 * @author quyin
 */
public interface RepositoryOrdering
{

  List<MetaMetadata> orderMetaMetadataForInheritance(List<MetaMetadata> mmds);
  
}
