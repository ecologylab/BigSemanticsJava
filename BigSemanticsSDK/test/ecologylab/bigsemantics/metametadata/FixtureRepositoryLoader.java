package ecologylab.bigsemantics.metametadata;

import java.io.InputStream;

import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;

/**
 * For test suites to easily load testing mmd repository.
 * 
 * @author quyin
 */
public class FixtureRepositoryLoader
{

  static SimplTypesScope mmdScope;
  
  static
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
	  MetadataScalarType.init();
	  mmdScope = MetaMetadataTranslationScope.get();
  }

  public MetaMetadataRepository loadRepository(String resourceName)
      throws SIMPLTranslationException
  {
    InputStream istream = this.getClass().getResourceAsStream(resourceName);
    MetaMetadataRepository repository =
        (MetaMetadataRepository) mmdScope.deserialize(istream, Format.XML);
    assert repository != null : "Failed to load testing repository!";
    MmdScope mmdScope = new MmdScope();
    for (MetaMetadata mmd : repository.getMetaMetadataCollection())
    {
      mmd.setMmdScope(mmdScope);
      mmdScope.put(mmd.getName(), mmd);
    }
    return repository;
  }

}
