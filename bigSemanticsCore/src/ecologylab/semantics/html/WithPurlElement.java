/**
 * 
 */
package ecologylab.semantics.html;

import org.w3c.dom.Node;

import ecologylab.net.ParsedURL;

/**
 * @author andruid
 *
 */
public class WithPurlElement extends HTMLElementDOM
{
	ParsedURL			basePurl;
	

/**
 * 
 * @param node
 * @param basePurl
 */
	public WithPurlElement(Node node, ParsedURL basePurl)
	{
		super();
		this.node			= node;
		this.basePurl	= basePurl;
		addAttributes(node.getAttributes());
	}

}
