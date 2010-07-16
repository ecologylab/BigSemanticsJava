package ecologylab.semantics.metametadata.example.googleImage;

import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.serialization.SIMPLTranslationException;

public class TmpMetadataCompiler
{

	public static void main(String[] args) throws SIMPLTranslationException
	{
			MetadataCompiler compiler = new MetadataCompiler(args);
			compiler.compile("repoTest", ".");
	}
}