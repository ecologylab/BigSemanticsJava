package ecologylab.semantics.library.scalar;

import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.model.text.Term;
import ecologylab.semantics.model.text.FeatureVector;

public class MetadataScalarBase extends MetadataBase
{

	protected FeatureVector<Term> termVector;

	public MetadataScalarBase()
	{
		super();
	}

}