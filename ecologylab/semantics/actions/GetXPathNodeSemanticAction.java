package ecologylab.semantics.actions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import ecologylab.xml.simpl_inherit;
import ecologylab.xml.ElementState.xml_tag;

@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.GET_XPATH_NODE) class GetXPathNodeSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	/**
	 *  DOM node on which to apply xpath
	 */
	@simpl_scalar private String node;
	
	/**
	 * The XPath to apply.
	 */
	@simpl_scalar private String xpath;
	
	/**
	 * The return type of this evaluation after applicatiopn of XPath
	 */
	@simpl_scalar private String returnObject;
	
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
