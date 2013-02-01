/**
 * 
 */
package ecologylab.semantics.model.text;

import java.util.ArrayList;
import java.util.Map;

import ecologylab.serialization.ElementStateOrmBase;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * @author andrew
 *
 */
@simpl_inherit
public class TermVectorState extends ElementStateOrmBase
{
	@simpl_collection("term_vector_entry") ArrayList<TermVectorEntry> terms = new ArrayList<TermVectorEntry>();
	
	public TermVectorState() 
	{
	}
	
	public TermVectorState(TermVector termVector)
	{
		addTermVector(termVector);
	}
	
	public void addTermVector(ITermVector termVector)
	{
		Map<Term, Double> map = termVector.map();
		if (map != null)
		{
			for (Term term : map.keySet())
			{
				double freq = map.get(term);
				terms.add(new TermVectorEntry(term, freq));
			}
		}
	}
	
	public ArrayList<TermVectorEntry> terms()
	{
		return terms;
	}
}
