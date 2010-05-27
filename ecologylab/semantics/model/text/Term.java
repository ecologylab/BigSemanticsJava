package ecologylab.semantics.model.text;

import java.text.DecimalFormat;
import java.util.Hashtable;

import ecologylab.xml.ElementState;


public class Term extends ElementState implements Comparable<Term>
{

	private static final Hashtable<String, String> uniqueStemObjectMap = new Hashtable<String, String>();
	
	@xml_attribute private String stem;
	@xml_attribute private String word;
	private boolean hasWord = false;
	@xml_attribute private double idf;
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
