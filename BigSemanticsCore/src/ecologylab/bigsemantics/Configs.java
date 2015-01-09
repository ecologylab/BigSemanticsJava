package ecologylab.bigsemantics;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load configurations.
 * 
 * @author quyin
 */
public class Configs
{

  static Logger logger = LoggerFactory.getLogger(Configs.class);

  /**
   * According to commons-configuration documentation, property files will be searched: (1) in the
   * current directory, (2) in the user home directory, and (3) in the classpath.
   * 
   * @param configFileName
   * @return
   */
  public static Configuration loadProperties(String configFileName)
  {
    CompositeConfiguration result = new CompositeConfiguration();

    // load the configurations from the property file.
    PropertiesConfiguration configs = loadPropertiesHelper(configFileName);
    if (configs != null)
    {
      result.addConfiguration(configs);
    }

    int p = configFileName.lastIndexOf('.');
    if (p >= 0)
    {
      // try to guess the file name for the default configurations and use it.
      String defaultsFileName =
          configFileName.substring(0, p) + "-defaults" + configFileName.substring(p);
      PropertiesConfiguration defaults = loadPropertiesHelper(defaultsFileName);
      if (defaults != null)
      {
        result.addConfiguration(defaults);
      }
    }

    return result;
  }

  private static PropertiesConfiguration loadPropertiesHelper(String fileName)
  {
    PropertiesConfiguration result = null;
    try
    {
      result = new PropertiesConfiguration(fileName);
    }
    catch (ConfigurationException e)
    {
      logger.warn("Cannot load configurations from " + fileName + ": " + e.getMessage());
    }
    return result;
  }

}
