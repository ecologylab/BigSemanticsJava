package ecologylab.semantics.actions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
public @xml_tag(SemanticActionStandardMethods.GET_XPATH_NODE) class GetXPathNodeSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	/**
	 *  DOM node on which to apply xpath
	 */
	@xml_attribute private String node;
	
	/**
	 * The XPath to apply.
	 */
	@xml_attribute private String xpath;
	
	/**
	 * The return type of this evaluation after applicatiopn of XPath
	 */
	@xml_attribute private String returnObject;
	
	@Override
	public String getActionName()
	{
		return GET_XPATH_NODE;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the node
	 */
	public final String getNode()
	{
		return node;
	}

	/**
	 * @return the xpath
	 */
	public final String getXpath()
	{
		return xpath;
	}

	/**
	 * Tells the return TYPE.
	 * TODO: MIght need to implement all the possible XPathConstants
	 * @return the returnType
	 */
	public final QName getReturnObject()
	{
		if(SemanticActionsKeyWords.NODE_SET.equals(returnObject))
		{
			return XPathConstants.NODESET;
		}
		return null;
	}

}
