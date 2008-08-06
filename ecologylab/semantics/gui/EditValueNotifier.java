/**
 * 
 */
package ecologylab.semantics.gui;

import ecologylab.semantics.metadata.MetadataBase;


/**
 * @author bharat
 *
 */
public interface EditValueNotifier
{
	public void addEditValueListener(EditValueListener listener);
	public void removeEditValueListener(EditValueListener listener);
	public void fireEditValue(MetadataBase metadata, String fieldValueString);
}
