package ecologylab.bigsemantics;

import org.apache.commons.configuration.Configuration;

/**
 * Configurable classes.
 * 
 * @author quyin
 */
public interface Configurable
{

  void configure(Configuration configuration) throws Exception;

  Configuration getConfiguration();

}
