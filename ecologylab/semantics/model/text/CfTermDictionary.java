package ecologylab.semantics.model.text;

import java.io.FileNotFoundException;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.io.Assets;

public class CfTermDictionary extends XTermDictionary implements ApplicationProperties
{

	static String DICTIONARY = "dictionary";

	public static void download(float dictionaryAssetVersion)
	{
		Assets.downloadSemanticsZip(DICTIONARY, null, !USE_ASSETS_CACHE,
				dictionaryAssetVersion);
		try
		{
			createDictionary(Assets.getSemanticsFile(DICTIONARY + "/dictionary.yaml"));
		} catch (FileNotFoundException e)
		{
			System.err.println("Error: cannot find dictionary file.");
		}
	}
}
