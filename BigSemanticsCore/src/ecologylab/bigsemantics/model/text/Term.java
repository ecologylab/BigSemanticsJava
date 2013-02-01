package ecologylab.bigsemantics.model.text;

import java.text.DecimalFormat;
import java.util.Hashtable;

import ecologylab.serialization.ElementStateOrmBase;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class Term extends ElementStateOrmBase implements Comparable<Term>
{

	private static final Hashtable<String, String> uniqueStemObjectMap = new Hashtable<String, String>();
	
	@simpl_scalar private String stem;
	@simpl_scalar private String word;
	private boolean hasWord = false;
	@simpl_scalar private double idf;
	public static DecimalFormat ONE_DECIMAL_PLACE = new DecimalFormat("#0.0");

	public double idf()
	{
		return idf;
	}

	public Term() { } 
	
	protected Term(String stem, double idf)
	{
		this.stem = getUniqueStem(stem);
		this.idf = idf;
	}
	
	@Override
	public String toString()
	{
		return stem;
	}

	protected void setWord ( String word )
	{
		hasWord = true;
		this.word = word;
	}
	
	public String getWord()
	{
		if (!hasWord)
			return stem;
		return word;
	}
	
	public String getStem()
	{
		return stem;
	}
	
	public boolean hasWord()
	{
		return hasWord;
	}

	public boolean isStopword ( )
	{
		return false;
	}

	@Override
	public int compareTo(Term o) 
	{
		double difference = this.idf() - o.idf();
			
		if (difference > 0)
			return -1;
		else
			return (difference == 0) ? this.getWord().compareTo(o.getWord()) : 1;
	}

	/**
	 * Ensure that only one unique memory location is used for each stem for any term.
	 * @param stem
	 * @return
	 */
	public static String getUniqueStem(String stem)
	{
		String result = uniqueStemObjectMap.get(stem);
		if(result == null)
		{
			uniqueStemObjectMap.put(stem, stem);
			result = stem;
		}
		return result;
	}
}
