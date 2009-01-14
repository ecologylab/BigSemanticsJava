package ecologylab.media.html.dom;

import java.util.HashMap;

import org.w3c.tidy.TdNode;

/**
 * data structure that contains the DOM node for the <img> element and it's associate attributes. 
 * 
 * @author eunyee
 *
 */    
public class ImgNodewithAttr
{
	private TdNode node;
	private HashMap<String, Object> attributesMap;
	
	public void addToAttributesMap(String key, Object value)
	{
		attributesMap.put(key, value);
	}
	
	public void setAttributesMap(HashMap<String, Object> attributesMap) 
	{
		this.attributesMap = attributesMap;
	}
	
	public HashMap<String, Object> getAttributesMap() 
	{
		return attributesMap;
	}
	
	public void setNode(TdNode node) 
	{
		this.node = node;
	}
	
	public TdNode getNode() 
	{
		return node;
	}
}