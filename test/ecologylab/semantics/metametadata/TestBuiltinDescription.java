package ecologylab.semantics.metametadata;

import static org.junit.Assert.*;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.builtins.declarations.MetadataDeclaration;
import ecologylab.serialization.ClassDescriptor;


import org.junit.Test;

public class TestBuiltinDescription {

	@Test
	public void test() {
		ClassDescriptor<?> cd = ClassDescriptor.getClassDescriptor(MetadataDeclaration.class);		
	}

}
