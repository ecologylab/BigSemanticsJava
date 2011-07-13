/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.types.scalar.MappingConstants;
import ecologylab.serialization.types.scalar.ReferenceType;
import ecologylab.serialization.types.scalar.ScalarType;
import ecologylab.serialization.types.scalar.TypeRegistry;

/**
 * Cool class for de/serializing MetadataScalarTypes from simple strings, like Integer...
 * 
 * @author andruid
 */
public class MetadataScalarTypeType extends ReferenceType<MetadataScalarType>
{
	public MetadataScalarTypeType()
	{
		super(MetadataScalarType.class);
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
		int length			= value.length();
		if ((value != null) && (length > 0))
		{
			StringBuilder buffy	= new StringBuilder(length + 12);	// includes room for "Metadata" & "Type"
			buffy.append("Metadata");
			char firstChar			= value.charAt(0);
			if (Character.isLowerCase(firstChar))
			{
				buffy.append(Character.toUpperCase(firstChar));
				if (length > 1)
					buffy.append(value, 1, length - 1);
			}
			else
				buffy.append(value);
			buffy.append("Type");
			
			result	= (MetadataScalarType) TypeRegistry.getType(buffy.toString());
		}
		return result;			
	}

	@Override
	public String getCSharptType()
	{
		return MappingConstants.DOTNET_SCALAR_TYPE;
	}
	
	@Override
	public String getJavaType()
	{
		return MappingConstants.JAVA_SCALAR_TYPE;
	}

	@Override
	public String getDbType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObjectiveCType()
	{
		return MappingConstants.OBJC_SCALAR_TYPE;
	}

}
