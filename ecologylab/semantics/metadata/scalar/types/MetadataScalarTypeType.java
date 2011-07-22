/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.types.CrossLanguageTypeConstants;
import ecologylab.serialization.types.ScalarType;
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
		MetadataScalarType result	= null;
		if ("int".equals(value) || "Int".equals(value))
			value										= "Integer";	// be flexible about integer types
		
		int length			= value.length();
		if ((value != null) && (length > 0))
		{
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
				buffy.append(value);
//			buffy.append("ScalarType");
			
			String buffyString = buffy.toString();
			result	= (MetadataScalarType) TypeRegistry.getScalarTypeBySimpleName(buffyString);
		}
		return result;			
	}
}
