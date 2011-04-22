package ecologylab.semantics.metadata.builtins;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.semantics.collecting.NewInfoCollector;
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
	ImageClosure(Image document, SemanticInLinks semanticInlinks)
	{
		super(document, semanticInlinks);
	}
	
	@Override
	protected void downloadAndParse()
	{
		warning("downloadAndParse() not implemented.");
	}

	@Override
	public DownloadMonitor<ImageClosure> downloadMonitor()
	{
		return isDnd() ? NewInfoCollector.IMAGE_DND_DOWNLOAD_MONITOR : 
		  NewInfoCollector.IMAGE_DOWNLOAD_MONITOR;
	}
}
