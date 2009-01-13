package ecologylab.semantics.model.text;

import java.text.DecimalFormat;
import java.util.HashSet;


public class XTerm
{

	public String stem;
	public String word;
	public boolean hasWord = false;
	public HashSet<XReferringElement> referringElements;
	private double idf;
	public static DecimalFormat ONE_DECIMAL_PLACE = new DecimalFormat("#0.0");

	public double idf()
	{
		return idf;
	}

	protected XTerm(String stem, double idf)
	{
		this.stem = stem;
		this.idf = idf;
	}

	protected void addReference(XReferringElement r)
	throws ReferringElementException
	{
		if (referringElements.contains(r))
			throw new ReferringElementException("Referring Element " + r + " already exists in term: " + this);
		referringElements.add(r);
	}

	protected void removeReference(XReferringElement r)
	throws ReferringElementException
	{
		if (!referringElements.contains(r))
			throw new ReferringElementException("Referring Element " + r + " doesn't exist in term: " + this);
		referringElements.remove(r);
	}


	class ReferringElementException extends RuntimeException
	{
		public ReferringElementException(String man)
		{
			super(man);
		}
	}

	public String toString()
	{
		return stem;
	}

	public void setWord ( String word )
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
	
	public boolean hasWord()
	{
		return hasWord;
	}

	public boolean isStopword ( )
	{
		return false;
	}

}
