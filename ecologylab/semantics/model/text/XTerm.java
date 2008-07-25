package ecologylab.semantics.model.text;

import java.util.HashSet;

import ecologylab.model.text.ReferringElement;

public class XTerm {
	
	public String stem;
	public HashSet<ReferringElement> referringElements;
	private double idf;
	
	protected XTerm(String stem, double idf) {
		this.stem = stem;
		this.idf = idf;
	}
	
	class ReferringElementException extends RuntimeException {
		public ReferringElementException(String man) { super(man); }
	}
	
	protected void addReference(ReferringElement r) 
		throws ReferringElementException 
	{
		if(referringElements.contains(r))
			throw new ReferringElementException(
					"Referring Element " 
					+ r 
					+ " already exists in term: " 
					+ this);
		referringElements.add(r);
	}
	
	protected void removeReference(ReferringElement r) 
		throws ReferringElementException 
	{
		if(!referringElements.contains(r))
			throw new ReferringElementException(
					"Referring Element " 
					+ r 
					+ " doesn't exist in term: " 
					+ this);
		referringElements.remove(r);
	}

}
