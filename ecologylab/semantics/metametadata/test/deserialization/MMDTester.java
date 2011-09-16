package ecologylab.semantics.metametadata.test.deserialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.TranslationScope;

public class MMDTester
{
	static
	{
		MetaMetadataRepository.initializeTypes();
	}

	private static final TranslationScope	META_METADATA_TRANSLATIONS	= MetaMetadataTranslationScope.get();

	public static void main(String[] args) throws IOException
	{
		System.out.println("Enter mmd string:");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    String s;
		s = in.readLine();
		
		try
		{
			ElementState deserializeCharSequence = (ElementState) META_METADATA_TRANSLATIONS.deserialize(s, StringFormat.JSON);
			System.out.println("Deserialized");
		}
		catch (SIMPLTranslationException e)
		{
			System.err.println("Deserialization failed!");
			e.printStackTrace();
		}
		
    
	}

}
