package ecologylab.semantics.metametadata.test;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.cyberneko.CybernekoWrapper;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestReselect
{

	static void download(ParsedURL purl, SemanticsSessionScope infoCollector)
	{
		Document doc = infoCollector.getOrConstructDocument(purl);
		doc.queueDownload();
	}

	public static void main(String[] args)
	{
		ParsedURL url1 = ParsedURL.getAbsolute("http://www.amazon.com/gp/product/1118013689/"); // book
		ParsedURL url2 = ParsedURL.getAbsolute("http://www.amazon.com/gp/product/B004Z6NWAU"); // electronic

		SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), CybernekoWrapper.class);
		download(url1, infoCollector);
		download(url2, infoCollector);

		try
		{
			Thread.sleep(5000); // waiting for finish
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		infoCollector.getDownloadMonitors().stop(false);

		try
		{
			SimplTypesScope.serialize(infoCollector.getOrConstructDocument(url1), System.out, StringFormat.XML);
			
			System.out.println();
			SimplTypesScope.serialize(infoCollector.getOrConstructDocument(url2), System.out, StringFormat.XML);
			
			System.out.println();
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.flush();
	}

}
