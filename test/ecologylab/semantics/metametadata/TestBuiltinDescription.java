package ecologylab.semantics.metametadata;

import static org.junit.Assert.*;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.builtins.declarations.MetadataDeclaration;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.SimplTypesScope;


import org.junit.Test;

public class TestBuiltinDescription {

	
	@Test
	public void testThatTheRepositoryScopeExists()
	{
		
		SimplTypesScope defaultGet = RepositoryMetadataTranslationScope.get();
		
		assertNotNull(defaultGet);
		
		SimplTypesScope sts = SimplTypesScope.get("repository_metadata");
		
		assertNotNull(sts);		
		
	}
	@Test
	public void testThatCreationDoesntThrowAnException() {
		//SimplTypesScope defaultGet = RepositoryMetadataTranslationScope.get();

		ClassDescriptor<?> cd = ClassDescriptor.getClassDescriptor(MetadataString.class);	
		ClassDescriptor<?> mdCD = ClassDescriptor.getClassDescriptor(MetadataDeclaration.class);
		MetadataDeclaration md = new MetadataDeclaration();
	}

}
