package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.Observer;

public class XCompositeTermVector extends Observable implements Observer, ITermVector {
	
	public XCompositeTermVector() { }
	
	private XTermVector compositeTermVector;	
	private HashSet<ITermVector> termVectors;
	
	/**
	 * Adds a Term Vector to this Composite Term Vectors collection.
	 * @param tv The Term Vector you wish to add.
	 */
	public void add(ITermVector tv) {
		if(termVectors.add(tv)) {
			tv.addObserver(this);
			compositeTermVector.add(tv);
			setChanged();
			notifyObservers();
		}
	}
	
	/**
	 * Removes a Term Vector from this Composite Term Vectors collection.
	 * @param tv The Term Vector you wish to remove.
	 */
	public void remove(ITermVector tv) {
		if(termVectors.remove(tv)) {
			tv.deleteObserver(this);
			compositeTermVector.add(-1,tv);
			setChanged();
			notifyObservers();
		}
	}
	
	// TODO: look in to sending the old value in "arg", 
	// and then subtracting it, then adding the new one
	// instead of rebuilding the whole thing each time.
	public void update(Observable o, Object arg) {
		if ((String)arg == "delete") {
			ITermVector tv = (ITermVector) o;
			remove(tv);
		}
		rebuildCompositeTermVector();
		setChanged();
		notifyObservers();
	}
	
	public void recycle() {
		for (ITermVector tv : termVectors)
			tv.deleteObserver(this);
	}
	
	private synchronized void rebuildCompositeTermVector() {
		compositeTermVector = new XTermVector(compositeTermVector.size());
		for (ITermVector tv : termVectors) {
			compositeTermVector.add(tv);
		}
	}
	
	public double dot(VectorType<XTerm> v) {
		return compositeTermVector.dot(v);
	}
	
	public Set<XTerm> elements() {
		return compositeTermVector.elements();
	}
	public double get(XTerm term) {
		return compositeTermVector.get(term);
	}
	public HashMap<XTerm, Double> map() {
		return compositeTermVector.map();
	}
	public Set<Double> values() {
		return compositeTermVector.values();
	}	
	
}