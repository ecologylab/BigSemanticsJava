/**
 * 
 */
package ecologylab.semantics.metadata;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ecologylab.semantics.gui.EditValueEvent;
import ecologylab.semantics.gui.EditValueListener;
import ecologylab.semantics.gui.EditValueNotifier;
import ecologylab.semantics.metadata.scalar.MetadataScalarBase;
import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MmdGenericTypeVar;
import ecologylab.semantics.metametadata.MetaMetadataNestedField;
import ecologylab.semantics.metametadata.MmdCompilerService;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.CollectionType;
import ecologylab.serialization.types.ScalarType;
import ecologylab.serialization.types.scalar.CompositeAsScalarType;

/**
 * @author andruid
 *
 */
public class MetadataFieldDescriptor<M extends Metadata> extends FieldDescriptor implements EditValueNotifier
{
	
	final private boolean									isMixin;

	Method																hwSetMethod;

	Method																getter;
	
	/**
	 * The name in the MetaMetadataComposite field whose declaration resulted in the generation of
	 * this.
	 */
	@simpl_scalar
	private String												mmName;

	private ArrayList<EditValueListener>	editValueListeners							= new ArrayList<EditValueListener>();

	private MetaMetadataField							definingMmdField;

	private boolean												startedTraversalForPolymorphism	= false;
	
	public MetadataFieldDescriptor(ClassDescriptor declaringClassDescriptor, Field field, int annotationType) // String nameSpacePrefix
	{
		super(declaringClassDescriptor, field, annotationType);
		if (field != null)
		{
			isMixin							= field.isAnnotationPresent(semantics_mixin.class);
			//TODO -- for future expansion??? andruid 4/14/09
//			hwSetMethod					= ReflectionTools.getMethod(thatClass, "hwSet", SET_METHOD_ARG);
		}
		else
		{
			isMixin							= false;
		}
		this.mmName						= deriveMmName();
		checkScalarType();
	}
	
	public MetadataFieldDescriptor(ClassDescriptor baseClassDescriptor, FieldDescriptor wrappedFD, String wrapperTag)
	{
		super(baseClassDescriptor, wrappedFD, wrapperTag);
		isMixin				= false;
		checkScalarType();
	}
	
	public MetadataFieldDescriptor(MetaMetadataField definingMmdField, String tagName, String comment, int type, ClassDescriptor elementClassDescriptor,
			ClassDescriptor declaringClassDescriptor, String fieldName, ScalarType scalarType,
			Hint xmlHint, String fieldType)
	{
		super(tagName, comment, type, elementClassDescriptor, declaringClassDescriptor, fieldName, scalarType, xmlHint, fieldType);
		this.isMixin = false;
		this.definingMmdField = definingMmdField;
		
		if (definingMmdField.getOtherTags() != null)
		{
			String[] otherTags = definingMmdField.getOtherTags().split(",");
			for (String otherTag : otherTags)
				this.otherTags().add(otherTag.trim());
		}
		
		// child tag for collections
		if (definingMmdField instanceof MetaMetadataCollectionField)
		{
			String childTag = ((MetaMetadataCollectionField) definingMmdField).getChildTag();
			this.setCollectionOrMapTagName(childTag);
		}
		
		// simpl_scope for inherently polymorphic fields
		if (definingMmdField instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			String scopeName = nested.getPolymorphicScope();
			this.setUnresolvedScopeAnnotation(scopeName);
		}
		
		checkScalarType();
	}
	
	private void checkScalarType()
	{
		if (this.field != null
				&& MetadataScalarBase.class.isAssignableFrom(this.field.getType())
				&& this.getScalarType() != null
				&& this.getScalarType() instanceof CompositeAsScalarType)
		{
			warning("A CompositeAsScalarType Field!");
			warning("Please check if metadata scalar types registered before MetadataFieldDescriptors formed!");
		}
	}

	@Override
	public boolean isMixin() 
	{
		return isMixin;
	}

