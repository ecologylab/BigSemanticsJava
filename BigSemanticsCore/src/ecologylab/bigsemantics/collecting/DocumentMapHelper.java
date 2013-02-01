/**
 * 
 */
package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.net.ParsedURL;

/**
 * @author andruid
 *
 */
public interface DocumentMapHelper<D extends Document> extends ConditionalValueFactory<ParsedURL, D>
{
	D recycledValue();
	
	D undefinedValue();

	public D constructValue(MetaMetadata mmd, ParsedURL key);
}
