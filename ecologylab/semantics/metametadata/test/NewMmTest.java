/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * @author andruid
 *
 */
public class NewMmTest extends Debug
implements DispatchTarget<DocumentClosure>
{

	/**
	 * 
	 */
	public NewMmTest()
	{
		// TODO Auto-generated constructor stub
	}

	public void collect(String[] urlStrings)
	{
		// create the infoCollector
//		MetaMetadataRepository repository = MetaMetadataRepository.load(new File(
//				"../ecologylabSemantics/repository"));
		NewInfoCollector infoCollector = new NewInfoCollector(GeneratedMetadataTranslationScope.get());

		// seed start urls
		DownloadMonitor downloadMonitor	= null;
		for (int i = 0; i < urlStrings.length; i++)
		{
			if ("//".equals(urlStrings[i]))
			{
				System.err.println("Terminate due to //");
				break;
			}
			ParsedURL thatPurl	= ParsedURL.getAbsolute(urlStrings[i]);
			Document document		= infoCollector.getOrConstructDocument(thatPurl);
			DocumentClosure documentClosure	= document.getOrConstructClosure();
			documentClosure.setDispatchTarget(this);
			if (downloadMonitor == null)
				downloadMonitor		= documentClosure.downloadMonitor();
			documentClosure.queueDownload();
		}
		downloadMonitor.requestStop();
	}

	public static void main(String[] args)
	{
		NewMmTest mmTest	= new NewMmTest();
		mmTest.collect(args);
	}
	
	@Override
	public void delivery(DocumentClosure documentClosure)
	{
		Document document	= documentClosure.getDocument();
		try
		{
			document.serialize(System.out);
		}
		catch (SIMPLTranslationException e)
		{
			error("Could not serialize " + document);
			e.printStackTrace();
		}
	}
}
