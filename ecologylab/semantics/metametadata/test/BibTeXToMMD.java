package ecologylab.semantics.metametadata.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

public class BibTeXToMMD extends NewMmTest
{

	public BibTeXToMMD(OutputStream outputStream)
	{
		super(outputStream);
	}
	
	public static void main(String[] args)
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		BibTeXToMMD mmTest	= new BibTeXToMMD(System.out);
		mmTest.collect(args);
	}
	
	@Override
	public void callback(DocumentClosure incomingClosure)
	{
		if (outputOneAtATime)
			incomingClosure.serialize(outputStream, FORMAT.BIBTEX);
		else if (++currentResult == documentCollection.size())
		{
			System.out.println("\n\n");
			for (DocumentClosure documentClosure : documentCollection)
				documentClosure.serialize(System.out, FORMAT.BIBTEX);
			downloadMonitor.stop();
		}
	}
}
