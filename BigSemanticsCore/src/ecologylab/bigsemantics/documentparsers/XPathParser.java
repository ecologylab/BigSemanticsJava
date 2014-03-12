/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.actions.SemanticsConstants;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;

/**
 * @author amathur
 * 
 */
public class XPathParser extends ParserBase<Document> implements
    SemanticsConstants
{

  @Override
  public Document populateMetadata(Document document,
                                   MetaMetadataCompositeField metaMetadata,
                                   org.w3c.dom.Document DOM,
                                   SemanticActionHandler handler)
  {
    recursiveExtraction(metaMetadata, document, DOM, null, handler.getSemanticActionVariableMap());
    return document;
  }

}
