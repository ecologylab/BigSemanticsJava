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
public class HTMLElement
implements HTMLAttributeNames, ImageConstants
{
	private static final int	INDEX_NOT_CALCULATED	= -1;
	private TdNode node;
	private HashMap<String, String> attributesMap;
	
	String	xPath;
	
	int			localXPathIndex	= INDEX_NOT_CALCULATED;
	
	
	public HTMLElement(TdNode node)
	{
		this.node						= node;
		addAttributes(node.attributes);
	}
	
	public void setAttribute(String key, String value)
	{
		if (attributesMap == null)
			attributesMap	= new HashMap<String, String>();
		
		attributesMap.put(key, value);
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
	private void addAttributes(AttVal attr)
	{
		if (attr != null)
		{
			setAttribute(attr.attribute, attr.value);
			if (attr.next != null)
				addAttributes(attr.next);
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
	
	/**
	 * Get the alt text attribute from the image node, if there is one.
	 * Check to see if it is not bogus (not empty, "null", a url, contains advertis).
	 * If it is bogus, clear the attribute in the image node.
	 * Otherwise, return it.
	 * 
	 * @param imageNode
	 * @return		null, or a usable alt String.
	 */
	public String getNonBogusAlt()
	{
		String altText 					= this.getAttribute(ALT);
		if ((altText != null) && (ImageFeatures.altIsBogus(altText)))
		{
			altText								= null;
			this.clearAttribute(ALT);
		}
		return altText;
	}

	/**
	 * Recognize whether the image is informative or not based on its attributes and size, aspect ratio. 
	 * 
	 * @param imageNode		HTML node from <code>img</code> tag, with attributes.
	 * 
	 * @return						true if image is recognized as informative otherwise false.
	 */
	public boolean isInformativeImage() 
	{
		int width 		= this.getAttributeAsInt(WIDTH, 0);
		int height 		= getAttributeAsInt(HEIGHT, 0);
		boolean isMap	= getAttributeAsBoolean(ISMAP);
		String alt 		= getNonBogusAlt();
		String src		= this.getAttribute(SRC);
		
		boolean informImg = !(alt!=null && alt.toLowerCase().contains("advertis")) ;
//	String imgUrl = imageNode.getAttribute(SRC);
		//TODO -- should we do more advertisement filtering here?!
		
		//TODO -- should we use an encompassing hyperlink and its destination as features ???!
		
		if (informImg)
		{
			int mimeIndex		= ParsedURL.mimeIndex(src);
			int designRole	= ImageFeatures.designRole(width, height, mimeIndex, isMap);
			informImg	= (designRole == INFORMATIVE) || (designRole == UNKNOWN);
		}
		return informImg;
	}



}