/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.OneLevelNestingIterator;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.ScalarUnmarshallingContext;
import ecologylab.xml.serial_field_descriptors;

/**
 * Base class for Metadata fields that represent scalar values.
 * 
 * These, for example, lack mixins.
 * 
 * @author andruid
 *
 */
@serial_field_descriptors(MetadataFieldDescriptor.class)
public class MetadataBase<MM extends MetaMetadataField> extends ElementState implements Iterable<FieldDescriptor>
{

	HashMapArrayList<String, FieldDescriptor> metadataFieldDescriptors;

	/**
	 * Hidden reference to the MetaMetadataRepository. DO NOT access this field directly.
	 * DO NOT create a static public accessor.
	 * -- andruid 10/7/09.
	 */
	private static MetaMetadataRepository		repository;

	MM							metaMetadata;
	
	/**
	 * Map of MetadataFieldDescriptor maps(by tag name): for each MetadataBase subclass.
	 */
	static final HashMap<String, HashMapArrayList<String, FieldDescriptor>>	fieldDescriptorsByTagNameMap	= new HashMap<String, HashMapArrayList<String, FieldDescriptor>>();
	
	/**
	 * MetadataFieldDescriptor map by tag name: for this MetadataBase subclass.
	 */
	protected final HashMapArrayList<String, FieldDescriptor> 	fieldDescriptorsByTagName;


	/**
	 * 
	 */
	public MetadataBase()
	{
		fieldDescriptorsByTagName	= lookupFieldDescriptorsByTagName();
	}
	
	/**
	 * Obtain a map of FieldDescriptors for this class, with the field names as key, but with the mixins field removed.
	 * Use lazy evaluation, caching the result by class name.
	 * 
	 * @return	A map of FieldDescriptors, with the field names as key, but with the mixins field removed.
	 */
	private final HashMapArrayList<String, FieldDescriptor> lookupFieldDescriptorsByTagName()
	{
		ClassDescriptor classDescriptor = classDescriptor();
		String className	= classDescriptor.getDescribedClassPackageName() + "." + classDescriptor.getDecribedClassSimpleName();
		HashMapArrayList<String, FieldDescriptor> result	= fieldDescriptorsByTagNameMap.get(className);
		if (result == null)
		{
			synchronized (fieldDescriptorsByTagNameMap)
			{
				result	= fieldDescriptorsByTagNameMap.get(className);
				if (result == null)
				{
					result	= computeFieldDescriptorsByTagName();
					fieldDescriptorsByTagNameMap.put(className, result);
				}
			}
		}
		return result;
	}
	/**
	 * Compute the map of FieldDescriptors for this class, with the field names as key, but with the mixins field removed.
	 * 
	 * @return	A map of FieldDescriptors, with the field names as key, but with the mixins field removed.
	 */
	private final HashMapArrayList<String, FieldDescriptor> computeFieldDescriptorsByTagName()
	{
		HashMapArrayList<String, FieldDescriptor> allFieldDescriptorsByFieldName	= classDescriptor().getFieldDescriptorsByFieldName();
		HashMapArrayList<String, FieldDescriptor> result	= new HashMapArrayList<String, FieldDescriptor>(allFieldDescriptorsByFieldName.size() - 1);
		for (FieldDescriptor fieldDescriptor : allFieldDescriptorsByFieldName.values())
		{
			String tagName	= fieldDescriptor.getTagName();
			if (!excludeFieldByTag(tagName))
				result.put(tagName, fieldDescriptor);
		}
		return result;
	}
	
	/**
	 * Don't exclude any fields from MetadataBase.
	 * 
	 * @param tagName
	 * 
	 * @return	false, always.
	 */
	public boolean excludeFieldByTag(String tagName)
	{
		return false;
	}
	public static void setRepository(MetaMetadataRepository repo)
	{
		repository	= repo;
	}
	
	/**
	 * Only use this accessor, in order to maintain future code compatability.
	 * 
	 * @return
	 */
	public MetaMetadataRepository repository()
	{
		return repository;
	}
	/**
	 * This is actually the real composite term vector.
	 * 
	 * @return	Null for scalars.
	 */
	public ITermVector termVector()
	{
		return null;
	}



	public void recycle()
	{
		super.recycle();
		
		metadataFieldDescriptors = null;
	}
	

	/**
	 * Rebuilds the composite TermVector from the individual TermVectors, when there is one.
	 * This implementation, in the base class, does nothing.
	 */
	public void rebuildCompositeTermVector()
	{
		
	}

	/**
	 * Determine if the Metadata has any entries.
	 * @return	True if there are Metadata entries.
	 */
	public boolean hasCompositeTermVector()
	{
		return false;
	}

	public HashMapArrayList<String, FieldDescriptor> fieldDescriptorsByTagName()
	{
		return fieldDescriptorsByTagName;
	}

	public MetaMetadataField metaMetadataField()
	{
		Metadata parent	= (Metadata) this.parent();
		return (parent == null) ? null : parent.metaMetadataField();
	}
	
	public Iterator<FieldDescriptor> iterator()
	{
		return fieldDescriptorsByTagName.iterator();
	}

	//FIXEME:The method has to search even all the mixins for the key.
	public MetadataFieldDescriptor getFieldDescriptorByTagName(String tagName)
	{
		return (MetadataFieldDescriptor) fieldDescriptorsByTagName.get(tagName);
	}
	

	public FieldDescriptor getFieldDescriptorByFieldName(String tagName)
	{
		return classDescriptor().getFieldDescriptorByFieldName(tagName);
	}
	
	public boolean setByTagName(String tagName, String value)
	{
		return setByTagName(tagName, value, null);
	}

	/**
	 * Unmarshall the valueString and set the field to 
	 * 
	 * @param tagName
	 * @param marshalledValue
	 * @param scalarUnMarshallingContext
	 * @return
	 */
	public boolean setByTagName(String tagName, String marshalledValue, ScalarUnmarshallingContext scalarUnMarshallingContext)
	{
		//FIXME -- why is this necessary???????????????????????
		if (marshalledValue != null && marshalledValue.length()!=0)
		{
			tagName = tagName.toLowerCase();
			FieldDescriptor fieldDescriptor = getFieldDescriptorByTagName(tagName);
			if(fieldDescriptor != null /* && value != null && value.length()!=0 */)	// allow set to nothing -- andruid & andrew 4/14/09
			{
				fieldDescriptor.set(this, marshalledValue, scalarUnMarshallingContext);
				return true;
			}
			else 
			{
				debug("Not Able to set the field: " + tagName);
			}
		}
		return false;
	}
	
	public boolean hwSet(String tagName, String value)
	{
		return setByTagName(tagName, value);
	}
		
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Inherited
    public @interface semantics_mixin
    {

    }
    
    public MetaMetadata getMetaMetadata()
    {
   	 return null;
    }
    
    public void setMetaMetadata(MetaMetadata metaMetadata)
    {
    	
    }

    public ArrayList<Metadata> getMixins()
    {
   	 return null;
    }
    
 	/**
 	 * Provides MetadataFieldDescriptors for each of the ecologylab.xml annotated fields in this
 	 * (probably a subclass).
 	 */
 	public OneLevelNestingIterator<FieldDescriptor, ? extends MetadataBase> fullNonRecursiveIterator()
	{
		return new OneLevelNestingIterator<FieldDescriptor, MetadataBase<?>>(this, null);
	}


}
