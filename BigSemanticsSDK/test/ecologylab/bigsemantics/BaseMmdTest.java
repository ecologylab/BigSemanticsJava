package ecologylab.bigsemantics;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;

/**
 * Basic initialization and utilities for testing meta-metadata.
 * 
 * All tests in this package should extend this class.
 * 
 * @author quyin
 */
public class BaseMmdTest
{

  static Logger                 logger = LoggerFactory.getLogger(BaseMmdTest.class);

  static SimplTypesScope        mmdScope;

  static SemanticsSessionScope  semanticsScope;

  static MetaMetadataRepository realRepository;

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
    return repository;
  }

  public MetaMetadataRepository loadRealRepository()
  {
    if (realRepository == null)
    {
      SemanticsSessionScope semanticsScope = null;
      if (semanticsScope == null)
      {
        semanticsScope =
            new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(),
                                      CybernekoWrapper.class);
      }
      realRepository = semanticsScope.getMetaMetadataRepository();
    }
    return realRepository;
  }

  public MetaMetadataField getNestedField(MetaMetadata mmd, String... fieldNames)
  {
    if (mmd == null || fieldNames == null)
    {
      return null;
    }

    MetaMetadataField result = mmd;
    for (String fieldName : fieldNames)
    {
      result = result.getChildrenMap().get(fieldName);
      if (result == null)
      {
        return null;
      }
    }
    return result;
  }

  public MetaMetadataField getNestedField(MetaMetadataRepository repository,
                                          String mmdName,
                                          String... fieldNames)
  {
    MetaMetadata mmd = repository.getMMByName(mmdName);
    return getNestedField(mmd, fieldNames);
  }

  public void serializeToTempFile(Object obj, String suffix)
  {
    try
    {
      File tempFile = File.createTempFile("mmd-test-inheritance", ".xml");
      File file = new File(tempFile.getParentFile(),
                           "mmd-test-inheritance-" + suffix + ".xml");
      tempFile.renameTo(file);
      SimplTypesScope.serialize(obj, file, Format.XML);
      logger.info("Serialized to: " + file.getAbsolutePath());
    }
    catch (Exception e)
    {
      logger.error("Could not serialize to temporary file.", e);
    }
  }

  public static void setMetadataFieldDescriptor(MetaMetadataField field,
                                                MetadataFieldDescriptor descriptor)
  {
    field.setMetadataFieldDescriptor(descriptor);
  }

}
