/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import java.lang.reflect.Field;

import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.metadata.scalar.MetadataScalarBase;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.types.ScalarType;
import ecologylab.serialization.types.TypeRegistry;
import ecologylab.serialization.types.scalar.ReferenceType;

/**
 * Basis for scalar types for classes that derive from MetadataScalarBase.
 * 
 * M is the MetadataBase type, like MetadataString.
 * 
 * T is the nested type, like String.
 * 
 * @author andruid
 */
public abstract class MetadataScalarType<M, T> extends ReferenceType<M>
{
	ScalarType<T>						valueScalarType;

	Field										valueField;

	private static boolean	metadataScalarTypesRegistered	= false;

/**
 * Construct.
 * Use the simple name of the metadataScalarTypeClass as the javaTypeName.
 * Use the dbTypeName of the valueClass as the dbTypeName.
 * 
 * @param metadataScalarTypeClass
 * @param valueClass
 * @param cSharpTypeName
 * @param objectiveCTypeName
 */
	public MetadataScalarType(Class<M> metadataScalarTypeClass, Class valueClass,
														String cSharpTypeName, String objectiveCTypeName)
	{
		this(metadataScalarTypeClass, valueClass, metadataScalarTypeClass.getSimpleName(), cSharpTypeName, objectiveCTypeName);
	}
	public MetadataScalarType(Class<M> metadataScalarTypeClass, Class valueClass,
														String javaTypeName, String cSharpTypeName, String objectiveCTypeName)
	{
		super(metadataScalarTypeClass, javaTypeName, cSharpTypeName,  objectiveCTypeName, 
					TypeRegistry.getScalarType(valueClass).getDbTypeName());
		
		this.valueScalarType = TypeRegistry.getScalarType(valueClass);
		valueField();
	}

	Field valueField()
	{
		Field result = valueField;
		if (result == null)
		{
			Class typeClass = MetadataScalarBase.class;
			try
			{
				result = typeClass.getDeclaredField(MetadataScalarBase.VALUE_FIELD_NAME);
				result.setAccessible(true);
				valueField = result;
			}
			catch (SecurityException e)
			{
				error("Can't access value field for " + typeClass);
			}
			catch (NoSuchFieldException e)
			{
				error("Can't find value field for " + typeClass);
			}
		}
		return result;
	}

	/**
	 * Set the value field inside the MetadataScalarBase subtype object that largerMetadataContext
	 * refers to. Instantiate that MetadataScalarBase subtype object if necessary, and set it there.
	 * 
	 * Used for deserializing.
	 */
	@Override
	public boolean setField(Object largerMetadataContext, Field field, String valueString,
			String[] format, ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		if (valueString == null)
			return true;

		boolean result = false;
		T valueObject;

		try
		{
			valueObject = valueScalarType.getInstance(valueString, format, scalarUnmarshallingContext);
			if (valueObject != null)
			{
				M metadataScalarContext = (M) field.get(largerMetadataContext);
				if (metadataScalarContext == null)
				{
					metadataScalarContext = (M) ReflectionTools.getInstance(field.getType());
					field.set(largerMetadataContext, metadataScalarContext);
				}
				valueField().set(metadataScalarContext, valueObject);
				result = true;
			}
		}
		catch (Exception e)
		{
			setFieldError(field, valueString, e);
		}
		return result;
	}

	/**
	 * Called during deserialization.
	 * 
	 * Uses the valueScalarType stored inside the scalar type to get an instance of type T. Called
	 * inside the various subtypes of this.
	 * 
	 * @param value
	 * @param formatStrings
	 * @param scalarUnmarshallingContext
	 * @return
	 */
	public T getValueInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return valueScalarType.getInstance(value, formatStrings, scalarUnmarshallingContext);
	}

	public static synchronized void init()
	{
		if (!metadataScalarTypesRegistered)
		{
			metadataScalarTypesRegistered = true;
			new SemanticsTypes();
		}
	}

	@Override
	public ScalarType operativeScalarType()
	{
		return valueScalarType;
	}

	@Override
	public Field operativeField(Field externalField)
	{
		return valueField;
	}

	@Override
	public String toString(Field field, Object largerMetadataContext)
	{
		String result = "COULDNT CONVERT!";
		try
		{
			M metadataScalarContext = (M) field.get(largerMetadataContext);
			if (metadataScalarContext != null)
			{
				T instance = (T) valueField().get(metadataScalarContext);
				if (instance == null)
					result = DEFAULT_VALUE_STRING;
				else
					result = toString(instance);
			}
			else
				result = DEFAULT_VALUE_STRING;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public String toString(T instance)
	{
		return instance.toString();
	}

	public boolean isDefaultValue(String value)
	{
		return valueScalarType.isDefaultValue(value);
	}
	
	/**
	 * True if the user should be able to express interest in fields of this type.
	 * 
	 * @return true for Strings
	 */
	public boolean affordsInterestExpression()
	{
		return true;
	}
	
}
