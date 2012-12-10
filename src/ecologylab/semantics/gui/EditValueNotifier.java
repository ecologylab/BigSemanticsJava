/**
 * 
 */
package ecologylab.semantics.gui;

import ecologylab.semantics.metadata.Metadata;


/**
 * @author bharat
 *
 */
public interface EditValueNotifier
{
	public void addEditValueListener(EditValueListener listener);
	public void removeEditValueListener(EditValueListener listener);
	public boolean fireEditValue(Metadata metadata, String fieldValueString);
}
