package ecologylab.bigsemantics.documentparsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.actions.SemanticsConstants;
import ecologylab.bigsemantics.collecting.DocumentDownloadedEventHandler;
import ecologylab.bigsemantics.collecting.DocumentDownloadingMonitor;
import ecologylab.bigsemantics.collecting.DownloadStatus;
import ecologylab.bigsemantics.collecting.LinkedMetadataMonitor;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.MetadataBase;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataParsedURLScalarType;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
import ecologylab.bigsemantics.metametadata.DefVar;
import ecologylab.bigsemantics.metametadata.FieldOp;
import ecologylab.bigsemantics.metametadata.FieldParser;
import ecologylab.bigsemantics.metametadata.FieldParserElement;
import ecologylab.bigsemantics.metametadata.FieldParserForRegexSplit;
import ecologylab.bigsemantics.metametadata.FilterLocation;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCollectionField;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataNestedField;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MetaMetadataScalarField;
import ecologylab.bigsemantics.metametadata.MetaMetadataValueField;
import ecologylab.bigsemantics.metametadata.ScalarDependencyException;
import ecologylab.bigsemantics.metametadata.ScalarDependencyManager;
import ecologylab.bigsemantics.metametadata.declarations.MetaMetadataFieldDeclaration;
import ecologylab.bigsemantics.namesandnums.DocumentParserTagNames;
import ecologylab.collections.Scope;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
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
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ParserBase<D extends Document> extends HTMLDOMParser<D>
implements ScalarUnmarshallingContext, SemanticsConstants
{

  static Logger                   logger = LoggerFactory.getLogger(ParserBase.class);

  protected XPath                 xpath;

  protected ParsedURL             truePURL;

  protected SemanticActionHandler handler;

  public ParserBase()
  {
    super();
    xpath = XPathFactory.newInstance().newXPath();
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
   * populate associated metadata with the container and handler.
   * 
   * @param document
   * @param metaMetadata
   * @param dom
   * 
   * @param handler
   * @return
   */
  public abstract Document populateMetadata(Document document,
                                            MetaMetadataCompositeField metaMetadata,
                                            org.w3c.dom.Document dom,
                                            SemanticActionHandler handler) throws IOException;

  public final Document parse(Document document,
                              MetaMetadataCompositeField metaMetadata,
                              org.w3c.dom.Document dom) throws IOException
  {
    // init
    handler = new SemanticActionHandler(getSemanticsScope(), this);
    truePURL = document.getLocation();
    initializeParameterScope(metaMetadata);

    // build the metadata object
    // DomTools.prettyPrint(DOM);
    Document resultingMetadata = populateMetadata(document, metaMetadata, dom, handler);
    resultingMetadata.setMetadataChanged(true);

    if (this.getSemanticsScope().ifLookForFavicon())
    {
      findFaviconPath(resultingMetadata, xpath);
    }

    try
    {
      logger.info("Metadata parsed from: " + document.getLocation());
      if (resultingMetadata != null)
      {
        logger.debug(SimplTypesScope.serialize(resultingMetadata, StringFormat.XML).toString());
      }
    }
    catch (Exception e)
    {
      logger.error("Cannot serialize extracted metadata in XML: " + resultingMetadata, e);
      return null;
    }

    if (resultingMetadata != null)
    {
      handler.takeSemanticActions(resultingMetadata);

      // make sure termVector is built here
      if (!resultingMetadata.ignoreInTermVector())
      {
        resultingMetadata.rebuildCompositeTermVector();
      }
      else
      {
        logger.debug("Do not build term vector because ignore_in_term_vector is true");
      }

      // linking
      MetaMetadataRepository metaMetaDataRepository =
          getSemanticsScope().getMetaMetadataRepository();
      LinkedMetadataMonitor monitor = metaMetaDataRepository.getLinkedMetadataMonitor();
      monitor.tryLink(metaMetaDataRepository, resultingMetadata);
      monitor.addMonitors(resultingMetadata);
    }

    return resultingMetadata;
  }

  /**
   * (1) Populate Metadata. (2) Rebuild composite term vector. (3) Take semantic actions.
   * 
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
   * @param metaMetadata
   */
  private void initializeParameterScope(MetaMetadataCompositeField metaMetadata)
  {
    Node documentRoot = null;
    try
    {
      documentRoot = getDom();
      if (documentRoot != null)
      {
        Scope<Object> parameters = handler.getSemanticActionVariableMap();
        parameters.put(DOCUMENT_ROOT_NODE, documentRoot);
        updateDefVars(metaMetadata, documentRoot);
      }
    }
    catch (IOException e)
    {
      logger.error("Cannot get DOM for document " + truePURL, e);
    }
  }

  private void updateDefVars(MetaMetadataNestedField nestedField, Node contextNode)
  {
    assert (handler != null);
    Scope<Object> parameters = handler.getSemanticActionVariableMap();
    ArrayList<DefVar> defVars = nestedField.getDefVars();
    if (defVars != null)
    {
      for (DefVar defVar : defVars)
      {
        List<String> xpaths = defVar.getXpaths();
        if (xpaths != null)
        {
          for (String xpath : xpaths)
          {
            if (extractDefVar(defVar, contextNode, xpath, parameters))
            {
              break;
            }
          }
        }
      }
    }
  }

  private boolean extractDefVar(DefVar defVar,
                                Node contextNode,
                                String xpathExpression,
                                Scope<Object> params)
  {
    String varName = defVar.getName();
    QName varType = defVar.getType();

    try
    {
      String contextNodeName = defVar.getContextNode();
      String varValue = defVar.getValue();

      // Does the var have a constant value?
      if (varValue == null)
      {
        // No. Evaluate the Xpath to obtain the value.
        if (contextNodeName != null)
        {
          // get the context node from parameters
          contextNode = (Node) params.get(contextNodeName);
        }

        if (contextNode != null)
        {
          if (varType != null)
          {
            Object evalResult = xpath.evaluate(xpathExpression, contextNode, varType);
            params.put(varName, evalResult);
          }
          else
          {
            // its gonna be a simple string evaluation
            String evaluation = xpath.evaluate(xpathExpression, contextNode);
            params.put(varName, evaluation);
          }
        }
      }
      else
      {
        // If we have a variable value, we'll just use it!
        params.put(varName, varValue);
      }
      
      return true;
    }
    catch (Exception e)
    {
      String msg =
          String.format("Error in <def_var>: contextNode=%s, xpath=%s, returnObjectType=%s",
                        contextNode, xpathExpression, varType);
      logger.error(msg, e);
      return false;
    }
  }

  /**
   * This helper class is used for returning information from extractNestedHelper().
   * 
   * @author quyin
   */
  private static class NestedFieldHelper
  {

    public Node                      node;

    public Map<String, String>       fieldParserContext;

    public NodeList                  nodeList;

    public List<Map<String, String>> fieldParserContextList;

    private int                      listSize = -1;

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
  protected boolean recursiveExtraction(MetaMetadataNestedField mmdField,
                                        Metadata metadata,
                                        Node contextNode,
                                        Map<String, String> fieldParserContext,
                                        Scope<Object> params)
  {
    HashMapArrayList<String, MetaMetadataField> fieldSet = mmdField.getChildrenMap();
    if (fieldSet == null || fieldSet.isEmpty())
    {
      return false;
    }
    pushSurroundingMmd(params, mmdField);
    updateDefVars(mmdField, contextNode);
    boolean result = true;

    synchronized (fieldSet)
    {
      if (fieldsetContainsFieldsWithDependencies(fieldSet))
      {
        try
        {
          ScalarDependencyManager depMan = new ScalarDependencyManager(fieldSet);
          fieldSet = depMan.sortFieldSetByDependencies(metadata);
        }
        catch (ScalarDependencyException e)
        {
          logger.error("Error resolving scalar dependencies; "
                       + "proceeding anyway but will result in null values.",
                       e);
        }
      }

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
            if (mmcf != null)
            {
              suc = extractCollection(mmcf, metadata, contextNode, fieldParserContext, params);
            }
          }
          else
          {
            // scalar
            MetaMetadataScalarField mmsf = (MetaMetadataScalarField) field;
            suc = extractScalar(mmsf, metadata, contextNode, fieldParserContext, params);
          }

          if (field.isRequired() && !suc)
          {
            result = false;
            break;
          }
        }
        catch (Exception e)
        {
          logger.error("Error extracting " + field,e);
        }
      }
    }

    popSurroundingMmd(params);

    return result;
  }

  private void pushSurroundingMmd(Scope<Object> params, MetaMetadataNestedField surroundingMmd)
  {
    Stack<MetaMetadataField> surroundingMmdStack =
        (Stack<MetaMetadataField>) params.get(SURROUNDING_META_METADATA_STACK);
    if (surroundingMmdStack == null)
    {
      surroundingMmdStack = new Stack<MetaMetadataField>();
      params.put(SURROUNDING_META_METADATA_STACK, surroundingMmdStack);
    }
    surroundingMmdStack.push(surroundingMmd);
  }
  
  private void popSurroundingMmd(Scope<Object> params)
  {
    Stack<MetaMetadataField> surroundingMmdStack =
        (Stack<MetaMetadataField>) params.get(SURROUNDING_META_METADATA_STACK);
    if (surroundingMmdStack != null && surroundingMmdStack.size() > 0)
    {
      surroundingMmdStack.pop();
    }
  }

  private MetaMetadataField getCurrentSurroundingField(Scope<Object> params)
  {
    Stack<MetaMetadataField> stack =
        (Stack<MetaMetadataField>) params.get(SURROUNDING_META_METADATA_STACK);
    if (stack != null && stack.size() > 0)
    {
      return stack.peek();
    }
    return null;
  }

  /**
   * Determines if a given collection of metametadata fields contain dependencies (for concatenation
   * or other value semantics) on other mmd fields.
   * 
   * @param fieldSet
   *          The set of metametadata fields to check.
   * @return True if there are dependencies that need to be handled correctly
   */
  private boolean fieldsetContainsFieldsWithDependencies(HashMapArrayList<String, MetaMetadataField> fieldSet)
  {
    for (MetaMetadataField field : fieldSet)
    {
      if (field instanceof MetaMetadataScalarField)
      {
        MetaMetadataScalarField m = (MetaMetadataScalarField) field;
        if (m.hasValueDependencies())
        {
          return true;
        }
      }
    }
    return false;
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
  private NestedFieldHelper extractNestedHelper(MetaMetadataNestedField mmdField,
                                                Node contextNode,
                                                Map<String, String> fieldParserContext,
                                                Scope<Object> params)
  {
    // get context node, field parser definition & key: basic information for following
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
      if (fieldParserElement == null)
      {
        evaluateXpathForField(mmdField, contextNode, params, result);
      }
      else
      {
        FieldParser fieldParser =
            getSemanticsScope().getFieldParserRegistry().get(fieldParserElement.getName());

        if (mmdField instanceof MetaMetadataCompositeField)
        {
          String valueString = null;
          if (fieldParserKey != null && fieldParserKey.length() > 0)
          {
            valueString = getFieldParserValueByKey(fieldParserContext, fieldParserKey);
          }
          else
          {
            evaluateXpathForField(mmdField, contextNode, params, result);
            if (result.node != null)
            {
              if (mmdField.isExtractAsHtml())
              {
                valueString = getInnerHtml(result.node);
              }
              else
              {
                valueString = result.node.getTextContent();
              }
            }
          }

          if (valueString != null && valueString.length() > 0)
          {
            result.fieldParserContext =
                fieldParser.getKeyValuePairResult(fieldParserElement, valueString.trim());
          }
        }
        else if (mmdField instanceof MetaMetadataCollectionField)
        {
          if (!((MetaMetadataCollectionField) mmdField).isCollectionOfScalars()
              && fieldParserElement.isForEachElement())
          {
            evaluateXpathForField(mmdField, contextNode, params, result);
            result.fieldParserContextList = new ArrayList<Map<String, String>>();
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
                Map<String, String> aContext =
                    fieldParser.getKeyValuePairResult(fieldParserElement, valueString.trim());
                result.fieldParserContextList.add(aContext);
              }
            }
          }
          else
          {
            String valueString = null;
            if (fieldParserKey != null && fieldParserKey.length() > 0)
            {
              valueString = getFieldParserValueByKey(fieldParserContext, fieldParserKey);
            }
            else
            {
              evaluateXpathForField(mmdField, contextNode, params, result);
              if (result.nodeList != null && result.nodeList.getLength() >= 1)
              {
                if (mmdField.isExtractAsHtml())
                {
                  valueString = getInnerHtml(result.nodeList.item(0));
                }
                else
                {
                  valueString = result.nodeList.item(0).getTextContent();
                }
              }
            }

            if (valueString != null && valueString.length() > 0)
            {
              result.fieldParserContextList = 
                  fieldParser.getCollectionResult(fieldParserElement, valueString.trim());
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      logger.error("Error extracting " + mmdField + ", contextNode=" + contextNode,
                   e);
    }

    if (result.node == null
        && result.nodeList == null
        && result.fieldParserContext == null
        && result.fieldParserContextList == null)
    {
      return null;
    }
    return result;
  }

  private Node findContextNodeIfNecessary(MetaMetadataField mmdField,
                                          Node currentContextNode,
                                          Scope<Object> params)
  {
    String contextNodeName = mmdField.getContextNode();
    if (contextNodeName != null)
    {
      currentContextNode = (Node) params.get(contextNodeName);
    }
//    if (currentContextNode == null)
//    {
//      currentContextNode = (Node) params.get(DOCUMENT_ROOT_NODE);
//    }
    return currentContextNode;
  }

  /**
   * Evaluate the xpath associated with a field.
   * 
   * For a scalar field, returns the evaluation results as a string.
   * 
   * For a nested field, returns null, but fills the result parameter for outputing.
   * 
   * @param mmdField
   * @param contextNode
   * @param params
   * @param result
   * @return
   * @throws XPathExpressionException
   */
  private String evaluateXpathForField(MetaMetadataField mmdField,
                                       Node contextNode,
                                       Scope<Object> params,
                                       NestedFieldHelper result) throws XPathExpressionException
  {
    String evaluation = null;
    if (contextNode != null)
    {
      MetaMetadataField surroundingField = getCurrentSurroundingField(params);

      int i = 0;
      do
      {
        // This loop need to be executed at least once.

        String xpathString = mmdField.getXpath(i);
        if ((xpathString == null || xpathString.length() == 0)
            && mmdField.parent() == surroundingField
            && mmdField instanceof MetaMetadataNestedField
            && !((MetaMetadataNestedField) mmdField).isPolymorphicInherently())
        {
          // If there is no xpath associated with the current nested field, and
          // the current field is intentionally authored, the runtime will just
          // create the structure, and pass the contextNode to nested fields.
          xpathString = ".";
        }
        
        // at this point, if the xpathString is still null, then:
        // - if this field is a scalar, it has no xpath attached, and can be ignored safely;
        // - if this field is a nested, it has no xpath attached and is purely inherited, thus can
        //   be ignored safely.
  
        if (xpathString != null)
        {
          xpathString = getSemanticsScope().getXPathAmender().amend(xpathString, params);

          if (mmdField instanceof MetaMetadataCompositeField)
          {
            result.node = (Node) xpath.evaluate(xpathString, contextNode, XPathConstants.NODE);
            if (result.node != null)
            {
              return null;
            }
          }
          else if (mmdField instanceof MetaMetadataCollectionField)
          {
            result.nodeList = (NodeList) xpath.evaluate(xpathString,
                                                        contextNode,
                                                        XPathConstants.NODESET);
            if (result.nodeList != null && result.nodeList.getLength() > 0)
            {
              return null;
            }
          }
          else if (mmdField instanceof MetaMetadataScalarField)
          {
            if (mmdField.isExtractAsHtml())
            {
              Node targetNode =
                  (Node) xpath.evaluate(xpathString, contextNode, XPathConstants.NODE);
              if (targetNode != null)
              {
                evaluation = getInnerHtml(targetNode);
              }
            }
            else
            {
              evaluation = xpath.evaluate(xpathString, contextNode);
            }
            if (evaluation != null && evaluation.length() > 0)
            {
              logger.debug("Extraction succeeded for {}, using xpath \"{}\", result={}",
                           mmdField, xpathString, evaluation);
              return evaluation;
            }
          } // composite / collection / scalar
        } // if (xpathString != null)
        
        i++;
      } while (i < mmdField.getXpathsSize());
    }

    return evaluation;
  }

  private String getFieldParserValueByKey(Map<String, String> fieldParserContext,
                                          String fieldParserKey)
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
   * @return if extraction was successful.
   */
  private boolean extractComposite(MetaMetadataCompositeField mmdField,
                                   Metadata metadata,
                                   Node contextNode,
                                   Map<String, String> fieldParserContext,
                                   Scope<Object> params)
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
    thisMetadata.setSemanticsSessionScope(getSemanticsScope());

    if (recursiveExtraction(mmdField, thisMetadata, thisNode, thisFieldParserContext, params))
    {
      if (getSemanticsScope().ifAutoUpdateDocRefs())
      {
        Document downloadedMetadata = lookupDownloadedDocument(thisMetadata);
        if (downloadedMetadata != null)
        {
          thisMetadata = downloadedMetadata;
        }
        setupDocumentChangedEventListener(mmdField, metadata, thisMetadata);
      }

      thisMetadata.setMetaMetadata(mmdField);
      Metadata changedMetadata = lookupTrueMetaMetadata(mmdField.getRepository(), thisMetadata);
      if (changedMetadata != null)
      {
        Metadata.fieldWiseCopy(changedMetadata, thisMetadata);
        thisMetadata = changedMetadata;
      }

      // TODO check for polymorphism. if this is an inherent polymorphic fields, we may need to
      // replace thisMetadata completely if its type changes.

      // here everything seems ok. assign result composite back to input metadata object
      Field javaField = mmdField.getMetadataFieldDescriptor().getField();
      ReflectionTools.setFieldValue(metadata, javaField, thisMetadata);

      // try to link result metadata
      MetaMetadataRepository repository = getSemanticsScope().getMetaMetadataRepository();
      LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
      monitor.tryLink(repository, thisMetadata);

      return true;
    }

    return false;
  }

  /**
   * 
   * @param mmdField
   * @param hostMetadata
   * @param docToDownload
   * @param isCollection
   */
  private void setupDocumentChangedEventListener(MetaMetadataNestedField mmdField,
                                                 Metadata hostMetadata, Metadata docToDownload)
  {
    if (docToDownload instanceof Document)
    {
      Document doc = (Document) docToDownload;
      if (doc.getDownloadStatus() != DownloadStatus.DOWNLOAD_DONE)
      {
        ParsedURL listeningLoc = doc.getLocation();
        DocumentDownloadingMonitor monitor = getSemanticsScope().getDocumentDownloadingMonitor();
        DocumentDownloadedEventHandler listener = new DocumentDownloadedEventHandler();
        Field javaField = mmdField.getMetadataFieldDescriptor().getField();
        monitor.listenForDocumentDownloading(hostMetadata, listeningLoc, javaField, listener);
      }
    }
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
        Document existingDoc = getSemanticsScope().lookupDocument(location);
        if (existingDoc != null && existingDoc.getDownloadStatus() == DownloadStatus.DOWNLOAD_DONE)
        {
          // add the replaced Document object as a mixin in the downloaded Document.
          existingDoc.addMixin(doc); 
          return existingDoc;
        }
      }
    }
    return null;
  }

  /**
   * if we got a document, we may want to look up its true meta-metadata type by location. before
   * doing connect(), what we can do to find out the true meta-metadata type is quite limited
   * (location, suffix, tag name). here we do location & suffix. tag name is mainly used by direct
   * binding cases.
   * 
   * @param repository
   * @param thisMetadata
   */
  protected Metadata lookupTrueMetaMetadata(MetaMetadataRepository repository,
                                            Metadata thisMetadata)
  {
    if (thisMetadata instanceof Document)
    {
      ParsedURL thisMetadataLocation = thisMetadata.getLocation();
      if (thisMetadataLocation != null)
      {
        MetaMetadata locMmd = repository.getRichDocumentMM(thisMetadataLocation);
        if (locMmd != null
            && !locMmd.getName().equals(DocumentParserTagNames.RICH_DOCUMENT_TAG))
        {
          Class thisMetadataClass = thisMetadata.getClass();
          Class trueMetadataClass = locMmd.getMetadataClass();
          if (thisMetadataClass.isAssignableFrom(trueMetadataClass))
          {
            logger.debug("Changing mmd for extracted value {} to {}", thisMetadata, locMmd);
            if (thisMetadataClass == trueMetadataClass)
            {
              // when the two metadata classes are the same, we can safely change the meta-metadata
              // since they have exactly the same set of fields, and thus no binding errors will
              // occur.
              thisMetadata.setMetaMetadata(locMmd);
            }
            else
            {
              // when the two metadata classes are not the same, we need to be careful. create
              // the right metadata object and copy values.
              Metadata changedMetadata = locMmd.constructMetadata();
              changedMetadata.setMetaMetadata(locMmd);
              return changedMetadata;
            }
          }
          else
          {
            logger.error("Type mismatch when changing mmd for extracted value {} to {}, "
                         + "expected type: {}; "
                         + "check <selector> to see if it is not specific enough.",
                         thisMetadata, locMmd, thisMetadata.getMetaMetadata());
          }
        }
      }
    }
    return null;
  }

  /**
   * Extract a collection field of the given metadata object.
   * 
   * @param mmdField
   * @param metadata
   * @param contextNode
   * @param fieldParserContext
   * @param params
   * @return true if the extraction was successful and the result collection is not empty; otherwise
   *         false. The result collection field will not contain null references or failed elements
   *         (elements that has no actual information or lacks required field values).
   */
  private boolean extractCollection(MetaMetadataCollectionField mmdField,
                            Metadata metadata,
                            Node contextNode,
                            Map<String, String> fieldParserContext,
                            Scope<Object> params)
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
    Class elementClass = null;
    MetadataScalarType scalarType = null;
    MetadataFieldDescriptor metadataFieldDescriptor = mmdField.getMetadataFieldDescriptor();
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
        logger.error("child_scalar_type not specified or registered: " + mmdField);
        return false;
      }
    }
    else
    {
      if (metadataFieldDescriptor != null)
      {
        ClassDescriptor elementClassDescriptor =
            metadataFieldDescriptor.getElementClassDescriptor();
        if (metadataFieldDescriptor.isPolymorphic())
        {
          String polymorphTagName =
              mmdField.getElementComposite().getTypeMmd().getTagForTypesScope();
          if (polymorphTagName != null)
          {
            elementClassDescriptor =
                metadataFieldDescriptor.elementClassDescriptor(polymorphTagName);
          }
        }
        if (elementClassDescriptor != null)
        {
          elementClass = elementClassDescriptor.getDescribedClass();
        }
      }
      else
      {
        logger.warn("metadataFieldDescriptor not found in " + mmdField);
      }
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
    Object[] argObjects = new Object[] { mmdField.getElementComposite() };
    for (int i = 0; i < size; ++i)
    {
      Node thisNode = (nodeList == null) ? null : nodeList.item(i);
      Map<String, String> thisFieldParserContext =
          (fieldParserContextList == null) ? null : fieldParserContextList.get(i);

      if (!mmdField.isCollectionOfScalars())
      {
        Metadata element =
            (Metadata) ReflectionTools.getInstance(elementClass, argClasses, argObjects);
        element.setSemanticsSessionScope(getSemanticsScope());

        // the index of the current element in the current collection may be useful for further
        // extraction.
        params.put(ELEMENT_INDEX_IN_COLLECTION, i);

        if (recursiveExtraction(mmdField, element, thisNode, thisFieldParserContext, params))
        {
          if (getSemanticsScope().ifAutoUpdateDocRefs())
          {
            Document downloadedDocument = lookupDownloadedDocument(element);
            if (downloadedDocument != null)
            {
              element = downloadedDocument;
            }
            setupDocumentChangedEventListener(mmdField, metadata, element);
          }

          element.setMetaMetadata(mmdField);
          Metadata changedElement = lookupTrueMetaMetadata(mmdField.getRepository(), element);
          if (changedElement != null)
          {
            Metadata.fieldWiseCopy(changedElement, element);
            element = changedElement;
          }

          // TODO check for polymorphism. if this is an inherent polymorphic fields, we may need to
          // replace element completely if its type changes.

          elements.add(element);
        }
      }
      else
      {
        String value = null;
        if (fieldParserContextList != null)
        {
          if (thisFieldParserContext != null)
          {
            value = thisFieldParserContext.get(FieldParserForRegexSplit.DEFAULT_KEY);
          }
        }
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
          value = applyFieldOps(value, mmdField);

          MetadataBase element;
          element = (MetadataBase) scalarType.getInstance(value, null, this);
          if (element != null)
          {
            elements.add(element);
          }
        }
      }
    }

    // if more than 0 elements are extracted, assign the collection back
    if (elements.size() > 0)
    {
      Field javaField = metadataFieldDescriptor.getField();
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
  private boolean extractScalar(MetaMetadataScalarField mmdField,
                                Metadata metadata,
                                Node contextNode,
                                Map<String, String> fieldParserContext,
                                Scope<Object> params)
  {
    String xpathString = mmdField.getXpath();
    String fieldParserKey = mmdField.getFieldParserKey();
    contextNode = findContextNodeIfNecessary(mmdField, contextNode, params);
  
    String evaluation = null;
    if (xpathString != null
        && xpathString.length() > 0
        && contextNode != null
        && fieldParserKey == null)
    {
      try
      {
        evaluation = evaluateXpathForField(mmdField, contextNode, params, null);
      }
      catch (Exception e)
      {
        logger.error("Error extracting " + mmdField + ", contextNode=" + contextNode.getNodeName(),
                     e);
      }
    }
    else if (fieldParserKey != null)
    {
      evaluation = fieldParserContext == null ? null : fieldParserContext.get(fieldParserKey);
    }
    else if (!mmdField.hasConcatenateValues())
    {
      return false; // This is the final catch all.
    }
  
    evaluation = concatenateValues(evaluation, mmdField, metadata, params);
    
    MetadataFieldDescriptor fd = mmdField.getMetadataFieldDescriptor();
    ScalarType fdScalarType = fd == null ? null : fd.getScalarType();
    if (fdScalarType != null && fdScalarType instanceof MetadataParsedURLScalarType)
    {
    	MetadataParsedURL wholePurl =
    			(MetadataParsedURL) fdScalarType.getInstance(evaluation, null, this);
    	evaluation = wholePurl.toString();
    }
  
    // after we have evaluated the expression we might need to modify it.
    evaluation = applyFieldOps(evaluation, mmdField);
    if (StringTools.isNullOrEmpty(evaluation))
      return false;
  
    if (fdScalarType != null && fdScalarType instanceof MetadataParsedURLScalarType)
    {
      // if this is a ParsedURL, we try to filter it using <filter_location>, if applicable.
      MetadataParsedURL metadataPurl = (MetadataParsedURL) fdScalarType.getInstance(evaluation,
                                                                                    null,
                                                                                    this);
      if (metadataPurl != null)
      {
        ParsedURL purl = metadataPurl.getValue();
        ParsedURL filteredPurl =
            FilterLocation.filterIfNeeded(metadataPurl.getValue(), null, getSemanticsScope());
        if (filteredPurl != null)
        {
          if (purl == null || !purl.equals(filteredPurl))
            metadataPurl.setValue(filteredPurl);
        }
      }
  
      if (metadataPurl != null && metadataPurl.getValue() != null)
      {
        fd.setField(metadata, metadataPurl);
      }
    }
    else
    {
      // for other scalar types, we only need to create and assign the value.
      metadata.setByFieldName(mmdField.getFieldNameInJava(false), evaluation, this);
    }
  
    return true;
  }

  /**
   * Handles concatenation semantics for a field value.
   * 
   * @param evaluation
   *          String originally in the scalar value (will be appended at the beginning of the string
   * @param mmdField
   *          The scalar field with values to concatenate
   * @param metadata
   *          Metadata object that contains the field
   * @param params
   *          Scope of parsing with variables / etc
   * @return String value concatenated to pass onto other tasks (like regexing)
   */
  private String concatenateValues(String evaluation,
                                   MetaMetadataScalarField mmdField,
                                   Metadata metadata,
                                   Scope<Object> params)
  {
    if (mmdField.hasConcatenateValues())
    {
      List<MetaMetadataValueField> fields = mmdField.getConcatenateValues();
  
      StringBuffer buffy = new StringBuffer();
  
      if (evaluation != null)
      {
        // If we have a value already for the mmd field, append it at the beginning
        buffy.append(evaluation);
      }
  
      for (MetaMetadataValueField v : fields)
      {
        String varValue = v.getReferencedValue(mmdField, metadata, params);
  
        if (varValue == null)
        {
          varValue = "";
          logger.warn("Attempted to concatenate null value from value referenced as: "
                      + v.getReferenceName());
        }
  
        buffy.append(varValue);
      }
  
      return buffy.toString();
    }
    else
    {
      return evaluation;
    }
  }

  /**
   * Modifies evaluation results based on FieldOps defined for that field.
   * 
   * @param evaluation
   * @param field
   * @return Modified evaluation.
   */
  private String applyFieldOps(String evaluation, MetaMetadataField field)
  {
    if (evaluation == null)
    {
      return null;
    }
  
    // to remove unwanted XML characters
    evaluation = XMLTools.unescapeXML(evaluation);
  
    List<FieldOp> fieldOps = field.getFieldOps();
    if (fieldOps != null)
    {
      for (int i = 0; i < fieldOps.size(); ++i)
      {
        evaluation = fieldOps.get(i).operateOn(evaluation);
      }
    }
  
    // remove white spaces if any
    evaluation = evaluation.trim();
    return evaluation;
  }

  private static Properties innerHtmlProps = new Properties();
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
  private static String getInnerHtml(Node node)
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
    catch (Exception e)
    {
      logger.error("Error getting inner HTML for " + node, e);
      return null;
    }
    return w.toString();
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

      SimplTypesScope tscope = metaMetadata.getLocalMetadataTypesScope();
      InputStream istream = getDownloadController().getInputStream();
      DeserializationHookStrategy<Metadata, MetadataFieldDescriptor> strategy =
          new ParserDeserializationHookStrategy(this);
      newDocument = (Document) tscope.deserialize(istream, strategy, Format.XML);

      logger.debug(SimplTypesScope.serialize(newDocument, StringFormat.XML).toString());

      // the old document is basic, so give it basic meta-metadata (so recycle does not tank)
      Document oldDocument = getDocumentClosure().getDocument();
      oldDocument.setMetaMetadata(getSemanticsScope().DOCUMENT_META_METADATA);
      getDocumentClosure().changeDocument(newDocument);
    }
    catch (SIMPLTranslationException e)
    {
      logger.error("Cannot serialize direct binding result metadata: " + newDocument, e);
    }
    return newDocument;
  }
  
  /**
   * Helper class for direct binding.
   * 
   * @author quyin
   */
  public static class ParserDeserializationHookStrategy
  implements DeserializationHookStrategy<Metadata, MetadataFieldDescriptor>
  {
    
    DocumentParser                 parser;

    Stack<MetaMetadataNestedField> currentMMstack    = new Stack<MetaMetadataNestedField>();

    boolean                        deserializingRoot = true;

    boolean                        polymorphMmd      = false;
    
    public ParserDeserializationHookStrategy(DocumentParser parser)
    {
      this.parser = parser;
    }

    /**
     * For the root, compare the meta-metadata from the binding with the one we started with. Down
     * the hierarchy, try to perform similar bindings.
     */
    @Override
    public void deserializationPreHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
    {
      if (deserializingRoot)
      {
        deserializingRoot = false;
        Document document = parser.getDocument();
        MetaMetadataCompositeField preMM = document.getMetaMetadata();
        MetadataClassDescriptor mcd =
            (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(deserializedMetadata);
        MetaMetadataCompositeField metaMetadata;
        String tagName = mcd.getTagName();
        if (preMM.getTagForTypesScope().equals(tagName))
        {
          metaMetadata = preMM;
        }
        else
        {
          // just match in translation scope
          // TODO use local TranslationScope if there is one
          metaMetadata = parser.getSemanticsScope().getMetaMetadataRepository()
              .getMMByName(tagName);
        }
        deserializedMetadata.setMetaMetadata(metaMetadata);

        polymorphMmd = true;

        currentMMstack.push(metaMetadata);
      }
      else
      {
        String mmName = mfd.getMmName();
        MetaMetadataNestedField currentMM = currentMMstack.peek();
        // the following fails for collections :-(
        MetaMetadataNestedField childMMNested =
            (MetaMetadataNestedField) currentMM.lookupChild(mmName);
        if (childMMNested == null)
        {
          String msg =
              String.format("Can't find composite child meta-metadata for %s amidst %s; "
                            + "This probably means mmd repository do not match metadata classes; "
                            + "Have you changed built-in metadata classes w/o updating primitives.xml?",
                            mmName,
                            mfd);
          logger.error(msg);
          throw new RuntimeException(msg);
        }
        MetaMetadataCompositeField childMMComposite = null;
        if (childMMNested.isPolymorphicInherently())
        {
          String tagName = ClassDescriptor.getClassDescriptor(deserializedMetadata).getTagName();
          childMMComposite =
              parser.getSemanticsScope().getMetaMetadataRepository().getMMByName(tagName);
          polymorphMmd = true;
        }
        else
        {
          childMMComposite = childMMNested.metaMetadataCompositeField();
        }
        deserializedMetadata.setMetaMetadata(childMMComposite);
        currentMMstack.push(childMMComposite);
      }
    }

    @Override
    public void deserializationInHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
    {
      // for efficiency; if it is not polymorphic case we don't have to look up mmd
      // at this point of time
      if (polymorphMmd)
      {
        String mmName = deserializedMetadata.getMetaMetadataName();
        if (mmName != null && mmName.length() > 0)
        {
          MetaMetadata trueMmd =
              parser.getSemanticsScope().getMetaMetadataRepository().getMMByName(mmName);
          if (trueMmd != null)
          {
            logger.debug("setting [{}].metaMetadata to {} (mm_name={})...",
                         deserializedMetadata, trueMmd, mmName);
            deserializedMetadata.setMetaMetadata(trueMmd);
          }
          else
          {
            logger.warn("polymorphicly looking up meta-metadata failed: cannot find mmd named as "
                        + mmName);
          }
        }
        polymorphMmd = true;
      }
    }

    @Override
    public void deserializationPostHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
    {
      currentMMstack.pop();
    }

    @Override
    public Metadata changeObjectIfNecessary(Metadata deserializedMetadata,
                                            MetadataFieldDescriptor mdf)
    {
      if (parser.getSemanticsScope().ifAutoUpdateDocRefs())
      {
        Document downloadedDoc = parser.lookupDownloadedDocument(deserializedMetadata);
        return downloadedDoc == null ? deserializedMetadata : downloadedDoc;
      }
      return deserializedMetadata;
    }

  }

}
