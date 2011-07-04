/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.collections.ConcurrentHashSet;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.gui.InteractiveSpace;
import ecologylab.semantics.gui.WindowSystemBridge;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.semantics.namesandnums.SemanticsSessionObjectNames;
import ecologylab.semantics.seeding.SemanticsPrefs;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class SemanticsSessionScope extends SemanticsGlobalScope
implements SemanticsPrefs, ApplicationProperties, DocumentParserTagNames
{
	private final Crawler												crawler;
	
	private  WindowSystemBridge									guiBridge;
	
	private InteractiveSpace										interactiveSpace;
	
	private final Seeding												seeding;
	
	private ConcurrentHashSet<ParsedURL> 				visitedLocations	= new ConcurrentHashSet<ParsedURL>(DocumentLocationMap.NUM_DOCUMENTS);

	
	public SemanticsSessionScope(TranslationScope metaMetadataTranslations)
	{
		this(META_METADATA_REPOSITORY, metaMetadataTranslations);
	}
	public SemanticsSessionScope(MetaMetadataRepository metaMetadataRepository, TranslationScope metadataTranslationScope)
	{
		this(metadataTranslationScope, new Scope(), null);
	}

	public SemanticsSessionScope(TranslationScope metadataTranslationScope, Scope sessionScope, Crawler crawler)
	{
		super(metadataTranslationScope);
		this.put(SemanticsSessionObjectNames.INFO_COLLECTOR, this);	//TODO make this unnecessary; its a band-aid on old code
		this.crawler											= crawler;
		this.seeding											= new Seeding(this);
		if (crawler != null)
		{
			crawler.semanticsSessionScope		= this;
			crawler.setSeeding(seeding);
		}
		Debug.println("");
	}

	///////////////////////////////////////// WindowSystem / Display Stuff ////////////////////////////////////////
	public void displayStatus(String message)
	{
		if (guiBridge != null)
			guiBridge.displayStatus(message);
		else
			debug(message);
	}

  public void displayStatus(String message, int ticks)
  {
		if (guiBridge != null)
			guiBridge.displayStatus(message, ticks);
		else
			debug(message);
  }
  
  public int showOptionsDialog(String message, String title, String[] options, int initialOptionIndex)
  {
  	int result	= initialOptionIndex;
		if (guiBridge != null)
			result		= guiBridge.showOptionsDialog(message, title,options, initialOptionIndex);
		else
			debug("No GuiBridge, so not displaying: " + message + "\nReturning initial option.");
		
		return result;
  }
  
  public int getAppropriateFontIndex()
  {
  	return (guiBridge != null) ? guiBridge.getAppropriateFontIndex() : -1;
  }

	/**
	 * @return the interactiveSpace
	 */
	public InteractiveSpace getInteractiveSpace()
	{
		return interactiveSpace;
	}
	///////////////////////////////////// end WindowSystem / Display Stuff ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

  public  void warning(CharSequence message)
  {
  	Debug.warning(this, message);
  }
  public  void debug(CharSequence message)
  {
  	Debug.println(this, message);
  }
  public  void debugT(CharSequence message)
  {
  	Debug.debugT(this, message);
  }
	
	public Document getOrConstructDocument(ParsedURL location)
	{
		if (location == null)
			return null;
		Document result	= globalCollection.getOrConstruct(location);
		result.setSemanticsSessionScope(this);
		return result;
	}
	public Image getOrConstructImage(ParsedURL location)
	{
		if (location == null)
			return null;
		Document constructDocument = globalCollection.getOrConstruct(location);

		Image result	= null;
		if (constructDocument.isImage())
		{
			result	= (Image) constructDocument;
			result.setSemanticsSessionScope(this);
		}
		return result;
	}	

	/**
	 * Accept the purl if there is no crawler, or if the Seeding says to.
	 * 
	 * @param purl
	 * @return
	 */
	public boolean accept(ParsedURL purl)
	{
		return !isAcceptAll() && seeding.accept(purl);
	}
	/**
	 * @return the crawler
	 */
	public Crawler getCrawler()
	{
		return crawler;
	}
	public boolean hasCrawler()
	{
		return crawler != null;
	}
	public boolean isAcceptAll()
	{
		return crawler == null;
	}
	/**
	 * @return the seeding
	 */
	public Seeding getSeeding()
	{
		return seeding;
	}

	public boolean isLocationNew(ParsedURL location)
	{
		return visitedLocations.add(location);
	}
}
