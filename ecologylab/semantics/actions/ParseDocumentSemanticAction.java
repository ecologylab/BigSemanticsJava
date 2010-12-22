/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;

import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * @author amathur
 * 
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.PARSE_DOCUMENT)
class ParseDocumentSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH>
{

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected boolean	now	= false;

	/**
	 * This attribute is meant to be used when we only require the top document to actually be sent 
	 * to the infoCollector. It requires two strings 
	 */
	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected boolean onlyPickTopDocuments = false;
	
	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected int numberOfTopDocuments = 1;
	
	public boolean isNow()
	{
		return now;
	}
	
	public boolean onlyPickTopDocument()
	{
		return onlyPickTopDocuments;
	}

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.PARSE_DOCUMENT;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		Container container = semanticActionHandler.createContainer(this, documentParser, infoCollector);
		if (container != null)
		{
			container.queueDownload();
//			infoCollector.getContainerDownloadIfNeeded(documentParser.getContainer(), null, null, container.purl(),
//					null, false, false, false);
		}
		return null;
	}

}
