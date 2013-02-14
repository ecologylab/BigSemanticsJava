package ecologylab.bigsemantics.metametadata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class TestFieldParserForNsfPubList
{

	@Test
	public void testRemovingHtmlTag()
	{
	  FieldParserForNsfPubList fp = new FieldParserForNsfPubList();
	  
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader("data/testRemovingHtmlTag.html"));
			StringBuilder sb = new StringBuilder();
			while (true)
			{
				String line = br.readLine();
				if (line == null)
					break;
				sb.append(line).append("\n");
			}
			String input = sb.toString();
			
			fp.getCollectionResult(null, input);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
