package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.metadata.Metadata;

/**
 * A class to hold dependency management of metametadata scalars for extraction. 
 * @author twhite
 *
 */
public class ScalarDependencyManager {
	
	//TODO: Clean up all of the predicate logic with some Google Guava stuff. 
	
	private HashMapArrayList<String, MetaMetadataField> fieldSet;
	
	public ScalarDependencyManager(HashMapArrayList<String, MetaMetadataField> fields)
	{
		this.fieldSet = fields;
	}	
	
	private List<String> getNoIncomingNodes(HashMap<String, List<String>> list)
	{
		List<String> ourList = new ArrayList<String>();
		
		for(String en : list.keySet())
		{
			if(list.get(en).isEmpty())
			{
				ourList.add(en);
			}
		}
		
		return ourList;
	}
	
	private HashMapArrayList<String, MetaMetadataScalarField> getScalars(HashMapArrayList<String, MetaMetadataField> fields)
	{
		HashMapArrayList<String, MetaMetadataScalarField> scalars = new HashMapArrayList<String, MetaMetadataScalarField>();
		for(String s : fields.keySet())
		{
			MetaMetadataField mmf = fields.get(s);
			if(mmf instanceof MetaMetadataScalarField)
			{
				scalars.put(s, (MetaMetadataScalarField)mmf);
			}
		}
		return scalars;
	}
	
	private HashMapArrayList<String, MetaMetadataField> getNonScalars(HashMapArrayList<String, MetaMetadataField> fields)
	{
		HashMapArrayList<String, MetaMetadataField> nonScalars = new HashMapArrayList<String, MetaMetadataField>();
		for(String s : fields.keySet())
		{
			MetaMetadataField mmf = fields.get(s);
			if(!(mmf instanceof MetaMetadataScalarField))
			{
				nonScalars.put(s, mmf);
			}
		}
		return nonScalars;
	}
	
	
	/**
	 * Attempts to perform a topological sort of the Fields to sort by dependnencies. 
	 * @param metadata The metadata that dependencies are connected to
	 * @return A sorted fieldset (such that values which depend on other values are extracted /after/ their dependencies
	 * @throws ScalarDependencyException If the dependencies have a cycle, will throw an exception. 
	 */
	public HashMapArrayList<String, MetaMetadataField> sortFieldSetByDependencies(Metadata metadata) throws ScalarDependencyException {
		
		HashMapArrayList<String, MetaMetadataField> ourCopy = new HashMapArrayList<String, MetaMetadataField>();
		ourCopy.putAll(fieldSet);
		
		HashMapArrayList<String, MetaMetadataScalarField> ourScalars = getScalars(ourCopy);
		HashMapArrayList<String, MetaMetadataField> ourNonScalars = getNonScalars(ourCopy);
		
		HashMap<String, List<String>> incomingEdges = new HashMap<String, List<String>>();
		
		for(String s: ourScalars.keySet())
		{
			incomingEdges.put(s, new ArrayList<String>());
		}
		
		for(String s: ourScalars.keySet())
		{
				MetaMetadataScalarField mmsf = ourScalars.get(s);
				
				if(mmsf.hasValueDependencies())
				{
					for(MetaMetadataValueField dep : mmsf.getValueDependencies())
					{
						String Name = dep.getScalarField(metadata).getName();
						incomingEdges.get(Name).add(mmsf.getName());
					}
				}
		}
		
		List<String> noIncomingNodes = getNoIncomingNodes(incomingEdges);
		List<MetaMetadataScalarField> visited = new ArrayList<MetaMetadataScalarField>();

		while(!noIncomingNodes.isEmpty())
		{
			String key = noIncomingNodes.remove(0);
			MetaMetadataScalarField mmsf = (MetaMetadataScalarField)ourCopy.get(key);
			visited.add(mmsf);
			
			for(MetaMetadataValueField mmvf : mmsf.getValueDependencies())
			{
				String WithIncomingEdge = mmvf.getScalarField(metadata).getName();
				incomingEdges.get(WithIncomingEdge).remove(key);
				if(incomingEdges.get(WithIncomingEdge).isEmpty())
				{
					noIncomingNodes.add(WithIncomingEdge);
				}
			}
		}
				
		if(visited.size() == ourScalars.size()) // If these aren't equal, we have a cycle. 
		{
			HashMapArrayList<String, MetaMetadataField> sortedFieldSet  = new HashMapArrayList<String, MetaMetadataField>();
			
			// Put all non-scalar values in, since they can't have dependencies.
			sortedFieldSet.putAll(ourNonScalars);
			
			// We have to flip the topological sort we conducted to get the right order for our purposes.
			Collections.reverse(visited);
			
			for(MetaMetadataScalarField f : visited)
			{
				sortedFieldSet.put(f.getName(), f);
			}
			
			return sortedFieldSet;
		}else{
			throw new ScalarDependencyException(); //We have a cycle. 
		}
	}
}
