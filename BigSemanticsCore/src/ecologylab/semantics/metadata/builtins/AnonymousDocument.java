/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * Class for use during Drag and Drop, when we do not have a real Document because we don't know the source location.
 * 
 * @author andruid
 */
@simpl_inherit
public class AnonymousDocument extends Document
{
	
	public static final ParsedURL		ANONYMOUS_LOCATION	= ParsedURL.getAbsolute("http://anonymous.location.com");

	/**
	 * 
	 */
	public AnonymousDocument()
	{
		super(ANONYMOUS_LOCATION);
	}

	/**
	 * @param metaMetadata
	 */
	public AnonymousDocument(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Lookout for instances of the AnonymousDocument.
	 * @return	true
	 */
	@Override
	public boolean isAnonymous()
	{
		return true;
	}

}
