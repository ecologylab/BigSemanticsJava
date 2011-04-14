/**
 * 
 */
package ecologylab.semantics.connectors;

import java.util.concurrent.ConcurrentHashMap;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metametadata.MetaMetadata;

/**
 * @author andruid
 *
 */
public class DocumentLocationMap<D extends Document> extends ConcurrentHashMap<ParsedURL, D>
{
	DocumentMapHelper<D> 									mapHelper;

	/**
	 * 
	 */
	public DocumentLocationMap(DocumentMapHelper<D> mapHelper)
	{
		super();
//		this							= new ConcurrentHashMap<ParsedURL, D>();
		this.mapHelper	= mapHelper;
	}
	/**
	 * Look-up in map and get from there if possible.
	 * If not, construct by using location to lookup meta-metadata.
	 * Then construct the subclass of Document that the meta-metadata specifies.
	 * Add it to the map and return.
	 * 
	 * @param location
	 * @return
	 */
	public D getOrConstruct(ParsedURL location)
	{
    D result = this.get(location);
    if (result == null) 
    {
    	// record does not yet exist
    	D newValue = mapHelper.constructValue(location);
    	result = this.putIfAbsent(location, newValue);
    	if (result == null) 
    	{
    		// put succeeded, use new value
    		result = newValue;
    	}
    }
    return result;
	}	
	
	public D getOrConstruct(MetaMetadata mmd, ParsedURL location)
	{
    D result = this.get(location);
    if (result == null) 
    {
    	// record does not yet exist
    	D newValue = mapHelper.constructValue(mmd, location);
    	result = this.putIfAbsent(location, newValue);
    	if (result == null) 
    	{
    		// put succeeded, use new value
    		result = newValue;
    	}
    }
    String inputName = mmd.getName();
		String resultName = result.getMetaMetadata().getName();
		if (!inputName.equals(resultName))
    {
    	System.err.println("DocumentLocationMap.getOrConstruct() ERROR: Meta-metadata inputName=" + inputName + " but resultName="+resultName + " for "+
    			location);
    	result	= null;
    }
    return result;
	}	
	
	public void setRecycled(ParsedURL location)
	{
		this.put(location, mapHelper.recycledValue());
	}
	
	public void setRecycled(MetadataParsedURL mPurl)
	{
		setRecycled(mPurl.getValue());
	}
	
	public void setUndefined(ParsedURL location)
	{
		this.put(location, mapHelper.undefinedValue());
	}

	public boolean isRecycled(ParsedURL location)
	{
		return mapHelper.recycledValue() == this.get(location);
	}

	public boolean isUndefined(ParsedURL location)
	{
		return mapHelper.undefinedValue() == this.get(location);
	}

	/**
	 * Change the mapped Document of reference for location to document.
	 * Also make sure that newDocument's locations are mapped.
	 * 
	 * @param location		The location that gets a new mapping.
	 * @param newDocument	The new document to be mapped to location.
	 */
	public void remap(ParsedURL location, Document newDocument)
	{
		put(location, (D) newDocument);
		ParsedURL newDocumentLocation = newDocument.getLocation();
		if (!location.equals(newDocumentLocation))
			put(newDocumentLocation, (D) newDocument);	// just to make sure
	}
/**
 * Change the mapped Document of reference for location to document.
 * 
 * @param oldDocument	The document no longer of record, but whose location is being mapped.
 * @param newDocument	The new document to be mapped to location.
 */
	public void remap(Document oldDocument, Document newDocument)
	{
		remap(oldDocument.getLocation(), newDocument);
	}
}
