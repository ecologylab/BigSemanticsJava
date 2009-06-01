/**
 * 
 */
package ecologylab.semantics.html;

import org.w3c.tidy.TdNode;

import ecologylab.net.ParsedURL;

/**
 * The anchor HTMLElement, used for hyperlinks.
 * 
 * @author andruid
 */
public class AElement extends HTMLElement
{
	ParsedURL			href;
	
	/**
	 * @param node
	 */
	public AElement(TdNode node)
	{
		super(node);
		// TODO Auto-generated constructor stub
	}

}
