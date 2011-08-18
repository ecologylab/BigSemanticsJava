/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ecologylab.semantics.gui.EditValueEvent;
import ecologylab.semantics.gui.EditValueListener;
import ecologylab.semantics.gui.EditValueNotifier;
import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataNestedField;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldTypes;
import ecologylab.serialization.Hint;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.types.ScalarType;

/**
 * @author andruid
 *
 */
public class MetadataFieldDescriptor<M extends Metadata> extends FieldDescriptor implements EditValueNotifier
{
	
	private static Class<?>								PROXY_CLASS																	= null;

	private static Method									PROXY_METHOD_HANDLER_GETTER									= null;

	/**
	 * the class that connects MethodHandler and LazyInitializer.
	 */
	private static Class<?>								HIBERNATE_JAVASSIST_LAZY_INITIALIZER_CLASS	= null;

	private static Method									PERSIST_OBJECT_GETTER												= null;
	
	static
	{
		try
		{
			PROXY_CLASS = Class.forName("javassist.util.proxy.ProxyObject");
			PROXY_METHOD_HANDLER_GETTER = PROXY_CLASS.getMethod("getHandler");
			HIBERNATE_JAVASSIST_LAZY_INITIALIZER_CLASS = Class.forName("org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer");
			PERSIST_OBJECT_GETTER = HIBERNATE_JAVASSIST_LAZY_INITIALIZER_CLASS.getMethod("getImplementation");
		}
		catch (ClassNotFoundException e)
		{
			// nothing
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
		this.isMixin = false;
		this.definingMmdField = definingMmdField;
		
		if (definingMmdField.getOtherTags() != null)
		{
			String[] otherTags = definingMmdField.getOtherTags().split(",");
			for (String otherTag : otherTags)
				this.addOtherTag(otherTag.trim());
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
	
	public MetadataBase getNestedMetadata(MetadataBase context)
	{
		return isScalar() ? null : (MetadataBase) getNested((ElementState) context);
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
			if (!this.isScalar() && !thatField.isAnnotationPresent(Metadata.mm_no.class))
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
		String name = getName(); if (name == null) name = "NO_FIELD";
		return this.getClassSimpleName() + "[" + name + " < " + declaringClassDescriptor.getDescribedClass()
				+ " type=0x" + Integer.toHexString(getType()) + "]";
	}
	
	@Override
	public void setWrapped(boolean wrapped)
	{
		super.setWrapped(wrapped);
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
	
//	public void traverseAndResolvePolymorphismAndOtherTagsForCompilation()
//	{
//		startedTraversalForPolymorphism = true;
//		
//		// @xml_other_tags
//		if (this.definingMmdField instanceof MetaMetadataScalarField)
//		{
//			this.otherTags = this.definingMmdField.getOtherTags();
//		}
//		else if (this.definingMmdField instanceof MetaMetadataNestedField)
//		{
//			MetaMetadataNestedField nested = (MetaMetadataNestedField) this.definingMmdField;
//			
//			// @xml_other_tags: for collection fields always add, for composite fields only add for non-
//			// polymorphic ones
//			if (nested instanceof MetaMetadataCollectionField || !nested.isPolymorphicInDescendantFields())
//				this.otherTags = this.definingMmdField.getOtherTags();
//			
//			if (nested.getFieldType() != FieldTypes.COLLECTION_SCALAR)
//			{
//				// resolve @simpl_classes if any
//				HashSet<MetaMetadata> polyMmds = nested.getPolymorphicMmds();
//				if (polyMmds != null && polyMmds.size() > 0)
//				{
//					for (MetaMetadata polyMmd : polyMmds)
//					{
//						MetadataClassDescriptor mcd = polyMmd.getMetadataClassDescriptor();
//						this.registerPolymorphicDescriptor(mcd);
//					}
//				}
//				// @simpl_classes for inherently polymorphic fields
//				String polyClassStr = nested.getPolymorphicClasses();
//				if (polyClassStr != null)
//				{
//					String[] polyClassTags = polyClassStr.split(MetaMetadataNestedField.POLYMORPHIC_CLASSES_SEP);
//					if (polyClassTags != null)
//					{
//						for (String polyClassTag : polyClassTags)
//						{
//							String truePolyClassTag = polyClassTag.trim();
//							MetaMetadata thatMmd = this.definingMmdField.getRepository().getByTagName(truePolyClassTag);
//							if (thatMmd != null)
//							{
//								MetadataClassDescriptor thatCd = thatMmd.getMetadataClassDescriptor();
//								if (thatCd != null)
//								{
//									this.registerPolymorphicDescriptor(thatCd);
//								}
//								else
//								{
//									warning("can't find metadata class descriptor for " + thatMmd + ": ignoring tag " + truePolyClassTag + " in polymorphic_classes.");
//								}
//							}
//							else
//							{
//								warning("can't find meta-metadata with tag " + truePolyClassTag + ": ignoring that tag in polymorphic_classes.");
//							}
//						}
//					}
//	
//					// recursion
//					for (MetaMetadataField kid : nested.getChildMetaMetadata())
//					{
//						System.out.println(kid);
//						MetadataFieldDescriptor kidMFD = kid.getMetadataFieldDescriptor();
//						if (kidMFD != null && !kidMFD.startedTraversalForPolymorphism)
//							kidMFD.traverseAndResolvePolymorphismAndOtherTagsForCompilation();
//					}
//				}
//			}
//		}
//	}

	public Object getValue(Object context)
	{
		Object value = null;
		try
		{
			if (context != null)
			{
				if (PROXY_CLASS == null || !PROXY_CLASS.isAssignableFrom(context.getClass()))
				{
					return super.getValue(context);
				}
				else
				{
					Object handler = PROXY_METHOD_HANDLER_GETTER.invoke(context);
					Object target = PERSIST_OBJECT_GETTER.invoke(handler);
					// use getter to take advantage of AOP by javassist
					Method getter = this.getGetter();
					if (getter != null)
						value = getter.invoke(target);
				}
			}
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch bloc
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	public boolean isDefaultValue(Object value)
	{
		if (this.getType() == FieldTypes.SCALAR)
			return value == null || this.getScalarType().isDefaultValue(value.toString());
		else
			return value == null;
	}
	
	private synchronized Method getGetter()
	{
		if (getter != null)
			return getter;
		
		String fieldName = this.getName();
		String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		if (this.getType() == FieldTypes.SCALAR)
			getterName += "Metadata";
		try
		{
			getter = this.field.getDeclaringClass().getMethod(getterName);
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getter;
	}
	
}
