package ecologylab.semantics.tools;

import java.util.Iterator;

import org.w3c.dom.NodeList;

/**
 * 
 * @author amathur
 *
 */
public class NodeListIterator implements Iterator
{
	
	private NodeList nodeList;
	
	// counter for the list
	private int count;
	

	public NodeListIterator(NodeList collectionObject)
	{
		this.nodeList = collectionObject;
		this.count=0;
	}


	@Override
	public boolean hasNext()
	{
		if(nodeList.getLength()>0)
		{
			return true;
		}
		return false;
	}
	

	@Override
	public Object next()
	{
		Object returnValue= nodeList.item(count);
		count++;
		return returnValue;
	}


	@Override
	public void remove()
	{
	 // TODO implement me
		
	}

}
