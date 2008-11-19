package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Observer;
import java.util.Set;

import ecologylab.generic.VectorType;

public class NullTermVector extends VectorType<XTerm> {

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
		return new HashSet<XTerm>();
	}

	public double get(XTerm term) {
		// TODO Auto-generated method stub
		return 0;
	}

	public HashMap<XTerm, Double> map() {
		// TODO Auto-generated method stub
		return new HashMap();
	}

	public Set<Double> values() {
		// TODO Auto-generated method stub
		return new HashSet<Double>();
	}
	
	public double norm() {
	  return 0;
	}

  @Override
  public double idfDot(VectorType<XTerm> v)
  {
    return 0;
  }

}
