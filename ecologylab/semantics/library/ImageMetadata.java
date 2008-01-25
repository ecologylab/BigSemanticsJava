package ecologylab.semantics.library;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;

public class ImageMetadata extends Metadata
{
	@xml_attribute String 		caption;
	@xml_attribute ParsedURL	url;
	
}
