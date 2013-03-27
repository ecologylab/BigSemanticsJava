/**
 * 
 */
package ecologylab.bigsemantics.metadata;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCollectionField;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataNestedField;
import ecologylab.bigsemantics.metametadata.MetaMetadataScalarField;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;

/**
 * MetadataComparator implements the java.util.Comparator for Metadata
 * 
 * @author ajit
 *
 */

public class MetadataComparator extends Debug implements Comparator<Metadata>
{
	@Override
	public int compare(Metadata m1, Metadata m2)
	{
		if (m1 == null || m2 == null)
			return compareNull(m1, m2);
		
		if (m1.getClass() != m2.getClass())
		  return -1;
		
		MetaMetadata mmd1 = (MetaMetadata) m1.getMetaMetadata();
		MetaMetadata mmd2 = (MetaMetadata) m2.getMetaMetadata();
		
		// all values are looked up from hashmaps, so expected to be same
		// change to name?
		if (mmd1 != mmd2)
		{
			error("metametadata of the metadata objects is not same");
			return -1;
		}
		
		return recursiveComparison(m1, m2, mmd1);
	}

	private int recursiveComparison(Metadata m1, Metadata m2, MetaMetadataNestedField mmd)
	{
		HashMapArrayList<String, MetaMetadataField> fieldSet = mmd.getChildMetaMetadata();
		if (fieldSet == null || fieldSet.isEmpty())
			return -1;
		
		MetaMetadataNestedField targetParent = mmd.isUsedForInlineMmdDef() ? mmd.getInheritedMmd() : mmd;
		for (MetaMetadataField field : fieldSet)
		{
			if (!field.isAuthoredChildOf(targetParent))
			{
				// if 'field' is purely inherited, we ignore it to prevent infinite loops.
				// infinite loops can happen when 'field' uses the same mmd type as where it is defined,
				// e.g. google_patent.references are google_patent too.
				// this behavior is not necessarily required to prevent infinite loops, but it works
				// for our use cases now.
				// -- yin qu, 2/21/2012
				continue;
			}			
		
			try
			{
				int suc = 0;
				if (field instanceof MetaMetadataCompositeField)
				{
					MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) field;
					suc = compareCompositeField(m1, m2, mmcf);
				}
				else if (field instanceof MetaMetadataCollectionField)
				{
					MetaMetadataCollectionField mmcf = (MetaMetadataCollectionField) field;
					if (mmcf != null)
						suc = compareCollectionField(m1, m2, mmcf);
				}
				else
				{
					// scalar
					MetaMetadataScalarField mmsf = (MetaMetadataScalarField) field;
					suc = compareScalarField(m1, m2, mmsf);
				}
				if (suc != 0)
				{
					error("in metadata " + mmd.getMetadataClassDescriptor());
					return -1;
				}
			}
			catch (Exception e)
			{
				error(String.format("EXCEPTION when comparing %s: %s", field, e.getMessage()));
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	private int compareScalar(Object c1, Object c2)
	{
		if (c1 == null || c2 == null)
			return compareNull(c1, c2);
				
		if (c1.equals(c2))
			return 0;
		else
			error("scalar not equal");
		return -1;
	}

	private int compareScalarField(Metadata m1, Metadata m2, MetaMetadataScalarField mmsf)
	{
		Field javaField = mmsf.getMetadataFieldDescriptor().getField();
		Object o1 = ReflectionTools.getFieldValue(m1, javaField);
		Object o2 = ReflectionTools.getFieldValue(m2, javaField);
		
		int suc = compareScalar(o1, o2);
		if (suc != 0)
			error("scalar field " + javaField + " not equal");
		return suc;
	}

	private int compareCollectionField(Metadata m1, Metadata m2, MetaMetadataCollectionField mmcf)
	{
		Field javaField = mmcf.getMetadataFieldDescriptor().getField();
		javaField.setAccessible(true);
		Collection cf1 = (Collection) ReflectionTools.getFieldValue(m1, javaField);
		Object fieldValue2 = ReflectionTools.getFieldValue(m2, javaField);
    Collection cf2 = (Collection) fieldValue2;
		if (cf1 == null || cf2 == null)
		{
			return compareNull(cf1, cf2);
		}
		
		if (cf1.size() != cf2.size())
		{
			error("collection field " + javaField + " not having same size in metadata");
			return -1;
		}
		else
		{
			int suc = 0;
			Iterator iter1 = cf1.iterator();
			Iterator iter2 = cf2.iterator();
			while (iter1.hasNext() && iter2.hasNext())
			{
				Object c1 = iter1.next();
				Object c2 = iter2.next();
				
				if (mmcf.isCollectionOfScalars())
				{
					if ((suc = compareScalar(c1, c2)) != 0)
						break;
				}
				else
				{
					if ((suc = compare((Metadata)c1, (Metadata)c2)) != 0)
						break;
				}
			}
			if (suc != 0)
				error("in collection field " + javaField);
			return suc;
		}
	}

	private int compareCompositeField(Metadata m1, Metadata m2, MetaMetadataCompositeField mmcf)
	{
		Field javaField = mmcf.getMetadataFieldDescriptor().getField();
		Metadata mf1 = (Metadata) ReflectionTools.getFieldValue(m1, javaField);
		Metadata mf2 = (Metadata) ReflectionTools.getFieldValue(m2, javaField);
		if (mf1 == null || mf2 == null)
			return compareNull(mf1, mf2);
		
		int suc = recursiveComparison(mf1, mf2, mmcf);
		if (suc != 0)
			error("in composite field " + javaField);
		return suc;
	}
	
	private int compareNull(Object o1, Object o2)
	{
		int suc = 0;
		if ((o1 != null) && (o2 == null))
		{
			error("object1 is null");
			suc = -1;
		}
		else if ((o1 == null) && (o2 != null))
		{
			error("object2 is null");
			suc = 1;
		}
		else if ((o1 == null) && (o2 == null))
		{
			suc = 0;
		}
		return suc;
	}
}
