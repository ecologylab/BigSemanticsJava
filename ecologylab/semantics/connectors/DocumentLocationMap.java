/**
 * 
 */
package ecologylab.semantics.connectors;

import java.util.concurrent.ConcurrentHashMap;

import ecologylab.generic.Debug;
import ecologylab.generic.HashMapWriteSynch;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
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

	//FIXME -- woefully inadequate!!!
	public D substitute(D document)
	{
		this.put(document.getLocation(), document);
		
		return document;
	}
}
