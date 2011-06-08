/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import java.io.OutputStream;
import java.util.ArrayList;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.Debug;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

/**
 * @author andruid
 *
 */
public class NewMmTest extends Debug
implements Continuation<DocumentClosure>
{
	ArrayList<DocumentClosure>	documentCollection	= new ArrayList<DocumentClosure>();

	int 					currentResult;
	
	boolean				outputOneAtATime;
	
	OutputStream	outputStream;

	DownloadMonitor downloadMonitor	= null;

	/**
	 * 
	 */
	public NewMmTest(OutputStream	outputStream)
	{
		this.outputStream	= outputStream;
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

		// process documents after parsing command line so we now how many are really coming
		for (DocumentClosure documentClosure: documentCollection)
		{
			documentClosure.setDispatchTarget(this);
			if (downloadMonitor == null)
				downloadMonitor		= documentClosure.downloadMonitor();
			documentClosure.queueDownload();
		}
	}

	public static void main(String[] args)
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		NewMmTest mmTest	= new NewMmTest(System.out);
		mmTest.collect(args);
	}
	
	@Override
	public void callback(DocumentClosure incomingClosure)
	{
		if (outputOneAtATime)
			incomingClosure.serialize(outputStream);
		else if (++currentResult == documentCollection.size())
		{
			System.out.println("\n\n");
			for (DocumentClosure documentClosure : documentCollection)
				documentClosure.serialize(System.out);
			downloadMonitor.stop();
		}
	}
}
