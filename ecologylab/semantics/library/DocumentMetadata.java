package ecologylab.semantics.library;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;

public class DocumentMetadata extends Metadata
{
	@xml_attribute String 			title;
	@xml_attribute String			description;
	@xml_attribute ParsedURL		url;
	
}

// Caption 			Images
// Title 			Documents
// Description		Documents
// Anchor			Documents
