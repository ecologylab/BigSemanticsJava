/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.generic.ValueFactory;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;

/**
 * @author andruid
 *
 */
public interface DocumentMapHelper<D extends Document> extends ValueFactory<ParsedURL, D>
{
	D recycledValue();
	
	D undefinedValue();

	public D constructValue(MetaMetadata mmd, ParsedURL key);
}
