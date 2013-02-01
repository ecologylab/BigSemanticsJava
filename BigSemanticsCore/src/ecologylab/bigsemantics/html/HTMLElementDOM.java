package ecologylab.bigsemantics.html;

import java.util.HashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ecologylab.bigsemantics.html.documentstructure.ImageConstants;
import ecologylab.bigsemantics.html.utils.HTMLNames;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.generic.Generic;
import ecologylab.generic.StringTools;

/**
 * data structure that contains the DOM node for the {@link ImgElement} and it's associate attributes. 
 * 
 * @author eunyee
 *
 */    
public class HTMLElementDOM
implements HTMLNames, ImageConstants
{
	public static final int	INDEX_NOT_CALCULATED	= -1;
	protected	Node 			node;
	
	//FIXME -- get rid of this inefficient beast!
	private HashMap<String, String> attributesMap;
	
	String	xpath;
	
	String	cssClass;
	
	String	id;
	
	int			localXPathIndex	= INDEX_NOT_CALCULATED;
	
	public HTMLElementDOM()
	{
		
	}
	
	public HTMLElementDOM(Node node)
	{
		this.node						= node;
		addAttributes(node.getAttributes());
	}
	
	protected void setAttribute(String key, String value)
	{
		if ("id".equals(key))
			id								= value;
		else if ("class".equals(key))
			cssClass					= value;
		else
		{
			//FIXME -- get rid of this old code!
			if (attributesMap == null)
				attributesMap	= new HashMap<String, String>();
			
			attributesMap.put(key, value);
		}
	}
	
	public void clearAttribute(String key)
	{
		if (attributesMap != null)
			attributesMap.put(key, null);
	}
	
	public String getAttribute(String key)
	{
		return attributesMap == null ? null : attributesMap.get(key);
	}
	
	public int getAttributeAsInt(String key)
	{
		String value	= getAttribute(key);
		return value == null ? INDEX_NOT_CALCULATED : Generic.parseInt(value, INDEX_NOT_CALCULATED );
	}
	public int getAttributeAsInt(String key, int defaultValue)
	{
		String value	= getAttribute(key);
		return value == null ? INDEX_NOT_CALCULATED : Generic.parseInt(value, defaultValue);
	}
	
	public boolean getAttributeAsBoolean(String key)
	{
		String value	= getAttribute(key);
		return value != null && "true".equals(value);
	}
	
	public void setNode(Node node) 
	{
		this.node = node;
	}

	public String xpath()
	{
		String result = this.xpath;
		if (result == null)
		{
			StringBuilder buffy = StringBuilderUtils.acquire();
			xpath(buffy, node);
			result = StringTools.toString(buffy);
			this.xpath = result;
			buffy.setLength(0);
			StringBuilderUtils.release(buffy);
		}

		return result;
	}

	public void xpath(StringBuilder buffy, Node xpathNode)
	{
		if (xpathNode.getParentNode() != null && node.getParentNode().getNodeName() != null)
			xpath(buffy, xpathNode.getParentNode());
		thisNodeXPath(buffy);
	}

	public void thisNodeXPath(StringBuilder buffy)
	{
		buffy.append('/').append(node.getNodeName());
		int count = 1;
		Node prev = node.getPreviousSibling();
		while (prev != null)
		{
			if (node.getNodeName().equals(prev.getNodeName()))
				count++;
			prev = prev.getPreviousSibling();
		}
		if (count > 1)
			buffy.append('[').append(count).append(']');
	}
	
	public Node getNode() 
	{
		return node;
	}
	
	/**
	 * Create HashMap of attributes from tidy AttVal linked list of them.
	 */
	protected void addAttributes(NamedNodeMap attributes)
	{
		for (int i=0; i<attributes.getLength(); i++)
		{
			Node attr = attributes.item(i);
			setAttribute(attr.getNodeName(), attr.getNodeValue());
		}
	}

	public void recycle()
	{
		if (attributesMap != null)
		{
			attributesMap.clear();
			attributesMap	= null;
		}

		node					= null;
	}



}