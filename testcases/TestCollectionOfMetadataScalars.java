package testcases;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SimplTypesScope;

public class TestCollectionOfMetadataScalars
{

	public static void test()
	{
		MetadataScalarType.init(); // register metadata scalar types
		SimplTypesScope ts = SimplTypesScope.get("test", ElementState.class, Metadata.class, Document.class);
		ClassDescriptor cd = ts.getClassDescriptorByTag("document");
		System.out.println(cd.getFieldDescriptorByTag("additional_locations", ts).getScalarType());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		test();
	}

}
