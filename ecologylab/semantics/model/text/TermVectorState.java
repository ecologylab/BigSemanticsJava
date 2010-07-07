/**
 * 
 */
package ecologylab.semantics.model.text;

import java.util.ArrayList;
import java.util.Map;

import ecologylab.serialization.ElementState;

/**
 * @author andrew
 *
 */
public class TermVectorState extends ElementState
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
