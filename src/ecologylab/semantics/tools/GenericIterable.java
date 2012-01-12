/**
 * 
 */
package ecologylab.semantics.tools;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;

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



	public Iterator iterator()
	{
		// if this is an instance of NodeList
		if(collectionObject instanceof DTMNodeList )
		{
			// need to do some thing
			return  new DTMNodeListIterator((DTMNodeList)collectionObject);
		}
		else
		{
			// the simple case :-)
			return ((Iterable)collectionObject).iterator();
		}
	}
	
	public int size()
		{
			if(collectionObject instanceof DTMNodeList)
			{
				return ((DTMNodeList)collectionObject).getLength();
			}
			else
				return ((ArrayList)collectionObject).size();
		}
			
	 public Object get(int i)
	 {
		 if(collectionObject instanceof DTMNodeList)
			{
				return ((DTMNodeList)collectionObject).item(i);
			}
			else
				return ((ArrayList)collectionObject).get(i);
		}
	 }


	
	
	
	


