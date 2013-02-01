package ecologylab.bigsemantics.metametadata;

import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;

/**
 * Exceptions that occur with dependencies in scalar values (concatenation, etc)
 * @author twhite
 *
 */
public class ScalarDependencyException extends MetaMetadataException{

	public ScalarDependencyException()
	{
		super("Cycle detected in dependencies for this MetaMetadata Object. Check your value references and modify the metametadata wrapper to resolve this issue.");
	}
}
