package ecologylab.semantics.metametadata.metasearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ecologylab.generic.Debug;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.generated.library.search.Search;
import ecologylab.semantics.generated.library.search.SearchResult;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.output.HtmlRenderer;

public class SearchDispatcher extends Debug implements Continuation<DocumentClosure>
{
	private HtmlRenderer	renderer;

	private Object				outputLock				= new Object();

	private int						totalCount;

	private int						finishedCount;

	private Object				countLock					= new Object();

	private List<Search>	searchResultPages	= new ArrayList<Search>();

	public SearchDispatcher(HtmlRenderer renderer)
	{
		this.renderer = renderer;
	}

	public void search(String[] urls)
	{
		// create the infoCollector
		SemanticsSessionScope infoCollector = new SemanticsSessionScope(GeneratedMetadataTranslationScope.get());

		// seed start urls
		totalCount = urls.length;
		for (int i = 0; i < totalCount; i++)
		{
			ParsedURL seedUrl = ParsedURL.getAbsolute(urls[i]);
			Document doc = infoCollector.getOrConstructDocument(seedUrl);
			doc.queueDownload(this);
		}
	}

	@Override
	public void callback(DocumentClosure closure)
	{
		synchronized (countLock)
		{
			finishedCount++;
		}

		Document doc = closure.getDocument();
		if (doc == null)
		{
			warning("NULL document for: " + closure);
			return;
		}

		synchronized (outputLock)
		{
			if (doc instanceof Search)
			{
				Search search = (Search) doc;

				ArrayList<SearchResult> searchResults = search.getSearchResults();
				int numSearchResults = searchResults == null ? 0 : searchResults.size();

				debug("\nSearch results[" + numSearchResults +"] from: " + search.getLocation() + "\n");

				if (searchResults != null)
				{
					searchResultPages.add(search);
				}
			}
		}

		synchronized (countLock)
		{
			if (finishedCount == totalCount)
			{
				int i = 0;
				boolean resultRendered = true;
				while (resultRendered)
				{
					resultRendered = false;
					for (Search search : searchResultPages)
					{
						int numSearchResults = search.getSearchResults().size();
						if (i < numSearchResults)
						{
							SearchResult result = search.getSearchResults().get(i);
							if (!result.isNullHeading())
							{
								result.setEngine(search.getLocation().domain());
								renderer.appendMetadata(result);
								resultRendered = true;
							}
						}
					}
					i++;
				}
				renderer.close();
				closure.getSemanticsSessionScope().getDownloadMonitors().stop(false);
			}
		}
	}
}
