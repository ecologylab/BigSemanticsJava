package ecologylab.semantics.metametadata.test;

import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.Format;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
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
			ClassDescriptor.serialize(incomingClosure, outputStream, Format.BIBTEX);
			
		else if (++currentResult == documentCollection.size())
		{
			System.out.println("\n\n");
			for (DocumentClosure documentClosure : documentCollection)
				try
				{
					ClassDescriptor.serialize(incomingClosure, System.out, StringFormat.BIBTEX);
				}
				catch (SIMPLTranslationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			semanticsSessionScope.getDownloadMonitors().stop(false);
		}
	}
}
