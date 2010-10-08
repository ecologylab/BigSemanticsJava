package ecologylab.semantics.documentparsers;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.tidy.DOMNodeImpl;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.AbstractImgElement;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.XMLTools;

/**
 * Parse HTML page and create DOM
 * @author eunyee
 *
 */
public class HTMLDOMParser<C extends Container, IC extends InfoCollector<C>> 
extends HTMLParserCommon<C, IC>
{

	protected SemanticActionHandler<C, IC>	semanticActionHandler;
	
	/**
	 * Root DOM node of the current HTML document
	 */
	protected org.w3c.dom.Document	document	= null;
	
	protected	Tidy 									tidy 			= new Tidy();
	
	public HTMLDOMParser(IC infoCollector,SemanticActionHandler<C,IC> semanticActionHandler)
	{
		super(infoCollector);
		this.semanticActionHandler = semanticActionHandler;
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
	}
	public HTMLDOMParser(IC infoCollector)
	{
		super(infoCollector);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
	}
	/**
	 * 
	 * @return The root node of the document, which should be <html>.
	 */
	public TdNode getRootNode()
	{
		return ((DOMNodeImpl) document).adaptee;
	}
	/**
	 * Andruid says: NEVER override this method when you parse HTML. 
	 * Instead, override postParse().
	 */
	@Override
	public void parse() 
	{   	
//	   HTMLDOMParser domParser	= new HTMLDOMParser();
	   try
	   {
	  	 // we dont build a tidy for for direct binding
	  	 //Uncomment System.out to print parsed page to console.
	  	 if(metaMetadata == null || !"direct".equals(metaMetadata.getParser()))
	  		 		document 							= tidy.parseDOM(inputStream(), /*System.out*/ null );
	  	 
//	  	 SimpleTimer.get("fetching_and_rendering.log").finishTiming(getContainer());
	  	 
		   postParse();
	   } catch (Exception e) 
	   {
		   debug("ERROR: while parsing document - " + getContainer().purl());
		   e.printStackTrace();
	   }	
	   finally
	   {
	  	 recycle();
	   }
	}

	protected void postParse()
	{
		semanticActionHandler.getSemanticActionVariableMap().put(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE, document);
	}
	/**
	 * Root DOM node of the current HTML document
	 * 
	 * @return
	 */
	public org.w3c.dom.Document getDocumentNode()
	{
		return document;
	}
	
	public void recycle()
	{
		document 	= null;
		tidy 			= null;
		if (semanticActionHandler != null)
		{
			semanticActionHandler.recycle();
			semanticActionHandler = null;
		}
		super.recycle();
	}

	/**
	 * create image and text surrogates for this HTML document, and add these surrogates 
	 * into the localCollection in Container.
	 */
	public void newImgTxt(ImgElement imgNode, ParsedURL anchorHref)
	{
		String alt				= imgNode.getAlt();
		int width					= imgNode.getWidth();
		int height				= imgNode.getHeight();
		boolean isMap			= imgNode.isMap();

		ParsedURL srcPurl = imgNode.getSrc();
		
		if (anchorHref != null)
			processHref(anchorHref);

		if (srcPurl != null)
		{
			String textContext	= imgNode.getTextContext();
			if( anchorHref != null )
				currentAnchorHref = anchorHref;

			if (alt != null)
				alt			= alt.trim();
			AbstractImgElement imageElement = container.createImageElement(srcPurl, alt, width, height, isMap, currentAnchorHref);

			// add the imgElement to LocalCollection 
			if ( imageElement != null )
			{
				//imgElement.addMetadataField(CONTEXT, textContext);
				if( textContext!=null )
				{
					// i am commenting this out bcause it has been done already in
					// DOMWalkInformationTagger
					textContext	= textContext.trim();

					// caption may be alt, but may have been filtered by ImgElement.deriveMetadata()
					// Checking whether the mined textContext is exactly the same as the alt text. 
					// In that case, we don't want to display the redundant information. 
					if (!imageElement.isNullCaption())
					{
						String caption	= imageElement.caption();
						if (!textContext.equals(caption))
							imageElement.hwSetContext( trimTooLongContext(textContext) );
					}
				}

				container.allocLocalCollections();

				container.addToCandidateLocalImages(imageElement);
			}

			// Reset the currentAnchorHref
			currentAnchorHref = null;
		}
	}

	@Override
	public void newAHref(HashMap<String, String> attributesMap)
	{
		super.newAHref(attributesMap);
	}

	/**
	 * Called when the parser see's the <code>&lt;title&gt; tag.
	 */
	public void setTitle(TdNode titleNode) 
	{
		StringBuilder title	= null;
		for (TdNode node = titleNode.content(); node != null; node = node.next())
		{
			if( node.type == TdNode.TextNode )
			{
				title			= StringBuilderUtils.trimAndDecodeUTF8(title, node, 0, true);
				if (title != null)
				{
					XMLTools.unescapeXML(title);
					container.hwSetTitle(StringTools.toString(title));
					StringBuilderUtils.release(title);
				}
				break;
			}
		}
	}
	
	/**
	 * Create TextElement and add to the localCollection in Container.
	 */
	//TODO -- take a BtringBuilder instead of a String. and use if efficiently!!!!!!
	public void newTxt(ParagraphText paraText) 
	{
		if ((paraText!=null) && (paraText.length()>0) && (container != null))
		{
//			container.createTextElementAndAddToCollections(contextChunk, contextString, userRequested)
			container.createTextElementAndAddToCollections(paraText);
		}
	}

	public int numCandidatesExtractedFrom()
	{
		return container.numLocalCandidates();
	}

	public void removeTheContainerFromCandidates(ParsedURL containerPURL)
	{
		C candidate = getInfoCollector().getContainer(container, containerPURL, false, false, null, null, false);
		getInfoCollector().removeCandidateContainer(candidate);
	}


	/**
	 * add an image+text surrogate for this that was extracted from a different document.
	 * FIXME this currently does the same thing as a surrogate extracted from this, but we might want to make a special collection 
	 * for these "anchor surrogates".
	 */
	public void newAnchorImgTxt(ImgElement imgNode, ParsedURL anchorHref) 
	{
		newImgTxt(imgNode, anchorHref);
	}

	/**
	 * For each anchorContext:
	 * 	 create purl and check to see 
	 *   Aggregates AnchorContext by their destination hrefs.
	 * 	 sets the metadata
	 * 	 creates a container
	 *   adds an outlink from the ancestor
	 *   
	 */
	public void generateCandidateContainersFromContexts(ArrayList<AnchorContext> anchorContexts, boolean fromContentBody)
	{
		C	container	= this.container();
		
		HashMapArrayList<ParsedURL, ArrayList<AnchorContext>> hashedAnchorContexts = new HashMapArrayList<ParsedURL, ArrayList<AnchorContext>>();
		for(AnchorContext anchorContext : anchorContexts)
		{
			
			ParsedURL destHref = anchorContext.getHref();
			if(destHref.isImg())
			{	// The href associated is actually an image. Create a new img element and associate text to it.
				newImg(destHref, anchorContext.getAnchorText(), 0,0, false);
				continue;
			}
			
			ArrayList<AnchorContext> arrayList = hashedAnchorContexts.get(destHref);
			if(arrayList == null)
			{
				arrayList = new ArrayList<AnchorContext>();
				hashedAnchorContexts.put(destHref, arrayList);
			}
			arrayList.add(anchorContext);
		}
		//Now that we have aggregated AnchorContext, 
		//We generate One SemanticAnchor per purl, that aggregates all the semantics of the set of anchorContexts
		for(ParsedURL hrefPurl : hashedAnchorContexts.keySet())
		{
			ArrayList<AnchorContext> anchorContextsPerHref = hashedAnchorContexts.get(hrefPurl);
			
			SemanticAnchor semanticAnchor = new SemanticAnchor(hrefPurl, anchorContextsPerHref, false, 1, purl(), fromContentBody, false);
			//generateCanidateContainerFromContext(aggregated,container, false);
			createContainerFromSemanticAnchor(container, hrefPurl, semanticAnchor);
		}
	}
	
	protected void createContainerFromSemanticAnchor(C container, ParsedURL hrefPurl, SemanticAnchor semanticAnchor)
	{
		if(hrefPurl !=null && !hrefPurl.isNull())
		{	
			newAHref(hrefPurl); //Parser count maintenance. 

			if(!infoCollector.accept(hrefPurl))
				return;
			
			MetaMetadataRepository mmdRepository	= infoCollector.metaMetaDataRepository();
			MetaMetadata metaMetadata 						= mmdRepository.getDocumentMM(hrefPurl);
			Container hrefContainer 							= infoCollector.getContainer(container, hrefPurl, false, false, null, metaMetadata, false);
			
			if (hrefContainer == null)
				return; //Should actually raise an exception, but this could happen when a container is not meant to be reincarnated
			
			hrefContainer.addSemanticInLink(semanticAnchor, container);

			container.setInArticleBody(semanticAnchor.fromContentBody());			
			container.addCandidateContainer(hrefContainer);
		}
	}

	static final PrefBoolean SHOW_PAGE_STRUCTURE_PREF	= PrefBoolean.usePrefBoolean("show_page_structure", false);

	public void setRecognizedDocumentStructure(Class<? extends RecognizedDocumentStructure> pageType)
	{
		if (SHOW_PAGE_STRUCTURE_PREF.value())
		{
			Document metadata	=(Document) container.metadata();
			if (metadata != null)
				metadata.setPageStructure(pageType.getSimpleName());
			else
				error("Can't setPageStructure() cause NULL Metadata :-(");
		}
	}


	public static int MAX_TEXT_CONTEXT_LENGTH = 1500;

	/**
	 * trim the text context under the limit if it is too long. 
	 * 
	 * @param textContext
	 * @return
	 */
	//FIXME -- call site for this should really be when we build contexts in walkAndTagDOM()
	public static String trimTooLongContext(String textContext)
	{
		if(textContext.length()>MAX_TEXT_CONTEXT_LENGTH)
			return textContext.substring(0, MAX_TEXT_CONTEXT_LENGTH);
		else
			return textContext;
	}

}