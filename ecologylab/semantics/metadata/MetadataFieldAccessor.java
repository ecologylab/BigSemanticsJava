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
import ecologylab.xml.FieldToXMLOptimizations;
import ecologylab.xml.Optimizations;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * @author bharat
 *
 */
public class MetadataFieldAccessor<M extends Metadata> extends FieldAccessor
{

	public static final String NULL = "null";
	
	private MetadataValueChangedListener	metadataValueChangedListener;
	
	public MetadataFieldAccessor(FieldToXMLOptimizations f2XO)
	{
		super(f2XO);
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
