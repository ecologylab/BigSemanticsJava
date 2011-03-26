/**
 * 
 */
package ecologylab.semantics.connectors;

import ecologylab.generic.ValueFactory;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;

/**
 * @author andruid
 *
 */
public interface DocumentMapHelper<D extends Document> extends ValueFactory<ParsedURL, D>
{
	D recycledValue();
	
	D undefinedValue();
	
}
