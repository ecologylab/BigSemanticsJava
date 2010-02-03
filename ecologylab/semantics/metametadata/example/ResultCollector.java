/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.util.ArrayList;

/**
 * @author quyin
 *
 */
public class ResultCollector {
	protected ResultCollector()
	{
		list = new ArrayList<Object>();
	}
	
	private static ResultCollector instance;
	public static ResultCollector get()
	{
		if (instance == null)
			instance = new ResultCollector();
		return instance;
	}

	private ArrayList<Object> list;
	public ArrayList<Object> list()
	{
		return list;
	}
	
	public void collect(Object obj)
	{
		list.add(obj);
	}
}
