package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;

public class ReferencesMap extends Metadata
{
	private	HashMapArrayList<ParsedURL, Reference>		referenceMap 		= new HashMapArrayList<ParsedURL, Reference>();

	public Reference get(String key)
	{
		return referenceMap.get(ParsedURL.getAbsolute(key));
	}
	public Reference get(int index)
	{
		if(index < referenceMap.size())
		{
			return referenceMap.get(index);
		}
		return null;
	}
	
	public void add(String key)
	{
		Reference reference = new Reference(key);
		this.addReference(reference);
	}
	public void addReference(Reference reference)
	{
		referenceMap.put(reference.link, reference);
	}
	/**
	 * @return the referenceMap
	 */
	public HashMapArrayList<ParsedURL, Reference> getReferenceMap()
	{
		return referenceMap;
	}
	/**
	 * @param referenceMap the referenceMap to set
	 */
	public void setReferenceMap(HashMapArrayList<ParsedURL, Reference> referenceMap)
	{
		this.referenceMap = referenceMap;
	}
	
}
