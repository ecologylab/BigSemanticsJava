package ecologylab.semantics.connectors.old;

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

	boolean deliverOrDownload();

	IFeatureVector<Term> termVector();
	
	public float mediaWeight();
	
	/**
	 * Operational for regular ImageElements, but a no-op for AbstractPDFImgElements.
	 * A convenience enabling putting more HTML processing code into ecologylab.semantics.
	 * 
	 * @param context
	 */
	public void hwSetContext(String context);
	
	/**
	 * Operational for regular ImageElements, but a no-op for AbstractPDFImgElements.
	 * A convenience enabling putting more HTML processing code into ecologylab.semantics.
	 * @return
	 */
	public String caption();
	
	public boolean isNullCaption();
}
