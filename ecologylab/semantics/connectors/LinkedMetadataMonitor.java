package ecologylab.semantics.connectors;

import java.util.HashMap;
import java.util.Map;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.LinkWith;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;

public class LinkedMetadataMonitor
{

	/**
	 * map a monitored class name to a collection of monitor records, each of which corresponds to a
	 * metadata object.
	 */
	private Map<String, Map<Metadata, LinkWith>>	monitorRecords	= new HashMap<String, Map<Metadata, LinkWith>>();

	public void registerName(String name)
	{
		if (!monitorRecords.containsKey(name))
			monitorRecords.put(name, new HashMap<Metadata, LinkWith>());
	}

	public void addMonitors(Metadata object)
	{
		MetaMetadata mmd = (MetaMetadata) object.getMetaMetadata();
		Map<String, LinkWith> linkWiths = mmd.getLinkWiths(); // linkWiths can't be null
		for (String name : linkWiths.keySet())
		{
			LinkWith lw = linkWiths.get(name);
			Map<Metadata, LinkWith> collection = monitorRecords.get(lw.getName()); // collection can't be
																																							// null
			synchronized (collection)
			{
				collection.put(object, lw);
			}
		}
	}

	public void removeMonitors(Metadata object)
	{
		MetaMetadata mmd = (MetaMetadata) object.getMetaMetadata();
		Map<String, LinkWith> linkWiths = mmd.getLinkWiths();
		for (String name : linkWiths.keySet())
		{
			LinkWith lw = linkWiths.get(name);
			Map<Metadata, LinkWith> collection = monitorRecords.get(lw.getName());
			synchronized (collection)
			{
				collection.remove(object);
			}
		}
	}

	public boolean tryLink(MetaMetadataRepository repository, Metadata parsedMetadata)
	{
		if (parsedMetadata == null)
			return false;

		MetaMetadata mmd = (MetaMetadata) parsedMetadata.getMetaMetadata();
		String mmdName = mmd.getTypeName();

		while (!monitorRecords.containsKey(mmdName))
		{
			if (mmdName == null || "metadata".equals(mmdName))
				return false;
			mmdName = mmd.getSuperMmdTypeName();
			mmd = repository.getByTagName(mmdName);
		}

		Map<Metadata, LinkWith> records = monitorRecords.get(mmdName);
		for (Metadata object : records.keySet())
		{
			LinkWith lw = records.get(object);
			if (lw.tryLink(parsedMetadata, object))
				return true;
		}

		return false;
	}

}
