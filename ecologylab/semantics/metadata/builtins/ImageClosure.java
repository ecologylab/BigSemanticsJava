package ecologylab.semantics.metadata.builtins;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.DispatchTarget;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;

/**
 * 
 */

/**
 * @author andruid
 *
 */
public class ImageClosure extends DocumentClosure<Image, ImageClosure>
{
	ImageClipping				imageClipping;
	
	ImageClosure(Image document, SemanticInLinks semanticInlinks)
	{
		super(document, semanticInlinks);
	}

	@Override
	protected DownloadMonitor<ImageClosure> downloadMonitor()
	{
		return isDnd() ? NewInfoCollector.IMAGE_DND_DOWNLOAD_MONITOR : 
		  NewInfoCollector.IMAGE_DOWNLOAD_MONITOR;
	}
}
