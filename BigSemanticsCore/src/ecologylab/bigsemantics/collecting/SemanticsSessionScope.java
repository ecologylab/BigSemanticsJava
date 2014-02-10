/**
 * 
 */
package ecologylab.bigsemantics.collecting;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.appframework.ApplicationProperties;
import ecologylab.bigsemantics.gui.InteractiveSpace;
import ecologylab.bigsemantics.gui.WindowSystemBridge;
import ecologylab.bigsemantics.html.dom.IDOMProvider;
import ecologylab.bigsemantics.namesandnums.DocumentParserTagNames;
import ecologylab.bigsemantics.seeding.SemanticsPrefs;
import ecologylab.collections.ConcurrentHashSet;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * The fundamental Crossroads of the S.IM.PL Semantics session. Contains references to all major
 * objects specific to the session, including VisitedLocationsMap, Crawler, Seeding,
 * WindowSystemBridge, and InteractiveSpace.
 * <p/>
 * Also, by inheritance, refers to GlobalCollections, MetaMetadataRepository, and
 * SemanticsDownloadMonitors.
 * <p/>
 * When this was called InfoCollector, all of the functionality of these objects was moshed together
 * in this. Now, they are broken out.
 * 
 * @author andruid
 */
public class SemanticsSessionScope extends SemanticsGlobalScope
    implements SemanticsPrefs, ApplicationProperties, DocumentParserTagNames
{

  static Logger                        logger;

  static
  {
    logger = LoggerFactory.getLogger(SemanticsSessionScope.class);
  }

  private final Crawler                crawler;

  private final Seeding                seeding;

  private InteractiveSpace             interactiveSpace;

  private WindowSystemBridge           guiBridge;

  private ConcurrentHashSet<ParsedURL> visitedLocations;

  public SemanticsSessionScope(SimplTypesScope metadataTypesScope,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    this(null, metadataTypesScope, domProviderClass);
  }

  public SemanticsSessionScope(File repositoryLocation,
                               SimplTypesScope metadataTypesScope,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    this(repositoryLocation, metadataTypesScope, null, domProviderClass);
  }

  public SemanticsSessionScope(File repositoryLocation,
                               Format repositoryFormat,
                               SimplTypesScope metadataTypesScope,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    this(repositoryLocation, repositoryFormat, metadataTypesScope, null, domProviderClass);
  }

  public SemanticsSessionScope(SimplTypesScope metadataTypesScope,
                               Crawler crawler,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    this(null,
         MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT,
         metadataTypesScope,
         new Seeding(),
         crawler,
         domProviderClass);
  }

  public SemanticsSessionScope(File repositoryLocation,
                               SimplTypesScope metadataTypesScope,
                               Crawler crawler,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    this(repositoryLocation,
         MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT,
         metadataTypesScope,
         new Seeding(),
         crawler,
         domProviderClass);
  }

  public SemanticsSessionScope(Format repositoryFormat,
                               SimplTypesScope metadataTypesScope,
                               Crawler crawler,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    this(null, repositoryFormat, metadataTypesScope, new Seeding(), crawler, domProviderClass);
  }

  public SemanticsSessionScope(File repositoryLocation,
                               Format repositoryFormat,
                               SimplTypesScope metadataTypesScope,
                               Crawler crawler,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    this(repositoryLocation,
         repositoryFormat,
         metadataTypesScope,
         new Seeding(),
         crawler,
         domProviderClass);
  }

  public SemanticsSessionScope(File repositoryLocation,
                               Format repositoryFormat,
                               SimplTypesScope metadataTypesScope,
                               Seeding seeding,
                               Crawler crawler,
                               Class<? extends IDOMProvider> domProviderClass)
  {
    super(repositoryLocation, repositoryFormat, metadataTypesScope, domProviderClass);
    // this.put(SemanticsSessionObjectNames.INFO_COLLECTOR, this);
    this.visitedLocations = new ConcurrentHashSet<ParsedURL>(DocumentLocationMap.NUM_DOCUMENTS);
    this.crawler = crawler;
    this.seeding = seeding;
    seeding.setSemanticsSessionScope(this);
    if (crawler != null)
    {
      crawler.semanticsSessionScope = this;
      crawler.setSeeding(seeding);
    }
    Debug.println("");
  }

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
   * True if all links should be accepted, without checking their traversability. This is true if
   * there is no Crawler, or no Seeding,
   * 
   * @return
   */
  public boolean isAcceptAll()
  {
    return crawler == null || seeding == null;
  }

  /**
   * Add element to visitedLocations set if it wasn't there already.
   * 
   * @param location
   *          Location to test for membership in visitedLocations and add if it was absent.
   * 
   * @return true if the visitedLocations set changed, that is, if the location was new.
   */
  public boolean isLocationNew(ParsedURL location)
  {
    return visitedLocations.add(location);
  }

  /**
   * @return the seeding
   */
  public Seeding getSeeding()
  {
    return seeding;
  }

  @Override
  public int getAppropriateFontIndex()
  {
    return (guiBridge != null) ? guiBridge.getAppropriateFontIndex() : -1;
  }

  @Override
  public boolean hasCrawler()
  {
    return crawler != null;
  }

  /**
   * @return the crawler
   */
  @Override
  public Crawler getCrawler()
  {
    return crawler;
  }

  public void stopCrawler(boolean kill)
  {
    if (hasCrawler())
      crawler.stop(kill);
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

  @Override
  public void displayStatus(String message)
  {
    super.displayStatus(message);
    if (guiBridge != null)
      guiBridge.displayStatus(message);
  }

  @Override
  public void displayStatus(String message, int ticks)
  {
    super.displayStatus(message, ticks);
    if (guiBridge != null)
      guiBridge.displayStatus(message, ticks);
  }

  public int showOptionsDialog(String message,
                               String title,
                               String[] options,
                               int initialOptionIndex)
  {
    int result = initialOptionIndex;
    if (guiBridge != null)
    {
      result = guiBridge.showOptionsDialog(message, title, options, initialOptionIndex);
    }
    else
    {
      logger.warn("No GuiBridge, so not displaying and returning initial options: " + message);
    }
    return result;
  }

}
