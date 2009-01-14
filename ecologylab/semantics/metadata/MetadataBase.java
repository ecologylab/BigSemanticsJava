/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.OneLevelNestingIterator;
import ecologylab.generic.VectorType;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.model.text.NullTermVector;
import ecologylab.semantics.model.text.XTerm;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.Optimizations;
import ecologylab.xml.types.element.ArrayListState;

/**
 * Base class for Metadata fields that represent scalar values.
 * 
 * These, for example, lack mixins.
 * 
 * @author andruid
 *
 */
public class MetadataBase extends ElementState implements Iterable<FieldAccessor>
{

	HashMapArrayList<String, FieldAccessor> metadataFieldAccessors;


	/**
	 * 
	 */
	public MetadataBase()
	{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This is actually the real composite term vector.
	 * 
	 * @return	Null for scalars.
	 */
	public VectorType<XTerm> termVector()
	{
		return null;
	}



	public void recycle()
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
	
	/**
	 * Efficiently retrieve appropriate MetadataFieldAccessor, using lazy evaluation.
	 * 
	 * @param fieldName
	 * @return
	 */
	public MetadataFieldAccessor getMetadataFieldAccessor(String fieldName)
	{
		return (MetadataFieldAccessor) metadataFieldAccessors().get(fieldName);
	}

	protected HashMapArrayList<String, FieldAccessor> metadataFieldAccessors()
	{
		HashMapArrayList<String, FieldAccessor> result	= this.metadataFieldAccessors;
		if (result == null)
		{
			result			= computeFieldAccessors();
			metadataFieldAccessors	= result;
		}
		return result;
	}


	protected HashMapArrayList<String, FieldAccessor> computeFieldAccessors()
	{
		return Optimizations.getFieldAccessors(this.getClass(), MetadataFieldAccessor.class);
	}


	public Iterator<FieldAccessor> iterator()
	{
		return metadataFieldAccessors().iterator();
	}

	
	//FIXEME:The method has to search even all the mixins for the key.
	public FieldAccessor get(String key)
	{
		HashMapArrayList<String, FieldAccessor> fieldAccessors = metadataFieldAccessors();
		return fieldAccessors.get(key);
	}
	
	public boolean set(String tagName, String value)
	{
		tagName = tagName.toLowerCase();
		//Taking care of mixins
		MetadataBase metadata = getMetadataWhichContainsField(tagName);

		if(value != null && value.length()!=0)
		{
			if(metadata != null)
			{
				FieldAccessor fieldAccessor = get(tagName);
				if(fieldAccessor != null && value != null && value.length()!=0)
				{
					fieldAccessor.set(metadata, value);
					return true;
				}
				else 
				{
					debug("Not Able to set the field: " + tagName);
					return false;
				}
			}
		}
		return false;
	}
	
	public MetadataBase getMetadataWhichContainsField(String tagName)
	{
		HashMapArrayList<String, FieldAccessor> fieldAccessors = metadataFieldAccessors();
		
		FieldAccessor metadataFieldAccessor = fieldAccessors.get(tagName);
		if (metadataFieldAccessor != null)
		{
			return this;
		}
		//No mixins in MetadataBase.
//		if(mixins() != null && mixins().size() > 0)
//		{
//			for (Metadata mixinMetadata : mixins())
//			{
//				fieldAccessors 	= mixinMetadata.metadataFieldAccessors();
//				FieldAccessor mixinFieldAccessor 	= fieldAccessors.get(tagName);
//				if(mixinFieldAccessor != null)
//				{
//					return mixinMetadata;
//				}
//			}
//		}
		return null;
	}
	
	public boolean hwSet(String tagName, String value)
	{
		if(set(tagName, value))
		{
			//value is properly set.
			//FIXME!!rebuildCompositeTermVector()
			return true;
		}
		return false;
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

    public ArrayListState<Metadata> getMixins()
    {
   	 return null;
    }
    
 	/**
 	 * Provides MetadataFieldAccessors for each of the ecologylab.xml annotated fields in this
 	 * (probably a subclass).
 	 */
 	public OneLevelNestingIterator<FieldAccessor, ? extends MetadataBase> fullNonRecursiveIterator()
	{
		return new OneLevelNestingIterator<FieldAccessor, MetadataBase>(this, null);
	}

}
