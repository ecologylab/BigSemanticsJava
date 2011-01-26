package ecologylab.semantics.html;

import java.util.HashMap;

import org.w3c.tidy.AttVal;
import org.w3c.tidy.TdNode;

import ecologylab.generic.Generic;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.html.documentstructure.ImageConstants;
import ecologylab.semantics.html.utils.HTMLAttributeNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;

/**
 * data structure that contains the DOM node for the {@link ImgElement} and it's associate attributes. 
 * 
 * @author eunyee
 *
 */    
public class HTMLElementTidy
implements HTMLAttributeNames, ImageConstants
{
	public static final int	INDEX_NOT_CALCULATED	= -1;
	protected	TdNode 			node;
	
	//FIXME -- get rid of this inefficient beast!
	private HashMap<String, String> attributesMap;
	
	String	xPath;
	
	String	cssClass;
	
	String	id;
	
	int			localXPathIndex	= INDEX_NOT_CALCULATED;
	
	public HTMLElementTidy()
	{
		
	}
	
	public HTMLElementTidy(TdNode node)
	{
		this.node						= node;
		addAttributes(node.attributes);
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
	
	public void setNode(TdNode node) 
	{
		this.node = node;
	}
	
	public int localXPathIndex()
	{
		int result	= this.localXPathIndex;
		if (result == INDEX_NOT_CALCULATED)
		{
			TdNode currentNode	= node;
			result = localXPathIndex(currentNode);
			this.localXPathIndex= result;
		}
		return result;
	}

	protected static int localXPathIndex(TdNode currentNode)
	{
		int result					= 0;
		String tag					= currentNode.element;

		while ((currentNode = currentNode.prev()) != null)
		{
			if (tag.equals(currentNode.element))
				result++;
		}
		return result;
	}
	public String xPath()
	{
		String result	= this.xPath;
		if (result == null)
		{
			StringBuilder buffy	= StringBuilderUtils.acquire();
			xPath(node, buffy);
			result							= StringTools.toString(buffy);
			this.xPath					= result;
			StringBuilderUtils.release(buffy);
		}
		return result;
	}
	public static void xPath(TdNode node, StringBuilder buffy)
	{
		TdNode parent = node.parent();
		if (parent != null)
		{
			xPath(parent, buffy);
			buffy.append('/').append(node.element);
			int elementIndex = localXPathIndex(node);
			buffy.append('[').append(elementIndex).append(']');
		}
		else
			buffy.append('/');
		
	}
	public TdNode getNode() 
	{
		return node;
	}
	
	/**
	 * Create HashMap of attributes from tidy AttVal linked list of them.
	 */
	protected void addAttributes(AttVal attr)
	{
		while (attr != null)
		{
			setAttribute(attr.attribute, attr.value);
			attr					= attr.next;
		}
	}

	public void recycle()
	{
		if (attributesMap != null)
		{
			attributesMap.clear();
			attributesMap	= null;
		}
		if (node != null)
			node.recycle();
		node					= null;
	}



}