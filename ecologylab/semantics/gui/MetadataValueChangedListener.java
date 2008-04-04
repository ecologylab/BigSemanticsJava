/**
 * 
 */
package ecologylab.semantics.gui;

import ecologylab.model.MetadataField;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataFieldAccessor;

/**
 * @author bharat
 *
 */
public interface MetadataValueChangedListener
{
//	public void endEditHandler(String iconID, MetadataFieldAccessor metadataFieldAccessor);
	
	public void fieldValueChanged(MetadataFieldAccessor metadataFieldAccessor, Metadata metadata);
}
