/**
 * 
 */
package ecologylab.semantics.collecting;

import java.io.File;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.collections.ConcurrentHashSet;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.gui.InteractiveSpace;
import ecologylab.semantics.gui.WindowSystemBridge;
import ecologylab.semantics.html.dom.IDOMProvider;
import ecologylab.semantics.namesandnums.DocumentParserTagNames;
import ecologylab.semantics.namesandnums.SemanticsSessionObjectNames;
import ecologylab.semantics.seeding.SemanticsPrefs;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * The fundamental Crossroads of the S.IM.PL Semantics session. 
 * Contains references to all major objects specific to the session, including 
 * 		VisitedLocationsMap, Crawler, Seeding, WindowSystemBridge, and InteractiveSpace.
 * <p/>
 * Also, by inheritance, refers to GlobalCollections, MetaMetadataRepository, and SemanticsDownloadMonitors.
 * <p/>
 * When this was called InfoCollector, all of the functionality of these objects was moshed together in this.
 * Now, they are broken out.
 * 
 * @author andruid
 */
public class SemanticsSessionScope extends SemanticsGlobalScope
implements SemanticsPrefs, ApplicationProperties, DocumentParserTagNames
{
	
	private static SemanticsSessionScope	globalSemanticsScope;

	public static SemanticsSessionScope get()
	{
		return globalSemanticsScope;
	}
	
	////////////////////////////////////////////////////////////////////////
	
	private final Crawler												crawler;
	
	protected  WindowSystemBridge								guiBridge;
	
	protected InteractiveSpace									interactiveSpace;
	
	private final Seeding												seeding;
	
	private ConcurrentHashSet<ParsedURL> 				visitedLocations	= new ConcurrentHashSet<ParsedURL>(DocumentLocationMap.NUM_DOCUMENTS);
	
	/**
	 * Construct with no Crawler, and empty Seeding.
	 * 
	 * @param metadataTypesScope	Generated metadataTypesScope.
	 */
	public SemanticsSessionScope(SimplTypesScope metadataTypesScope, Class<? extends IDOMProvider> domProviderClass)
	{
		this(metadataTypesScope, null, domProviderClass);
	}
	
	public SemanticsSessionScope(File repositoryLocation, SimplTypesScope metadataTypesScope, Class<? extends IDOMProvider> domProviderClass)
	{
		this(repositoryLocation, metadataTypesScope, null, domProviderClass);
	}
	
	public SemanticsSessionScope(File repositoryLocation, Format repositoryFormat, SimplTypesScope metadataTypesScope, Class<? extends IDOMProvider> domProviderClass)
	{
		this(repositoryLocation, repositoryFormat, metadataTypesScope, null, domProviderClass);
	}
	
	public SemanticsSessionScope(SimplTypesScope metadataTypesScope, Crawler crawler, Class<? extends IDOMProvider> domProviderClass)
	{
		this(null, MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT, metadataTypesScope, new Seeding(), crawler, domProviderClass);
	}
	
	public SemanticsSessionScope(File repositoryLocation, SimplTypesScope metadataTypesScope, Crawler crawler, Class<? extends IDOMProvider> domProviderClass)
	{
		this(repositoryLocation, MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT, metadataTypesScope, new Seeding(), crawler, domProviderClass);
	}
	
	public SemanticsSessionScope(Format repositoryFormat, SimplTypesScope metadataTypesScope, Crawler crawler, Class<? extends IDOMProvider> domProviderClass)
	{
		this(null, repositoryFormat, metadataTypesScope, new Seeding(), crawler, domProviderClass);
	}
	
	public SemanticsSessionScope(File repositoryLocation, Format repositoryFormat, SimplTypesScope metadataTypesScope, Crawler crawler, Class<? extends IDOMProvider> domProviderClass)
	{
		this(repositoryLocation, repositoryFormat, metadataTypesScope, new Seeding(), crawler, domProviderClass);
	}
	
	public SemanticsSessionScope(File repositoryLocation, Format repositoryFormat, SimplTypesScope metadataTypesScope, Seeding seeding, Crawler crawler, Class<? extends IDOMProvider> domProviderClass)
	{
		super(repositoryLocation, repositoryFormat, metadataTypesScope, domProviderClass);
		this.put(SemanticsSessionObjectNames.INFO_COLLECTOR, this);	//TODO make this unnecessary; its a band-aid on old code
		this.crawler											= crawler;
		this.seeding											= seeding;
		seeding.setSemanticsSessionScope(this);
		if (crawler != null)
		{
			crawler.semanticsSessionScope		= this;
			crawler.setSeeding(seeding);
		}
		Debug.println("");
		globalSemanticsScope = this;
	}

	///////////////////////////////////////// WindowSystem / Display Stuff ////////////////////////////////////////
	
	@Override
	public void displayStatus(String message)
	{
		if (guiBridge != null)
			guiBridge.displayStatus(message);
		else
			debug(message);
	}
	
	@Override
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
  
  @Override
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
	
  public void setInteractiveSpace(InteractiveSpace interactiveSpace)
	{
		this.interactiveSpace = interactiveSpace;
	}
  
	///////////////////////////////////// end WindowSystem / Display Stuff ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
	/**
	 * Accept the purl if there is no crawler, or if the Seeding says to.
	 * 
	 * @param purl
	 * @return
	 */
	public boolean accept(ParsedURL purl)
	{
		return isAcceptAll() || seeding.accept(purl);
	}
	
	/**
	 * @return the crawler
	 */
	@Override
	public Crawler getCrawler()
	{
		return crawler;
	}
	
	@Override
	public boolean hasCrawler()
	{
		return crawler != null;
	}
	
	public void stopCrawler(boolean kill)
	{
		if (hasCrawler())
			crawler.stop(kill);
	}
	
	/**
	 * True if all links should be accepted, without checking their traversability.
	 * This is true if there is no Crawler, or no Seeding, 
	 * @return
	 */
	public boolean isAcceptAll()
	{
		return crawler == null || seeding == null;
	}
	
	/**
	 * @return the seeding
	 */
	public Seeding getSeeding()
	{
		return seeding;
	}

	/**
	 * Add element to visitedLocations set if it wasn't there already.
	 * 
	 * @param	location	Location to test for membership in visitedLocations and add if it was absent.
	 * 
	 * @return	true if the visitedLocations set changed, that is, if the location was new.
	 */
	public boolean isLocationNew(ParsedURL location)
	{
		return visitedLocations.add(location);
	}

}
