package ecologylab.semantics.metametadata;

import java.io.IOException;
import java.util.List;

import ecologylab.serialization.ClassDescriptor;


/**
 * Make it possible for MetaMetadataField objects to use compiler services.
 * 
 * @author quyin
 *
 */
public interface MmdCompilerService
{

	void addGlobalDependency(String name);

	void addCurrentClassDependency(ClassDescriptor dependency);

	void addLibraryTScopeDependency(String name);
	
	void appendGenericTypeVarParameterizations(Appendable appendable, List<MetaMetadataGenericTypeVar> mmdGenericTypeVars, MetaMetadataRepository repository) throws IOException;
	
}
