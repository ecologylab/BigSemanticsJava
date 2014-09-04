package ecologylab.bigsemantics.metametadata;

import java.io.IOException;
import java.util.Collection;

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
	
	void appendGenericTypeVarParameterizations(Appendable appendable, Collection<MmdGenericTypeVar> mmdGenericTypeVars, MetaMetadataRepository repository) throws IOException;
	
	void appendGenericTypeVarExtends(Appendable appendable, Collection<MmdGenericTypeVar> mmdGenericTypeVars, MetaMetadataRepository repository) throws IOException;
	
}
