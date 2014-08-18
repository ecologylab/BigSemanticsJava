package ecologylab.bigsemantics.metametadata;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * Utilities for testing.
 * 
 * @author quyin
 */
public class UtilsForTest
{

  static Logger logger = LoggerFactory.getLogger(UtilsForTest.class);

  public static MetaMetadataField getNestedField(MetaMetadata mmd, String... fieldNames)
  {
    if (mmd == null || fieldNames == null)
    {
      return null;
    }

    MetaMetadataField result = mmd;
    for (String fieldName : fieldNames)
    {
      result = result.getChildMetaMetadata().get(fieldName);
      if (result == null)
      {
        return null;
      }
    }
    return result;
  }

  public static MetaMetadataField getNestedField(MetaMetadataRepository repository,
                                                 String mmdName,
                                                 String... fieldNames)
  {
    MetaMetadata mmd = repository.getMMByName(mmdName);
    return getNestedField(mmd, fieldNames);
  }

  public static void serializeToTempFile(Object obj, String suffix)
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

}
