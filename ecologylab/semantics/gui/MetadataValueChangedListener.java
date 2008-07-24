/**
 * 
 */
package ecologylab.semantics.gui;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataFieldAccessor;

/**
 * @author bharat
 *
 */
public interface MetadataValueChangedListener
{
	public void fieldValueChanged(MetadataFieldAccessor metadataFieldAccessor, Metadata metadata);
}
