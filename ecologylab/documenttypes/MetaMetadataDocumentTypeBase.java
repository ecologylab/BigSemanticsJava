package ecologylab.documenttypes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionParameters;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.types.element.ArrayListState;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * This is the base class for the all the document type which we create using meta-metadata.
 * 
 * @author amathur
 * 
 */
public abstract class MetaMetadataDocumentTypeBase<M extends MetadataBase,SA extends SemanticAction,C  extends Container> extends DocumentType
{

	private SemanticActionHandler<SA,C,?>	semanticActionHandler;

	/**
	 * Translation scope of the metadata classes, generated during compile time.
	 */
	private TranslationScope			metadataTranslationScope;

	private M											metadata;

	public MetaMetadataDocumentTypeBase()
	{
	}

	/**
	 * 
	 * @param infoCollector
	 */
	public MetaMetadataDocumentTypeBase(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	public MetaMetadataDocumentTypeBase(InfoCollector infoCollector,
			SemanticActionHandler<SA,C,?> semanticActionHandler)
	{
		super(infoCollector);
		this.semanticActionHandler = semanticActionHandler;
	}

	public abstract M buildMetadataObject();

	/**
	 * Main method in which we take semantic actions
	 * 
	 * @param populatedMetadata
	 */
	protected void takeSemanticActions(M populatedMetadata)
	{
		// get the semantic actions
		ArrayListState<SA> semanticActions = metaMetadata.getSemanticActions();

		// build the standardObjectInstanceMap
		Scope standardObjectInstanceMap = buildStandardObjectInstanceMap(populatedMetadata);

		// build the semantic action parameter
		SemanticActionParameters parameter = new SemanticActionParameters(standardObjectInstanceMap);

		// handle the semantic actions sequentially
		for (int i = 0; i < semanticActions.size(); i++)
		{
			SA action = semanticActions.get(i);
			semanticActionHandler.handleSemanticAction(action, parameter, this, abstractInfoCollector);
		}
	}

	/**
	 * Build the standard object instance map which can then be used via reflection.
	 * 
	 * @param populatedMetadata
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Scope buildStandardObjectInstanceMap(M populatedMetadata)
	{
		Scope standardObjectInstanceMap = new Scope();
		standardObjectInstanceMap.put("documentType", this);
		standardObjectInstanceMap.put("metadata", populatedMetadata);
		standardObjectInstanceMap.put("false", false);
		standardObjectInstanceMap.put("true", true);
		standardObjectInstanceMap.put("null", null);
		return standardObjectInstanceMap;
	}

	public void initailizeMetadataObjectBuilding()
	{
		metadataTranslationScope = container.getTranslationScope();
		Class metadataClass = metadataTranslationScope.getClassByTag(metaMetadata.getType());
		metadata = (M) ReflectionTools.getInstance(metadataClass);
		metadata.setMetaMetadata(metaMetadata);
	}

	/**
	 * @return the metadataTranslationScope
	 */
	public final TranslationScope getMetadataTranslationScope()
	{
		return metadataTranslationScope;
	}

	/**
	 * @return the metadata
	 */
	public final M getMetadata()
	{
		return metadata;
	}

	@Override
	public void parse() throws IOException
	{
		M populatedMetadata = buildMetadataObject();
		if (populatedMetadata != null)
			takeSemanticActions(populatedMetadata);
	}

	/**
	 * Extracts metadata using the tidyDOM from the purl.
	 * 
	 * @param translationScope
	 *          TODO Add the ability to have some regular expressions also to be applied to extarcted
	 *          data.This is needed in many cases when we need to get only some of the information or
	 *          we need to preprocess the extracted information.
	 * 
	 *          TODO suppport for Maps to be added[It was there orginially. Just have to verify and
	 *          understand it and put it back.]
	 * @param mmdField
	 *          The MetaMetadata field from which we will read the extraction information about the
	 *          metadadta
	 * @param metadata
	 *          The metadata object in which the information will be filled
	 * @param tidyDOM
	 *          The dom on which the extraction expressions[xPath and reg ex will be applied]
	 * @param purl
	 * @return
	 */
	protected M recursiveExtraction(TranslationScope translationScope, MetaMetadataField mmdField,
			M metadata, final Document tidyDOM, XPath xpath)
	{

		// Gets the child metadata of the mmdField.
		HashMapArrayList<String, MetaMetadataField> mmdFieldSet = mmdField.getSet();

		// Traverses through the child metadata to populate.
		if (mmdFieldSet != null)
		{
			synchronized (mmdFieldSet)
			{
				for (MetaMetadataField mmdElement : mmdFieldSet)
				{

					// Used to get the field value from the web page.
					String xpathString = mmdElement.getXpath();
					// xpathString="/html/body[@id='gsr']/div[@id='res']/div[1]/ol/li[@*]/h3/a";
					System.out.println("DEBUG::xPathString=\t" + xpathString);

					// name of the metaMetadata element.
					String mmdElementName = mmdElement.getName();
					System.out.println("DEBUG::mmdElementName= \t" + mmdElementName);

					if (xpathString != null)
					{
						// this is a simple scalar field
						String evaluation = "";

						try
						{
							evaluation = xpath.evaluate(xpathString, tidyDOM);
							System.out.println("DEBUG::evaluation from DOM=\t" + evaluation);
						}
						catch (XPathExpressionException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// after we have evaluated the expression we might need
						// to modify it.
						evaluation = applyPrefixAndRegExOnEvaluation(evaluation, mmdElement);
						/*
						 * System.out.println("DEBUG::evaluation after applyPrefixAndRegExOnEvaluation=\t" +
						 * evaluation);
						 */

						metadata.set(mmdElementName, evaluation);// evaluation);
					}
					else
					{
						// this is either a nested element or a collection element
						// If the field is nested
						if (mmdElement.isNested())
						{
							extractNested(translationScope, metadata, tidyDOM, mmdElement, mmdElementName, xpath);
						}
						// If the field is list.
						else if ("ArrayList".equals(mmdElement.collection()))
						{
							extractArrayList(translationScope, metadata, tidyDOM, mmdElement, mmdElementName,
									xpath);
						}
					}
				}// end for of all metadatafields
			}

		}
		return metadata;
	}

	/**
	 * @param translationScope
	 * @param metadata
	 * @param tidyDOM
	 * @param purl
	 * @param mmdElement
	 * @param mmdElementName
	 */
	private void extractNested(TranslationScope translationScope, M metadata, final Document tidyDOM,
			MetaMetadataField mmdElement, String mmdElementName, XPath xpath)
	{
		M nestedMetadata = null;

		// Have to return the nested object for the field.
		FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);
		nestedMetadata = (M) fieldAccessor.getAndPerhapsCreateNested(metadata);
		recursiveExtraction(translationScope, mmdElement, nestedMetadata, tidyDOM, xpath);
	}

	/**
	 * @param translationScope
	 * @param metadata
	 * @param tidyDOM
	 * @param mmdElement
	 * @param mmdElementName
	 */
	private void extractArrayList(TranslationScope translationScope, M metadata,
			final Document tidyDOM, MetaMetadataField mmdElement, String mmdElementName, XPath xpath)
	{
		// this is the field accessor for the collection field
		FieldAccessor fieldAccessor = metadata.getMetadataFieldAccessor(mmdElementName);

		// now get the collection field
		Field collectionField = fieldAccessor.getField();

		// get the child meta-metadata fields
		HashMapArrayList<String, MetaMetadataField> childFieldList = mmdElement.getSet();

		// list to hold the collectionInstances
		ArrayList<Metadata> collectionInstanceList = new ArrayList<Metadata>();

		// loop over all the child meta-metadata fields of
		// the collection meta-metadatafield
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
			ArrayList<String> list = getTypedListFromNodes(nodes, scalarType, childMetadataField);

			Class collectionChildClass = translationScope.getClassByTag(mmdElement.getCollectionChildType());

			// only first time we need to create a list of
			// instances which is equal to
			// the number of results returned.
			if (i == 0)
			{
				for (int j = 0; j < list.size(); j++)
				{
					collectionInstanceList.add((Metadata) ReflectionTools.getInstance(collectionChildClass));
				}
			}

			// now for each child-meta-metadata field set
			// the value in the collection instance.
			for (int j = 0; j < list.size(); j++)
			{
				// FIXME -- workaround concurrency issue?!
				// --
				// andruid 1/14/09
				if (j < collectionInstanceList.size())
				{
					// get the child field name
					String childFieldName = childMetadataField.getName();
					collectionInstanceList.get(j).set(childFieldName, list.get(j));
				}
			}
		}
		// set the value of collection list in the meta-data
		ReflectionTools.setFieldValue(metadata, collectionField, collectionInstanceList);
	}

