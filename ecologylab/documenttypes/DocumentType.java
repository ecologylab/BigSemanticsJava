/**
 * 
 */
package ecologylab.documenttypes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;

import ecologylab.collections.PrefixCollection;
import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ConnectionHelper;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActions;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.ElementState;

/**
 * Super class for all document types. This class obtains the connection to a document. A parse
 * method may be present to process the document.
 * <p/>
 * Their role is to parse documents. They start with a PURL, which the static connect() method
 * translates into an PURLConnection, and an appropriate instance of a subclass. The
 * {@link #parse() parse()} method is then called to translate the document into the semantic model,
 * using the PURLConnection.
 * <p/>
 * The translation from a PURL to an appropriate subclass instance is performed using a combination
 * of the PURLs extension, if it has a useful one, and then the mime-type returned by the
 * URLConnection response header. These keys are then used to perform a lookup in one of the
 * registries maintained in this class.
 * 
 * @author andruid
 * @author eunyee
 */
abstract public class DocumentType<AC extends Container, IP extends InfoCollector<AC, IP>, ES extends ElementState>
		extends Debug
{
	
	protected PURLConnection		purlConnection;

	protected MetaMetadata			metaMetadata;

	protected AC					container;

	protected IP					abstractInfoCollector;

	protected static final String[]	IMAGE_MIME_STRINGS		= javax.imageio.ImageIO
																	.getReaderMIMETypes();

	protected static final String[]	IMAGE_SUFFIX_STRINGS	= ImageIO.getReaderFormatNames();

	protected static final class DocumentTypeRegistry extends Scope<DocumentType>
	{

	}

	protected static final Scope<Class<? extends DocumentType>>	registryByMimeType	= new Scope<Class<? extends DocumentType>>();

	protected static final Scope<Class<? extends DocumentType>>	registryBySuffix	= new Scope<Class<? extends DocumentType>>();

	// private static final ClassRegistry<? extends DocumentType> rbs = new
	// ClassRegistry<? extends DocumentType>();

	/**
	 * Prefix Collection for the documenttypes like ACMPortal type... Matches
	 * http://portal.acm.org/citation.cfm?...
	 */
	protected static final PrefixCollection						prefixCollection	= new PrefixCollection(
																							'/',
																							true);

	/**
	 * Keys are DocumentType class names without package names, returned by Class.getSimpleName().
	 */
	protected static final Scope<Class<? extends DocumentType>>	registryByClassName	= new Scope<Class<? extends DocumentType>>();

	/**
	 * DocumentType constructor
	 * 
	 */
	protected DocumentType ()
	{
	}

	/**
	 * Set the InfoCollector while constructing.
	 * 
	 * @param infoCollector
	 */
	protected DocumentType ( IP infoCollector )
	{
		this.abstractInfoCollector = infoCollector;
	}

	public abstract void parse ( ) throws IOException;
	
	/**
	 * Optional parse method takes an an XML DOM represented as an ElementState as input.
	 * 
	 * @param es
	 */
	public void parse(ES es)
	{
		
	}
	/**
	 * Call parse() and then the postParseHook().
	 * 
	 * @param elementState
	 */
	public final void performParse(ES elementState)
	{
		parse(elementState);
		postParseHook();
	}
	/**
	 * Called after parsing.
	 */
	public void postParseHook()
	{
	}

	/**
	 * Optional parse method takes an XML DOM represented as an org.w3c.Document as input.
	 * @param dom
	 */
	public void parse(Document dom)
	{
		
		
	}
	
	interface DocumentTypeHelper extends ConnectionHelper
	{
		DocumentType getResult ( );
	}

	/**
	 * Open a connection to the URL. Read the header, but not the content. Look at if the path
	 * exists, if there is a redirect, and the mime type. If there is a redirect, process it.
	 * <p/>
	 * Create an InputStream. Using reflection (Class.newInstance()), create the appropriate
	 * DocumentType, based on that mimeType, using the allTypes HashMap. Return it.
	 */
	public static DocumentType connect ( final ParsedURL purl, final Container container,
			final InfoCollector infoCollector, SemanticActions semanticAction )
	{
		DocumentTypeHelper helper = new DocumentTypeHelper()
		{
			DocumentType	result;

			public void handleFileDirectory ( File file )
			{
				// result = new FileDirectoryType(file, container,
				// infoCollector);
				result = infoCollector.newFileDirectoryType(file);
			}

			public boolean parseFilesWithSuffix ( String suffix )
			{
				result = getInstanceBySuffix(suffix, infoCollector);
				return (result != null);
			}

			public void displayStatus ( String message )
			{
				infoCollector.displayStatus(message);
			}

			public void badResult ( )
			{
				if (result != null)
				{
					result.recycle();
					result = null;
				}
			}

			public boolean processRedirect ( URL connectionURL ) throws Exception
			{
				ParsedURL connectionPURL = new ParsedURL(connectionURL);
				Container redirectedAbstractContainer = infoCollector
						.lookupAbstractContainer(connectionPURL);
				if (redirectedAbstractContainer != null)
				{
					// the redirected url has been visited already.
					// add this url into the redirected AbstractContainers's
					// additionalURLs.
					if (container != null)
						container.redirectInlinksTo(redirectedAbstractContainer);

					synchronized (infoCollector.globalCollectionContainersLock())
					{
						redirectedAbstractContainer.addAdditionalPURL(purl);
						infoCollector.mapContainerToPURL(purl, redirectedAbstractContainer);
					}

					redirectedAbstractContainer.performDownload();

					// we dont need the new container object that was passed in
					// TODO recycle it!
				}
				else
				// redirect to a new url
				{

					if (infoCollector.accept(connectionPURL))
					{
						println("redirect: " + purl + " -> " + connectionPURL);
						String domain = connectionPURL.domain();
						String connPURLSuffix = connectionPURL.suffix();
						// add entry to GlobalCollections containersHash
						if (container != null)
						{
							// FIXME:hack for acmPortal pdf containers.
							// The redirected URL has a timeout...which creates
							// a problem while
							// opening the saved xml.
							// if(connectionPURL.toString().startsWith(
							// "http://delivery.acm.org"))
							// {
							// return true;
							// }
							if ("acm.org".equals(domain) && "pdf".equals(connPURLSuffix))
							{
								return true;
							}
							infoCollector.mapContainerToPURL(purl, container);
							// redirect the AbstractContainer object
							container.resetPURL(connectionPURL);
						}
						// this is the only redirect case in which we continue
						// processing
						return true;
					}
					else
						println("rejecting redirect: " + purl + " -> " + connectionPURL);
				}
				return false;
			}

			public DocumentType getResult ( )
			{
				return result;
			}

		};

		MetaMetadata metaMetadata = infoCollector.metaMetaDataRepository().getByPURL(purl);
		PURLConnection purlConnection = purl.connect(helper, (metaMetadata == null) ? null
				: metaMetadata.getUserAgentString());
		DocumentType result = helper.getResult();

		// if(purlConnection.toString().startsWith(
		// "http://portal.acm.org/citation.cfm?id=336478"))
		// //
		// if(purl.toString().startsWith("http://portal.acm.org/citation.cfm"))
		// {
		// println("debug");
		// }
		// In some cases, the documentType is preset (e.g., GoogleSearch,
		// FlickrSearch, ...).
		// So, in this case, set the result now, instead of divining it from
		// mimeType.
		if ((result == null) && (container != null))
			result = container.documentType();

		if ((purlConnection != null) && (result == null))
		{
			/**
			 * The acmPrefixCollection has to be general PrefixCollection where one of the prefix
			 * has to be acm's. The getLookupPurl() method takes in the purl and returns the prefix
			 * purl. This prefix purl has to be used in lookupSpecialExtractor() to get the desired
			 * document type.
			 */
			// if(acmPrefixCollection.match(purl.toString(), '?'))
			// FIXME -- Abhinav -- get rid of this SOON. Your type will
			// supercede it.
			/*
			 * ParsedURL lookupPurl = getLookupPurl(purl); result =
			 * lookupSpecialExtractor(lookupPurl, infoCollector);
			 * 
			 * result = lookupSpecialExtractor(purl);
			 */

			/*
			 * FIXME -- Abhinav -- get rid of the third condition (startsWith hack) for the
			 * following if statement after creating prefix comparison for getting metaMetadata by
			 * purl.
			 */
			if (metaMetadata != null && metaMetadata.doesGenerateClass()
					&& (purl.toString().startsWith("http://portal.acm.org/citation.cfm?")))
			{
				result = new MetaMetadataXPathType(infoCollector, semanticAction);
			}
			if (result == null)
			{
				// it wasn't a File or an IOError or a bad redirect
				// do more to seek the DocumentType

				// TODO -- ask Blake, Eunyee for opinions about the logic here
				result = getInstanceBySuffix(purl.suffix(), infoCollector);
				if (result == null)
				{
					// ACMPortal type document is decided based on mimeType
					// which is text/html
					String mimeType = purlConnection.mimeType();
					if (mimeType != null)
					{
						result = getInstanceByMimeType(mimeType, infoCollector);
					}
				}
			}
		}

		if (result != null)
		{
			result.metaMetadata = metaMetadata;
			result.fillValues(purlConnection, container, infoCollector);
		}
		return result;
	}

	/**
	 * Takes in the purl and returns the prefix purl
	 * 
	 * @param purl
	 * @return
	 */
	public static ParsedURL getLookupPurl ( ParsedURL purl )
	{
		// FIXME -- this was an awful kludge
		// if(prefixCollection.match(purl.toString(), '?'))
		// {
		// return ACMPortalType.PURL;
		// }
		// else
		return purl;
	}

	/**
	 * Fill out the instance of this resulting from a succcessful connect().
	 * 
	 * @param purlConnection
	 * @param container
	 * @param infoCollector
	 */
	protected void fillValues ( PURLConnection purlConnection, AC container, IP infoCollector )
	{
		this.purlConnection = purlConnection;
		setContainer(container);
		setInfoCollector(infoCollector);
	}

	/**
	 * True if our analysis indicates the present AbstractContainer is an article, and not a
	 * collection of links. This affects calls to getWeight() in the model!
	 * 
	 * @return true for an article. false for a collection of links (like a homepage).
	 */
	public boolean isAnArticle ( )
	{
		return true;
	}

	/**
	 * Free resources associated with the connection.
	 */
	public void connectionRecycle ( )
	{
		// parsing done. now free resources asap to avert leaking and memory fragmentation
		// (this is a known problem w java.net.HttpURLConnection)
		PURLConnection purlConnection = this.purlConnection;
		if (purlConnection != null)
		{
			purlConnection.recycle();
			this.purlConnection = null;
		}
	}

	/**
	 * Free resources.
	 * 
	 */
	public void recycle ( )
	{
		connectionRecycle();
		container = null;
		abstractInfoCollector = null;
	}

	/**
	 * This method enables the default behavior of showing the user a message on the console,
	 * "Downloading http://..." to be overriden in the case of particular DocumentTypes that display
	 * their own custom messages.
	 * 
	 * @return true -- the default implementation.
	 */
	public boolean downloadingMessageOnConnect ( )
	{
		return true;
	}

	/*
	 * This method is an odd out for File-based instances of DocumentType, that arise during parse()
	 * of FileDirectoryType.
	 * 
	 * If they are many, we may want to process them without doing downloadAndParse().
	 */
	public void processWithoutParsing ( )
	{

	}

	protected InputStream inputStream ( )
	{
		return (purlConnection == null) ? null : purlConnection.inputStream();
	}

	/**
	 * Return true if this DocumentType is a AbstractContainer.
	 * 
	 * @return The default response is false.
	 */
	public boolean isContainer ( )
	{
		return false;
	}

	public boolean nullPURLConnectionOK ( )
	{
		return false;
	}

	public void handleIoError ( )
	{

	}

	public PURLConnection purlConnection ( )
	{
		return purlConnection;
	}

	/**
	 * Set the inputStream of this documeType object. Called from HTMLFragment.
	 * 
	 * @param inputStream
	 */
	public void setPURLConnection ( InputStream inputStream )
	{
		// this.inputStream = inputStream;
		purlConnection = new PURLConnection(null, inputStream);
	}

	/**
	 * Set the container of this documentType object.
	 * 
	 * @param container
	 */
	public void setContainer ( AC container )
	{
		this.container = container;
	}

	/**
	 * Set the infoCollector of this documentType object.
	 * 
	 * @param infoCollector
	 */
	public void setInfoCollector ( IP infoCollector )
	{
		this.abstractInfoCollector = infoCollector;
	}

	/**
	 * Get the ParsedURL value of the current document. If you want to get a URL value, you can use
	 * url() method in ParsedURL.
	 * 
	 * @return
	 */
	public ParsedURL purl ( )
	{
		return (container == null) ? null : container.purl();
	}

	public String toString ( )
	{
		ParsedURL purl = purl();
		String purlString = (purl != null) ? purl.toString() : null;
		if (purlString == null)
			purlString = "no purl";
		return super.toString() + "[" + purlString + "]";
	}

	/**
	 * Create a mapping from MimeType to the class object for a DocumentType.
	 */
	protected static void registerMime ( String mimeType,
			Class<? extends DocumentType> documentTypeClass )
	{
		registryByMimeType.put(mimeType, documentTypeClass);

		String simpleName = documentTypeClass.getSimpleName();
		if (!registryByClassName.containsKey(simpleName))
		{
			println("register simple name: " + simpleName);
			registryByClassName.put(simpleName, documentTypeClass);
		}
	}

	/**
	 * Create a mapping from filename suffix to the class object for a DocumentType. Actually create
	 * two mappings, one lower case, and one upper case.
	 */
	protected static void registerSuffix ( String suffix,
			Class<? extends DocumentType> documentTypeClass )
	{
		String lc = suffix.toLowerCase();
		registryBySuffix.put(lc, documentTypeClass);
		String uc = suffix.toUpperCase();
		registryBySuffix.put(uc, documentTypeClass);
	}

	/**
	 * Find the DocumentType class that corresponds to the mimeType, and construct a fresh instance
	 * of that type.
	 * 
	 * @param infoCollector
	 *            TODO
	 * 
	 * @return an instance of the DocumentType subclass that corresponds to mimeType.
	 */
	public static DocumentType getInstanceByMimeType ( String mimeType, InfoCollector infoCollector )
	{
		return getInstanceFromRegistry(registryByMimeType, mimeType, infoCollector);
	}

	/**
	 * Find the DocumentType class that corresponds to the mimeType, and construct a fresh instance
	 * of that type.
	 * 
	 * @param infoCollector
	 *            TODO
	 * 
	 * @return an instance of the DocumentType subclass that corresponds to mimeType.
	 */
	public static DocumentType getInstanceBySuffix ( String suffix, InfoCollector infoCollector )
	{
		return ((suffix == null) || (suffix.length() == 0)) ? null : getInstanceFromRegistry(
				registryBySuffix, suffix, infoCollector);
	}

	/**
	 * Find the DocumentType class that corresponds to the documentTypeSimpleName, and construct a
	 * fresh instance of that type.
	 * 
	 * @param documentTypeSimpleName
	 *            - Name of the DocumentType subclass, without the package.
	 * @param infoCollector
	 *            TODO
	 * 
	 * @return an instance of the DocumentType subclass that corresponds to documentTypeSimpleName.
	 */
	public static DocumentType getInstanceBySimpleName ( String documentTypeSimpleName,
			InfoCollector infoCollector )
	{
		return getInstanceFromRegistry(registryByClassName, documentTypeSimpleName, infoCollector);
	}

	public static boolean isMimeTypeParsable ( String mimeType )
	{
		return registryByMimeType.containsKey(mimeType);
	}

	/**
	 * Given one of our registries, and a key, do a lookup in the registry to obtain the Class
	 * object for the DocumentType subclass corresponding to the key -- in that registry.
	 * <p/>
	 * Then, construct an instance of that object
	 * 
	 * @param infoCollector
	 *            TODO
	 * 
	 * @return an instance of the DocumentType subclass that corresponds to the key, in the
	 *         specified registry.
	 */
	public static <DT extends DocumentType> DT getInstanceFromRegistry (
			Scope<Class<? extends DT>> thatRegistry, String key, InfoCollector infoCollector )
	{
		DT result = null;
		Class<? extends DT> documentTypeClass = thatRegistry.get(key);

		Object[] constructorArgs = new Object[1];
		constructorArgs[0] = infoCollector;

		result = ReflectionTools.getInstance(documentTypeClass, infoCollector
				.getInfoProcessorClassArg(), constructorArgs);
		return result;
	}

	public static <DT extends DocumentType> DT getInstanceFromRegistry (
			Scope<Class<? extends DT>> thatRegistry, String key, Class<?>[] parameterTypes,
			Object[] args )
	{
		DT result = null;
		Class<? extends DT> documentTypeClass = thatRegistry.get(key);
		if (documentTypeClass != null)
		{
			if ((parameterTypes == null) || (args == null))
			{
				result = getInstance(documentTypeClass);
			}
			else
			{
				result = ReflectionTools.getInstance(documentTypeClass, parameterTypes, args);
			}
		}
		return result;
	}

	/**
	 * Filter description cFMetadata for junk
	 * 
	 * @param elementState
	 *            ElementState object with a TEXT Node for description.
	 * 
	 * @return true if the stuff is worth using
	 */
	public static boolean isNonJunkDescription ( ElementState elementState )
	{
		return (elementState == null) ? false : isNonJunkDescription(elementState
				.getTextNodeString());
	}

	/**
	 * Filter description cFMetadata for junk
	 * 
	 * @return true if the stuff is worth using
	 */
	public static boolean isNonJunkDescription ( String description )
	{
		// println("isNonJunkDescription("+description);
		return (description != null) && !description.startsWith("Find breaking")
				&& // nytimes
				!description.startsWith("Read full")
				&& // cnn
				!description.startsWith("CNN.com") && !description.startsWith("Visit BBC")
				&& !description.startsWith("Flickr is almost certainly");
	}

	/**
	 * Obtain an instance of an information extractor, based on a URL that takes arguments, if one
	 * is available. (The ParsedURL arguments, found after ? in the URL, are ignored for the
	 * matching.)
	 * 
	 * @param key
	 * @param thatMap
	 * @return
	 */
	public static DocumentType getInstanceFromMap ( ParsedURL purl,
			HashMap<String, Class<? extends DocumentType>> thatMap )
	{
		DocumentType result = null;
		Class<? extends DocumentType> documentTypeClass = thatMap.get(purl
				.noAnchorNoQueryPageString());

		result = ReflectionTools.getInstance(documentTypeClass);
		return result;
	}

	public static DocumentType getInstanceFromMap ( ParsedURL purl,
			HashMap<String, Class<? extends DocumentType>> thatMap, InfoCollector infoCollector )
	{
		DocumentType result = null;
		Class<? extends DocumentType> documentTypeClass = thatMap.get(purl
				.noAnchorNoQueryPageString());

		Object[] constructorArgs = new Object[1];
		constructorArgs[0] = infoCollector;

		result = ReflectionTools.getInstance(documentTypeClass, infoCollector
				.getInfoProcessorClassArg(), constructorArgs);
		return result;
	}

	/**
	 * Get a DocumentType from a generic parameterized Class object.
	 * 
	 * @param thatClass
	 * @return
	 */
	public static <DT extends DocumentType> DT getInstance ( Class<? extends DT> thatClass )
	{
		DT result = null;
		if (thatClass != null)
		{
			try
			{
				result = thatClass.newInstance();
			}
			catch (InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean isIndexPage ( )
	{
		return false;
	}
	
	public boolean isContentPage ( )
	{
		return false;
	}

	
}
