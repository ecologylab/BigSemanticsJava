/**
 * 
 */
package ecologylab.bigsemantics.collecting;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.net.ParsedURL;

/**
 * Monitors document downloading. Any object can
 * 
 * @author quyin
 * 
 */
public class DocumentDownloadingMonitor
{

	private SemanticsGlobalScope																					semanticsScope;

	private ConcurrentMap<ParsedURL, Set<DocumentDownloadedEventHandler>>	handlers;

	DocumentDownloadingMonitor(SemanticsGlobalScope semanticsScope)
	{
		this.semanticsScope = semanticsScope;
		this.handlers = new ConcurrentHashMap<ParsedURL, Set<DocumentDownloadedEventHandler>>();
	}

	public SemanticsGlobalScope getSemanticsScope()
	{
		return semanticsScope;
	}

	/**
	 * 
	 * @param hostObject
	 *          The object that is holding the Document that may change.
	 * @param location
	 *          The location that is listened for downloading.
	 * @param field
	 *          The Field that corresponds to the document to be downloaded, in hostObject.
	 * @param listener
	 *          The handler object that will actually handle the document downloaded event.
	 */
	public void listenForDocumentDownloading(Object hostObject, ParsedURL location, Field field,
			DocumentDownloadedEventHandler listener)
	{
		if (location != null)
		{
			listener.hostObject = hostObject;
			listener.location = location;
			listener.field = field;

			Set<DocumentDownloadedEventHandler> listenerSet = handlers.get(location);
			if (listenerSet == null)
			{
				synchronized (handlers)
				{
					listenerSet = handlers.get(location);
					if (listenerSet == null)
					{
						listenerSet = new HashSet<DocumentDownloadedEventHandler>();
						handlers.put(location, listenerSet);
					}
				}
			}
			synchronized (listenerSet)
			{
				listenerSet.add(listener);
			}
		}
	}

	public Set<DocumentDownloadedEventHandler> getListenersForDocument(Document downloadedDoc)
	{
		Set<DocumentDownloadedEventHandler> result = null;

		ParsedURL location = downloadedDoc.getLocation();
		result = getListenersForLocation(location, result);
		List<MetadataParsedURL> additionalLocations = downloadedDoc.getAdditionalLocations();
		if (additionalLocations != null && additionalLocations.size() > 0)
		{
			for (MetadataParsedURL mpurl : additionalLocations)
			{
				result = getListenersForLocation(mpurl.getValue(), result);
			}
		}

		return result;
	}

	public Set<DocumentDownloadedEventHandler> getListenersForLocation(ParsedURL purl,
			Set<DocumentDownloadedEventHandler> result)
	{
		Set<DocumentDownloadedEventHandler> newResult = handlers.get(purl);
		if (newResult != null)
		{
			synchronized (handlers)
			{
				handlers.remove(purl);
			}
			if (result == null)
				return newResult;
			else
				synchronized (newResult)
				{
					result.addAll(newResult);
				}
		}
		return result;
	}

}