	@Override
	public void addEditValueListener(EditValueListener listener)
	{
		editValueListeners.add(listener);
	}

/**
 * Edit the value of a scalar.
 * 
 * @return True if the value of the field is set; otherwise, false.
 */
	@Override
	public boolean fireEditValue(Metadata metadata, String fieldValueString)
	{
		boolean result = false;
		if (isScalar())
		{
			result = this.set(metadata, fieldValueString);
			if(result)	// uses reflection to call a set method or access the field directly if there is not one.
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
		
		return result;
	}
	
	@Override
	public void removeEditValueListener(EditValueListener listener)
	{
		editValueListeners.remove(listener);
	}
	
	public MetadataBase getNestedMetadata(MetadataBase context)
	{
		return isScalar() ? null : (MetadataBase) getNested(context);
	}
	
	@Override
	public void setFieldToScalar(Object context, String value, ScalarUnmarshallingContext scalarUnmarshallingContext)
	{		
		super.setFieldToScalar(context, value, scalarUnmarshallingContext);
	}
	
	private String deriveMmName()
	{
		String result	= null;
		
		Field thatField = this.field;
		final mm_name mmNameAnnotation 	= thatField.getAnnotation(mm_name.class);
	
		if (mmNameAnnotation != null)
		{
			result			= mmNameAnnotation.value();
		}
		if (result == null)
		{
			result			= XMLTools.getXmlTagName(thatField.getName(), null);
			if (!this.isScalar() && !thatField.isAnnotationPresent(mm_no.class))
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
	
	public void setDefiningMmdField(MetaMetadataField mmdField)
	{
		this.definingMmdField = mmdField;
	}
	
	@Override
	public String toString()
	{
		String name = getName(); if (name == null) name = "NO_FIELD";
		return this.getClassSimpleName() + "[" + name + " < " + declaringClassDescriptor.getDescribedClass()
				+ " type=0x" + Integer.toHexString(getType()) + "]";
	}
	
	@Override
	public void setWrapped(boolean wrapped)
	{
		super.setWrapped(wrapped);
	}
	
	public void setWrappedFD(MetadataFieldDescriptor wrappedFD)
	{
		super.setWrappedFD(wrappedFD);
	}
	
	@Override
	public void setTagName(String tagName)
	{
		super.setTagName(tagName);
	}
	
	@Override
	public void setCollectionOrMapTagName(String collectionOrMapTagName)
	{
		super.setCollectionOrMapTagName(collectionOrMapTagName);
	}
	
	@Override
	public MetadataFieldDescriptor clone()
	{
		return (MetadataFieldDescriptor) super.clone();
	}
	
	public void setGeneric(String genericParametersString)
	{
		this.isGeneric = true;
		this.genericParametersString = genericParametersString;
	}
	
	private MmdCompilerService compilerService;
	
	public void setCompilerService(MmdCompilerService compilerService)
	{
		this.compilerService = compilerService;
	}
	
	private String cachedJavaType;
	
	@Override
	public String getJavaType()
	{
		if (cachedJavaType != null)
			return cachedJavaType;
		
		CollectionType collectionType = this.getCollectionType();
		String javaType = super.getJavaType();
		
		if (compilerService != null && collectionType != null && definingMmdField instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			List<MmdGenericTypeVar> mmdGenericTypeVars = nested.getMetaMetadataGenericTypeVars();
			if (mmdGenericTypeVars != null && mmdGenericTypeVars.size() > 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(javaType.substring(0, javaType.indexOf('<')));
				sb.append("<");
				sb.append(this.getElementClassDescriptor().getDescribedClassSimpleName());
				try
				{
					compilerService.appendGenericTypeVarParameterizations(sb, mmdGenericTypeVars, nested.getRepository());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append(">");
				return sb.toString();
			}
		}
		
		if (getType() == COMPOSITE_ELEMENT)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			List<MmdGenericTypeVar> mmdGenericTypeVars = nested.getMetaMetadataGenericTypeVars();
			if (mmdGenericTypeVars != null && mmdGenericTypeVars.size() > 0)
			{
				for (MmdGenericTypeVar mmdGenericTypeVar : mmdGenericTypeVars)
				{
					if (mmdGenericTypeVar.getGenericType() != null)
					{
						javaType = mmdGenericTypeVar.getGenericType();
						break;
					}
				}
			}
		}
		
		cachedJavaType = javaType;
		return javaType;
	}
	
	private String cachedCSharpType;
	
	@Override
	public String getCSharpType()
	{
		if (cachedCSharpType != null)
			return cachedCSharpType;
		
		CollectionType collectionType = this.getCollectionType();
		String csType = super.getCSharpType();
		
		if (compilerService != null && collectionType != null && definingMmdField instanceof MetaMetadataNestedField)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			List<MmdGenericTypeVar> mmdGenericTypeVars = nested.getMetaMetadataGenericTypeVars();
			if (mmdGenericTypeVars != null && mmdGenericTypeVars.size() > 0)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(csType.substring(0, csType.indexOf('<')));
				sb.append("<");
				sb.append(this.getElementClassDescriptor().getDescribedClassSimpleName());
				try
				{
					compilerService.appendGenericTypeVarParameterizations(sb, mmdGenericTypeVars, nested.getRepository());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append(">");
				return sb.toString();
			}
		}
		
		if (getType() == COMPOSITE_ELEMENT)
		{
			MetaMetadataNestedField nested = (MetaMetadataNestedField) definingMmdField;
			List<MmdGenericTypeVar> mmdGenericTypeVars = nested.getMetaMetadataGenericTypeVars();
			if (mmdGenericTypeVars != null && mmdGenericTypeVars.size() > 0)
			{
				for (MmdGenericTypeVar mmdGenericTypeVar : mmdGenericTypeVars)
				{
					if (mmdGenericTypeVar.getGenericType() != null)
					{
						csType = mmdGenericTypeVar.getGenericType();
						break;
					}
				}
			}
		}
		
		cachedCSharpType = csType;
		return csType;
	}
	
}
