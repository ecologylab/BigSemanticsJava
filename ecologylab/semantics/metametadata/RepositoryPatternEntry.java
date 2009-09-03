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
	
	/**
	 * 
	 */
	public RepositoryPatternEntry(Pattern pattern, MetaMetadata metaMetadata)
	{
		this.pattern			= pattern;
		this.metaMetadata	= metaMetadata;
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

}
