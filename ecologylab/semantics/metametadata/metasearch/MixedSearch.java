package ecologylab.semantics.metametadata.metasearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class MixedSearch
{

	public static String[]	engines	= {
																	"http://www.bing.com/search?q=",
																	"http://www.google.com/search?hl=en&q=",
																	"http://slashdot.com/index2.pl?fhfilter=",
																	"http://www.tumblr.com/tagged/",
																	"http://buzz.yahoo.com/search?p=",
																	"http://delicious.com/search?p=",
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
		
		try
		{
			final PrintWriter writer = new PrintWriter(outFilePath);
			String title = "Search results";
			String header = "Search results for <i>" + query + "</i>";
			HtmlRenderer renderer = new HtmlRenderer(title, header, "mixed_search.css", "linking_metadata.js") {
				@Override
				public void appendFooter(Appendable a) throws IOException
				{
					super.appendFooter(a);
					writer.close();
				}
			};
			SearchDispatcher searchDispatcher = new SearchDispatcher(renderer, writer);
			searchDispatcher.search(urls);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("FileNotFoundException: " + e.getMessage());
		}
	}

}
