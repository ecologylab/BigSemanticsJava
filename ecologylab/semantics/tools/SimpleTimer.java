/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * a simple timer. it will record timing information in a local file. a timer can record multiple
 * entries for different objects.
 * 
 * note that this timer runs in the same thread as the caller, so there might be overhead. use this
 * only when you don't need much accuracy.
 * 
 * @author quyin
 * 
 */
public class SimpleTimer
{

	private BufferedWriter		out;

	private Map<Object, Long>	timeTable	= new HashMap<Object, Long>();

	private SimpleTimer(String recordFilePath) throws IOException
	{
		out = new BufferedWriter(new FileWriter(recordFilePath));
	}

	/**
	 * start timing on a particular object.
	 * 
	 * @param obj
	 */
	public synchronized void startTiming(Object obj)
	{
		timeTable.put(obj, System.currentTimeMillis());
	}

	/**
	 * finish timing on a particular object, and record interval into file.
	 * 
	 * @param obj
	 */
	public synchronized void finishTiming(Object obj)
	{
		long t1 = System.currentTimeMillis();
		if (timeTable.containsKey(obj))
		{
			long t0 = timeTable.get(obj);
			long interval = t1 - t0;
			try
			{
				out.write(String.format("timing %s: %d ms.\n", obj, interval));
			}
			catch (IOException e)
			{
				System.err.println("error finishing timing " + obj + ": " + e.getMessage());
			}
			finally
			{
				timeTable.remove(obj);
			}
		}
	}

	static private Map<String, SimpleTimer>	theMap	= new HashMap<String, SimpleTimer>();

	/**
	 * create or retrieve a timer using given file path.
	 * 
	 * @param recordFilePath
	 * @return
	 * @throws IOException
	 */
	static public SimpleTimer get(String recordFilePath) throws IOException
	{
		if (theMap.containsKey(recordFilePath))
			return theMap.get(recordFilePath);
		SimpleTimer pm = new SimpleTimer(recordFilePath);
		theMap.put(recordFilePath, pm);
		return pm;
	}

	/**
	 * close a particular timer. must be called after use.
	 * 
	 * @param recordFilePath
	 * @throws IOException
	 */
	static public void close(String recordFilePath) throws IOException
	{
		if (theMap.containsKey(recordFilePath))
		{
			theMap.get(recordFilePath).out.flush();
			theMap.get(recordFilePath).out.close();
			theMap.remove(recordFilePath);
		}
	}

	/**
	 * close all timers. must be called if you haven't close each timer manually.
	 * 
	 * @throws IOException
	 */
	static public void closeAll() throws IOException
	{
		for (String path : theMap.keySet())
		{
			theMap.get(path).out.flush();
			theMap.get(path).out.close();
		}
		theMap.clear();
	}

}
