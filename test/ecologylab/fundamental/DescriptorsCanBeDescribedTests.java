package ecologylab.fundamental;

import org.junit.Test;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;

public class DescriptorsCanBeDescribedTests {

	@Test
	public void FieldDescriptorCanBeDescribed()
	{
		ClassDescriptor cd = ClassDescriptor.getClassDescriptor(FieldDescriptor.class);
	}
}
