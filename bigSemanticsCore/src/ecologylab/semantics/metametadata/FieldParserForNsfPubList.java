package ecologylab.semantics.metametadata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ecologylab.generic.StringTools;
import ecologylab.serialization.XMLTools;

public class FieldParserForNsfPubList extends FieldParserForAcmReferences
{
	
	@Override
	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement,
			String input)
	{
		input = input.replaceAll("\\s+", " ");
		input = StringTools.trimUntil(input, "</table>", true);
		input = StringTools.trimAfter(input, "</p>", true);
		
		input = XMLTools.removeHtmlTag(input, "script", false);
		input = XMLTools.removeHtmlTag(input, "noscript", false);
		input = XMLTools.removeHtmlTag(input, "i", true);
		
		String[] splits = input.split("(\\s*<br\\s*/?>\\s*)+");
		List<Map<String, String>> results = new ArrayList<Map<String,String>>();
		for (String split : splits)
		{
			if (split == null)
				continue;
			split = split.trim();
			if (split.length() == 0)
				continue;
			if (split.startsWith("<")) // must be some tag
				continue;
			
			split = XMLTools.unescapeXML(split);
			split = split.replaceFirst("^\\d+\\.\\s*", "");
			Map<String, String> parsedRef = super.getKeyValuePairResult(null, split);
			if (parsedRef != null && parsedRef.size() > 0)
			{
				System.out.println(parsedRef);
				results.add(parsedRef);
			}
		}
		
		return results;
	}
	
	@Test
	public void testRemovingHtmlTag()
	{
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
			
			getCollectionResult(null, input);
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
