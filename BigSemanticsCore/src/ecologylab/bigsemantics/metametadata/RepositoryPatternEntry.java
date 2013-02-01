/**
 * 
 */
package ecologylab.bigsemantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.generic.Debug;

/**
 * @author andruid
 *
 */
public class RepositoryPatternEntry extends Debug
{
  final private Pattern              pattern;

  final private MetaMetadata         metaMetadata;

  final private MetaMetadataSelector selector;

  final private boolean              isPatternFragment;
	
	/**
	 * 
	 */
  public RepositoryPatternEntry(Pattern pattern,
                                MetaMetadata metaMetadata,
                                MetaMetadataSelector selector,
                                boolean isPatternFragment)
	{
		this.pattern			= pattern;
		this.metaMetadata	= metaMetadata;
		this.selector     = selector;
		this.isPatternFragment = isPatternFragment;
	}

	/**
	 * @return the pattern
	 */
	public Pattern getPattern()
	{
		return pattern;
	}

	/**
	 * @return the metaMetadata
	 */
	public MetaMetadata getMetaMetadata()
	{
		return metaMetadata;
	}
	
	public MetaMetadataSelector getSelector()
	{
	  return selector;
	}
	
	public boolean isPatternFragment()
	{
		return isPatternFragment;
	}

}
