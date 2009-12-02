/**
 * 
 */
package ecologylab.documenttypes;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.seeding.Feed;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTranslationException;

/**
 * @author andruid
 * 
 */
public class MetaMetadataFeedParser
		extends MetaMetadataLinksetParser
{

	public MetaMetadataFeedParser(InfoCollector infoCollector)
	{
		super(infoCollector);
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataFeedParser(InfoCollector infoCollector, SemanticActionHandler semanticActionHandler,
			Feed feed)
	{
		super(infoCollector, semanticActionHandler);
		getMetaMetadataAndContainerAndQueue(infoCollector,feed.purl(),feed,null);
	}

	@Override
	public Document populateMetadataObject()
	{
		Document populatedMetadata = container.metadata();
		
		ParsedURL purl = container.purl();
		if(metaMetadata.isSupported(purl))
		{
			try
			{
				populatedMetadata = (Document) ElementState.translateFromXML(inputStream(), getMetadataTranslationScope());
			}
			catch (XMLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return populatedMetadata;
	}
	
	

}
