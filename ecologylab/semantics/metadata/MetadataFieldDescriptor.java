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
import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.Hint;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.types.scalar.ScalarType;

/**
 * @author andruid
 *
 */
public class MetadataFieldDescriptor<M extends Metadata> extends FieldDescriptor implements EditValueNotifier
{
	
	final private boolean									isMixin;

	Method																hwSetMethod;

	/**
	 * The name in the MetaMetadataComposite field whose declaration resulted in the generation of
	 * this.
	 */
	@simpl_scalar
	private String												mmName;

	private ArrayList<EditValueListener>	editValueListeners	= new ArrayList<EditValueListener>();

	private MetaMetadataField							definingMmdField;

	public MetadataFieldDescriptor(ClassDescriptor declaringClassDescriptor, Field field, int annotationType) // String nameSpacePrefix
	{
		super(declaringClassDescriptor, field, annotationType);
		if (field != null)
		{
			isMixin							= field.isAnnotationPresent(Metadata.semantics_mixin.class);
			//TODO -- for future expansion??? andruid 4/14/09
//			hwSetMethod					= ReflectionTools.getMethod(thatClass, "hwSet", SET_METHOD_ARG);
		}
		else
		{
			isMixin							= false;
		}
		this.mmName						= deriveMmName();
	}
	
	public MetadataFieldDescriptor(ClassDescriptor baseClassDescriptor, FieldDescriptor wrappedFD, String wrapperTag)
	{
		super(baseClassDescriptor, wrappedFD, wrapperTag);
		isMixin				= false;
	}
	
	public MetadataFieldDescriptor(MetaMetadataField definingMmdField, String tagName, String comment, int type, ClassDescriptor elementClassDescriptor,
			ClassDescriptor declaringClassDescriptor, String fieldName, ScalarType scalarType,
			Hint xmlHint, String fieldType)
	{
		super(tagName, comment, type, elementClassDescriptor, declaringClassDescriptor, fieldName, scalarType, xmlHint, fieldType);
		this.definingMmdField = definingMmdField;
		this.isMixin = false;
		
		if (definingMmdField instanceof MetaMetadataCollectionField)
		{
			String childTag = ((MetaMetadataCollectionField) definingMmdField).getChildTag();
			this.setCollectionOrMapTagName(childTag);
		}
	}

	public boolean isMixin() 
	{
		return isMixin;
	}

	public void addEditValueListener(EditValueListener listener)
	{
		editValueListeners.add(listener);
	}

/**
 * Edit the value of a scalar.
 */
	public void fireEditValue(Metadata metadata, String fieldValueString)
	{
		if (isScalar())
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
	}
	
	public void removeEditValueListener(EditValueListener listener)
	{
		editValueListeners.remove(listener);
	}
	
	public ElementState getNested(MetadataBase context)
	{
		return isScalar() ? null : getNested((ElementState) context);
	}
	
	@Override
	protected void setFieldToScalar(Object context, String value, ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		
		super.setFieldToScalar(context, value, scalarUnmarshallingContext);
	}
	
	private String deriveMmName()
	{
		String result	= null;
		
		Field thatField = this.field;
		final Metadata.mm_name mmNameAnnotation 	= thatField.getAnnotation(Metadata.mm_name.class);
	
		if (mmNameAnnotation != null)
		{
			result			= mmNameAnnotation.value();
		}
		if (result == null)
		{
			result			= XMLTools.getXmlTagName(thatField.getName(), null);
			if (!this.isScalar())
				error("Missing @mm_name annotation for " + thatField + "\tusing " + result);
		}
		return result;
	}
	
	/**
	 * @return the mmName
	 */
	public String getMmName()
	{
		return mmName;
	}
	
	/**
	 * get the (defining) meta-metadata field object. currently, only used by the compiler.
	 *  
	 * @return
	 */
	public MetaMetadataField getDefiningMmdField()
	{
		return definingMmdField;
	}
	
	public String toString()
	{
		String name = getFieldName(); if (name == null) name = "NO_FIELD";
		return this.getClassName() + "[" + name + " < " + declaringClassDescriptor.getDescribedClass()
				+ " type=0x" + Integer.toHexString(getType()) + "]";
	}
	
	@Override
	public void setWrapped(boolean wrapped)
	{
		super.setWrapped(wrapped);
	}

}
