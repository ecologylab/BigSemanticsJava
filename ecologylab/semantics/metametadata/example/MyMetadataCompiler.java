package ecologylab.semantics.metametadata.example;

import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.xml.XMLTranslationException;

/**
 * Use the MetadataCompiler class to compile an meta-metadata repository. The generated class
 * definitions will go to the default location.
 * 
 * @author quyin
 * 
 */
public class MyMetadataCompiler
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		try
		{
			// use the default repository location
			// needed to provide this!!
			MetadataCompiler compiler = new MetadataCompiler(args);
			compiler.compile(".");
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
