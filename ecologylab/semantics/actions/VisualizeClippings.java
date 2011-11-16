package ecologylab.semantics.actions;
import java.io.IOException;
import java.util.List;


import ecologylab.generic.Continuation;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;
import ecologylab.semantics.gui.InteractiveSpace;
import ecologylab.semantics.metadata.builtins.Clipping;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metadata.builtins.TextClipping;

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

			List<Clipping> clippings = compoundSource.getClippings();
			if(clippings != null)
			{
				for (Clipping clipping : clippings)
				{
					if (clipping instanceof TextClipping)
					{
						//debug("Found text clipping");
						if(selectionMethod == SelectionMethod.FIFO)
						{
							if(bestTextClipping == null)
							{
								bestTextClipping = (TextClipping)clipping;
							}
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
					bestImageClipping.getMedia().getOrConstructClosure().addContinuation(new DropImageContinuation(bestImageClipping));
					bestImageClipping.getMedia().getOrConstructClosure().setDndPoint(closure.getDndPoint());
					bestImageClipping.getMedia().getOrConstructClosure().queueDownload();

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
					interactiveSpace.createAndAddClipping((TextClipping)bestTextClipping, closure.getDndPoint().getX(), closure.getDndPoint().getY());
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
