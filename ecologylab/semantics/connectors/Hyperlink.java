package ecologylab.semantics.connectors;

import ecologylab.net.ParsedURL;

/**
 * This represents an object there is a link to, that is, 
 * a hyperlink reference.
 *
 * @author andruid
 */
public interface Hyperlink<C extends Container>
{
	public ParsedURL purl();
	
	/**
	 * 
	 * @return	If this is a Contaiiner, return this. Otherwise, return null.
	 */
	public C container();
}
