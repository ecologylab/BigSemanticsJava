package ecologylab.semantics.metametadata.linking;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.generated.library.scholarlyPublication.ScholarlyArticle;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metametadata.metasearch.HtmlRenderer;

public class LinkingMetadata implements DispatchTarget<DocumentClosure>
{

	private List<ScholarlyArticle>	collection	= new ArrayList<ScholarlyArticle>();

	private int											count				= 0;

	private Object									countLock		= new Object();

	public void collect(List<String> urls) throws IOException
	{
		count = urls.size();

		// create the infoCollector
		NewInfoCollector infoCollector = new NewInfoCollector(GeneratedMetadataTranslationScope.get());

		// seed start urls
		for (String url : urls)
		{
			ParsedURL seedUrl = ParsedURL.getAbsolute(url);
			Document doc = infoCollector.getOrConstructDocument(seedUrl);
			doc.queueDownload(this);
		}
		NewInfoCollector.CRAWLER_DOWNLOAD_MONITOR.requestStop();

		PrintWriter writer = new PrintWriter("linking_metadata.html");

		synchronized (countLock)
		{
			if (count > 0)
			{
				try
				{
					countLock.wait(1000 * 60 * 60 * 14); // wait for 14 hours at most
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		try
		{
			Thread.sleep(1000 * 30); // wait for half a minute for possible linking
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String title = "Linking ACM Portal and CiteSeerX";
		String header = title;
		String styleSheet = "linking_metadata.css";
		String javascript = "linking_metadata.js";
		HtmlRenderer renderer = new HtmlRenderer(title, header, styleSheet, javascript);
		renderer.appendHeader(writer);
		for (ScholarlyArticle article : collection)
			renderer.appendItem(article, writer);
		renderer.appendFooter(writer);
		writer.close();
	}

	@Override
	public void delivery(DocumentClosure closure)
	{
		Document doc = closure.getDocument();
		if (doc != null)
			if (doc.getMetaMetadata().getName().equals("acm_portal"))
				collection.add((ScholarlyArticle) doc);

		synchronized (countLock)
		{
			count--;
			if (count == 0)
				countLock.notify();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		if (args.length <= 0)
		{
			System.err.println("args: <url-to-a-resource> | -l <path-to-a-text-file-listing-urls>");
			System.exit(-1);
			return;
		}

		List<String> urls = new ArrayList<String>();
		int i = 0;
		while (i < args.length && args[i] != null && args[i].length() > 0)
		{
			if ("-l".equals(args[i]))
			{
				++i;
				String pathList = args[i];
				BufferedReader br = new BufferedReader(new FileReader(pathList));
				String line = null;
				while ((line = br.readLine()) != null)
				{
					line = line.trim();
					if (!line.isEmpty() && !line.startsWith("#"))
						urls.add(line.trim());
				}
				br.close();
			}
			else
			{
				urls.add(args[i]);
			}
			++i;
		}

		LinkingMetadata lm = new LinkingMetadata();
		lm.collect(urls);
	}

}
