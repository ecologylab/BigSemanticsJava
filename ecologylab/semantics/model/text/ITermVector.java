package ecologylab.semantics.model.text;

import java.util.Observer;

public interface ITermVector extends VectorType<XTerm> {
	public void addObserver(Observer o);
	public void deleteObserver(Observer o);
}
