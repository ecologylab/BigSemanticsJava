/**
 * 
 */
package ecologylab.bigsemantics.html;

import org.w3c.dom.Node;

import ecologylab.net.ParsedURL;

/**
 * The anchor HTMLElement, used for hyperlinks.
 * 
 * @author andruid
 */
public class AElement extends WithPurlElement
{
	ParsedURL			href;
	
	/**
	 * @param node
	 * @param purl TODO
	 */
	public AElement(Node node, ParsedURL purl)
	{
		super(node, purl);
	}

	@Override
	protected void setAttribute(String key, String value)
	{
		if (HREF.equals(key))
			href 		= (basePurl == null) ? ParsedURL.getAbsolute(value) : basePurl.createFromHTML(value);
	}
	
	public ParsedURL getHref()
	{
		return href;
	}
	public void setHref(ParsedURL href)
	{
		this.href = href;
	}

}
