package ecologylab.semantics.library.scalar;

import ecologylab.generic.FeatureVector;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.model.text.Term;

abstract
public class MetadataScalarBase<T> extends MetadataBase
{
	public MetadataScalarBase()
	{
		super();
	}

	abstract public T getValue();
	
	public String toString()
	{
		T value				= getValue();
		return (value == null) ?
				super.toString() + "[null]" :
				super.toString() + "[" + value.toString() + "]";
	}
}