/**
 * 
 */
package ecologylab.semantics.gui;

import ecologylab.model.MetadataField;
import ecologylab.semantics.metadata.MetadataFieldAccessor;

/**
 * @author bharat
 *
 */
public interface MetadataEndEditListener
{
	public void endEditHandler(String iconID, MetadataFieldAccessor metadataFieldAccessor);
}
