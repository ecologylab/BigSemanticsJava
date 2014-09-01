package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.metadata.builtins.RichDocument;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;

/**
 * This is a parser for general HTML pages that does not do anything, as the default parsing action.
 * 
 * @author quyin
 */
public class DefaultHTMLDOMParser extends ParserBase<RichDocument>
{

  @Override
  public Document populateMetadata(Document document,
                                   MetaMetadataCompositeField metaMetadata,
                                   org.w3c.dom.Document dom,
                                   SemanticActionHandler handler) throws IOException
  {
		recursiveExtraction(metaMetadata, document, dom, null, handler.getSemanticActionVariableMap());
    return document;
  }

}
