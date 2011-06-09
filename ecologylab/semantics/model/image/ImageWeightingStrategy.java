package ecologylab.semantics.model.image;

import ecologylab.collections.WeightingStrategy;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.Image;

/**
 * This strategy uses mediaWeight from AbstractImgElement to sort all the elements in the set.
 * Alternate, more sophisticated strategies are sure to follow. 
 * @author damaraju
 *
 * @param <E>
 */
public class ImageWeightingStrategy extends WeightingStrategy<DocumentClosure<Image>>
{

	@Override
	public double getWeight(DocumentClosure<Image> e)
	{
		throw new RuntimeException("Not implemented.");
	}

}
