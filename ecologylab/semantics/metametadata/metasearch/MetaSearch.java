package ecologylab.semantics.metametadata.metasearch;

import java.io.File;

import ecologylab.semantics.metadata.output.HtmlRenderer;

public class MetaSearch
{

	public static String[]	engines	= {
//																	"http://www.bing.com/search?q=", // temporarily commented out.
																	"http://www.tumblr.com/tagged/",
																	"http://www.google.com/search?hl=en&q=",
																	"http://slashdot.com/index2.pl?fhfilter=",
																	"http://www.delicious.com/search?p=",
//																	"http://buzz.yahoo.com/search?p=", // yahoo buzz shut down
																	};

	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.err.println("args: <query> <output-html-file-path>");
			System.exit(-1);
		}
		
		String query = args[0];
		String outFilePath = args[1];

		String term = query.replace(' ', '+');
		String[] urls = new String[engines.length];
		for (int i = 0; i < engines.length; ++i)
		{
			urls[i] = engines[i] + term;
		}


		HtmlRenderer renderer = new HtmlRenderer(new File(outFilePath), "Search results", "Search results for <i>" + query + "</i>");

		if (!renderer.isBad())
		{
			SearchDispatcher searchDispatcher = new SearchDispatcher(renderer);
			searchDispatcher.search(urls);
		}
	}

}
