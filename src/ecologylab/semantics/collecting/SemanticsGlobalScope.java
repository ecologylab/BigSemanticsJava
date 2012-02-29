/**
 * 
 */
package ecologylab.semantics.collecting;

import java.io.File;

import ecologylab.generic.Debug;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.gui.InteractiveSpace;
import ecologylab.semantics.html.dom.IDOMProvider;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metametadata.FieldParserFactory;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * 
 * The SemanticsScope (also known as Crossroads) contains references to 
 * all of the big global singleton object of S.IM.PL Semantics:
 * (1) GlobalCollection that maps ParsedURL keys to <? extends Document> values.
 * (2) MetaMetadataRespository
 * (3) SemanticsDownloadMonitors -- a set of DownloadMonitors, with different priority levels and media assignments.
 * 
 * The SemanticsSessionScope will include references to Crossroads and to Crawler, if there is one. 
 * I believe it is also where we store state related to Seeding. 
 * 
 * @author andruid
 */
public class SemanticsGlobalScope extends MetaMetadataRepositoryInit
{
	
	/**
	 * Maps locations to Document Metadata subclasses. Constructs these Document instances as needed
	 * using the MetaMetadataRepository.
	 */
	final protected TNGGlobalCollections				globalCollection;

	/**
	 * Pool of DownloadMonitors used for parsing Documents of various types.
	 */
	final private SemanticsDownloadMonitors			downloadMonitors;

	final private Class<? extends IDOMProvider>	domProviderClass;

	final private FieldParserFactory						fieldParserFactory;
	
	public SemanticsGlobalScope(SimplTypesScope metadataTScope, Class<? extends IDOMProvider> domProviderClass)
	{
		this(null, metadataTScope, domProviderClass);
	}
	
	public SemanticsGlobalScope(File repositoryLocation, SimplTypesScope metadataTScope, Class<? extends IDOMProvider> domProviderClass)
	{
		this(repositoryLocation, MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FORMAT, metadataTScope, domProviderClass);
	}
	
	public SemanticsGlobalScope(File repositoryLocation, Format repositoryFormat, SimplTypesScope metadataTScope, Class<? extends IDOMProvider> domProviderClass)
	{
		super(repositoryLocation, repositoryFormat, metadataTScope);
		this.domProviderClass = domProviderClass;
		globalCollection = TNGGlobalCollections.getSingleton(getMetaMetadataRepository());
		downloadMonitors = new SemanticsDownloadMonitors();
		fieldParserFactory = new FieldParserFactory();
	}

	/**
	 * @return the globalCollection
	 */
	public TNGGlobalCollections getGlobalCollection()
	{
		return globalCollection;
	}

	/**
	 * Pool of DownloadMonitors used for parsing Documents of various types.
	 * 
	 * @return the downloadMonitors
	 */
	public SemanticsDownloadMonitors getDownloadMonitors()
	{
		return downloadMonitors;
	}	
	
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
	
	public IDOMProvider constructDOMProvider()
	{
		return ReflectionTools.getInstance(domProviderClass);
	}
	
	public Document lookupDocument(ParsedURL location)
	{
		return location == null ? null : globalCollection.lookupDocument(location);
	}

	public Document getOrConstructDocument(ParsedURL location)
	{
		if (location == null)
			return null;
		Document result	= globalCollection.getOrConstruct(location, false);
		result.setSemanticsSessionScope(this);
		return result;
	}
	
	public Image getOrConstructImage(ParsedURL location)
	{
		if (location == null)
			return null;
		Document constructDocument = globalCollection.getOrConstruct(location, true);

		Image result	= null;
		if (constructDocument.isImage())
		{
			result	= (Image) constructDocument;
			result.setSemanticsSessionScope(this);
		}
		return result;
	}	

	/**
	 * Does nothing in this base class.
	 * @param message
	 */
	public void displayStatus(String message)
	{
	}

	/**
	 * Does nothing in this base class.
	 * @param message
	 */
  public void displayStatus(String message, int ticks)
  {
  }
	/**
	 * Does nothing in this base class.
	 */
	public Seeding getSeeding()
	{
		return null;
	}
	/**
	 * Does nothing in this base class.
	 */
  public int getAppropriateFontIndex()
  {
  	return -1;
  }

	/**
	 * Does nothing in this base class.
	 */
	public Crawler getCrawler()
	{
		return null;
	}
	/**
	 * Does nothing in this base class.
	 */
	public boolean hasCrawler()
	{
		return false;
	}

  /**
   * Unlike the session scope, the global scope is not discriminating. 
   * 
   * @param purl
   * @return	true
   */
	public boolean accept(ParsedURL purl)
	{
		return true;
	}
	
	/**
	 * 
	 * @param location
	 * @return	always true in this, the base class
	 */
	public boolean isLocationNew(ParsedURL location)
	{
		return true;
	}

	/**
	 * 
	 * @return the field parser factory for this session.
	 */
	public FieldParserFactory getFieldParserFactory()
	{
		return fieldParserFactory;
	}
	
	public InteractiveSpace getInteractiveSpace()
	{
		return null;
	}

}
