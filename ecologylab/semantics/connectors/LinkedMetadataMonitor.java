package ecologylab.semantics.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.LinkWith;
import ecologylab.semantics.metametadata.MetaMetadata;

public class LinkedMetadataMonitor
{

	private static class MonitorRecord
	{

		public Metadata	object;

		public LinkWith	link;

	}

	/**
	 * meta-metadata types that are not monitored.
	 */
	private Set<String>																unmonitoredMetaMetadataTypeNames	= new HashSet<String>();

	/**
	 * map monitored class name to a collection of monitor records, each of which corresponds to a
	 * metadata object.
	 */
	private Map<String, Map<Metadata, MonitorRecord>>	monitorRecords										= new HashMap<String, Map<Metadata, MonitorRecord>>();

	/**
	 * map sub class name to monitored base class name.
	 */
	private Map<String, String>												monitoredMetaMetadataTypeMapping	= new HashMap<String, String>();

	public void addMonitors(Metadata object)
	{
		MetaMetadata mmd = (MetaMetadata) object.getMetaMetadata();
		ArrayList<LinkWith> linkWiths = mmd.getLinkWiths();
		if (linkWiths != null)
		{
			for (LinkWith lw : linkWiths)
			{
				MonitorRecord mr = new MonitorRecord();
				mr.object = object;
				mr.link = lw;

				if (!monitorRecords.containsKey(lw.getType()))
				{
					monitorRecords.put(lw.getType(), new HashMap<Metadata, MonitorRecord>());
				}
				Map<Metadata, MonitorRecord> collection = monitorRecords.get(lw.getType());
				collection.put(object, mr);
			}
		}
	}

	public void removeMonitors(Metadata object)
	{
		MetaMetadata mmd = (MetaMetadata) object.getMetaMetadata();
		ArrayList<LinkWith> linkWiths = mmd.getLinkWiths();
		if (linkWiths != null)
		{
			for (LinkWith lw : linkWiths)
			{
				if (monitorRecords.containsKey(lw.getType()))
				{
					Map<Metadata, MonitorRecord> collection = monitorRecords.get(lw.getType());
					collection.remove(object);
				}
			}
		}
	}

	public boolean link(Metadata parsedMetadata)
	{
		MetaMetadata mmd = (MetaMetadata) parsedMetadata.getMetaMetadata();
		String typeName = mmd.getType();
		
		// first check if we know that we don't need to monitor this class
		if (unmonitoredMetaMetadataTypeNames.contains(typeName))
		{
			return false;
		}
		
		// assume we do need to monitor this. check if the type name is registered.
		if (monitorRecords.containsKey(typeName))
		{
			Map<Metadata, MonitorRecord> records = monitorRecords.get(typeName);
			for (Metadata object : records.keySet())
			{
				MonitorRecord mr = records.get(object);
			}
		}
		
		
		
		
		return false;
	}

}
