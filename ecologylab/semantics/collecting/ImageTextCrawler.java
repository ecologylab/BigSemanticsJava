/**
 * 
 */
package ecologylab.semantics.collecting;

import java.util.Observable;

import ecologylab.collections.GenericElement;
import ecologylab.collections.GenericPrioritizedPool;
import ecologylab.collections.GenericWeightSet;
import ecologylab.collections.PrioritizedPool;
import ecologylab.collections.WeightSet;
import ecologylab.semantics.documentparsers.CompoundDocumentParserCrawlerResult;
import ecologylab.semantics.gui.InteractiveSpace;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;

/**
 * Adds collecting of ImageClippings and TextClippings to basic Crawler.
 * 
 * @author andruid
 */
public class ImageTextCrawler extends Crawler
{
	private static final int	STARVED_FOR_IMAGES_COUNT	= 2;

	/**
	 * When the {@link #candidateTextSet candidateTextSet} and the {@link #candidateImgSet
	 * candidateImgSet} have more entries than this, they will be pruned.
	 */
	static final int						MAX_MEDIA											= 3072;

	public static final int			NUM_GENERATIONS_IN_MEDIA_POOL = 3; 

	static final int						MAX_MEDIA_PER_GENERATION			= MAX_MEDIA / NUM_GENERATIONS_IN_MEDIA_POOL;

	/**
	 * Contains 3 visual pools. The first holds the first image of each container
	 */
	//FIXME This should be GenericWeightSet<ImageClipping> in order to use the right metadata in TermVector !!!!!!!!!
	private final PrioritizedPool<DocumentClosure> 				candidateImagesPool;
	
	/**
	 * Contains 2 FloatWeightSet pools. 
	 * The first holds the first text surrogate of each container
	 */
	private final GenericPrioritizedPool<TextClipping> 	candidateTextClippingsPool;
	

