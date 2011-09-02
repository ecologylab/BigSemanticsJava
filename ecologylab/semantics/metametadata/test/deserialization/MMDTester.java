package ecologylab.semantics.metametadata.test.deserialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

public class MMDTester
{
	static
	{
		MetaMetadataRepository.initializeTypes();
	}

	private static final TranslationScope	META_METADATA_TRANSLATIONS	= RepositoryMetadataTranslationScope
																																				.get();

	public static void main(String[] args) throws IOException
	{
		System.out.println("Enter mmd string:");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String s;
		s = in.readLine();
		
		try
		{
			ElementState deserializeCharSequence = META_METADATA_TRANSLATIONS.deserializeCharSequence(s, FORMAT.JSON);
			System.out.println("Deserialized");
		}
		catch (SIMPLTranslationException e)
		{
			System.err.println("Deserialization failed!");
			e.printStackTrace();
		}
		
    
	}

}
