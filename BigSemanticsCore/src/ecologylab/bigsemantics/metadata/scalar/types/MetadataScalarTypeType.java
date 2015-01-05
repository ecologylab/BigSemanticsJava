/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar.types;

import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.types.TypeRegistry;
import ecologylab.serialization.types.scalar.ReferenceType;

/**
 * Cool class for de/serializing MetadataScalarTypes from simple strings, like Integer...
 * 
 * @author andruid
 */
public class MetadataScalarTypeType extends ReferenceType<MetadataScalarType>
{
	public MetadataScalarTypeType()
	{
		super(MetadataScalarType.class, null, null, null, null);
	}

	/**
	 * Capitalize the value if  it wasn't.
	 * Append "Type".
	 * Use this to call TypeRegistry.getType().
	 */
	@Override
	public MetadataScalarType getInstance(String value, String[] formatStrings, ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		if ((value == null) || (value.length() == 0))
			return null;
		
		MetadataScalarType result	= null;
		String simpleName;
		
		if (value != null && value.startsWith("Metadata"))
			simpleName							= value;
		else
		{
			if ("int".equals(value) || "Int".equals(value))
				value										= "Integer";	// be flexible about integer types
			
			int length			= value.length();
	
			StringBuilder buffy	= new StringBuilder(length + 18);	// includes room for "Metadata" & "Type"
			buffy.append("Metadata");
			char firstChar			= value.charAt(0);
			if (Character.isLowerCase(firstChar))
			{
				buffy.append(Character.toUpperCase(firstChar));
				if (length > 1)
					buffy.append(value, 1, length);
			}
			else
			{
				buffy.append(value);
			}
			simpleName = buffy.toString();
		}
		return (MetadataScalarType) TypeRegistry.getScalarTypeBySimpleName(simpleName);			
	}
	
	/**
	 * As we write in the meta-metadata by hand, just save the name of the underlying type.
	 * getInstance() will properly bind that to the correct MetadataScalarType subclass during deserialization.
	 */
	@Override
	public String marshall(MetadataScalarType instance, TranslationContext serializationContext)
	{
		return instance.operativeScalarType().getJavaClass().getSimpleName();
	}

}