	/**
	 * 
	 */
	public ImageTextCrawler()
	{
		super();
		collectingImages									= true;
		collectingText										= true;
		
		TermVector piv 										= InterestModel.getPIV(); 

		//Similarly for text surrogates
		GenericWeightSet[] textWeightSets = { 
				new GenericWeightSet<TextClipping>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv)),
				new GenericWeightSet<TextClipping>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv)),
				new GenericWeightSet<TextClipping>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv))
		};
		candidateTextClippingsPool = new GenericPrioritizedPool<TextClipping>(textWeightSets);

		// Three pools for downloaded images      
		WeightSet<DocumentClosure>[] imageWeightSets	= new WeightSet[NUM_GENERATIONS_IN_MEDIA_POOL];
		for (int i = 0; i < NUM_GENERATIONS_IN_MEDIA_POOL; i++)
			imageWeightSets[i]	= new WeightSet<DocumentClosure>(MAX_MEDIA_PER_GENERATION, this, new TermVectorWeightStrategy(piv));
		candidateImagesPool = new PrioritizedPool<DocumentClosure>(imageWeightSets);
				
	}

	@Override
	public void stopCollectingAgents(boolean kill)
	{
		super.stopCollectingAgents(kill);

		candidateImagesPool.stop();
	}
	
	/**
	 * Replace images in the candidates with possible better ones from their containers.
	 */
	private void checkCandidatesForBetterImagesAndText()
	{
		synchronized (candidateImagesPool)
		{
//			ImageClosure[] poolCopy				= (ImageClosure[]) candidateImagesPool.toArray();
			for (DocumentClosure imageClosure : candidateImagesPool)
			{
				//TODO -- check among all source documents!!!
				Image image								= (Image) imageClosure.getDocument();
				Document sourceDocument		= image.getClippingSource();
				if (sourceDocument != null && sourceDocument.isCompoundDocument())
					sourceDocument.tryToGetBetterImageAfterInterestExpression(imageClosure);
			}
		}
		synchronized (candidateTextClippingsPool)
		{
//			ArrayList<GenericElement<TextClipping>> tlist = new ArrayList<GenericElement<TextClipping>>(candidateTextClippingsPool.size());
//			for (GenericWeightSet<TextClipping> ws : (GenericWeightSet<TextClipping>[]) candidateTextClippingsPool.getWeightSets())
//				for (GenericElement<TextClipping> i : ws)
//					tlist.add(i);
			for (GenericElement<TextClipping> i : candidateTextClippingsPool)
			{
				TextClipping textClipping	= i.getGeneric();
				Document sourceDocument		= textClipping.getSource();
				if (sourceDocument != null)
					sourceDocument.tryToGetBetterTextAfterInterestExpression(i);
			}
		}
	}

	/**
	 * Remove from candidate clippings pool.
	 * 
	 * @param replaceMe		TextClipping to remove
	 */
	@Override
	public void removeTextClippingFromPools(GenericElement<TextClipping> replaceMe)
	{
		candidateTextClippingsPool.remove(replaceMe);
	}
	/**
	 * Remove from candidate Images pool.
	 * 
	 * @param replaceMe		Image to Remove
	 */
	@Override
	public void removeImageClippingFromPools(DocumentClosure replaceMe)
	{
		candidateImagesPool.remove(replaceMe);
	}

	/**
	 * Number of display-able <code>Image</code>s that could be displayed.
	 * 
	 * Used for deciding how urgent downloading Images is.
	 */
	@Override
	public int imagePoolsSize()
	{
		return candidateImagesPool.size();
	}
	
	public static final int ALMOST_EMPTY_CANDIDATES_SET_THRESHOLD	= 5;
	
	/**
	 * Used to assess how much need we have for more TextClippings.
	 */
	@Override
	public boolean candidateTextClippingsSetIsAlmostEmpty()
	{
		return candidateTextClippingsPool.size() <= ALMOST_EMPTY_CANDIDATES_SET_THRESHOLD;
	}

	/**
	 * Collects TextClipping based on its weight and if it is the first representative for that CompoundDocument.
	 * @param numSurrogatesCollectedFromCompoundDocument	
	 * @param textClipping	TextClipping to potentially collect
	 * 
	 * @return	always false in this base class implementation, because we do not collect TextClippings.
	 */
	@Override
	public boolean collectTextClippingIfWorthwhile(GenericElement<TextClipping> textClippingGE, int numSurrogatesCollectedFromCompoundDocument, int clippingPoolPriority)
	{
		TextClipping textClipping	= textClippingGE.getGeneric();
		
		float adjustedWeight 		= InterestModel.getInterestExpressedInTermVector(textClipping.termVector()) / (float) numSurrogatesCollectedFromCompoundDocument;
		float meanTxtSetWeight	= candidateTextClippingsMean();
		boolean result = (adjustedWeight >= meanTxtSetWeight) || candidateTextClippingsSetIsAlmostEmpty();
		if (result)
		{
			addTextClippingToPool(textClippingGE, clippingPoolPriority);
		}
		return result;
	}

	public boolean collectImageIfWorthwhile(DocumentClosure imageClosure, int numSurrogatesCollectedFromCompoundDocument, int clippingPoolPriority)
	{
		boolean result	= false;
		if (imagePoolsSize() < STARVED_FOR_IMAGES_COUNT)
			result				= true;
		
		if (!result)
		{
			float adjustedWeight			= InterestModel.getInterestExpressedInTermVector(imageClosure.termVector()) / (float) numSurrogatesCollectedFromCompoundDocument;
			
			float meanImagesWeight		= candidateImagesMean();
			
			result										= adjustedWeight >= meanImagesWeight;
		}
		if (result)
		{
			addCandidateImage(imageClosure);
			
			//FIXME How do we download images and dispatch them to the space!!!????
		}
		return result;
	}

	public void addCandidateImage(DocumentClosure imageClosure)
	{
		candidateImagesPool.add(imageClosure);
	}

	/**
	 * 
	 * @return	Weighted mean of the members of the candidateFirstTextElementsSet and the candidateTextElementsSet.
	 */
	public float candidateTextClippingsMean()
	{
		return candidateTextClippingsPool.mean();
	}

	public float candidateImagesMean()
	{
		return candidateImagesPool.mean();
	}


	/**
	 * This is an Observer of changes in the TermVectors, which change when the interest model changes.
	 * 
	 * When the interest model changes, we iterate through candidate DocumentClosures to see if they have a better link
	 * to contribute to our global crawler state.
	 * We make the same checks for candidate Images and TextClippings.
	 */
	@Override
	public void update(Observable o, Object arg)
	{
		super.update(o, arg);
		checkCandidatesForBetterImagesAndText();
	}

	/**
	 * Add a TextClipping into our pool of candidates.
	 * 
	 * @param textClippingGE	GenericElement that contains the TextClipping.
	 * 
	 * @param poolPriority		Pool priority shapes which level in the candidates pool to insert into.
	 */
	@Override
	public void addTextClippingToPool(GenericElement<TextClipping> textClippingGE, int poolPriority)
	{
		candidateTextClippingsPool.insert(textClippingGE, poolPriority);
		
		InteractiveSpace interactiveSpace	= semanticsSessionScope.getInteractiveSpace();
		if (seeding.isPlayOnStart() && interactiveSpace != null)
			interactiveSpace.pressPlayWhenFirstMediaArrives();
	}
	
	/**
	 * Pause the candidate Images collecting thread.
	 * 
	 * Base class implementation does nothing.
	 */
	@Override
	protected void pauseImageCollecting()
	{
		candidateImagesPool.pause();
	}
	
	/**
	 * Unpause the candidate Images collecting thread.
	 * 
	 * Base class implementation does nothing.
	 */
	@Override
	protected void unpauseImageCollecting()
	{
		candidateImagesPool.unpause();
	}
	
	/**
	 * Clear the candidateImagesPool, and candidateTextClippingsPool.
	 * Call super() to clear the candidateDocumentClosuresPool.
	 * 
	 */
	@Override
	public void clearCollections()
	{
		candidateImagesPool.clear();
		candidateTextClippingsPool.clear();
		super.clearCollections();
	}

	/**
	 * Construct a CompoundDocument ParserResult object of type that matches this crawler.
	 * 
	 * @param compoundDocument	Document that is parsed.
	 * @param justCrawl					True if we should not collect Images and TextClippings, even if we could.
	 * 
	 * @return	CompoundDocumentParserCrawlerResult
	 */
	@Override
	public CompoundDocumentParserCrawlerResult 
	constructCompoundDocumentParserResult(CompoundDocument compoundDocument, boolean justCrawl)
	{
		return justCrawl ? new CompoundDocumentParserImageTextCrawlerResult(compoundDocument) 
			: super.constructCompoundDocumentParserResult(compoundDocument, justCrawl);
	}

}
