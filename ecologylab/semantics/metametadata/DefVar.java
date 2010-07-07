package ecologylab.semantics.metametadata;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.xml.ElementState;
import ecologylab.xml.types.scalar.ScalarType;

public class DefVar  extends ElementState
{
	/**
	 * Name of the variable to be declared
	 */
	@simpl_scalar private String name;
	
	/**
	 * XPath to evaluate this variable
	 */
	@simpl_scalar private String xpath;
	
	
	/**
	 * The return type of this evaluation after applicatiopn of XPath
	 */
	@simpl_scalar private String type;
	
	/**
	 * Node on which this XPath has to be applied
	 */
	@simpl_scalar private String contextNode;
	
	
	/**
	 * scalar type of variable
	 */
	@simpl_scalar	private ScalarType													scalarType;			
	
	/**
	 *  Value of variable
	 */
	@simpl_scalar private String 						value;
	
	/**
	 * comment for this variable
	 */
	@simpl_scalar private String comment;

	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the xpath
	 */
	public final String getXpath()
	{
		return xpath;
	}

	/**
	 * @param xpath the xpath to set
	 */
	public final void setXpath(String xpath)
	{
		this.xpath = xpath;
	}

	/**
	 * @return the type
	 */
	public final QName getType()
	{
		if(SemanticActionsKeyWords.NODE_SET.equals(type))
		{
			return XPathConstants.NODESET;
		}
		else if(SemanticActionsKeyWords.NODE.equals(type))
		{
			return XPathConstants.NODE;
		}
		return null;
	}

	/**
	 * @param type the type to set
	 */
	public final void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the comment
	 */
	public final String getComment()
	{
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public final void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the node
	 */
	public final String getNode()
	{
		return contextNode;
	}

	/**
	 * @param node the node to set
	 */
	public final void setNode(String node)
	{
		this.contextNode = node;
	}

	/**
	 * @return the scalarType
	 */
	public ScalarType getScalarType()
	{
		return scalarType;
	}

	/**
	 * @param scalarType the scalarType to set
	 */
	public void setScalarType(ScalarType scalarType)
	{
		this.scalarType = scalarType;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	
}
