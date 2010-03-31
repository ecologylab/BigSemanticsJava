package ecologylab.semantics.actions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StringPool
{
	private String					filePath;

	private BufferedWriter	writer;

	private StringPool(String filePath)
	{
		this.filePath = filePath;
		try
		{
			writer = new BufferedWriter(new FileWriter(filePath));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFilePath()
	{
		return filePath;
	}

	public synchronized void addLine(String s)
	{
		try
		{
			writer.write(s);
			writer.newLine();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close()
	{
		try
		{
			writer.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// singleton
	private static Map<String, StringPool>	pools	= new HashMap<String, StringPool>();

	public static StringPool get(String filePath)
	{
		if (!pools.containsKey(filePath))
		{
			pools.put(filePath, new StringPool(filePath));
		}
		return pools.get(filePath);
	}

	public static void closeAll()
	{
		for (String name : pools.keySet())
		{
			get(name).close();
		}
	}
}
