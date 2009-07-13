/**
 * 
 */
package ecologylab.documenttypes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionParameters;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.library.scalar.MetadataStringBuilder;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.types.element.ArrayListState;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * @author amathur
 * 
 */
public class MetaMetadataXPathType<M extends MetadataBase,SA extends SemanticAction> extends MetaMetadataDocumentTypeBase
{

	private XPath	xpath;

	private Tidy	tidy;

	public MetaMetadataXPathType(InfoCollector infoCollector)
	{
		super(infoCollector);
		tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath = XPathFactory.newInstance().newXPath();
	}

	public MetaMetadataXPathType(SemanticActionHandler semanticActionHandler,
			InfoCollector infoCollector)
	{
		super(infoCollector, semanticActionHandler);
		tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath = XPathFactory.newInstance().newXPath();
	}

	@Override
	public M buildMetadataObject()
	{
		M populatedMetadata = null;
		initailizeMetadataObjectBuilding();
		if (metaMetadata.isSupported(container.purl()))
		{
			Document tidyDOM = tidy.parseDOM(inputStream(), /*System.out*/null);
			populatedMetadata = (M) recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					(M) getMetadata(), tidyDOM, xpath);
		}
		return populatedMetadata;
	}

}