	/**
	 * This function does all the modifications on the evalaution based on string prefix as well as on
	 * regular expressions. TODO we might not even need the string prefix if we can write good regular
	 * expressions.
	 * 
	 * @param evaluation
	 * @param mmdElement
	 * @return
	 */
	private String applyPrefixAndRegExOnEvaluation(String evaluation, MetaMetadataField mmdElement)
	{
		// get the regular expression
		String regularExpression = mmdElement.getRegularExpression();
		/*
		 * System.out.println("DEBUG:: mmdElementName=\t" + mmdElement.getName());
		 * System.out.println("DEBUG:: regularExpression=\t" + regularExpression);
		 * System.out.println("DEBUG:: evaluation=\t" + evaluation);
		 */

		if (regularExpression != null)
		{
			// create a pattern based on regular expression
			Pattern pattern = Pattern.compile(regularExpression);

			// create a matcher based on input string
			Matcher matcher = pattern.matcher(evaluation);

			// TODO right now we r using regular expressions just to replace the
			// matching string we might use them for more better uses.
			// get the replacement thing.
			String replacementString = mmdElement.getReplacementString();
			if (replacementString != null)
			{
				// create string buffer
				StringBuffer stringBuffer = new StringBuffer();
				boolean result = matcher.find();
				if (result)
				{
					matcher.appendReplacement(stringBuffer, replacementString);
					result = matcher.find();
				}
				matcher.appendTail(stringBuffer);
				evaluation = stringBuffer.toString();
			}
		}

		// Now we apply the string prefix
		String stringPrefix = mmdElement.getStringPrefix();
		if (stringPrefix != null)
		{
			evaluation = stringPrefix + evaluation;
		}

		// to remove unwanted XML characters
		evaluation = XMLTools.unescapeXML(evaluation);
		// remove the white spaces
		evaluation = evaluation.trim();
		return evaluation;
	}

