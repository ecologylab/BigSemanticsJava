package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.net.ParsedURL;

/**
 * Help handle document creation.
 * 
 * @author andruid
 */
public interface DocumentMapHelper<D extends Document> extends
    ConditionalValueFactory<ParsedURL, D>
{

  /**
   * Create a document using the given meta-metadata, and set its location using the 2nd parameter.
   * 
   * @param mmd
   * @param location
   * @return
   */
  D constructValue(MetaMetadata mmd, ParsedURL location);

  /**
   * @return A special object reference used to denote a recycled document.
   */
  D recycledValue();

  /**
   * @return A special object reference used to denote an undefined document.
   */
  D undefinedValue();

}
