package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * MetaMetadata Style Class
 * 
 * @author ajit
 *
 */

@simpl_tag("style")
class MetaMetadataStyle {
	public MetaMetadataStyle()
	{
		
	}

	@simpl_scalar
	boolean isSameMetadata;
	
	@simpl_scalar
	boolean isOnlyElement;
	
	@simpl_scalar
	boolean isTopLevel;
	
	@simpl_scalar
	boolean isChildMetadata;
	
	@simpl_scalar
	boolean hide;
}
