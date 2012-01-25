package ecologylab.semantics.metametadata;


/**
 * Make it possible for MetaMetadataField objects to use compiler services.
 * 
 * @author quyin
 *
 */
public interface MmdCompilerService
{

	void addGlobalDependency(String name);

	void addCurrentClassDependency(String name);

	void addLibraryTScopeDependency(String name);
	
}
