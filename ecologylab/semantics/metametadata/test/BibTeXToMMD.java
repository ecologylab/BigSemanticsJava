package ecologylab.semantics.metametadata.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import ecologylab.generic.Debug;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

public class BibTeXToMMD extends NewMmTest
{

	public BibTeXToMMD() throws SIMPLTranslationException
	{
		super("BibTeXToMMD");
	}
	
	public static void main(String[] args)
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		BibTeXToMMD mmTest;
		try
		{
			mmTest = new BibTeXToMMD();
			mmTest.collect(args);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
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
			semanticsSessionScope.getDownloadMonitors().stop(false);
		}
	}
}
