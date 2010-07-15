package ecologylab.semantics.documentparsers;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.DefVar;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.types.scalar.ScalarType;

/**
 * This is the base class for the all the document type which we create using meta-metadata.
 * 
 * @author amathur
 * 
 */
public abstract class MetaMetadataParserBase
extends HTMLDOMParser implements ScalarUnmarshallingContext,SemanticActionsKeyWords
{

	/**
	 * Translation scope of the metadata classes, generated during compile time.
	 */
	private TranslationScope			metadataTranslationScope;

	
	protected XPath	xpath;
	
	/**
	 * True PURL for container
	 */
	ParsedURL truePURL;;

	/**
	 * 
	 * @param infoCollector
	 */
	public MetaMetadataParserBase(InfoCollector infoCollector)
	{
		super(infoCollector);
		xpath = XPathFactory.newInstance().newXPath();
	}

	public MetaMetadataParserBase(InfoCollector infoCollector,
			SemanticActionHandler semanticActionHandler)
	{
		super(infoCollector,semanticActionHandler);
		xpath = XPathFactory.newInstance().newXPath();
	}

	public abstract Document populateMetadataObject();

	/**
	 * Main method in which we take semantic actions
	 * 
	 * @param populatedMetadata
	 */
	protected void takeSemanticActions(Metadata populatedMetadata)
	{
		// get the semantic actions
		ArrayList<? extends SemanticAction> semanticActions = metaMetadata.getSemanticActions();

		addAdditionalParameters(populatedMetadata);

		// handle the semantic actions sequentially
		//FIXME: Throw semantic warning when no semantic actions exist
		if(semanticActions == null)
		{
			System.out.println("warning: no semantic actions exist");
			return;
		}
		
		for (int i = 0; i < semanticActions.size(); i++)
		{
			SemanticAction action = semanticActions.get(i);
			semanticActionHandler.handleSemanticAction(action, this, infoCollector);
		}
	}

	/**
	 * (1) Populate Metadata.
	 * (2) Rebuild composite term vector.
	 * (3) Take semantic actions.
	 */
	protected final void postParse()
	{
		super.postParse();
		instantiateMetaMetadataVariables();

		truePURL 						= container.purl();
		// build the metadata object

		Metadata populatedMetadata =populateMetadataObject();
		
		try
		{
			debug("Metadata parsed from: " + container.purl());
			debug(populatedMetadata.serialize());
			
		}
		
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// if the metametadata reference is null, assign the correct metametadata object to it.
		if (populatedMetadata.getMetaMetadata() == null)
			populatedMetadata.setMetaMetadata(metaMetadata);
		
		// make sure termVector is built here
		populatedMetadata.rebuildCompositeTermVector();
		
		if (populatedMetadata!= null)
			takeSemanticActions(populatedMetadata);

		semanticActionHandler.recycle();
		semanticActionHandler = null;
	}

	/**
	 * Build the standard object instance map which can then be used via reflection.
	 * 
	 * @param populatedMetadata
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void addAdditionalParameters(Metadata populatedMetadata)
	{
		Scope<Object> param = semanticActionHandler.getSemanticActionReturnValueMap();
		param.put(DOCUMENT_TYPE, this);
		param.put(METADATA, populatedMetadata);
		param.put(TRUE_PURL, truePURL);
	}


	/**
	 * @return the metadataTranslationScope
	 */
	public final TranslationScope getMetadataTranslationScope()
	{
		return container.getGeneratedMetadataTranslationScope();
	}



	protected void createDOMandParse(ParsedURL purl)
	{

	}

	/**
	 * Instantiate MetaMetadata variables that are used during XPath information extraction, and
	 * in semantic actions.
	 * 
	 * @param document
	 *          The root of the document
	 */
	private void instantiateMetaMetadataVariables()
	{
		// get the list of all variable defintions
		ArrayList<DefVar> defVars = metaMetadata.getDefVars();

		// get the parameters
		Scope<Object> parameters = semanticActionHandler.getSemanticActionReturnValueMap();
		if (defVars != null)
		{
			// only if some variables are there we create a DOM[for diect binidng types for others DOM is
			// already there]
			createDOMandParse(container.purl());
			for (DefVar defVar : defVars)
			{
				String xpathExpression = defVar.getXpath();
				String node = defVar.getNode();
				String name = defVar.getName();
				QName type = defVar.getType();
				Node contextNode = null;
				try
				{

					if (node == null)
					{
						// apply the XPath on the document root.
						contextNode = (Node) parameters
								.get(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE);
					}
					else
					{
						// get the context node from parameters
						contextNode = (Node) parameters.get(node);
					}

					if (type != null)
					{
						// apply xpath and get the node list
						if (type == XPathConstants.NODESET)
						{
							NodeList nList = (NodeList) xpath.evaluate(xpathExpression, contextNode, type);

							// put the value in the parametrers
							parameters.put(name, nList);
						}
						else if (type == XPathConstants.NODE)
						{
							Node n = (Node) xpath.evaluate(xpathExpression, contextNode, type);

							// put the value in the parametrers
							parameters.put(name, n);
						}
					}
					else
					{
						// its gonna be a simple string evaluation
						String evaluation = xpath.evaluate(xpathExpression, contextNode);

						// put it into returnValueMap
						parameters.put(name, evaluation);
					}
				}
				catch (Exception e)
				{
					StringBuilder buffy = StringBuilderUtils.acquire();
					buffy
							.append("################### ERROR IN VARIABLE DEFINTION ##############################\n");
					buffy.append("Variable Name::\t").append(name).append("\n");
					buffy.append("Check if the context node is not null::\t").append(contextNode)
							.append("\n");
					buffy.append("Check if the XPath Expression is valid::\t").append(xpathExpression)
							.append("\n");
					buffy.append("Check if the return object type of Xpath evaluation is corect::\t").append(
							type).append("\n");
					debug(buffy);
					StringBuilderUtils.release(buffy);
				}

			}
		}
	}

	/**
	 * Extracts metadata using the tidyDOM from the purl.
	 * 
	 * @param translationScope
	 *          TODO suppport for Maps to be added[It was there orginially. Just have to verify and
	 *          understand it and put it back.]
	 * @param mmdField
	 *          The MetaMetadata field from which we will read the extraction information about the
	 *          metadadta
	 * @param metadata
	 *          The metadata object in which the information will be filled
	 * @param param
	 *          TODO
	 * @param purl
	 * @return
	 */
	protected Metadata recursiveExtraction(TranslationScope translationScope, MetaMetadataField mmdField,
			Metadata metadata, XPath xpath, Scope<Object> param,Node contextNode)
	{

		Node rootNode = contextNode;
		// Gets the child metadata of the mmdField.
		HashMapArrayList<String, MetaMetadataField> mmdFieldSet = mmdField.getChildMetaMetadata();

		// Traverses through the child metadata to populate.
		if (mmdFieldSet != null)
		{
			synchronized (mmdFieldSet)
			{
				for (MetaMetadataField mmdElement : mmdFieldSet)
				{
					contextNode = rootNode; //Reset the contextNode to the rootNode for this level of recursion.
					try{
						// get the context Node
						String contextNodeName = mmdElement.getContextNode();
						if (contextNodeName != null)
						{
							contextNode = (Node) param.get(contextNodeName);
						
						}
					}catch(Exception e)
					{
						e.printStackTrace();
						//FIXME: Tell whoever wrote the metametadata that context node should only be a node, not a node set !!!
					}
					// Used to get the field value from the web page.
					String xpathString = mmdElement.getXpath();
					// xpathString="/html/body[@id='gsr']/div[@id='res']/div[1]/ol/li[@*]/h3/a";
					// System.out.println("DEBUG::xPathString=\t" + xpathString);

					// name of the metaMetadata element.
					String mmdElementName = mmdElement.getName();
					// System.out.println("DEBUG::mmdElementName= \t" + mmdElementName);

					// if it is nested
					if (mmdElement instanceof MetaMetadataCompositeField)
					{
						extractNested(translationScope, metadata,contextNode, mmdElement, mmdElementName, xpath, param,xpathString);
					}

					// if its is a array list
					else if (mmdElement instanceof MetaMetadataCollectionField)
					{
						extractArrayList(translationScope, metadata, contextNode, (MetaMetadataCollectionField)mmdElement, mmdElementName,
								xpath, param, xpathString);
					}
					else
					{
						// its a scalar
						String evaluation = extractScalar(xpath, mmdElement, contextNode, xpathString);
						metadata.setByTagName(mmdElementName, evaluation, this);// evaluation);
					}
				}// end for of all metadatafields
			}

		}
		return metadata;
	}

	/**
	 * @param xpath
	 * @param mmdElement
	 * @param contextNode
	 * @param xpathString
	 * @return
	 */
	private String extractScalar(XPath xpath, MetaMetadataField mmdElement, final Node contextNode,
			String xpathString)
	{
		// this is a simple scalar field
		String evaluation = "";

		try
		{
			if (xpathString != null && contextNode != null)
			{
				// evaluate only if some extraction rule is there
				evaluation = xpath.evaluate(xpathString, contextNode);
				// System.out.println("DEBUG::evaluation from DOM=\t" + evaluation);
			}
		}
		catch (Exception e)
		{
			StringBuilder buffy = StringBuilderUtils.acquire();
			buffy.append("################# ERROR IN EVALUATION OF A FIELD########################\n");
			buffy.append("Field Name::\t").append(mmdElement.getName()).append("\n");
			buffy.append("ContextNode::\t").append(contextNode.getNodeValue()).append("\n");
			buffy.append("XPath Expression::\t").append(xpathString).append("\n");
			System.out.println(buffy);
			StringBuilderUtils.release(buffy);
		}

		// after we have evaluated the expression we might need
		// to modify it.
		evaluation = applyPrefixAndRegExOnEvaluation(evaluation, mmdElement);
		/*
		 * System.out.println("DEBUG::evaluation after applyPrefixAndRegExOnEvaluation=\t" +
		 * evaluation);
		 */
		return evaluation;
	}

	/**
	 * @param translationScope
	 * @param metadata
	 * @param mmdElement
	 * @param mmdElementName
	 * @param purl
	 */
	private void extractNested(TranslationScope translationScope, Metadata metadata,Node contextNode,
			MetaMetadataField mmdElement, String mmdElementName, XPath xpath,
			Scope<Object> param,String xPathString)
	{
		try
		{
			Metadata nestedMetadata = null;
			// for nested objects xPath on context node will give only one node.
			Node parentNode = (Node)xpath.evaluate(xPathString, contextNode,
										XPathConstants.NODE);
			
			Class<? extends Metadata> metadataClass = mmdElement.getMetadataClass();
			MetaMetadata metaMetadata 							= mmdElement.metaMetadataRepository().getByClass(metadataClass);
			Class[] argClasses 	= new Class[] { metaMetadata.getClass() };
			Object[] argObjects = new Object[] { metaMetadata };
			nestedMetadata = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
			ReflectionTools.setFieldValue(metadata, mmdElement.getMetadataFieldDescriptor().getField(), nestedMetadata);
			//FIXME -- need to use repository recursively!
			//nestedMetadata = (Metadata) metadataFieldDescriptor.getAndPerhapsCreateNested(metadata);
			//nestedMetadata.setMetaMetadata(infoCollector.metaMetaDataRepository().getMM(nestedMetadata.getClass()));
			recursiveExtraction(translationScope,mmdElement, nestedMetadata, xpath, param,parentNode);
		}
		catch (Exception e)
		{
			StringBuilder buffy = StringBuilderUtils.acquire();
			buffy
					.append("################# ERROR IN EVALUATION OF A NESTED FIELD "+mmdElementName+" ########################\n");
			buffy.append("Field Name::\t").append(mmdElement.getName()).append("\n");
			buffy.append("ContextNode::\t").append(contextNode).append("\n");
			buffy.append("XPath Expression::\t").append(xPathString).append("\n");
			error(buffy);
			StringBuilderUtils.release(buffy);
		}
	}

	/**
	 * 
	 * @param translationScope
	 * @param metadata The metadata object representing the collection field
	 * @param contextNode The node on which the xpaths need to be applies
	 * @param mmdElement The collection meta-metadata field
	 * @param mmdElementName The name of the collection meta-metadata field
	 * @param xpath XPath object
	 * @param param Semantic Action Parameters
	 * @param parentXPathString The xpath string of the collection meta-metadata field
	 */
	private void extractArrayList(TranslationScope translationScope, Metadata metadata, Node contextNode,
			MetaMetadataCollectionField mmdElement, String mmdElementName, XPath xpath,
			Scope<Object> param,String parentXPathString)
	{
		Node originalNode = contextNode;
		// this is the field accessor for the collection field
		MetadataFieldDescriptor fieldDescriptor = mmdElement.getMetadataFieldDescriptor();
		if (fieldDescriptor != null)
		{
			
			// get class of the collection
			Class collectionChildClass = null;
			if(mmdElement.isChildEntity())
			{
				collectionChildClass=translationScope.getClassByTag(DocumentParserTagNames.ENTITY);
			}
			else
			{
				collectionChildClass =translationScope.getClassByTag(mmdElement.determineCollectionChildType()); //mmdElement.getChildTag());
			}
//			HashMapArrayList<String, MetadataFieldDescriptor> collectionElementAccessors = metadata.getMetadataFieldDescriptorsByTagName();

			// now get the collection field
			Field collectionField = fieldDescriptor.getField();

			// get the child meta-metadata fields with extraction rules
			//HashMapArrayList<String, MetaMetadataField> childMMdFieldList = mmdElement.getSet();

			// list to hold the collectionInstances
			ArrayList<Metadata> collectionInstanceList = new ArrayList<Metadata>();

			// get all the declared child fields for this collection
			// FIXME -- optimize by caching these!!!

			// Field[] fields = collectionChildClass.getDeclaredFields();

			DTMNodeList parentNodeList = null;
			// loop over all the child meta-metadata fields of
			// the collection meta-metadatafield
			boolean collectionInstanceListInitialized = false;
			// get the field from childField list which has the same name as this field
			for (MetaMetadataField childMetaMetadataField : mmdElement.getChildMetaMetadata())
			{
				// if this field exists in childField list this means there are some extraction rules for it
				// and so get the values
				//MetaMetadataField childMetadataField = mfa.getMetaMetadataField();
				
				if (childMetaMetadataField != null)
				{
					// so there are some extraction rules
					// get the parent node list only of its null for efficiency
					try
					{
						if (parentNodeList == null)
							parentNodeList = (DTMNodeList) xpath.evaluate(parentXPathString, contextNode,
									XPathConstants.NODESET);
					}
					catch (Exception e)
					{
						
						StringBuilder buffy = StringBuilderUtils.acquire();
						
						buffy
								.append("################# ERROR IN EVALUATION OF A COLLECTION FIELD ########################\n");
						buffy.append("Field Name::\t").append(mmdElement.getName()).append("\n");
						buffy.append("ContextNode::\t").append(contextNode).append("\n");
						buffy.append("XPath Expression::\t").append(parentXPathString).append("\n");
						buffy.append("Container Purl::\t").append(container.purl()).append("\n");
						error(buffy);
					

						StringBuilderUtils.release(buffy);
						return;
					}
					final int parentNodeListLength = parentNodeList.getLength();
					// only first time we need to add the instances

					if (!collectionInstanceListInitialized)
					{
						// we need to create a list of instances which is equal to the number of results
						// returned.
						for (int j = 0; j < parentNodeListLength; j++)
						{
							String childTag 								= mmdElement.determineCollectionChildType(); //getChildTag();
							MetaMetadata mmdForNewMetadata 	= mmdElement.metaMetadataRepository().getByTagName(childTag);
							Class[] argClasses 	= new Class[] { mmdForNewMetadata.getClass() };
							Object[] argObjects = new Object[] { mmdForNewMetadata };
							Metadata metadataInstance = (Metadata) ReflectionTools.getInstance(collectionChildClass, argClasses, argObjects);
							collectionInstanceList.add(metadataInstance);
							
						}
						collectionInstanceListInitialized = true;
					}
					// now we fill each instance
					for (int m = 0; m < parentNodeListLength; m++)
					{

						// get the xpath expression
						String childXPath = childMetaMetadataField.getXpath();
						// System.out.println("DEBUG:: child node xpath  "+childXPath);

						// apply xpaths on m th parent node
						contextNode = parentNodeList.item(m);

						// if some context node is specified find it.
						if (childMetaMetadataField.getContextNode() != null)
						{
							contextNode = (Node) (param.get(childMetaMetadataField.getContextNode()));
						}

						if (childMetaMetadataField instanceof MetaMetadataCollectionField)
						{
							extractArrayList(translationScope, collectionInstanceList.get(m), contextNode,
									(MetaMetadataCollectionField) childMetaMetadataField, childMetaMetadataField.getName(), xpath, param,
									childMetaMetadataField.getXpath());
						}
						if (childMetaMetadataField instanceof MetaMetadataCompositeField)
						{
							 extractNested(translationScope, collectionInstanceList.get(m), contextNode,childMetaMetadataField, childMetaMetadataField.getName(), xpath, param,
										childMetaMetadataField.getXpath());
						}
						else
						{
							// its a simple scalar field
							String evaluation = extractScalar(xpath, childMetaMetadataField, contextNode,
									childMetaMetadataField.getXpath());
							collectionInstanceList.get(m).setByTagName(childMetaMetadataField.getName(), evaluation, this);
						}
						/*
						 * String evaluation = xpath.evaluate(childXPath, contextNode);
						 * System.out.println("DEBUG:: evaluation from DOM::  "+evaluation);
						 * 
						 * evaluation = applyPrefixAndRegExOnEvaluation(evaluation, childMetadataField);
						 * if(evaluation.length()<=1) { evaluation="Test test"; } // get the child field name
						 * String childFieldName = childMetadataField.getName();
						 * collectionInstanceList.get(m).set(childFieldName, evaluation,this);
						 */
					}
				}// end xpath
				/*else
				{
					// else there are no extraction rules , just create a blank field
					for (int k = 0; k < collectionInstanceList.size(); k++)
					{
						// FIXME -- andruid believes this line can be removed! 9/2/09
						collectionInstanceList.get(k).setByTagName(mfa.getTagName(), "");
					}
				}*/
			}

			// set the value of collection list in the meta-data
			ReflectionTools.setFieldValue(metadata, collectionField, collectionInstanceList);
		}

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
	// FIXME -- make this operate directly on a StringBuilder (which will also change the return type
	// to void
	private String applyPrefixAndRegExOnEvaluation(String evaluation, MetaMetadataField mmdElement)
	{
		// regex replacing should happen only to scalar fields
		if (!(mmdElement instanceof MetaMetadataScalarField))
			return evaluation;
		
		MetaMetadataScalarField field = (MetaMetadataScalarField) mmdElement;
		
		// get the regular expression
		String regularExpression = field.getRegexPattern();
		/*
		 * System.out.println("DEBUG:: mmdElementName=\t" + mmdElement.getName());
		 * System.out.println("DEBUG:: regularExpression=\t" + regularExpression);
		 * System.out.println("DEBUG:: evaluation=\t" + evaluation);
		 */

		if (regularExpression != null)
		{
			// create a pattern based on regular expression
			Pattern pattern = Pattern.compile(regularExpression);
//			StringBuilder eval = new StringBuilder(evaluation);
			// create a matcher based on input string
			Matcher matcher = pattern.matcher(evaluation);

			// TODO right now we r using regular expressions just to replace the
			// matching string we might use them for more better uses.
			// get the replacement thing.
			String replacementString = field.getRegexReplacement();
			if (replacementString != null)
			{
				debug(String.format("regex replacement: regex=%s, replace=%s", regularExpression, replacementString));
			
				//Consecutively check for further matches. Replacing all with the replacementString
				evaluation = matcher.replaceAll(replacementString);
			}
		}

		// Now we apply the string prefix
		String stringPrefix = field.getStringPrefix();
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
	 * Takes a list of dom nodes and returns a list of corresponding text within them. Applies a
	 * regular expression to each such text if appropriate.
	 * 
	 * @param nodeList
	 * @param scalarType
	 * @param mmdElement
	 * @return
	 */
	private ArrayList<String> getTypedListFromNodes(final NodeList nodeList, ScalarType scalarType,
			MetaMetadataField mmdElement)
	{
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			list.add("defaultItem\t" + i);
		}

		boolean flag = true;
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			if (flag)
			{
				list.clear();
				flag = false;
			}

			Node node = nodeList.item(i);

			// get the value
			String value = node.getNodeType() == Node.ATTRIBUTE_NODE ? node.getNodeValue()
					: getAllTextFromNode(node);

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
		StringBuilder buffy = StringBuilderUtils.acquire();
		getAllTextFromNode(node, buffy);
		String result = buffy.toString();
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
		switch (nodeType)
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

	public ParsedURL purlContext()
	{
		return purl();
	}

	public File fileContext()
	{
		return null;
	}
	@Override
	public ParsedURL getTruePURL()
	{
		return (truePURL != null) ? truePURL : super.getTruePURL();
	}

}
