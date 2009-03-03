package ecologylab.semantics.model.image;

import ecologylab.collections.AbstractSetElement;
import ecologylab.generic.IFeatureVector;
import ecologylab.semantics.model.text.Term;

/**
 * An AbstractImgElement allows for WeightSets of Image Elements not in the MediaElement Hierarchy,
 * specifically created for AbstractPDFImageElement.
 * @author damaraju
 *
 */
public interface AbstractImgElement extends AbstractSetElement
{

	void deliverOrDownload();

	IFeatureVector<Term> termVector();
	
	public float mediaWeight();

}
