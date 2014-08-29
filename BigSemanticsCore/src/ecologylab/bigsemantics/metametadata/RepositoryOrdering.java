package ecologylab.bigsemantics.metametadata;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author quyin
 */
public interface RepositoryOrdering
{

  List<MetaMetadata> orderMetaMetadataForInheritance(Collection<MetaMetadata> mmds);
  
}
