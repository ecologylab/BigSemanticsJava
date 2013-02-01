/**
 * 
 */
package ecologylab.bigsemantics.tools;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.NodeList;

/**
 * @author amathur
 *
 */
public class GenericIterable implements Iterable
{
	
	Object collectionObject;
	
	public GenericIterable(Object collectionObject)
	{
		this.collectionObject=collectionObject;
	}



	@Override
	public Iterator iterator()
	{
		// if this is an instance of NodeList
		if(collectionObject instanceof NodeList )
		{
			// need to do some thing
			return  new NodeListIterator((NodeList)collectionObject);
		}
		else
		{
			// the simple case :-)
			return ((Iterable)collectionObject).iterator();
		}
	}
	
	public int size()
		{
			if(collectionObject instanceof NodeList)
			{
				return ((NodeList)collectionObject).getLength();
			}
			else
				return ((ArrayList)collectionObject).size();
		}
			
	 public Object get(int i)
	 {
		 if(collectionObject instanceof NodeList)
			{
				return ((NodeList)collectionObject).item(i);
			}
			else
				return ((ArrayList)collectionObject).get(i);
		}
	 }


	
	
	
	


