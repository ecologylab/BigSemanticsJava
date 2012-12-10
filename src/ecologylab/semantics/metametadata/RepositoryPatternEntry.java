/**
 * 
 */
package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.generic.Debug;

/**
 * @author andruid
 *
 */
public class RepositoryPatternEntry extends Debug
{
	final Pattern				pattern;
	final MetaMetadata	metaMetadata;
	
	final private boolean isPatternFragment;
	
	/**
	 * 
	 */
	public RepositoryPatternEntry(Pattern pattern, MetaMetadata metaMetadata, boolean isPatternFragment)
	{
		this.pattern			= pattern;
		this.metaMetadata	= metaMetadata;
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
	
	public boolean isPatternFragment()
	{
		return isPatternFragment;
	}

}
