/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.serialization.types.ScalarType;

/**
 * @author andruid
 *
 */
public class SemanticsTypes
{
	public static final ScalarType METADATA_STRING_TYPE 					= new MetadataStringScalarType();
	
	public static final ScalarType METADATA_STRING_BUILDER_TYPE		= new MetadataStringBuilderScalarType();
	
	public static final ScalarType METADATA_INTEGER_TYPE 					= new MetadataIntegerScalarType();
	
//	public static final ScalarType METADATA_LONG_TYPE 					= new MetadataLongScalarType();
//	
//	public static final ScalarType METADATA_SHORT_TYPE 					= new MetadataShortScalarType();
//	
//	public static final ScalarType METADATA_BOOLEAN_TYPE 				= new MetadataBooleanScalarType();
//	
	public static final ScalarType METADATA_FLOAT_TYPE 						= new MetadataFloatScalarType();
	
	public static final ScalarType METADATA_DOUBLE_TYPE 					= new MetadataDoubleScalarType();
	
	public static final ScalarType METADATA_PARSED_URL_TYPE 			= new MetadataParsedURLScalarType();
	
	public static final ScalarType METADATA_FILE_TYPE 						= new MetadataFileScalarType();
	
	public static final ScalarType METADATA_DATE_TYPE 						= new MetadataDateScalarType();
	
	public static final ScalarType METADATA_IMAGE_TYPE 						= new MetadataImageScalarType();
	
	public static final ScalarType METADATA_SCALAR_TYPE_TYPE 			= new MetadataScalarTypeType();

}
