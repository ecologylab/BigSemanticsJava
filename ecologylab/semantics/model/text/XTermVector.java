package ecologylab.semantics.model.text;

import ecologylab.model.text.ReferringElement;

public class XTermVector extends XVector<XTerm> {

	public XTermVector(){}
	
	public XTermVector(int size) {
		super(size); // lol
	}
	
	public XTermVector(String s) {
		
	}
	
	public void addReference(ReferringElement r) {
		for(XTerm t : values.keySet())
			t.addReference(r);
	}
	
}
