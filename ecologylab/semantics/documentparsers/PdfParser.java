/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.io.IOException;

import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;

/**
 * @author andruid
 *
 */
public class PdfParser extends DocumentParser<CompoundDocument>
{

	@Override
	public Document parse() throws IOException
	{
		warning("Not yet implemented in this environment.");
		return getDocument();
	}

}
