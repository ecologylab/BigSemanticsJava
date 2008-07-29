package ecologylab.semantics.model.text;

import java.util.Observer;

import ecologylab.model.text.ReferringElement;

public interface ITermVector extends VectorType<XTerm> {
	public void addObserver(Observer o);
	public void deleteObserver(Observer o);
}
