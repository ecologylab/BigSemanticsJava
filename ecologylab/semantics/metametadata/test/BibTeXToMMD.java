package ecologylab.semantics.metametadata.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.example.MyContainer;
import ecologylab.semantics.metametadata.example.MyInfoCollector;
import ecologylab.serialization.SIMPLTranslationException;

public class BibTeXToMMD extends Debug
{
	public static void main(String[] args)
	{
		for (int i=0; i <args.length; i++)
		{
			String fileName = args[i];
			try
			{
				FileInputStream input = new FileInputStream(fileName);
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}
