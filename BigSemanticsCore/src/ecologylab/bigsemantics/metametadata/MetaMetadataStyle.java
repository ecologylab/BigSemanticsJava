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
	String isSameMetadata;
	
	@simpl_scalar
	String isOnlyElement;
	
	@simpl_scalar
	String isTopLevel;
	
	@simpl_scalar
	String isChildMetadata;
	
	@simpl_scalar
	boolean hide;
}
