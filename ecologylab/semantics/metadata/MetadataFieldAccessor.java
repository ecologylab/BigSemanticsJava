/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.reflect.Field;

import ecologylab.generic.HashMapArrayList;
//import ecologylab.model.MetadataEndEditListener;
import ecologylab.model.MetadataField;
import ecologylab.semantics.gui.MetadataValueChangedListener;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.Optimizations;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * @author bharat
 *
 */
public class MetadataFieldAccessor<M extends Metadata> extends FieldAccessor
{

	public static final String NULL = "null";
	private M metadata;
	
	private MetadataValueChangedListener	metadataValueChangedListener;
	
//	T	value = null;
	
	public MetadataFieldAccessor(M metadata, FieldAccessor fieldAccessor)
	{
		this(metadata,fieldAccessor.getField(),fieldAccessor.getScalarType(), fieldAccessor.getTagName());
	}
	
	public MetadataFieldAccessor(M metadata, Field field, ScalarType<?> scalarType, String tagName)
	{
		super(field, scalarType, tagName);
		this.setMetadata(metadata);
	}
	
	public void editValue(Metadata context, String newValue)
	{
		if (metadataValueChangedListener != null)
			metadataValueChangedListener.fieldValueChanged(this, context);
		
		this.hwSet(context, newValue);
	}
	
		
	public void hwSet(Metadata context, String newValue)
	{
		this.set(context, newValue);
		context.rebuildCompositeTermVector();
	}
	
	public void set(Metadata context, String newValue)
	{
		context.set(this.getTagName(), newValue);
	}
	
//	public void endEditHandlerDispatch(MetadataValueChangedListener listener, String iconID)
//  	{
//  		endEditHandler(listener, iconID);
//  	}
//	
//	protected void endEditHandler(MetadataValueChangedListener listener, String iconID)
//  	{
//		listener.endEditHandler(iconID, this);
//  	}
	
	public HashMapArrayList<String, FieldAccessor> getFieldAccessorsForThis(Class<? extends FieldAccessor> fieldAccessorClass)
	{
		return Optimizations.getFieldAccessors(metadata.getClass());
	}
//		
//		
//		HashMapArrayList<String, FieldAccessor> result	= fieldAccessors;
//		if (result == null)
//		{
//			result				= createFieldAccessors(fieldAccessorClass);
//			this.fieldAccessors	= result;
//		}
//		return result;
//	}

	public String getValueString()
	{
		return super.getValueString(metadata);
	}
	
	/**
	 * @return the metadata
	 */
	public M getMetadata()
	{
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(M metadata)
	{
		this.metadata = metadata;
	}

	/**
	 * @return the metadataValueChangedListener
	 */
	public MetadataValueChangedListener getMetadataValueChangedListener()
	{
		return metadataValueChangedListener;
	}

	/**
	 * @param metadataValueChangedListener the metadataValueChangedListener to set
	 */
	public void setMetadataValueChangedListener(
			MetadataValueChangedListener metadataValueChangedListener)
	{
		this.metadataValueChangedListener = metadataValueChangedListener;
	}

	
}
