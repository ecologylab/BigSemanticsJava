/**
 * 
 */
package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.collecting.MetadataElement;
import ecologylab.bigsemantics.documentparsers.CompoundDocumentParserCrawlerResult;
import ecologylab.bigsemantics.metadata.builtins.Clipping;
import ecologylab.bigsemantics.metadata.builtins.CompoundDocument;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.ImageClipping;
import ecologylab.bigsemantics.metadata.builtins.TextClipping;
import ecologylab.bigsemantics.model.text.InterestModel;
import ecologylab.bigsemantics.model.text.TermVectorWeightStrategy;
import ecologylab.collections.GenericElement;
import ecologylab.collections.GenericWeightSet;
import ecologylab.collections.WeightSet;
import ecologylab.generic.Continuation;

/**
 * The per CompoundDocument component of the ImageTextCrawler.
 * 
 * @author andruid
 */
public class CompoundDocumentParserImageTextCrawlerResult 
extends CompoundDocumentParserCrawlerResult<ImageTextCrawler>
implements Continuation<DocumentClosure>
{
	/**
	 * Weighted collection of <code>ImageElement</code>s.
	 * Contain elements that have not been transported to candidatePool. 
	 */
	//FIXME This should be GenericWeightSet<ImageClipping> in order to use the right metadata in TermVector !!!!!!!!!
	private WeightSet<DocumentClosure>	candidateImageClosures;

	/**
	 * Weighted collection of <code>TextElement</code>s.
	 * Contain elements that have not been transported to candidatePool.
	 */
	private GenericWeightSet<TextClipping>	candidateTextClippings;
	
	private boolean				crawlingImages;
	private boolean				crawlingTextClippings;
	
	double mostRecentImageWeight = 0, mostRecentTextWeight = 0;
	

	/**
	 * @param compoundDocument
	 */
	public CompoundDocumentParserImageTextCrawlerResult(CompoundDocument compoundDocument)
	{
		super(compoundDocument);
	}

	int sizeCandidateTextClippings()
	{
		return candidateTextClippings == null ? 0 : candidateTextClippings.size();
	}
	
	int sizeCandidateImageClosures()
	{
		return candidateImageClosures == null ? 0 : candidateImageClosures.size();
	}
	
	public int sizeLocalCandidates()
	{
		return sizeCandidateTextClippings() + sizeCandidateImageClosures();
	}
	
	protected synchronized void perhapsAddTextClippingToCrawler()
	{
		GenericElement<TextClipping> textClippingGE = null;
		if (candidateTextClippings != null)
		{
			textClippingGE = candidateTextClippings.maxSelect();
		}
		if( textClippingGE!=null )
		{
			// If no surrogate has been delivered to the candidate pool from the container, 
			// send it to the candidate pool without checking the media weight. 
			if (firstClipping() )
				crawler.addTextClippingToPool(textClippingGE, clippingPoolPriority());
			else
			{
				if (crawler.collectTextClippingIfWorthwhile(textClippingGE, numSurrogatesFrom, clippingPoolPriority()))
				{
					mostRecentTextWeight = InterestModel.getInterestExpressedInTermVector(textClippingGE.getGeneric().termVector());
				}
				else
				{
					textClippingGE.recycle(false);
					crawlingTextClippings	= false;
					//recycle(false);
				}
			}
		}
		else
			crawlingTextClippings	= false;
	}

	private boolean firstClipping()
	{
		return numSurrogatesFrom==0;
	}
	protected synchronized void perhapsAddImageClosureToCrawler()
	{
		DocumentClosure imageClosure = null;
		if (candidateImageClosures != null)
			imageClosure = candidateImageClosures.maxSelect();

		if (imageClosure!=null && imageClosure.termVector() != null && !imageClosure.termVector().isRecycled())
		{
			// If no surrogate has been delivered to the candidate pool from the container, 
			// send it to the candidate pool without checking the media weight.
			if (firstClipping())
				crawler.addCandidateImage(imageClosure);
			else
			{
				if (crawler.collectImageIfWorthwhile(imageClosure, numSurrogatesFrom, clippingPoolPriority()))
				{
					mostRecentImageWeight = InterestModel.getInterestExpressedInTermVector(imageClosure.termVector());

				}
				else
				{
					//FIXME -- what about the ImageClipping?!
					imageClosure.recycle(false);
					crawlingImages	= false;
					//recycle(false);
				}				
			}
		}
		else
			crawlingImages	= false;
	}
	
	@Override
	public void callback(DocumentClosure imageClosure)
	{
		mostRecentImageWeight = InterestModel.getInterestExpressedInTermVector(imageClosure.termVector());
		perhapsAddImageClosureToCrawler();
	}
	
	@Override
	protected void collect(Clipping clipping)
	{
		if (clipping.isImage())
		{
			collect((ImageClipping) clipping);
		}
		else
		{	// text clipping
			collect((TextClipping) clipping);
		}
		super.collect(clipping);
	}
	/**
	 * Add an ImageClipping to our candidates collection.
	 * 
	 * @param textClipping
	 */
	protected void collect(ImageClipping imageClipping)
	{
		if (candidateImageClosures == null)
			candidateImageClosures	=  new WeightSet<DocumentClosure>(new TermVectorWeightStrategy(InterestModel.getPIV()));
		candidateImageClosures.insert(imageClipping.getMedia().getOrConstructClosure());
	}
	/**
	 * Add a TextClipping to our candidates collection.
	 * 
	 * @param textClipping
	 */
	protected void collect(TextClipping textClipping)
	{
		if (candidateTextClippings == null)
			candidateTextClippings	=  new GenericWeightSet<TextClipping>(new TermVectorWeightStrategy(InterestModel.getPIV()))
			{
				@Override
				public boolean insert(TextClipping go)
				{
					return insert(new MetadataElement<TextClipping>(go));
				}
			};
		candidateTextClippings.insert(textClipping);
	}
	
	/**
	 * Start up collecting loops -- TextClippings, Images, DocumentClosures.
	 */
	@Override
	protected void initiateCollecting()
	{
		if (crawler.isCollectingText())
		{
			crawlingTextClippings	= true;
			perhapsAddTextClippingToCrawler();
		}
		if (crawler.isCollectingImages())
		{
			crawlingImages	= true;
			perhapsAddImageClosureToCrawler();
		}
		super.initiateCollecting();
	}
	
	public synchronized void tryToGetBetterTextAfterInterestExpression(GenericElement<TextClipping> replaceMe)
	{
		if (candidateTextClippings == null || candidateTextClippings.size() == 0)
			return;
		
		GenericElement<TextClipping> te = candidateTextClippings.maxPeek();
		if (InterestModel.getInterestExpressedInTermVector(te.getGeneric().termVector()) > mostRecentTextWeight)
		{
			crawler.removeTextClippingFromPools(replaceMe);
			perhapsAddTextClippingToCrawler();
			// perhapsAddAdditionalTextSurrogate could call recycle on this container
			if (!this.isRecycled())
				candidateTextClippings.insert(replaceMe);
		}
	}
	
	public synchronized void tryToGetBetterImageAfterInterestExpression(DocumentClosure replaceMe)
	{
		if (candidateImageClosures == null || candidateImageClosures.size() == 0)
			return;
		
		DocumentClosure aie = candidateImageClosures.maxPeek();
		if (InterestModel.getInterestExpressedInTermVector(aie.termVector()) > mostRecentImageWeight)
		{
			crawler.removeImageClippingFromPools(replaceMe);
			perhapsAddImageClosureToCrawler();
			candidateImageClosures.insert(replaceMe);
		}
	}


	/**
	 * @return	true if there are no MediaElements that this Container is tracking
	 */
	protected boolean isEmpty()
	{
		return ((candidateImageClosures == null) || (candidateImageClosures.size()==0)) &&
				((candidateTextClippings == null) || (candidateTextClippings.size()==0)) && super.isEmpty();
	}
	
	/**
	 * Test for recycleable.
	 * 
	 * @return	true if this is still involved in collecting.
	 */
	@Override
	protected boolean isActive()
	{
		return super.isActive() && crawlingImages && crawlingTextClippings;
	}



}
