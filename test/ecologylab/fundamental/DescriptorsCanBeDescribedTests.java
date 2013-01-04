package ecologylab.fundamental;

import org.junit.Before;
import org.junit.Test;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SimplTypesScope;

public class DescriptorsCanBeDescribedTests {

	
	@Before
	public void ResetSTS()
	{
		SimplTypesScope.ResetAllTypesScopes();
	}
	
	@Test
	public void FieldDescriptorCanBeDescribed()
	{
		ClassDescriptor cd = ClassDescriptor.getClassDescriptor(FieldDescriptor.class);
	}
}
