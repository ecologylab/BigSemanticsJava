package ecologylab.documenttypes;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.AbstractImgElement;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.RecognizedDocumentStructure;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTools;

/**
 * Parse HTML page and create DOM
 * @author eunyee
 *
 */
public class HTMLDOMType<C extends Container, IC extends InfoCollector<C>, ES extends ElementState> 
extends HTMLCommon<C, IC, ES>
{

	protected SemanticActionHandler<C, IC>	semanticActionHandler;
	
	/**
	 * Root DOM node of the current HTML document
	 */
	protected org.w3c.dom.Document	document	= null;
	
	protected	Tidy 									tidy 			= new Tidy();
	
	public HTMLDOMType(IC infoCollector,SemanticActionHandler<C,IC> semanticActionHandler)
	{
		super(infoCollector);
		this.semanticActionHandler = semanticActionHandler;
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
	}
	public HTMLDOMType(IC infoCollector)
	{
		super(infoCollector);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
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
	  	 // we dont build a tidy for for direct biniding
	  	 if(!metaMetadata.getBinding().equals("direct"))
	  		 		document 							= tidy.parseDOM(inputStream(),/* System.out*/null);
		   postParse();
	   } catch (Exception e) 
	   {
		   debug("ERROR: while parsing document - " + getContainer().purl());
		   e.printStackTrace();
	   }	
	}

	protected void postParse()
	{
		semanticActionHandler.getParameter().addParameter(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE, document);
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
		document = null;
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
					String caption	= imageElement.caption();
					// Checking whether the mined textContext is exactly the same as the alt text. 
					// In that case, we don't want to display the redundant information. 
					if ((caption==null) || !textContext.equals(caption))
						imageElement.hwSetContext( trimTooLongContext(textContext) );
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
		C candidate = getInfoCollector().getContainer(container, containerPURL, false, false, null);
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
	 * 	 creates a container
	 * 	 sets the metadata
	 *   adds an outlink from the ancestor
	 *   add a semantic inlink to hrefContainer from this.
	 *   
	 */
	public void generateCandidateContainersFromContexts(ArrayList<AnchorContext> anchorContexts)
	{
		C	container	= this.container();
		for(AnchorContext anchorContext : anchorContexts)
		{
				generateCanidateContainerFromContext(anchorContext,container);
		}
	}
	
	public void generateCanidateContainerFromContext(AnchorContext anchorContext, C container)
	{
		ParsedURL hrefPurl 			= anchorContext.getHref();
		if(hrefPurl !=null && !hrefPurl.isNull())
		{	
			newAHref(hrefPurl);
	
			if (!hrefPurl.isImg() && infoCollector.accept(hrefPurl))
			{
				MetaMetadataRepository mmdRepository= infoCollector.metaMetaDataRepository();
				MetaMetadata metaMetadata 					= mmdRepository.getDocumentMM(hrefPurl);
				C hrefContainer 					= infoCollector.getContainer(container, hrefPurl, false, false, metaMetadata);
				if (hrefContainer != null)
				{
					SemanticAnchor semAnchor 					= new SemanticAnchor(container.purl(), anchorContext);
					hrefContainer.addSemanticInLink(semAnchor, container);
	
					// this is not being performed because we create weights through SemanticInlinks
					//				metadata.appendAnchorText(anchorText);
					//				metadata.hwAppendAnchorContextString(anchorContextString);
	
					container.addCandidateContainer(hrefContainer);
	
					//FIXME: All links use this control flow. Why are we considering all of them as from article body ?
					container.setInArticleBody(true);
				}
			}
			//The href associated is actually an image. Create a new img element and associate text to it.
			else
			{
				//TODO Send this anchorText into it's appropriate place in the metadata
				//For now it's being set as the caption of the img.
				newImg(hrefPurl, anchorContext.getAnchorText(), 0,0, false);
			}
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