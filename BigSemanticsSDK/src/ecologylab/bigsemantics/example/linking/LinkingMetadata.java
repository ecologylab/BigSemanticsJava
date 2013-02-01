package ecologylab.bigsemantics.example.linking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.generated.library.scholarlyArticle.ScholarlyArticle;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.output.HtmlRenderer;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;

public class LinkingMetadata implements Continuation<DocumentClosure>
{

	private List<ScholarlyArticle>	collection	= new ArrayList<ScholarlyArticle>();

	private int											count				= 0;

	private Object									countLock		= new Object();
	
	HtmlRenderer 										renderer;

	public void collect(List<String> urls) throws IOException
	{
		String title = "Linking ACM Portal and CiteSeerX";
		String header = title;
		String styleSheet = "linking_metadata.css";
		String javascript = "linking_metadata.js";
		renderer = new HtmlRenderer(new File("linking_metadata.html"), title, header, styleSheet, javascript);
		if (!renderer.isBad())
		{
			count = urls.size();
	
			// create the infoCollector
			SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), CybernekoWrapper.class);
	
			// seed start urls
			for (String url : urls)
			{
				ParsedURL seedUrl = ParsedURL.getAbsolute(url);
				Document doc = infoCollector.getOrConstructDocument(seedUrl);
				doc.queueDownload(this);
			}
		}
	}

	@Override
	public void callback(DocumentClosure closure)
	{
		Document doc = closure.getDocument();
		if (doc != null)
			if (doc.getMetaMetadata().getName().equals("acm_portal"))
				collection.add((ScholarlyArticle) doc);

		synchronized (countLock)
		{
			count--;
			if (count == 0)
			{
				for (ScholarlyArticle article : collection)
					renderer.appendMetadata(article);
				
				renderer.close();
				closure.getSemanticsScope().getDownloadMonitors().stop(false);
			}
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
					if (line.length() > 0 && !line.startsWith("#"))
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
