/**
 * 
 */
package ecologylab.semantics.html;

import java.util.HashMap;

import org.w3c.tidy.TdNode;

import ecologylab.generic.Generic;
import ecologylab.net.ParsedURL;

/**
 * HTMLElement that corresponds to the img tag.
 * 
 * @author andruid
 *
 */
public class ImgElement extends HTMLElement
{
	ParsedURL				src;
	
	String					alt;
	
	int							width;
	int							height;
	boolean					isMap;
	
	String					textContext;
	

	/**
	 * @param node
	 */
	public ImgElement(TdNode node)
	{
		super(node);
	}

	@Override
	public void setAttribute(String key, String value)
	{
		if (SRC.equals(key))
			src = null;	//FIXME
		else if (ALT.equals(key))
			alt			= value;
		else if (WIDTH.equals(key))
			width		= value == null ? INDEX_NOT_CALCULATED : Generic.parseInt(value, INDEX_NOT_CALCULATED );
		else if (HEIGHT.equals(key))
			height	= value == null ? INDEX_NOT_CALCULATED : Generic.parseInt(value, INDEX_NOT_CALCULATED );
		else if (ISMAP.equals(key))
			isMap		= value != null && "true".equals(value);
		else
			super.setAttribute(key, value);
	}

	public ParsedURL getSrc()
	{
		return src;
	}

	public void setSrc(ParsedURL src)
	{
		this.src = src;
	}

	public String getAlt()
	{
		return alt;
	}

	public void setAlt(String alt)
	{
		this.alt = alt;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public boolean isMap()
	{
		return isMap;
	}

	public void setMap(boolean isMap)
	{
		this.isMap = isMap;
	}

	public String getTextContext()
	{
		return textContext;
	}

	public void setTextContext(String textContext)
	{
		this.textContext = textContext;
	}

}
