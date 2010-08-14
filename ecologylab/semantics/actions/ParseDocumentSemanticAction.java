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

	public boolean isNow()
	{
		return now;
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
		if (isNow())
		{
			parseDocumentNow();
		}
		else
		{
			parseDocumentLater();
		}
		return null;
	}

	/**
	 * This action downloads the document from a given URL, parses it with specified parsing method,
	 * and put the results in a Container which can be used later.
	 */
	protected void parseDocumentNow()
	{
		Container container = semanticActionHandler
				.createContainer(this, documentParser, infoCollector);
		if (container != null)
		{
			try
			{
				container.performDownload();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * add a URL to a waiting list. URLs in the waiting list is weighted based on contents, links,
	 * user interests, etc.
	 */
	protected void parseDocumentLater()
	{
		Container container = semanticActionHandler
				.createContainer(this, documentParser, infoCollector);
		if (container != null)
		{
			infoCollector.getContainerDownloadIfNeeded(documentParser.getContainer(), container.purl(),
					null, false, false, false);
		}
	}

}
