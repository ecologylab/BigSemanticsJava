/**
 * 
 */
package ecologylab.documenttypes;

import java.io.IOException;
import java.util.HashMap;

import ecologylab.generic.ReflectionTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionParameters;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.types.element.ArrayListState;

/**
 * This class is used for directly biniding the XML document with metadata classes to build metadata
 * objects.
 * 
 * @author amathur
 * 
 */
public class MetaMetadataDirectBindingType<M extends MetadataBase, SA extends SemanticAction>
		extends MetaMetadataDocumentTypeBase
{

	public MetaMetadataDirectBindingType(SemanticActionHandler semanticActionHandler,
			InfoCollector infoCollector)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public M buildMetadataObject()
	{
		M populatedMetadata = null;
		initailizeMetadataObjectBuilding();
		if (metaMetadata.isSupported(container.purl()))
		{
			try
			{
				// if no xpath variables, then do this directly.
				populatedMetadata = (M) ElementState.translateFromXML(inputStream(),
						getMetadataTranslationScope());
			}
			catch (XMLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return populatedMetadata;
	}

	void createDOMandParse(ParsedURL purl)
	{
		org.w3c.dom.Document document	= ElementState.buildDOM(purl);
		
		try
		{
			// if no xpath variables, then do this directly.
			ElementState.translateFromXMLDOM(document, getMetadataTranslationScope());
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
