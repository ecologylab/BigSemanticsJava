package ecologylab.semantics.seeding;

import java.awt.Point;
import java.io.File;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.metadata.builtins.Document;
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
	
	Document									document;
	
	/**
	 * If true, then no media should be collected from this document.
	 * Rather, it will be treated as a collection of links, that will be fed to the focused web crawler agent.
	 */
	@simpl_scalar		boolean		justCrawl;

	/**
	 * If true, then no links should be collected from this document and fed to the focused web crawler agent.
	 * Instead, only collect media to form image and text surrogates.
	 */
	@simpl_scalar		boolean		justMedia;
	
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
		this.action	= action;
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
	@Override
	public void performInternalSeedingSteps(SemanticsGlobalScope infoCollector)
	{
		if (url != null)
		{
	 		// get a Container object associated with this, associate a seed, and initiate download
	 		println("-- processing document seed: " + url);
	 		infoCollector.getSeeding().traversable(url);
	 		// strangely enough, a file document seed can have a parentContainer!
	 		File file					= url.file();
	 		document					= infoCollector.getOrConstructDocument(url);
	 		if (file != null)
	 		{
				File parent				= file.getParentFile();	// the directory the file lives in
				Document parentDocument	= infoCollector.getOrConstructDocument(new ParsedURL(parent));
		 		document.addInlink(parentDocument);
	 		}
	 		if (query != null)
	 		{
	 			document.setQuery(query);
	 			InterestModel.expressInterest(query, (short)3);
	 		}
	 		document.queueDownload();
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
	
	@Override
	public boolean isJustCrawl()
	{
		return justCrawl;
	}

	@Override
	public boolean isJustMedia()
	{
		return justMedia;
	}
}
