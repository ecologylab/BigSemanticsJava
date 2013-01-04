package ecologylab.fundamental;

import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.annotations.FieldUsage;
import ecologylab.serialization.annotations.simpl_collection;

public class DBALTests {

	@Test
	public void simpl_Collection_transmitsNameIntoFD()
	{
		final class myCollectionClass
		{
			@simpl_collection("excluded_usage")
			private ArrayList<FieldUsage> excludedUsages;	
		}
		
		ClassDescriptor cd = ClassDescriptor.getClassDescriptor(myCollectionClass.class);
		FieldDescriptor fd = (FieldDescriptor)cd.allFieldDescriptors().get(0);
		assertNotNull(fd.getTagName());
	}
}
