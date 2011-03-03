/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.html.documentstructure.LinkType;
import ecologylab.semantics.metametadata.MetaMetadata;
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
	protected boolean	now										= false;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected LinkType linkType									= LinkType.WILD;
	
	/**
	 * This attribute is meant to be used when we only require the top document to actually be sent to
	 * the infoCollector. It requires two strings
	 */
	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected boolean	onlyPickTopDocuments	= false;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	protected int			numberOfTopDocuments	= 1;

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
		ParsedURL purl = (ParsedURL) getArgumentObject(SemanticActionNamedArguments.CONTAINER_LINK);
		if (purl != null)
		{
			Container ancestor = documentParser.getContainer();
			MetaMetadata mmd = infoCollector.metaMetaDataRepository().getDocumentMM(purl);
			Container container = infoCollector.getContainer(ancestor, null, mmd, purl, false, true, false);
			if (container != null)
				container.queueDownload();
		}
		return null;
	}

}
