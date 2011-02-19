package ecologylab.semantics.documentparsers;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.LinkedMetadataMonitor;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.DefVar;
import ecologylab.semantics.metametadata.FieldParser;
import ecologylab.semantics.metametadata.FieldParserElement;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataNestedField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;

/**
 * This is the base class for the all the document type which we create using meta-metadata.
 * 
 * @author amathur
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class ParserBase extends HTMLDOMParser implements ScalarUnmarshallingContext,
		SemanticActionsKeyWords, DeserializationHookStrategy<Metadata, MetadataFieldDescriptor>
{

	protected XPath			xpath;

	protected ParsedURL	truePURL;

	public ParserBase(InfoCollector infoCollector)
	{
		super(infoCollector);
		xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * @return the metadataTranslationScope
	 */
	public final TranslationScope getMetadataTranslationScope()
	{
		return container.getGeneratedMetadataTranslationScope();
	}

	/**
	 * populate associated metadata with the container and handler.
	 * 
	 * @param handler
	 * @return
	 */
	public abstract Document populateMetadata(SemanticActionHandler handler);

	/**
	 * (1) Populate Metadata. (2) Rebuild composite term vector. (3) Take semantic actions.
	 */
	protected final void postParse()
	{
		super.postParse();

		// init
		SemanticActionHandler handler = new SemanticActionHandler(infoCollector, this);
		handler.getSemanticActionVariableMap().put(SemanticActionsKeyWords.PURLCONNECTION_MIME,
				purlConnection.mimeType());
		instantiateMetaMetadataVariables(handler);
		truePURL = container.purl();

		// build the metadata object
		Metadata populatedMetadata = populateMetadata(handler);
		populatedMetadata.setMetadataChanged(true);

		try
		{
			debug("Metadata parsed from: " + container.purl());
			if (populatedMetadata != null)
			{
				debug(populatedMetadata.serialize());
			}
		}
		catch (Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// FIXME -- should be able to get rid of this step here. its way too late!
		// if the metametadata reference is null, assign the correct metametadata object to it.
		if (populatedMetadata.getMetaMetadata() == null)
		{
			warning("meta-metadata not set after populating!!!!!");
			populatedMetadata.setMetaMetadata(metaMetadata);
		}

		// make sure termVector is built here
		populatedMetadata.rebuildCompositeTermVector();

		if (populatedMetadata != null)
			handler.takeSemanticActions((MetaMetadata) metaMetadata, populatedMetadata);
	}

	/**
	 * Instantiate MetaMetadata variables that are used during XPath information extraction, and in
	 * semantic actions.
	 * 
	 * @param document
	 *          The root of the document
	 */
	private void instantiateMetaMetadataVariables(SemanticActionHandler handler)
	{
		// get the list of all variable defintions
		ArrayList<DefVar> defVars = metaMetadata.getDefVars();

		// get the parameters
		Scope<Object> parameters = handler.getSemanticActionVariableMap();
		if (defVars != null)
		{
			// only if some variables are there we create a DOM[for diect binidng types for others DOM is
			// already there]
			// TODO -- if direct binding, make sure that there are vars that use xPath.
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
						contextNode = getDom();
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
	 * This helper class is used for returning information from extractNestedHelper().
	 * 
	 * @author quyin
	 * 
	 */
	private static class NestedFieldHelper
	{
		public Node												node;

		public Map<String, String>				fieldParserContext;

		public DTMNodeList								nodeList;

		public List<Map<String, String>>	fieldParserContextList;

		private int												listSize	= -1;

		public int getListSize()
		{
			if (listSize < 0)
			{
				if (nodeList != null)
					listSize = nodeList.getLength();
				else if (fieldParserContextList != null)
					listSize = fieldParserContextList.size();
				else
					listSize = 0;
			}
			return listSize;
		}
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

		synchronized (fieldSet)
		{
			for (MetaMetadataField field : fieldSet)
			{
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
		if (xpathString != null && xpathString.isEmpty())
			xpathString = null;
		String contextNodeName = mmdField.getContextNode();
		if (contextNodeName != null)
		{
			contextNode = (Node) params.get(contextNodeName);
		}
		FieldParserElement fieldParserElement = mmdField.getFieldParserElement();
		String fieldParserKey = mmdField.getFieldParserKey();

		// init result
		NestedFieldHelper result = new NestedFieldHelper();

		if (mmdField instanceof MetaMetadata)
		{
			result.node = contextNode;
			return result;
		}

		try
		{
			// extract information to prepare for recursive descent
			if (xpathString != null && contextNode != null && fieldParserElement == null)
			{
				// use xpath for child fields
				if (mmdField instanceof MetaMetadataCompositeField)
					result.node = (Node) xpath.evaluate(xpathString, contextNode, XPathConstants.NODE);
				else if (mmdField instanceof MetaMetadataCollectionField)
					result.nodeList = (DTMNodeList) xpath.evaluate(xpathString, contextNode,
							XPathConstants.NODESET);
			}
			else if (xpathString != null && contextNode != null && fieldParserElement != null)
			{
				// use xpath to get the string to feed field parser, and use field parser for child fields

				// retrieve pre-defined parsers
				FieldParser fieldParser = FieldParser.get(fieldParserElement.getName());

				if (mmdField instanceof MetaMetadataCompositeField)
				{
					String valueString = xpath.evaluate(xpathString, contextNode);
					if (valueString != null && !valueString.isEmpty())
						result.fieldParserContext = fieldParser.getKeyValuePairResult(fieldParserElement,
								valueString);
				}
				else if (mmdField instanceof MetaMetadataCollectionField)
				{
					String valueString = xpath.evaluate(xpathString, contextNode);
					if (valueString != null && !valueString.isEmpty())
						result.fieldParserContextList = fieldParser.getCollectionResult(fieldParserElement,
								valueString);
				}
			}
			else if (fieldParserElement != null && fieldParserKey != null && !fieldParserKey.isEmpty())
			{
				// use field parser key to look up the value string and use field parser for child fields

				// retrieve pre-defined parsers
				FieldParser fieldParser = FieldParser.get(fieldParserElement.getName());

				if (mmdField instanceof MetaMetadataCompositeField)
				{
					String valueString = fieldParserContext.get(fieldParserKey);
					if (valueString != null && !valueString.isEmpty())
						result.fieldParserContext = fieldParser.getKeyValuePairResult(fieldParserElement,
								valueString);
				}
				else if (mmdField instanceof MetaMetadataCollectionField)
				{
					String valueString = fieldParserContext.get(fieldParserKey);
					if (valueString != null && !valueString.isEmpty())
						result.fieldParserContextList = fieldParser.getCollectionResult(fieldParserElement,
								valueString);
				}
			}
			else
				return null;
		}
		catch (Exception e)
		{
			String msg = getErrorMessage(mmdField, contextNode, xpathString, e);
			debug(msg);
			e.printStackTrace();
		}

		if (result.node == null && result.nodeList == null && result.fieldParserContext == null
				&& result.fieldParserContextList == null)
			return null;

		return result;
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

		if (recursiveExtraction(mmdField, thisMetadata, thisNode, thisFieldParserContext, params))
		{
			// here everything seems ok. assign result composite back to input metadata object
			Field javaField = mmdField.getMetadataFieldDescriptor().getField();
			ReflectionTools.setFieldValue(metadata, javaField, thisMetadata);

			// try to link result metadata
			MetaMetadataRepository repository = metaMetadata.getRepository();
			LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
			monitor.tryLink(repository, thisMetadata);

			return true;
		}

		return false;
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
		TranslationScope tscope = getMetadataTranslationScope();
		Class elementClass = null;
		if (mmdField.isChildEntity())
		{
			elementClass = tscope.getClassByTag(DocumentParserTagNames.ENTITY);
		}
		else
		{
			elementClass = tscope.getClassByTag(mmdField.determineCollectionChildType());
		}

		// build the result list and populate
		ArrayList<Metadata> elements = new ArrayList<Metadata>();
		Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
		Object[] argObjects = new Object[] { mmdField.getChildComposite() };
		for (int i = 0; i < size; ++i)
		{
			Metadata element = (Metadata) ReflectionTools.getInstance(elementClass, argClasses,
					argObjects);
			Node thisNode = (nodeList == null) ? null : nodeList.item(i);
			Map<String, String> thisFieldParserContext = (fieldParserContextList == null) ? null
					: fieldParserContextList.get(i);
			if (recursiveExtraction(mmdField, element, thisNode, thisFieldParserContext, params))
				elements.add(element);
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
		String fieldParserKey = mmdField.getFieldParserKey();

		String evaluation = null;
		if (xpathString != null && contextNode != null && fieldParserKey == null)
		{
			try
			{
				evaluation = xpath.evaluate(xpathString, contextNode);
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
		if (evaluation == null || evaluation.isEmpty())
			return false;

		metadata.setByTagName(mmdField.getTagForTranslationScope(), evaluation, this);
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
		buffy.append("ContextNode::\t").append(contextNode.getNodeValue()).append("\n");
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
		String regularExpression = field.getRegexPattern();

		if (regularExpression != null)
		{
			// create a pattern based on regular expression
			Pattern pattern = Pattern.compile(regularExpression);
			// create a matcher based on input string
			Matcher matcher = pattern.matcher(evaluation);

			// TODO right now we r using regular expressions just to replace the
			// matching string we might use them for more better uses.
			// get the replacement thing.
			String replacementString = field.getRegexReplacement();
			if (replacementString == null)
				replacementString = "";
			debug(String.format("regex replacement: regex=%s, replace=%s", regularExpression,
					replacementString));

			// Consecutively check for further matches. Replacing all with the replacementString
			evaluation = matcher.replaceAll(replacementString);
		}

		// remove white spaces if any
		evaluation = evaluation.trim();
		return evaluation;
	}

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

	/**
	 * @return Document subclass metadata resulting from s.im.pl deserialization of the input stream.
	 */
	protected Document directBindingPopulateMetadata()
	{
		Document populatedMetadata = null;
		try
		{
			populatedMetadata = (Document) getMetadataTranslationScope()
					.deserialize(purlConnection, this);
			populatedMetadata.serialize(System.out);
			System.out.println();
		}
		catch (SIMPLTranslationException e)
		{
			warning("Direct binding failed " + e);
		}
		return populatedMetadata;
	}

	/**
	 * @param metadataFromDerialization
	 */
	private boolean bindMetaMetadataToMetadata(MetaMetadataField deserializationMM,
			MetaMetadataField originalMM)
	{
		if (deserializationMM != null) // should be always
		{
			MetadataClassDescriptor originalClassDescriptor = originalMM.getMetadataClassDescriptor();
			MetadataClassDescriptor deserializationClassDescriptor = deserializationMM
					.getMetadataClassDescriptor();

			// quick fix for a NullPointerException for RSS. originalClassDescriptor can be null because
			// it might be a meta-metadata that does not generate metadata class, e.g. xml
			if (originalClassDescriptor == null)
				return true; // use the one from deserialization

			boolean sameMetadataSubclass = originalClassDescriptor.equals(deserializationClassDescriptor);
			// if they have the same metadataClassDescriptor, they can be of the same type, or one
			// of them is using "type=" attribute.
			boolean useMmdFromDeserialization = sameMetadataSubclass
					&& (deserializationMM.getType() != null);
			if (!useMmdFromDeserialization && !sameMetadataSubclass)
				// if they have different metadataClassDescriptor, need to choose the more specific one
				useMmdFromDeserialization = originalClassDescriptor.getDescribedClass().isAssignableFrom(
						deserializationClassDescriptor.getDescribedClass());
			return useMmdFromDeserialization;
		}
		else
		{
			error("No meta-metadata in root after direct binding :-(");
			return false;
		}
	}

	Stack<MetaMetadataNestedField>	currentMMstack	= new Stack<MetaMetadataNestedField>();

	/**
	 * For the root, compare the meta-metadata from the binding with the one we started with. Down the
	 * hierarchy, try to perform similar bindings.
	 */
	@Override
	public void deserializationPreHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
	{
		if (deserializedMetadata.parent() == null)
		{
			MetaMetadataCompositeField deserializationMM = (MetaMetadata) deserializedMetadata
					.getMetaMetadata();
			if (bindMetaMetadataToMetadata(deserializationMM, metaMetadata))
			{
				metaMetadata = (MetaMetadata) deserializationMM;
			}
			else
			{
				deserializedMetadata.setMetaMetadata(metaMetadata);
			}
			currentMMstack.push(metaMetadata);
		}
		else
		{
			String mmName = mfd.getMmName();
			MetaMetadataNestedField currentMM = currentMMstack.peek();
			MetaMetadataNestedField childMMNested = (MetaMetadataNestedField) currentMM
					.lookupChild(mmName); // this fails for collections :-(
			MetaMetadataCompositeField childMMComposite = childMMNested.metaMetadataCompositeField();
			deserializedMetadata.setMetaMetadata(childMMComposite);
			currentMMstack.push(childMMComposite);
		}
	}

	public void deserializationPostHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
	{
		currentMMstack.pop();
	}

}