	/**
	 * Takes a list of dom nodes and returns a list of corresponding text within them.
	 * Applies a regular expression to each such text if appropriate.
	 * @param nodeList
	 * @param scalarType
	 * @param mmdElement
	 * @return
	 */
	private ArrayList<String> getTypedListFromNodes(NodeList nodeList, ScalarType scalarType,
			MetaMetadataField mmdElement)
	{
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);

			// get the value
			String value = node.getNodeType() == Node.ATTRIBUTE_NODE ? 
					node.getNodeValue() : getAllTextFromNode(node);

			// we might need to apply some regular expressions on the value
			value = applyPrefixAndRegExOnEvaluation(value, mmdElement);
			// System.out.println("DEBUG::value after applyPrefixAndRegExOnEvaluation=\t" + value);
			list.add(value);
			/*
			 * String fieldTypeName = scalarType.fieldTypeName(); if (fieldTypeName.equals("String")) { //
			 * TypeCast into MetadataString MetadataString s = new MetadataString(); s.setValue(value);
			 * list.add(value); } else if (fieldTypeName.equals("StringBuilder")) { // TypeCast into
			 * MetadataStringBuilder MetadataStringBuilder s = new MetadataStringBuilder();
			 * s.setValue(value); list.add(value); } else if (fieldTypeName.equals("ParsedURL")) {
			 * MetadataParsedURL s = new MetadataParsedURL(); list.add(value); }
			 */
			/*
			 * else if (fieldTypeName.equals("int")) { MetadataInteger s = new MetadataInteger();
			 * s.setValue(Integer.parseInt(value)); list.add(value); }
			 */
		}
		return list;
	}
	private String getAllTextFromNode(Node node)
	{
		StringBuilder buffy	= StringBuilderUtils.acquire();
		getAllTextFromNode(node, buffy);
		String result	= buffy.toString();
		StringBuilderUtils.release(buffy);
		return result;
	}
	/**
	 * This method get all the text from the subtree rooted at a node.The reason for this
	 * implementation is that when we write a xpath we might get node of any type. Now if the node has
	 * some text inside it we would like to get it. And so this method get all the text in the subtree
	 * rooted at that node. Eliminates all formatting tags.
	 * 
	 * @param node
	 * @return
	 */
	private void getAllTextFromNode(Node node, StringBuilder buffy)
	{
		short nodeType = node.getNodeType();
		switch(nodeType)
		{
		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
			buffy.append(node.getNodeValue());
			break;
		case Node.ATTRIBUTE_NODE:
		case Node.COMMENT_NODE:
		case Node.PROCESSING_INSTRUCTION_NODE:
			break;
		default:
			NodeList cList = node.getChildNodes();
			if (cList != null)
			{
				for (int k = 0; k < cList.getLength(); k++)
				{
					buffy.append(getAllTextFromNode(cList.item(k)));
				}
			}
			break;
		}
	}
	/**
	 * TODO remove this some how. Have to think what it does and how to remove it. Bascially depending
	 * upon the meta_metadata each meta_metadata_field which is of type ParsedURL we might need to
	 * append/do some manuplulations to get exact parsed url. This I think can be achived in a much
	 * better and cleaner way by either having a field which says how to appned some thing to the
	 * evaluation or may be thru some regular expressions.
	 * 
	 * @param mmdElement
	 * @param evaluation
	 * @return
	 */
	/*
	 * private String parsedURLTypeKludge(MetaMetadataField mmdElement, String evaluation) { if
	 * (mmdElement.getScalarType() instanceof ParsedURLType) { ParsedURL parsedURL = null; try { if
	 * (evaluation != "null" && (evaluation.length() != 0)) { System.out.println("########" +
	 * mmdElement.getName()); parsedURL = ParsedURL.getRelative(new URL("http://portal.acm.org/"),
	 * evaluation, ""); evaluation = parsedURL.toString(); } } catch (MalformedURLException e) { //
	 * Should not come here b'coz the domainString has to be // properly formed all the time.
	 * e.printStackTrace(); } } return evaluation; }
	 */
}
