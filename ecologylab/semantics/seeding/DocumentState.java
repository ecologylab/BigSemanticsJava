package ecologylab.semantics.seeding;

import java.awt.Point;
import java.io.File;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.connectors.InfoCollectorBase;
import ecologylab.semantics.model.text.InterestModel;
import ecologylab.serialization.simpl_inherit;

/**
 * {@link Seed Seed} element used to tell combinFormation to process a document.
 *
 * @author andruid
 */
@simpl_inherit
public class DocumentState extends Seed
{
	private static final String DOCS_FEEDS = "docs_feeds";

	/**
	 * URL of the document or container specified for downloading and processing.
	 */
	@simpl_scalar		ParsedURL	url;
	
	/**
	 * If true, then no media should be collected from this document.
	 * Rather, it will be treated as a collection of links, that will be fed to the focused web crawler agent.
	 */
	@simpl_scalar		boolean		justcrawl;
	/**
	 * If true, then no links should be collected from this document and fed to the focused web crawler agent.
	 * Instead, only collect media to form image and text surrogates.
	 */
	@simpl_scalar		boolean		justmedia;
	
	//action is just a placeholder, and doesn't do anything.
	private String 		action;

	/**
	 * This field get's filled out for a Drag and Drop Seed.
	 */
	private Point		dropPoint;
	   
	public DocumentState()
	{
		super();
	}
	
	public DocumentState(String purlString, String action)
	{
		super();
		this.setCategory(action);
		this.setValue(purlString);
	}
	
	public DocumentState(ParsedURL purl)
	{
		this.url		= purl;
	}

	/**
	 * Bring this seed into the agent or directly into the composition.
	 * 
	 * @param objectRegistry		Context passed between services calls.
	 * @param infoCollector TODO
	 */
	public void performInternalSeedingSteps(InfoCollector infoCollector)
	{
		if (url != null)
		{
	 		// get a Container object associated with this, associate a seed, and initiate download
	 		println("-- processing document seed: " + url);
	 		infoCollector.traversable(url);
	 		// strangely enough, a file document seed can have a parentContainer!
	 		File file					= url.file();
	 		Container parentContainer	= null;
	 		if (file != null)
	 		{
				File parent				= file.getParentFile();	// the directory the file lives in
				ParsedURL parentPURL	= new ParsedURL(parent);
				parentContainer	= infoCollector.getContainer(null, null, InfoCollectorBase.DOCUMENT_META_METADATA, parentPURL, false, false, false);
	 		}
	 		Container container =infoCollector.getContainer(parentContainer, null, null, url, false, false, true);
	 		if(query != null)
	 		{
	 			container.setQuery(query);
	 			InterestModel.expressInterest(query, (short)3);
	 		}
	 		container.queueDownload();
	// 		getContainerDownloadIfNeeded(parentContainer, url, this, 
	// 				(dropPoint != null), justcrawl, justmedia);
		}
	}
 	
 	/**
 	 * The String the dashboard needs to show.
 	 * 
 	 * @return	The purl -- as a String.
 	 */
 	public String valueString()
 	{
 		return (url != null) ? url.toString() : new String("");
 	}
 	/**
	 * The category the dashboard uses to show.
	 * 
	 * @return	The search category.
	 */
	public String categoryString()
	{
		return (action != null) ? action : DOCS_FEEDS;
	}
	
	/**
	 * Not used but necessary.
	 * 
	 * @return	The search engine category.
	 */
	public String detailedCategoryString()
	{
		return (action != null) ? action : DOCS_FEEDS;
	}
	
	/**
	 * @param actionTypeString
	 */
	public void setCategory(String actionTypeString)
	{
		action = actionTypeString;
	}
	
	/**
	 * Set the value of the purl field, if the String is valid.
	 * 
	 * @param value
	 * @return	true if the field was set succesfully, or false if the value is invalid.
	 */
	public boolean setValue(String value)
	{
		ParsedURL trialValue	= ParsedURL.getAbsolute(value, "error parsing from seed");
		boolean result			= (trialValue != null);
		if (result)
			url					= trialValue;
		return result;
	}
	
	/**
	 * @param dropPoint The dropPoint to set.
	 */
	public void setDropPoint(Point dropPoint)
	{
		this.dropPoint = dropPoint;
	}
	
	/**
	 * @return Returns the dropPoint.
	 */
	public Point dropPoint()
	{
		return dropPoint;
	}
	
	/**
	 * The ParsedURL for this document or feed.
	 * 
	 * @return
	 */
	public ParsedURL getUrl()
	{
		return url;
	}

	public boolean canChangeVisibility()
	{
		return true;
	}

	public boolean isDeletable()
	{
		return true;
	}

	public boolean isEditable()
	{
		return true;
	}

	public boolean isRejectable()
	{
		return false;
	}
	
	@Override
	public boolean isHomogenousSeed()
	{
		return true;
	}
}
