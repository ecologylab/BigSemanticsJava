/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import java.util.ArrayList;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

/**
 * @author andruid
 *
 */
public class NewMmTest extends Debug
implements DispatchTarget<DocumentClosure>
{
	ArrayList<DocumentClosure>	documentCollection	= new ArrayList<DocumentClosure>();

	int currentResult;
	
	/**
	 * 
	 */
	public NewMmTest()
	{
		// TODO Auto-generated constructor stub
	}

	public void collect(String[] urlStrings)
	{
		NewInfoCollector infoCollector = new NewInfoCollector(GeneratedMetadataTranslationScope.get());
		
		// seed start urls
		for (int i = 0; i < urlStrings.length; i++)
		{
			if ("//".equals(urlStrings[i]))
			{
				System.err.println("Terminate due to //");
				break;
			}
			ParsedURL thatPurl	= ParsedURL.getAbsolute(urlStrings[i]);
			Document document		= infoCollector.getOrConstructDocument(thatPurl);
			DocumentClosure documentClosure = document.getOrConstructClosure();
			if (documentClosure != null)	// super defensive -- make sure its not malformed or null or otherwise a mess
				documentCollection.add(documentClosure);
		}

		DownloadMonitor downloadMonitor	= null;
		// process documents after parsing command line so we now how many are really coming
		for (DocumentClosure documentClosure: documentCollection)
		{
			documentClosure.setDispatchTarget(this);
			if (downloadMonitor == null)
				downloadMonitor		= documentClosure.downloadMonitor();
			documentClosure.queueDownload();
		}
		downloadMonitor.requestStop();
	}

	public static void main(String[] args)
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		NewMmTest mmTest	= new NewMmTest();
		mmTest.collect(args);
	}
	
	@Override
	public void delivery(DocumentClosure incomingClosure)
	{
		if (++currentResult == documentCollection.size())
		{
			System.out.println("\n\n");
			for (DocumentClosure documentClosure : documentCollection)
			{
				Document document	= documentClosure.getDocument();
				try
				{
					document.serialize(System.out, FORMAT.XML);
					System.out.println("\n");
				}
				catch (SIMPLTranslationException e)
				{
					error("Could not serialize " + document);
					e.printStackTrace();
				}
			}
		}
	}
}
