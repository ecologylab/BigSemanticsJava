/**
 * 
 */
package ecologylab.semantics.metadata;

import java.util.ArrayList;

import ecologylab.gui.text.ExtentChangedEvent;
import ecologylab.gui.text.ExtentChangedListener;
import ecologylab.semantics.gui.EditValueEvent;
import ecologylab.semantics.gui.EditValueListener;
import ecologylab.semantics.gui.EditValueNotifier;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.FieldToXMLOptimizations;
import ecologylab.xml.xml_inherit;

/**
 * @author andruid
 *
 */
public class MetadataFieldAccessor<M extends Metadata> extends FieldAccessor implements EditValueNotifier
{
	final private boolean		isPseudoScalar;
	
	final private boolean		isMixin;
	
	private ArrayList<EditValueListener> editValueListeners = new ArrayList<EditValueListener>();
	
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

//	public void editValue(MetadataBase context, String newValue)
//	{
//		if (metadataValueChangedListener != null)
//			metadataValueChangedListener.fieldValueChanged(this, context);
//		
//		this.hwSet(context, newValue);
//	}
		
	public boolean hwSet(MetadataBase context, String newValue)
	{
//		this.set(context, newValue);
		return context.hwSet(this.getTagName(), newValue);
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

	public void addEditValueListener(EditValueListener listener)
	{
		editValueListeners.add(listener);
	}
	
	public void fireEditValue(MetadataBase metadata, String fieldValueString)
	{
		if(this.hwSet(metadata, fieldValueString))
		{
			//Call the listeners only after the field is properly set.
			EditValueEvent event = new EditValueEvent(this, metadata);

			for(EditValueListener listener : editValueListeners)
			{
				listener.editValue(event);
			}
		}
	}
	
	public void removeEditValueListener(EditValueListener listener)
	{
		editValueListeners.remove(listener);
	}
}
