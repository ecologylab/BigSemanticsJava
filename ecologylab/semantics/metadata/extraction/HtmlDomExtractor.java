package ecologylab.semantics.metadata.extraction;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.library.scalar.MetadataStringBuilder;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.types.scalar.ParsedURLType;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * Extracts Metadata from the AcmPortal pages using the xPath strings in the MetaMetadata.
 * 
 * @author bharat
 * 
 * @param <M>
 */
public class HtmlDomExtractor<M extends MetadataBase> extends Debug
{

	private String									domainString;

	private MetaMetadataRepository	metaMetaDataRepository;

	static Tidy											tidy;

	static XPath										xpath;

	public HtmlDomExtractor(MetaMetadataRepository metaMetaDataRepository)
	{
		tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath = XPathFactory.newInstance().newXPath();

		this.metaMetaDataRepository = metaMetaDataRepository;
	}

	
	/**
	 * Checks to see which HtmlDomExtractionPattern can be used and calls
	 * <code>extractMetadataWithPattern</code> using the appropriate patter
	 * @param purl
	 * @return
	 * @throws XmlTranslationException
	 */
	public M populateMetadata(M  metadata, ParsedURL purl, String domainString,TranslationScope translationScope)
	{
		MetaMetadata metaMetadata = metadata.getMetaMetadata();
		this.domainString = domainString;
		if(metaMetadata.isSupported(purl))
		{
			PURLConnection purlConnection 	= purl.connect(metaMetadata.getUserAgentString());
			Document tidyDOM 				= tidy.parseDOM(purlConnection.inputStream(), null /*System.out*/);
			M populatedMetadata 			= recursiveExtraction(translationScope, metaMetadata, metadata, tidyDOM, purl);

			return populatedMetadata;
		} else
		{
			println("Pattern supports only URLs starting with: "
					+ metaMetadata.getUrlBase());
		}

		return null;
		
	}
	
	/**
     * Extracts metadata using the tidyDOM from the purl.
     *
     * @param translationScope
     *          TODO
     * @param mmdField
     * @param metadata
     * @param tidyDOM
     * @param purl
     * @return
     */
    private M recursiveExtraction(TranslationScope translationScope, MetaMetadataField mmdField,
            M metadata, Document tidyDOM, ParsedURL purl)
    {

        // Gets the child metadata of the mmdField.
        HashMapArrayList<String, MetaMetadataField> mmdFieldSet = mmdField.getSet();

        // Traverses through the child metadata to populate.
        for (MetaMetadataField mmdElement : mmdFieldSet)
        {
            String xpathString = mmdElement.getXpath(); // Used to get the field value from the web page.
            String mmdElementName = mmdElement.getName();

            if (xpathString != null)
            {
                // this is a simple scalar field

                String evaluation = "";

                try
                {
                    evaluation = xpath.evaluate(xpathString, tidyDOM);
                }
                catch (XPathExpressionException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String stringPrefix = mmdElement.getStringPrefix();
                if (stringPrefix != null)
                {
                    if (evaluation.startsWith(stringPrefix))
                    {
                        evaluation = evaluation.substring(stringPrefix.length());
                    }
                }
                //to remove unwanted XML characters
                evaluation = XMLTools.unescapeXML(evaluation);
               
                //remove the white spaces
                evaluation=evaluation.trim();
                metadata.set(mmdElementName, parsedURLTypeKludge(mmdElement, evaluation));// evaluation);

            }
            else
            {
                // this is either a nested element or a collection element

                // If the field is nested
                if (mmdElement.isNested())
                {
                    M nestedMetadata = null;

                    // Have to return the nested object for the field.
                    FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);
                    nestedMetadata = (M) fieldAccessor.getAndPerhapsCreateNested(metadata);
                    recursiveExtraction(translationScope, mmdElement, nestedMetadata, tidyDOM, purl);
                }
                // If the field is list.
                else if (mmdElement.collection().equals("ArrayList"))
                {

                    // this is the field accessor for the collection field
                    FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);

                    // now get the collection field
                    Field collectionField = fieldAccessor.getField();

                    // get the child meta-metadata fields
                    HashMapArrayList<String, MetaMetadataField> childFieldList = mmdElement.getSet();

                    // list to hold the collectionInstances
                    ArrayList<Metadata> collectionInstanceList = new ArrayList();

                    // loop over all the child meta-metadata fields of the collection meta-metadatafield
                    for (int i = 0; i < childFieldList.size(); i++)
                    {
                        // the ith child field
                        MetaMetadataField childMetadataField = childFieldList.get(i);

                        // get the xpath expression
                        String childXPath = childMetadataField.getXpath();

                        NodeList nodes = null;
                        try
                        {
                            nodes = (NodeList) xpath.evaluate(childXPath, tidyDOM, XPathConstants.NODESET);
                        }
                        catch (XPathExpressionException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        ScalarType scalarType = childMetadataField.getScalarType();

                        // get the typed list from the node list
                        ArrayList list = getTypedListFromNodes(nodes, scalarType, childMetadataField);

                        Class collectionChildClass = translationScope.getClassByTag(mmdElement
                                .getCollectionChildType());

                        // only first time we need to create a list of instances which is equal to
                        // the number of results returned.
                        if (i == 0)
                        {
                            for (int j = 0; j < list.size(); j++)
                            {
                                collectionInstanceList.add((Metadata) ReflectionTools
                                        .getInstance(collectionChildClass));
                            }
                        }

                        // now for each child-meta-metadata field set the value in the collection instance.
                        for (int j = 0; j < list.size(); j++)
                        {
                            // get the child field name
                            String childFieldName = childMetadataField.getName();
                            collectionInstanceList.get(j).set(childFieldName, list.get(j).toString());
                        }

                    }
                    // set the value of collection list in the meta-data
                    ReflectionTools.setFieldValue(metadata, collectionField, collectionInstanceList);
                }
            }
        }// end for of all metadatafields
        return metadata;
    }

    private ArrayList getTypedListFromNodes(NodeList nodeList, ScalarType scalarType,
            MetaMetadataField mmdElement)
    {
        ArrayList list = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            String value = node.getNodeValue();

            String fieldTypeName = scalarType.fieldTypeName();
            if (fieldTypeName.equals("String"))
            {
                // TypeCast into MetadataString
                MetadataString s = new MetadataString();
                s.setValue(value);
                list.add(value);
            }
            else if (fieldTypeName.equals("StringBuilder"))
            {
                // TypeCast into MetadataStringBuilder
                MetadataStringBuilder s = new MetadataStringBuilder();
                s.setValue(value);
                list.add(value);
            }
            else if (fieldTypeName.equals("ParsedURL"))
            {
                MetadataParsedURL s = new MetadataParsedURL();
                list.add(parsedURLTypeKludge(mmdElement, value));
            }
            /*
             * else if (fieldTypeName.equals("int")) { MetadataInteger s = new MetadataInteger();
             * s.setValue(Integer.parseInt(value)); list.add(value); }
             */

        }
        return list;
    }

    private String parsedURLTypeKludge(MetaMetadataField mmdElement, String evaluation)
    {
        if (mmdElement.getScalarType() instanceof ParsedURLType)
        {
            ParsedURL parsedURL = null;
            try
            {
                if (evaluation != "null" && (evaluation.length() != 0))
                {
                    parsedURL = ParsedURL.getRelative(new URL("http://portal.acm.org/"), evaluation, "");
                    evaluation = parsedURL.toString();
                }
            }
            catch (MalformedURLException e)
            {
                // Should not come here b'coz the domainString has to be properly formed all the time.
                e.printStackTrace();
            }
        }
        return evaluation;
    }
}
