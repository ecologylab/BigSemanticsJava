package ecologylab.fundamental;

import static org.junit.Assert.*;

import org.junit.Test;

import ecologylab.serialization.ClassDescriptor;

public class ConstructClassDescriptor {

	
	// Todo: Stealthily swap this with a factory method.
	private ClassDescriptor<?> ConstructClassDescriptor(Class<?> lass)
	{
		return ClassDescriptor.getClassDescriptor(lass);
	}
	
	private void validateSingleField(String fieldName, Class<?> expectedType)
	{
		
		
	}
	
	private void testSimpleScalar(Class<?> lass)
	{
		ClassDescriptor<?> cd = ConstructClassDescriptor(lass);
	}
	
	@Test
	public void testSimpleScalar() {

		
		
		
	}

}
