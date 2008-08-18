package ecologylab.semantics.model.text;

import java.util.Hashtable;
import java.util.Observer;
import java.util.Set;

public class NullTermVector implements ITermVector {

	public static NullTermVector ntv = new NullTermVector();
	
	public static NullTermVector singleton() {
		return ntv;
	}
	
	public void addObserver(Observer o) {
		// TODO Auto-generated method stub

	}

	public void deleteObserver(Observer o) {
		// TODO Auto-generated method stub

	}

	public double dot(VectorType<XTerm> v) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Set<XTerm> elements() {
		// TODO Auto-generated method stub
		return null;
	}

	public double get(XTerm term) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Hashtable<XTerm, Double> map() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Double> values() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public double norm() {
	  return 0;
	}

}
