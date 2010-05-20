/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ecologylab.semantics.gui.EditValueEvent;
import ecologylab.semantics.gui.EditValueListener;
import ecologylab.semantics.gui.EditValueNotifier;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.FieldDescriptor;

/**
 * @author andruid
 *
 */
public class MetadataFieldDescriptor<M extends Metadata> extends FieldDescriptor implements EditValueNotifier
{
	final private boolean		isPseudoScalar;
	
	final private boolean		isMixin;
	
	Method									hwSetMethod;
	
	MetaMetadataField				metaMetadataField;

	private ArrayList<EditValueListener> editValueListeners = new ArrayList<EditValueListener>();
	
	public MetadataFieldDescriptor(ClassDescriptor declaringClassDescriptor, Field field, int annotationType) // String nameSpacePrefix
	{
		super(declaringClassDescriptor, field, annotationType);
		if (field != null)
		{
			isMixin							= field.isAnnotationPresent(MetadataBase.semantics_mixin.class);

			Class<?> thatClass	= field.getType();
			isPseudoScalar	= thatClass.isAnnotationPresent(semantics_pseudo_scalar.class);
			
			//TODO -- for future expansion??? andruid 4/14/09
//			hwSetMethod					= ReflectionTools.getMethod(thatClass, "hwSet", SET_METHOD_ARG);
		}
		else
		{
			isMixin							= false;
			isPseudoScalar			= false;		
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
		
	/**
	 * 
	 */
	//FIXME -- this seems like a bogus idea for a scalar. it only makes sense for a composite type
	public boolean hwSet(MetadataBase context, String newValue)
	{
//		this.set(context, newValue);
		return this.hwSet(context, newValue);
//		return context.hwSet(this.getTagName(), newValue);
	}
	
	//FIXME -- this looks wrong. why are we doing a lookup when we already have this?
	public void set(Metadata context, String newValue)
	{
		context.setByTagName(this.getTagName(), newValue);
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
		if(this.set(metadata, fieldValueString))	// uses reflection to call a set method or access the field directly if there is not one.
		{
			metadata.rebuildCompositeTermVector();	// makes this as if an hwSet().
			
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
