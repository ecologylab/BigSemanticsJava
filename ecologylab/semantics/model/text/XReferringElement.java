/**
 * 
 */
package ecologylab.semantics.model.text;


/**
 * This is an object that contains an XTerm; the Referent refers to the XTerm.
 * 
 * @author blake
 */
public interface XReferringElement
{
	public XTermVector termVector();
	
	/**
	 * @return	true if this should not be recycled, because the user may need it.
	 */
	public boolean onScreenOrUndoable();
	
	/**
	 * Shows the element and what refers to it.
	 */
	public String debugString();


}
