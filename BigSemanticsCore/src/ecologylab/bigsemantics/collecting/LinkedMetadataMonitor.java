package ecologylab.bigsemantics.collecting;

import java.util.HashMap;
import java.util.Map;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metametadata.LinkWith;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;

/**
 * Monitoring parsed metadata & composite fields for potential metadata links.
 * 
 * @author quyin
 * 
 */
public class LinkedMetadataMonitor
{

	/**
	 * map a monitored class name to a collection of monitor records, each of which corresponds to a
	 * metadata object.
	 */
	private Map<String, Map<Metadata, LinkWith>>	monitorRecords	= new HashMap<String, Map<Metadata, LinkWith>>();

	/**
	 * Register a meta-metadata wrapper name for monitoring.
	 * 
	 * @param name
	 *          The monitored meta-metadata wrapper name.
	 */
	public void registerName(String name)
	{
		if (!monitorRecords.containsKey(name))
			monitorRecords.put(name, new HashMap<Metadata, LinkWith>());
	}

	/**
	 * Add a monitor for a metadata object which has &lt;link_with&gt; defined in its meta-metadata.
	 * 
	 * @param object
	 *          The metadata object who wants to monitor a potential link.
	 */
	public void addMonitors(Metadata object)
	{
		if (object.getMetaMetadata() instanceof MetaMetadata)
		{
			MetaMetadata mmd = (MetaMetadata) object.getMetaMetadata();
			Map<String, LinkWith> linkWiths = mmd.getLinkWiths();
			if (linkWiths != null)
			{
				for (String name : linkWiths.keySet())
				{
					LinkWith lw = linkWiths.get(name);
					Map<Metadata, LinkWith> collection = monitorRecords.get(lw.getName());
					// collection can't be null
					synchronized (collection)
					{
						collection.put(object, lw);
					}
				}
			}
		}
	}

	/**
	 * Remove a monitor for a metadata object.
	 * 
	 * @param object
	 *          The metadata object who wanted to monitor a link but don't now.
	 */
	public void removeMonitors(Metadata object)
	{
		if (object.getMetaMetadata() instanceof MetaMetadata)
		{
			MetaMetadata mmd = (MetaMetadata) object.getMetaMetadata();
			Map<String, LinkWith> linkWiths = mmd.getLinkWiths();
			if (linkWiths != null)
			{
				for (String name : linkWiths.keySet())
				{
					LinkWith lw = linkWiths.get(name);
					Map<Metadata, LinkWith> collection = monitorRecords.get(lw.getName());
					// collection can't be null
					synchronized (collection)
					{
						collection.remove(object);
					}
				}
			}
		}
	}

	/**
	 * When a new metadata object is parsed, try to link it with some monitoring metadata objects that
	 * are waiting here.
	 * 
	 * @param repository
	 * @param parsedMetadata
	 * @return If the linking happens.
	 */
	public boolean tryLink(MetaMetadataRepository repository, Metadata parsedMetadata)
	{
		if (parsedMetadata == null)
		{
			return false;
		}
		MetaMetadataCompositeField composite = parsedMetadata.getMetaMetadata();
		if (composite == null)
		{
		  return false;
		}

		MetaMetadata mmd;
		String mmdName;

    if (composite instanceof MetaMetadata)
		{
			mmd = (MetaMetadata) composite;
			mmdName = mmd.getName();
		}
		else
		{
			mmdName = composite.getTypeName();
			mmd = repository.getMMByName(mmdName);
		}

		if (mmd == null)
		{
			// maybe it's defined inline -- we need a solution for this kind of inline definition thing!
			// currently you can't do metadata linking on inline-defined meta_metadata classes
			return false;
		}

		while (!monitorRecords.containsKey(mmdName))
		{
			if (mmdName == null || "metadata".equals(mmdName))
				return false;
			mmdName = mmd.getSuperMmdTypeName();
			mmd = repository.getMMByName(mmdName);
		}

		Map<Metadata, LinkWith> records = monitorRecords.get(mmdName);
		for (Metadata object : records.keySet())
		{
			LinkWith lw = records.get(object);
			if (lw.tryLink(parsedMetadata, object))
			{
				SemanticActionHandler handler = object.pendingSemanticActionHandler;
				handler.takeSemanticActions();
				return true;
			}
		}

		return false;
	}

}
