/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;

import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.old.InfoCollector;
import ecologylab.semantics.seeding.Seed;

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
abstract public class DocumentParser
		extends Debug
{
	/**
	 * All information about the connection. Filled out by the connect() method.
	 */
	protected PURLConnection		purlConnection;

	protected DocumentClosure		documentClosure;
	
	public boolean 					cacheHit = false;

	protected NewInfoCollector					infoCollector;

	protected static final Scope<Class<? extends DocumentParser>>	registryByMimeType	= new Scope<Class<? extends DocumentParser>>();

	protected static final Scope<Class<? extends DocumentParser>>	registryBySuffix	= new Scope<Class<? extends DocumentParser>>();
	
	public static final Scope<Class<? extends DocumentParser>> bindingParserMap   = new Scope<Class<? extends DocumentParser>>();

	// private static final ClassRegistry<? extends DocumentType> rbs = new
	// ClassRegistry<? extends DocumentType>();

	static
	{
		bindingParserMap.put(SemanticActionsKeyWords.DIRECT_BINDING_PARSER,DirectBindingParser.class);
		bindingParserMap.put(SemanticActionsKeyWords.XPATH_PARSER,XPathParser.class);
		bindingParserMap.put(SemanticActionsKeyWords.FEED_PARSER, FeedParser.class);
		bindingParserMap.put(SemanticActionsKeyWords.DEFAULT_PARSER, HTMLDOMImageTextParser.class);
		bindingParserMap.put(SemanticActionsKeyWords.IMAGE_PARSER, ImageParser.class);
		
	}

	/**
	 * Keys are DocumentType class names without package names, returned by Class.getSimpleName().
	 */
	protected static final Scope<Class<? extends DocumentParser>>	registryByClassName	= new Scope<Class<? extends DocumentParser>>();

	
	/**
	 * DocumentType constructor
	 * 
	 */
	protected DocumentParser ()
	{
	}

	/**
	 * Set the InfoCollector while constructing.
	 * 
	 * @param infoCollector
	 */
	protected DocumentParser ( NewInfoCollector infoCollector )
	{
		this.infoCollector = infoCollector;
	}
	
	public abstract Document parse ( ) throws IOException;


	/**
	 * Fill out the instance of this resulting from a succcessful connect().
	 * 
	 * @param purlConnection
	 * @param documentClosure TODO
	 * @param infoCollector
	 */
	public void fillValues ( PURLConnection purlConnection, DocumentClosure documentClosure, NewInfoCollector infoCollector )
	{
		this.purlConnection		= purlConnection;
		this.documentClosure	= documentClosure;
		this.infoCollector = infoCollector;
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
		infoCollector = null;
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
		purlConnection = new PURLConnection(purl(), null, inputStream);
	}

	/**
	 * Get the ParsedURL value of the current document. If you want to get a URL value, you can use
	 * url() method in ParsedURL.
	 * 
	 * @return
	 */
	public ParsedURL purl ( )
	{
		Document document	= documentClosure.getDocument();
		return purlConnection != null ? purlConnection.getPurl() : (document != null) ? document.getLocation() : null;
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
/*	public static void registerMime ( String mimeType,
			Class<? extends DocumentParser> documentTypeClass )
	{
		registryByMimeType.put(mimeType, documentTypeClass);

		String simpleName = documentTypeClass.getSimpleName();
		if (!registryByClassName.containsKey(simpleName))
		{
			println("register simple name: " + simpleName);
			registryByClassName.put(simpleName, documentTypeClass);
		}
	}*/

	/**
	 * Create a mapping from filename suffix to the class object for a DocumentType. Actually create
	 * two mappings, one lower case, and one upper case.
	 */
/*	public static void registerSuffix ( String suffix,
			Class<? extends DocumentParser> documentTypeClass )
	{
		String lc = suffix.toLowerCase();
		registryBySuffix.put(lc, documentTypeClass);
		String uc = suffix.toUpperCase();
		registryBySuffix.put(uc, documentTypeClass);
	}
*/
	/**
	 * Find the DocumentType class that corresponds to the mimeType, and construct a fresh instance
	 * of that type.
	 * 
	 * @param infoCollector
	 *            TODO
	 * 
	 * @return an instance of the DocumentType subclass that corresponds to mimeType.
	 */
	/*public static DocumentParser getInstanceByMimeType ( String mimeType, InfoCollector infoCollector )
	{
		return getInstanceFromRegistry(registryByMimeType, mimeType, infoCollector);
	}*/

	/**
	 * Find the DocumentType class that corresponds to the mimeType, and construct a fresh instance
	 * of that type.
	 * 
	 * @param infoCollector
	 *            TODO
	 * 
	 * @return an instance of the DocumentType subclass that corresponds to mimeType.
	 */
	/*public static DocumentParser getInstanceBySuffix ( String suffix, InfoCollector infoCollector )
	{
		return ((suffix == null) || (suffix.length() == 0)) ? null : getInstanceFromRegistry(
				registryBySuffix, suffix, infoCollector);
	}
*/

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
	public static DocumentParser getInstanceBySimpleName ( String documentTypeSimpleName,
			InfoCollector infoCollector )
	{
		return getInstanceFromRegistry(registryByClassName, documentTypeSimpleName, infoCollector);
	}

	public static boolean isMimeTypeParsable ( String mimeType )
	{
		return registryByMimeType.containsKey(mimeType);
	}

//	static final Class[]	DEFAULT_INFO_COLLECTOR_CLASS_ARG	= {InfoCollector.class};
	
	static final Class[]  DEFAULT_DOCUMENTPARSER_ARG        = {NewInfoCollector.class};
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
	public static DocumentParser getInstanceFromRegistry (
			Scope<Class<? extends DocumentParser>> thatRegistry, String key, InfoCollector infoCollector )
	{
		DocumentParser result = null;
		Class<? extends DocumentParser> documentTypeClass = thatRegistry.get(key);

		Object[] constructorArgs = new Object[1];
		constructorArgs[0] = infoCollector;

		result = ReflectionTools.getInstance(documentTypeClass, infoCollector.getMyClassArg(), constructorArgs);
		if (result == null)
			result = ReflectionTools.getInstance(documentTypeClass, DEFAULT_DOCUMENTPARSER_ARG, constructorArgs);
//		if (result == null)
//			throw new RuntimeException("Registry lookup worked, but constructor matching failed :-( :-( :-(");
		return result;
	}

	public static DocumentParser getInstanceFromRegistry (Scope<Class<? extends DocumentParser>> thatRegistry, 
			String key, Class<?>[] parameterTypes, Object[] args )
	{
		DocumentParser result = null;
		Class<? extends DocumentParser> documentTypeClass = thatRegistry.get(key);
		if (documentTypeClass != null)
		{
			if ((parameterTypes == null) || (args == null))
			{
				result = getDocumentParserInstance(documentTypeClass);
			}
			else
			{
				result = ReflectionTools.getInstance(documentTypeClass, parameterTypes, args);
			}
		}
		return result;
	}

	public static DocumentParser getParserInstanceFromBindingMap(String binding,
			NewInfoCollector infoCollector)
	{
		DocumentParser result = null;
		Class<? extends DocumentParser> documentTypeClass = (Class<? extends DocumentParser>) bindingParserMap.get(binding);

		Object[] constructorArgs = new Object[1];
		constructorArgs[0] = infoCollector;

		result = ReflectionTools.getInstance(documentTypeClass, DEFAULT_DOCUMENTPARSER_ARG, constructorArgs);
		return result;
	}

	/**
	 * Get a DocumentType from a generic parameterized Class object.
	 * 
	 * @param thatClass
	 * @return
	 */
	public static DocumentParser getDocumentParserInstance ( Class<? extends DocumentParser> thatClass )
	{
		DocumentParser result = null;
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

	/**
	 * Differentiates referential documents, like HTML from composite documents, like PDF.
	 * 
	 * @return 	true if images referred to by this document are stored within the document itself.
	 * 					The default implementation, here returns false.
	 */
	public boolean isCompositeDocument()
	{
		return false;
	}


	public NewInfoCollector getInfoCollector()
	{
		return infoCollector;
	}

	public MetaMetadataCompositeField getMetaMetadata()
	{
		Document document	= documentClosure.getDocument();
		return document == null ? null : document.getMetaMetadata();
	}

	public ParsedURL getTruePURL()
	{
		Document document	= documentClosure.getDocument();
		return document == null ? null : document.getLocation();
	}
	
	/**
	 * Connects to a SeedDistributor, when appropriate.
	 * 
	 * @return 	null always for the default base class.
	 */
	public Seed getSeed()
	{
		return null;
	}

	/**
	 * 
	 * @return	true to avoid building a DOM at the beginning
	 */
	public boolean doesDirectBinding()
	{
		return false;
	}

	/**
	 * @return the document
	 */
	public Document getDocument()
	{
		return documentClosure.getDocument();
	}

}
