package ecologylab.semantics.documentparsers;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.collecting.LinkedMetadataMonitor;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.semantics.metametadata.DefVar;
import ecologylab.semantics.metametadata.FieldParser;
import ecologylab.semantics.metametadata.FieldParserElement;
import ecologylab.semantics.metametadata.FieldParserForRegexSplit;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataNestedField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.types.ScalarType;

/**
 * This is the base class for the all the document type which we create using meta-metadata.
 * 
 * @author amathur
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class ParserBase<D extends Document> extends HTMLDOMParser<D> implements ScalarUnmarshallingContext,
		SemanticActionsKeyWords, DeserializationHookStrategy<Metadata, MetadataFieldDescriptor>
{

	protected XPath			xpath;

	protected ParsedURL	truePURL;

	public ParserBase(SemanticsGlobalScope infoCollector)
	{
		super(infoCollector);
		xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * populate associated metadata with the container and handler.
	 * @param dOM 
	 * @param metaMetadata 
	 * @param document 
	 * 
	 * @param handler
	 * @return
	 */
	public abstract Document populateMetadata(
			Document document,
			MetaMetadataCompositeField metaMetadata,
			Node dOM,
			SemanticActionHandler handler) throws IOException;

	public final Document parse(Document document, MetaMetadataCompositeField metaMetadata, org.w3c.dom.Node DOM) throws IOException
	{
		// init
		SemanticActionHandler handler = new SemanticActionHandler(semanticsScope, this);
		instantiateSemanticActionXPathVars(handler);
		truePURL = document.getLocation();

		// build the metadata object
		Document resultingMetadata = populateMetadata(document, metaMetadata, DOM, handler);
		resultingMetadata.setMetadataChanged(true);

		try
		{
			debug("Metadata parsed from: " + document.getLocation());
			if (resultingMetadata != null)
			{
				debug(SimplTypesScope.serialize(resultingMetadata, StringFormat.XML));
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (resultingMetadata != null)
		{
			handler.takeSemanticActions(resultingMetadata);

			// make sure termVector is built here
			resultingMetadata.rebuildCompositeTermVector();
			
			// linking
			MetaMetadataRepository metaMetaDataRepository = semanticsScope.getMetaMetadataRepository();
			LinkedMetadataMonitor monitor = metaMetaDataRepository.getLinkedMetadataMonitor();
			monitor.tryLink(metaMetaDataRepository, resultingMetadata);
			monitor.addMonitors(resultingMetadata);
		}

		return  resultingMetadata;
	}
	
	/**
	 * (1) Populate Metadata. (2) Rebuild composite term vector. (3) Take semantic actions.
	 * @throws IOException 
	 */
	@Override
	public void parse() throws IOException
	{
		parse(getDocument(), getMetaMetadata(), getDom());
	}

	/**
	 * Instantiate MetaMetadata variables that are used during XPath information extraction, and in
	 * semantic actions.
	 * 
	 * @param document
	 *          The root of the document
	 */
	private void instantiateSemanticActionXPathVars(SemanticActionHandler handler)
	{
		// get the list of all variable defintions
		ArrayList<DefVar> defVars = getMetaMetadata().getDefVars();

		// get the parameters
		Scope<Object> parameters = handler.getSemanticActionVariableMap();
		
		try
		{
			Node documentRoot = getDom();
			parameters.put(DOCUMENT_ROOT_NODE, documentRoot);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (defVars != null)
		{
			// only if some variables are there we create a DOM[for diect binidng types for others DOM is
			// already there]
			// TODO -- if direct binding, make sure that there are vars that use xPath.
			for (DefVar defVar : defVars)
			{
				String xpathExpression = defVar.getXpath();
				String nodeName 	= defVar.getNode();
				String varName 		= defVar.getName();
				QName varType 		= defVar.getType();
				Node contextNode	= null;
				try
				{
					if (nodeName == null)
					{
						// apply the XPath on the document root.
						contextNode = getDom();
					}
					else
					{
						// get the context node from parameters
						contextNode = (Node) parameters.get(nodeName);
					}

					if (varType != null)
					{
						// apply xpath and get the node list
						if (varType == XPathConstants.NODESET)
						{
							NodeList nList = (NodeList) xpath.evaluate(xpathExpression, contextNode, varType);

							// put the value in the parametrers
							parameters.put(varName, nList);
						}
						else if (varType == XPathConstants.NODE)
						{
							Node n = (Node) xpath.evaluate(xpathExpression, contextNode, varType);

							// put the value in the parametrers
							parameters.put(varName, n);
						}
					}
					else
					{
						// its gonna be a simple string evaluation
						String evaluation = xpath.evaluate(xpathExpression, contextNode);

						// put it into returnValueMap
						parameters.put(varName, evaluation);
					}
				}
				catch (Exception e)
				{
					StringBuilder buffy = StringBuilderUtils.acquire();
					buffy
							.append("################### ERROR IN VARIABLE DEFINTION ##############################\n");
					buffy.append("Variable Name::\t").append(varName).append("\n");
					buffy.append("Check if the context node is not null::\t").append(contextNode)
							.append("\n");
					buffy.append("Check if the XPath Expression is valid::\t").append(xpathExpression)
							.append("\n");
					buffy.append("Check if the return object type of Xpath evaluation is corect::\t").append(
							varType).append("\n");
					debug(buffy);
					StringBuilderUtils.release(buffy);
				}

			}
		}
	}

	/**
	 * This helper class is used for returning information from extractNestedHelper().
	 * 
	 * @author quyin
	 * 
	 */
	private static class NestedFieldHelper
	{
		public Node												node;

		public Map<String, String>				fieldParserContext;

		public NodeList										nodeList;

		public List<Map<String, String>>	fieldParserContextList;

		private int												listSize	= -1;

		public int getListSize()
		{
			if (listSize < 0)
			{
				if (fieldParserContextList != null)
					listSize = fieldParserContextList.size();
				else if (nodeList != null)
					listSize = nodeList.getLength();
				else
					listSize = 0;
			}
			return listSize;
		}
	}
	
	private boolean isAuthoredChildOf(MetaMetadataField parentField, MetaMetadataField childField)
	{
		if (parentField instanceof MetaMetadataCompositeField && childField.parent() == parentField)
			return true;
		if (parentField instanceof MetaMetadataCollectionField
				&& childField.parent() == ((MetaMetadataCollectionField) parentField).getChildComposite())
			return true;
		return false;
	}

	/**
	 * Recursively extract information from the sub DOM tree rooted at current context node to a given
	 * field on the given metadata, using given meta-metadata field information.
	 * 
	 * @param mmdField
	 *          The guiding meta-metadata field, indicating which field of <code>metadata</code>
	 *          should be extracted, and containing extraction rules.
	 * @param metadata
	 *          The metadata object holding the field to be extracted.
	 * @param contextNode
	 *          The context node for extraction.
	 * @param fieldParserContext
	 *          The context of field parsers, if any.
	 * @param params
	 *          The scope containing variables during parsing and semantic actions
	 * @return true if some information is extracted, and every required field has value. false if
	 *         nothing is extracted or a required field doesn't have value.
	 */
	protected boolean recursiveExtraction(MetaMetadataField mmdField, Metadata metadata,
			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
	{
		HashMapArrayList<String, MetaMetadataField> fieldSet = mmdField.getChildMetaMetadata();
		if (fieldSet == null || fieldSet.isEmpty())
			return false;
		
		params.put(SURROUNDING_META_METADATA_FIELD, mmdField);

		synchronized (fieldSet)
		{
			for (MetaMetadataField field : fieldSet)
			{
				if (!isAuthoredChildOf(mmdField, field))
				{
					// if 'field' is purely inherited, we ignore it to prevent infinite loops.
					// infinite loops can happen when 'field' uses the same mmd type as where it is defined,
					// e.g. google_patent.references are google_patent too.
					// this behavior is not necessarily required to prevent infinite loops, but it works
					// for our use cases now.
					// -- yin qu, 2/21/2012
					continue;
				}
				
				try
				{
					boolean suc = false;
					if (field instanceof MetaMetadataCompositeField)
					{
						MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) field;
						suc = extractComposite(mmcf, metadata, contextNode, fieldParserContext, params);
					}
					else if (field instanceof MetaMetadataCollectionField)
					{
						MetaMetadataCollectionField mmcf = (MetaMetadataCollectionField) field;
						if (mmcf != null)
							suc = extractCollection(mmcf, metadata, contextNode, fieldParserContext, params);
					}
					else
					{
						// scalar
						MetaMetadataScalarField mmsf = (MetaMetadataScalarField) field;
						suc = extractScalar(mmsf, metadata, contextNode, fieldParserContext, params);
					}
					if (field.isRequired() && !suc)
						return false;
				}
				catch (Exception e)
				{
					error(String.format("EXCEPTION when extracting %s: %s", field, e.getMessage()));
					e.printStackTrace();
				}
			}
		}

		return true;
	}

	/**
	 * This helper method builds the context for extracting a nested field, e.g. context node and
	 * field parser context.
	 * 
	 * @param mmdField
	 * @param contextNode
	 * @param fieldParserContext
	 * @param params
	 * @return A helper object holding necessary information, or null if no information is obtained.
	 */
	private NestedFieldHelper extractNestedHelper(MetaMetadataNestedField mmdField, Node contextNode,
			Map<String, String> fieldParserContext, Scope<Object> params)
	{
		// get xpath, context node, field parser defintion & key: basic information for following
		
		String xpathString = mmdField.getXpath();
		
//		if (xpathString != null && xpathString.length() > 0)		
//			xpathString = provider.xPathTagNamesToLower(xpathString);
		
		contextNode = findContextNodeIfNecessary(mmdField, contextNode, params);
		
		FieldParserElement fieldParserElement = mmdField.getFieldParserElement();
		String fieldParserKey = mmdField.getFieldParserKey();

		// init result
		NestedFieldHelper result = new NestedFieldHelper();

		if (mmdField instanceof MetaMetadata) // this should not happen, currently
		{
			result.node = contextNode;
			return result;
		}

		try
		{
			if (contextNode != null)
			{
				if ((xpathString == null || xpathString.length() == 0)
						&& mmdField.parent() == params.get(SURROUNDING_META_METADATA_FIELD))
				{
					// the condition above after '&&' holds when this field is actually authored there,
					// but not purely inherited.
					xpathString = ".";
				}
				
				if (xpathString != null)
				{
					// if at this point of time xpathString is null, this field must be purely inherited,
					// thus we may want to ignore it.
					// this behavior, as documented in recursiveExtraction(), is not necessarily required.
					// it basically prevents xpaths to be inherited by a subtype meta-metadata.
					// further extension may allow this inheritance, e.g. by explicitly saying 'I want to
					// inherit xpaths from the super wrapper', using some attribute on <meta-metadata>.
					// -- yin qu, 2/23/2012
					if (mmdField instanceof MetaMetadataCompositeField)
						result.node = (Node) xpath.evaluate(xpathString, contextNode, XPathConstants.NODE);
					else if (mmdField instanceof MetaMetadataCollectionField)
						result.nodeList = (NodeList) xpath.evaluate(xpathString, contextNode, XPathConstants.NODESET);
				}
			}
			
			if (fieldParserElement != null)
			{
				FieldParser fieldParser = this.getSemanticsScope().getFieldParserFactory().get(fieldParserElement.getName());

				if (mmdField instanceof MetaMetadataCompositeField)
				{
					String valueString = null;
					if (fieldParserKey != null && fieldParserKey.length() > 0)
						valueString = getFieldParserValueByKey(fieldParserContext, fieldParserKey);
					else if (result.node != null)
					{
						if (mmdField.isExtractAsHtml())
							valueString = getInnerHtml(result.node);
						else
							valueString = result.node.getTextContent();
					}
						
					if (valueString != null && valueString.length() > 0)
						result.fieldParserContext = fieldParser.getKeyValuePairResult(fieldParserElement, valueString.trim());
				}
				else if (mmdField instanceof MetaMetadataCollectionField)
				{
					if (!((MetaMetadataCollectionField) mmdField).isCollectionOfScalars() && fieldParserElement.isForEachElement())
					{
						result.fieldParserContextList = new ArrayList<Map<String,String>>();
						for (int i = 0; i < result.nodeList.getLength(); ++i)
						{
							Node node = result.nodeList.item(i);
							String valueString = null;
							if (mmdField.isExtractAsHtml())
							{
								valueString = getInnerHtml(node);
							}
							else
							{
								valueString = node.getTextContent();
							}
							if (valueString != null && valueString.length() > 0)
							{
								Map<String, String> aContext = fieldParser.getKeyValuePairResult(fieldParserElement, valueString.trim());
								result.fieldParserContextList.add(aContext);
							}
						}
					}
					else
					{
						String valueString = null;
						if (fieldParserKey != null && fieldParserKey.length() > 0)
							valueString = getFieldParserValueByKey(fieldParserContext, fieldParserKey);
						else if (result.nodeList != null && result.nodeList.getLength() >= 1)
						{
							if (mmdField.isExtractAsHtml())
								valueString = getInnerHtml(result.nodeList.item(0));
							else
								valueString = result.nodeList.item(0).getTextContent();
						}
						
						if (valueString != null && valueString.length() > 0)
							result.fieldParserContextList = fieldParser.getCollectionResult(fieldParserElement, valueString.trim());
					}
				}
			}
		}
		catch (Exception e)
		{
			String msg = getErrorMessage(mmdField, contextNode, xpathString, e);
			debug(msg);
			e.printStackTrace();
		}

		if (result.node == null && result.nodeList == null && result.fieldParserContext == null && result.fieldParserContextList == null)
			return null;

		return result;
	}

	private Node findContextNodeIfNecessary(MetaMetadataField mmdField, Node currentContextNode,
			Scope<Object> params)
	{
		String contextNodeName = mmdField.getContextNode();
		if (contextNodeName != null)
		{
			currentContextNode = (Node) params.get(contextNodeName);
		}
		if (currentContextNode == null)
		{
			currentContextNode = (Node) params.get(DOCUMENT_ROOT_NODE);
		}
		return currentContextNode;
	}
	
	private String getFieldParserValueByKey(Map<String, String> fieldParserContext, String fieldParserKey)
	{
		int pos = fieldParserKey.indexOf('|');
		if (pos < 0)
			return fieldParserContext.get(fieldParserKey);
		String[] keys = fieldParserKey.split("\\|");
		for (String key : keys)
			if (fieldParserContext.containsKey(key))
				return fieldParserContext.get(key);
		return null;
	}

	/**
	 * Extract a composite field of the given metadata object.
	 * 
	 * @param mmdField
	 * @param metadata
	 * @param contextNode
	 * @param fieldParserContext
	 * @param params
	 * @return
	 */
	private boolean extractComposite(MetaMetadataCompositeField mmdField, Metadata metadata,
			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
	{
		NestedFieldHelper helper = extractNestedHelper(mmdField, contextNode, fieldParserContext,
				params);
		if (helper == null)
			return false;

		// will be used for child fields
		Node thisNode = helper.node;
		Map<String, String> thisFieldParserContext = helper.fieldParserContext;

		// create a metadata instance for this field
		Class<? extends Metadata> metadataClass = mmdField.getMetadataClass();
		Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
		Object[] argObjects = new Object[] { mmdField };
		Metadata thisMetadata = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
		thisMetadata.setSemanticsSessionScope(semanticsScope);
		
		if (recursiveExtraction(mmdField, thisMetadata, thisNode, thisFieldParserContext, params))
		{
			Document downloadedMetadata = lookupDownloadedDocument(thisMetadata);
			if (downloadedMetadata != null)
			{
				thisMetadata = downloadedMetadata;
			}
			
			thisMetadata.setMetaMetadata(mmdField);
			lookupTrueMetaMetadata(mmdField.getRepository(), thisMetadata);
			// TODO check for polymorphism. if this is an inherent polymorphic fields, we may need to
			// replace thisMetadata completely if its type changes.
			
			// here everything seems ok. assign result composite back to input metadata object
			Field javaField = mmdField.getMetadataFieldDescriptor().getField();
			ReflectionTools.setFieldValue(metadata, javaField, thisMetadata);

			// try to link result metadata
			MetaMetadataRepository repository = semanticsScope.getMetaMetadataRepository();
			LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
			monitor.tryLink(repository, thisMetadata);

			return true;
		}

		return false;
	}
	
	/**
	 * looking at the global document collection, and reuse exising document object if it is already
	 * downloaded.
	 * 
	 * @param metadata
	 * @return
	 */
	protected Document lookupDownloadedDocument(Metadata metadata)
	{
		if (metadata instanceof Document)
		{
			Document doc = (Document) metadata;
			ParsedURL location = doc.getLocationOrFirstAdditionLocation();
			if (location != null)
			{
				Document existingDoc = semanticsScope.lookupDocument(location);
				if (existingDoc != null && existingDoc.getDownloadStatus() == DownloadStatus.DOWNLOAD_DONE)
				{
					return existingDoc;
				}
			}
		}
		return null;
	}

	/**
	 * if we got a compound document, we may want to look up its true meta-metadata type by location.
	 * before doing connect(), what we can do to find out the true meta-metadata type is quite limited
	 * (location, suffix, tag name). here we do location & suffix. tag name is mainly used by direct
	 * binding cases.
	 * 
	 * @param repository
	 * @param thisMetadata
	 */
	protected void lookupTrueMetaMetadata(MetaMetadataRepository repository, Metadata thisMetadata)
	{
		if (thisMetadata instanceof CompoundDocument)
		{
			ParsedURL thisMetadataLocation = thisMetadata.getLocation();
			if (thisMetadataLocation != null)
			{
				MetaMetadata locMmd = repository.getCompoundDocumentMM(thisMetadataLocation);
				if (locMmd != null && !locMmd.getName().equals(DocumentParserTagNames.COMPOUND_DOCUMENT_TAG))
				{
					debug("changing meta-metadata for extract value " + thisMetadata + " to " + locMmd);
					thisMetadata.setMetaMetadata(locMmd);
				}
			}
		}
	}

	/**
	 * Extract a collection field of the given metadata object.
	 * 
	 * @param mmdField
	 * @param metadata
	 * @param contextNode
	 * @param fieldParserContext
	 * @param params
	 * @return true if the result collection is not empty, or false. The result collection field will
	 *         not contain null references or failed elements (elements that has no actual information
	 *         or lacks required field values).
	 */
	private boolean extractCollection(MetaMetadataCollectionField mmdField, Metadata metadata,
			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
	{
		NestedFieldHelper helper = extractNestedHelper(mmdField, contextNode, fieldParserContext,
				params);
		if (helper == null)
			return false;

		// will be used for child fields
		NodeList nodeList = helper.nodeList;
		List<Map<String, String>> fieldParserContextList = helper.fieldParserContextList;
		int size = helper.getListSize();

		// get class of elements in the collection
		SimplTypesScope tscope = semanticsScope.getMetadataTranslationScope();
		Class elementClass = null;
		MetadataScalarType scalarType = null;
		if (mmdField.isCollectionOfScalars())
		{
			// registered at MetadataScalarScalarType.init()
			ScalarType theScalarType = mmdField.getChildScalarType();
			if (theScalarType != null && theScalarType instanceof MetadataScalarType)
			{
				scalarType = (MetadataScalarType) theScalarType;
				elementClass = scalarType.getJavaClass();
			}
			else
			{
				// error!
				throw new RuntimeException("child_scalar_type not specified or registered: " + mmdField);
			}
		}
		else
		{
//			elementClass = tscope.getClassByTag(mmdField.getChildType());
			ClassDescriptor elementClassDescriptor = mmdField.getMetadataFieldDescriptor().getElementClassDescriptor();
			if (elementClassDescriptor != null)
				elementClass = elementClassDescriptor.getDescribedClass();
		}
		
		if (elementClass == null)
		{
			// we cannot determine the class of this collection. this may be due to lack of type
			// specification there, but it may also be correct, e.g. for polymorphic fields
			return false;
		}

		// build the result list and populate
		ArrayList elements = new ArrayList();
		Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
		Object[] argObjects = new Object[] { mmdField.getChildComposite() };
		String[] fieldParserContextValues = null;
		for (int i = 0; i < size; ++i)
		{
			Node thisNode = (nodeList == null) ? null : nodeList.item(i);
			Map<String, String> thisFieldParserContext = (fieldParserContextList == null) ? null
					: fieldParserContextList.get(i);
			
			if (!mmdField.isCollectionOfScalars())
			{
				Metadata element = (Metadata) ReflectionTools.getInstance(elementClass, argClasses, argObjects);
				element.setSemanticsSessionScope(semanticsScope);
				
//				if (recursiveExtraction(mmdField.getChildComposite(), element, thisNode, thisFieldParserContext, params))
				if (recursiveExtraction(mmdField, element, thisNode, thisFieldParserContext, params))
				{
					Document downloadedDocument = lookupDownloadedDocument(element);
					if (downloadedDocument != null)
					{
						element = downloadedDocument;
					}
					
					element.setMetaMetadata(mmdField);
					lookupTrueMetaMetadata(mmdField.getRepository(), element);
					// TODO check for polymorphism. if this is an inherent polymorphic fields, we may need to
					// replace element completely if its type changes.
					
					elements.add(element);
				}
			}
			else
			{
				String value = null;
				if (fieldParserContextList != null)
					value = thisFieldParserContext == null ? null : thisFieldParserContext.get(FieldParserForRegexSplit.DEFAULT_KEY);
				else if (thisNode != null)
				{
					if (mmdField.isExtractAsHtml())
					{
						value = getInnerHtml(thisNode);
					}
					else
					{
						value = thisNode.getTextContent();
					}
				}
				
				if (value != null)
				{
					MetadataBase element;
					element = (MetadataBase) scalarType.getInstance(value, null, this);
					if (element != null)
						elements.add(element);
				}
			}
		}

		// if more than 0 elements are extracted, assign the collection back
		if (elements.size() > 0)
		{
			Field javaField = mmdField.getMetadataFieldDescriptor().getField();
			ReflectionTools.setFieldValue(metadata, javaField, elements);
			return true;
		}

		return false;
	}
	
	private static Properties	innerHtmlProps	= new Properties();
	static
	{
		innerHtmlProps.put(OutputKeys.METHOD, "html");
		innerHtmlProps.put(OutputKeys.INDENT, "yes");
	}
	
	/**
	 * using javax.xml.transform.Transformer to get the inner HTML of a node.
	 * 
	 * @param node
	 * @return
	 */
	private String getInnerHtml(Node node)
	{
		node.normalize();
		StringWriter w = new StringWriter();
		try
		{
			Transformer t = XmlTransformerPool.get().acquire();
			t.setOutputProperties(innerHtmlProps);
			t.transform(new DOMSource(node), new StreamResult(w));
			XmlTransformerPool.get().release(t);
		}
		catch (TransformerConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return w.toString();
	}

	/**
	 * Extract a scalar field of a given metadata object.
	 * 
	 * @param mmdField
	 * @param metadata
	 * @param contextNode
	 * @param fieldParserContext
	 * @param params
	 * @return true if the scalar value is not null / empty, or false. If &lt;filter&gt; defined it
	 *         will be applied before checking null / empty value.
	 */
	private boolean extractScalar(MetaMetadataScalarField mmdField, Metadata metadata,
			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
	{
		String xpathString = mmdField.getXpath();
//		if (xpathString != null)
//			xpathString = this.provider.xPathTagNamesToLower(xpathString);
		String fieldParserKey = mmdField.getFieldParserKey();
		
		contextNode = findContextNodeIfNecessary(mmdField, contextNode, params);

		String evaluation = null;
		if (xpathString != null && xpathString.length() > 0 && contextNode != null && fieldParserKey == null)
		{
			try
			{
				if (mmdField.isExtractAsHtml())
				{
					Node targetNode = (Node) xpath.evaluate(xpathString, contextNode, XPathConstants.NODE);
					if (targetNode != null)
						evaluation = getInnerHtml(targetNode);
				}
				else
				{
					evaluation = xpath.evaluate(xpathString, contextNode);
				}
//				debug("Evaluated : " + xpathString + " to :" + evaluation);
			}
			catch (Exception e)
			{
				String msg = getErrorMessage(mmdField, contextNode, xpathString, e);
				debug(msg);
				e.printStackTrace();
			}
		}
		else if (fieldParserKey != null)
		{
			evaluation = fieldParserContext.get(fieldParserKey);
		}
		else
			return false;

		// after we have evaluated the expression we might need to modify it.
		evaluation = applyPrefixAndRegExOnEvaluation(evaluation, mmdField);
		if (StringTools.isNullOrEmpty(evaluation))
			return false;

//		metadata.setByTagName(mmdField.getTagForTranslationScope(), evaluation, this);
		metadata.setByFieldName(mmdField.getFieldNameInJava(false), evaluation, this);
		return true;
	}

	/**
	 * Generate an error message containing parsing context information.
	 * 
	 * @param mmdField
	 * @param contextNode
	 * @param xpathString
	 * @param e
	 * @return
	 */
	private String getErrorMessage(MetaMetadataField mmdField, Node contextNode, String xpathString,
			Exception e)
	{
		StringBuilder buffy = StringBuilderUtils.acquire();
		buffy.append("################# ERROR IN EVALUATION OF A FIELD########################\n");
		buffy.append("Field Name::\t").append(mmdField.getName()).append("\n");
		buffy.append("ContextNode::\t").append(contextNode.getTextContent()).append("\n");
		buffy.append("XPath Expression::\t").append(xpathString).append("\n");
		buffy.append("Exception Message::\t").append(e.getMessage()).append("\n");
		String msg = buffy.toString();
		StringBuilderUtils.release(buffy);
		return msg;
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
		if (evaluation == null)
			return null;

		// regex replacing should happen only to scalar fields
		if (!(mmdElement instanceof MetaMetadataScalarField))
			return evaluation;

		MetaMetadataScalarField field = (MetaMetadataScalarField) mmdElement;

		// to remove unwanted XML characters
		evaluation = XMLTools.unescapeXML(evaluation);

		// get the regular expression
		Pattern regularExpression = field.getRegexPattern();

		if (regularExpression != null)
		{
			Matcher matcher = regularExpression.matcher(evaluation);
			String replacementString = field.getRegexReplacement();
			if (replacementString == null)
			{
				// without replacementString, we search for the input pattern as the evaluation result
				if (matcher.find())
				{
					evaluation = matcher.group();
//					debug(String.format("regex pattern hit: regex=%s", regularExpression));
				}
				else
				{
					evaluation = ""; // because nothing matched, we return an empty string
//					debug(String.format("regex pattern not hit: regex=%s", regularExpression));
				}
			}
			else
			{
				// with replacementString, we replace occurrences of input pattern with the replacementString
				evaluation = matcher.replaceAll(replacementString);
//				debug(String.format("regex replacement: regex=%s, replace=%s", regularExpression, replacementString));
			}
		}

		// remove white spaces if any
		evaluation = evaluation.trim();
		return evaluation;
	}

	@Override
	public ParsedURL purlContext()
	{
		return purl();
	}

	@Override
	public File fileContext()
	{
		return null;
	}

	@Override
	public ParsedURL getTruePURL()
	{
		return (truePURL != null) ? truePURL : super.getTruePURL();
	}

	/**
	 * @return Document subclass metadata resulting from s.im.pl deserialization of the input stream.
	 * @throws IOException 
	 */
	protected Document directBindingPopulateMetadata() throws IOException
	{
		Document newDocument = null;
		try
		{
			// this must be a top-level metadata object (i.e. not a field)
			// thus it must have a MetaMetadata attached (i.e. not a MetaMetadataCompositeField)
			// thus this conversion is safe
			MetaMetadata metaMetadata = (MetaMetadata) this.getMetaMetadata();
			
			SimplTypesScope tscope = metaMetadata.getLocalMetadataTranslationScope();
			newDocument = (Document) tscope.deserialize(purlConnection.getPurl(), this, Format.XML);
			
			SimplTypesScope.serialize(newDocument, System.out,  StringFormat.XML);
			
			System.out.println();
			// the old document is basic, so give it basic meta-metadata (so recycle does not tank)
			Document oldDocument	= documentClosure.getDocument();
			oldDocument.setMetaMetadata(semanticsScope.DOCUMENT_META_METADATA);
			documentClosure.changeDocument(newDocument);

			System.out.println();
		}
		catch (SIMPLTranslationException e)
		{
			warning("Direct binding failed " + e);
		}
		return newDocument;
	}

	Stack<MetaMetadataNestedField>	currentMMstack	= new Stack<MetaMetadataNestedField>();

	boolean deserializingRoot	= true;
	/**
	 * For the root, compare the meta-metadata from the binding with the one we started with. Down the
	 * hierarchy, try to perform similar bindings.
	 */
	@Override
	public void deserializationPreHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
	{
		if (deserializingRoot)
		{
			deserializingRoot													= false;
			Document document													= getDocument();
			MetaMetadataCompositeField preMM					= document.getMetaMetadata();
			MetadataClassDescriptor mcd								= (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(deserializedMetadata);;
			MetaMetadataCompositeField metaMetadata;
			String tagName 														= mcd.getTagName();
			if (preMM.getTagForTranslationScope().equals(tagName))
			{
				metaMetadata														= preMM;
			}
			else
			{	// just match in translation scope
				//TODO use local TranslationScope if there is one
				metaMetadata														= semanticsScope.getMetaMetadataRepository().getMMByName(tagName);
			}
			deserializedMetadata.setMetaMetadata(metaMetadata);

			currentMMstack.push(metaMetadata);
		}
		else
		{
			String mmName = mfd.getMmName();
			MetaMetadataNestedField currentMM = currentMMstack.peek();
			MetaMetadataNestedField childMMNested = (MetaMetadataNestedField) currentMM
					.lookupChild(mmName); // this fails for collections :-(
			if (childMMNested == null)
				throw new RuntimeException("Can't find composite child meta-metadata for " + mmName + " amidst "+ mfd +
						"\n\tThis probably means there is a conflict between the meta-metadata repository and the runtime."+
						"\n\tProgrammer: Have you Changed the fields in built-in Metadata subclasses without updating primitives.xml???!");
			MetaMetadataCompositeField childMMComposite = null;
			if (childMMNested.isPolymorphicInherently())
			{
				String tagName = ClassDescriptor.getClassDescriptor(deserializedMetadata).getTagName();
				childMMComposite	= semanticsScope.getMetaMetadataRepository().getMMByName(tagName);
			}
			else
			{
				childMMComposite	= childMMNested.metaMetadataCompositeField();
			}
			deserializedMetadata.setMetaMetadata(childMMComposite);
			currentMMstack.push(childMMComposite);
		}
	}

	@Override
	public void deserializationPostHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
	{
		currentMMstack.pop();
	}

}
