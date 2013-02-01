package ecologylab.bigsemantics.actions;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


import ecologylab.appframework.types.prefs.Pref;
import ecologylab.bigsemantics.actions.SemanticAction;
import ecologylab.bigsemantics.actions.SemanticActionStandardMethods;
import ecologylab.bigsemantics.gui.InteractiveSpace;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Clipping;
import ecologylab.bigsemantics.metadata.builtins.CompoundDocument;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.ImageClipping;
import ecologylab.bigsemantics.metadata.builtins.TextClipping;
import ecologylab.bigsemantics.model.text.TermWithScore;
import ecologylab.generic.Continuation;

/**
 * 
 */

/**
 * @author andruid, rhema
 *  This semantic action visualizes clippings of a compound document.
 *  For now, we visualize one clipping.
 */

//
//

public class VisualizeClippings extends SemanticAction
implements SemanticActionStandardMethods
{

	public VisualizeClippings()
	{
		
	}

	@Override
	public String getActionName()
	{
		return VISUALIZE_CLIPPINGS;
	}
	
	//need a score function for text that I can also use for clippings...
	
	public double scoreTextAtPoint(String text, int x, int y)
	{
		if(!Pref.lookupBoolean(Metadata.USE_SEMANTIC_SEARCH_PREF))
		    return 0;
		//just idf
		//TermDictionary.getTermForUnsafeWord("sanity")
		//getTermsFromInteractiveSpace
		//with 
		
		InteractiveSpace interactiveSpace = sessionScope.getInteractiveSpace();
		TermWithScore[] terms = interactiveSpace.getTermScoresAtPoint(x, y);
		//should add somet sort of stemming here or something... :(
		HashMap<String, TermWithScore> termHash = new HashMap<String, TermWithScore>();
		for(TermWithScore term: terms)
		{
//			debug(term.getWord());
			termHash.put(term.getWord(), term);
		}
		
		String[] words = text.split(" ");
		double score = 0;
		for(String word : words)
		{
			word = word.replaceAll("[^A-Za-z]", "");
			word = word.toLowerCase();
//			debug("Does the hash contain:"+word+"?");
			if(termHash.containsKey(word))
			{
				score += termHash.get(word).getScore();
//				debug("yes");
			}
			else
			{
//				debug("no");
			}
		}
		return score;
	}
	
	public String gistForTextAndPosition(int x, int y, int numWords, String text)
	{
		//debug("make gist at "+x+","+y+" with "+numWords+" and the text "+text);
		String[] words = text.split(" ");
		double bestScore = -1;
		String bestGist = "";
		for(int wordOffset=0; wordOffset<Math.min(words.length, numWords+words.length); wordOffset++)
		{
			String wholeGist = "";
			int wordCount = 0;
			//this should be repeated with different indexes
			//calkins1942@gmail.com
			for(int i=wordOffset; i<words.length; i++)
			{
				String word = words[i];
				if(wordCount > 0)
				wholeGist += " ";
				wholeGist += word;
				wordCount++;
				if(wordCount >= numWords)
					break;
			}
			///add up total score here...
			
			double gScore = scoreTextAtPoint(wholeGist, x, y);
			//debug("check dist:"+wholeGist +" gets the score:"+gScore);
			if(gScore > bestScore)
			{
				bestScore = gScore;
				bestGist = wholeGist;
			}
		}
		return bestGist;
	}

	@Override
	public void handleError()
	{
		
	}

	enum SelectionMethod
	{
		FIFO,SITUATED_SIMILARITY
	}
	//TODO add  arguments:
	// selection method: default fifo
	// filters: text only, picture only....
	// number of clippings added default 1...  for searches mainly.
	// max_extent:
	//method for display. Currently using FIFO.  Also add filters: like text only

	
	@Override
	public Object perform(Object obj) throws IOException
	{
		InteractiveSpace interactiveSpace = sessionScope.getInteractiveSpace();
		Document sourceDocument = resolveSourceDocument();
		DocumentClosure closure = documentParser.getDocumentClosure();
		SelectionMethod selectionMethod = SelectionMethod.FIFO;
		if (closure.isDnd() && interactiveSpace != null)
		{
			debug("This is a drop.  Finding clippings to visualize.");
			//TODO make something that waits for images to be downloaded
			CompoundDocument compoundSource = null;
			if(sourceDocument instanceof CompoundDocument)
			{
				compoundSource = (CompoundDocument) sourceDocument;
			}
			
			TextClipping bestTextClipping = null;
			ImageClipping bestImageClipping = null;
			double bestTextClippingScore = -1;

			List<Clipping> clippings = compoundSource.getClippings();
			if(clippings != null)
			{
				for (Clipping clipping : clippings)
				{
					if (clipping instanceof TextClipping)
					{
						/*
						//debug("Found text clipping");
						if(selectionMethod == SelectionMethod.FIFO)
						{
							if(bestTextClipping == null)
							{
								bestTextClipping = (TextClipping)clipping;
							}
						}
						*/
						double textClippingScore = scoreTextAtPoint(((TextClipping)clipping).getText(), closure.getDndPoint().getX(), closure.getDndPoint().getY());
						if(textClippingScore > bestTextClippingScore)//tbd, normalize on lenght
						{
							 bestTextClippingScore = textClippingScore;
							 bestTextClipping = (TextClipping) clipping;
						}
					}
					else if(clipping instanceof ImageClipping)
					{
						//debug("Found image clipping");
						if(selectionMethod == SelectionMethod.FIFO)
						{
							if(bestImageClipping == null)
							{
								bestImageClipping = (ImageClipping)clipping;
							}
						}
					}
				}
				//TODO: Handle piles et cetera
				if(bestImageClipping != null)
				{

					//try to download or make closure et cetera...
					sessionScope.getOrConstructImage(bestImageClipping.getMedia().getLocation());
					DocumentClosure bestImageClippingClosure = bestImageClipping.getMedia().getOrConstructClosure();
					bestImageClippingClosure.addContinuation(new DropImageContinuation(bestImageClipping));
					bestImageClippingClosure.setDndPoint(closure.getDndPoint());
					bestImageClippingClosure.queueDownload();

					if(bestImageClipping.getMedia().isDownloadDone())
					{
						debug("image already exists");
						interactiveSpace.createAndAddClipping((ImageClipping)bestImageClipping, closure.getDndPoint().getX(), closure.getDndPoint().getY());
					}

					return null;
				}
				if(bestTextClipping != null)
				{
					//preprocess at this point to make smaller...
					TextClipping gistWithContext = (TextClipping)bestTextClipping;
					gistWithContext.setText(gistForTextAndPosition(closure.getDndPoint().getX(),closure.getDndPoint().getY(),10,gistWithContext.getText()));
					interactiveSpace.createAndAddClipping(gistWithContext, closure.getDndPoint().getX(), closure.getDndPoint().getY());
					return null;
				}
			}
			else
			{
				debug("No clippings were found. Nothing visualized.");
			}
			debug("J");
		}
		else
		{
			debug("Ignore because not a drop");
		}
		return null;
	}


	class DropImageContinuation implements Continuation<DocumentClosure>
	{
		ImageClipping bestImageClipping;
		public DropImageContinuation()
		{
			super();
		}
		public DropImageContinuation(ImageClipping imageClipping)
		{
			super();
			bestImageClipping = imageClipping;
		}
		
		@Override
		public void callback(DocumentClosure o)
		{
			//debug("Here is the url for the document that should be showing up as the source:"+bestImageClipping.getSourceDoc().getLocation());
     // debug(""+bestImageClipping.getSourceDoc().setTitle(title));
      
			//TODO we may want to download all of the images first before picking one.
			//for now, we assume that we just display anything that uses this as its continuation.
			//bestImageClipping.getSourceDoc().setTitle("SWIINGERS ARE FUNNY!!!");
			//bestImageClipping.setSourceDoc(sessionScope.getOrConstructDocument((bestImageClipping.getSourceDoc().getDownloadLocation())));

			if(bestImageClipping.getSourceDoc() == null)
				debug("There is no source document for this image");
			InteractiveSpace interactiveSpace = sessionScope.getInteractiveSpace();
			interactiveSpace.createAndAddClipping((ImageClipping)bestImageClipping, o.getDndPoint().getX(), o.getDndPoint().getY());
			debug("Dropped image");
		}
	}
}
