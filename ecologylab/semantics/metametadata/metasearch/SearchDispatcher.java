package ecologylab.semantics.metametadata.metasearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ecologylab.generic.Debug;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.generated.library.search.Search;
import ecologylab.semantics.generated.library.search.SearchResult;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

public class SearchDispatcher extends Debug implements Continuation<DocumentClosure>
{

	private HtmlRenderer	renderer;

	private Appendable		appendable;

	private Object				outputLock				= new Object();

	private int						totalCount;

	private int						finishedCount;

	private Object				countLock					= new Object();

	private List<Search>	searchResultPages	= new ArrayList<Search>();

	public SearchDispatcher(HtmlRenderer renderer, Appendable appendable)
	{
		this.renderer = renderer;
		this.appendable = appendable;
	}

	public void search(String[] urls)
	{
		// create the infoCollector
		NewInfoCollector infoCollector = new NewInfoCollector(GeneratedMetadataTranslationScope.get());

		// seed start urls
		totalCount = urls.length;
		for (int i = 0; i < urls.length; i++)
		{
			ParsedURL seedUrl = ParsedURL.getAbsolute(urls[i]);
			Document doc = infoCollector.getOrConstructDocument(seedUrl);
			doc.queueDownload(this);
		}
		infoCollector.requestStopDownloadMonitors();
	}

	@Override
	public void callback(DocumentClosure closure)
	{
		synchronized (countLock)
		{
			finishedCount++;
			if (finishedCount == 1)
			{
				try
				{
					renderer.appendHeader(appendable);
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Document doc = closure.getDocument();
		if (doc == null)
		{
			warning("null document for: " + closure);
			return;
		}

		synchronized (outputLock)
		{
			if (doc instanceof Search)
			{
				Search search = (Search) doc;

				// for debug
				debug("\n\n\nSearch results from: " + search.getLocation() + "\n\n\n");

				if (search.getSearchResults() != null)
				{
					// for debug
					debug("\n\n\nNumber of search result: " + search.getSearchResults().size() + "\n\n\n");

					searchResultPages.add(search);
				}
			}
		}

		synchronized (countLock)
		{
			if (finishedCount == totalCount)
			{
				try
				{
					int i = 0;
					boolean resultRendered = true;
					while (resultRendered)
					{
						resultRendered = false;
						for (Search search : searchResultPages)
						{
							if (i < search.getSearchResults().size())
							{
								SearchResult result = search.getSearchResults().get(i);
								if (!result.isNullHeading())
								{
									result.setEngine(search.getLocation().domain());
									try
									{
										renderer.appendItem(result, appendable);
										resultRendered = true;
									}
									catch (IOException e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
						i++;
					}
					renderer.appendFooter(appendable);
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
