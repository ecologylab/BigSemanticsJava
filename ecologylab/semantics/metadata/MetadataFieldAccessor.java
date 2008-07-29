/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.semantics.gui.MetadataValueChangedListener;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.FieldToXMLOptimizations;
import ecologylab.xml.xml_inherit;

/**
 * @author andruid
 *
 */
public class MetadataFieldAccessor<M extends Metadata> extends FieldAccessor
{
	final private boolean		isPseudoScalar;
	
	final private boolean		isMixin;
	
	public static final String NULL = "null";
	
	private MetadataValueChangedListener	metadataValueChangedListener;
	
	public MetadataFieldAccessor(FieldToXMLOptimizations f2XO)
	{
		super(f2XO);
		if (field != null)
		{
			isMixin			= field.isAnnotationPresent(MetadataBase.semantics_mixin.class);

			Class<?> thatClass	= field.getType();
			isPseudoScalar	= thatClass.isAnnotationPresent(semantics_pseudo_scalar.class);
		}
		else
		{
			isMixin			= false;
			isPseudoScalar	= false;		
		}
	}
	
	public boolean isPseudoScalar() 
	{
		return isPseudoScalar;
	}

	public boolean isMixin() 
	{
		return isMixin;
	}

	public void editValue(MetadataBase context, String newValue)
	{
		if (metadataValueChangedListener != null)
			metadataValueChangedListener.fieldValueChanged(this, context);
		
		this.hwSet(context, newValue);
	}
		
	public void hwSet(MetadataBase context, String newValue)
	{
//		this.set(context, newValue);
		context.hwSet(this.getTagName(), newValue);
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
