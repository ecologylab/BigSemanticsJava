package ecologylab.bigsemantics.example.metasearch;

import java.io.File;
import java.util.Arrays;

import ecologylab.bigsemantics.metadata.output.HtmlRenderer;

public class MetaSearch
{

	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.err.println("args: <output-html-file-path> <search-url> [<search-url> ...]");
			System.exit(-1);
		}
		
		String outFilePath = args[0];
		int end = 1;
		for (; end < args.length && !args[end].equals("//"); ++end)
			;
		String[] urls = Arrays.copyOfRange(args, 1, end);
		
		HtmlRenderer renderer = new HtmlRenderer(new File(outFilePath), "Search results", "Search results");
		if (!renderer.isBad())
		{
			SearchDispatcher searchDispatcher = new SearchDispatcher(renderer);
			searchDispatcher.search(urls);
		}
	}

}
