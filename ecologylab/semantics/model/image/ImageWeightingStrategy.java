package ecologylab.semantics.model.image;

import ecologylab.collections.WeightingStrategy;
import ecologylab.semantics.metadata.builtins.ImageClosure;

/**
 * This strategy uses mediaWeight from AbstractImgElement to sort all the elements in the set.
 * Alternate, more sophisticated strategies are sure to follow. 
 * @author damaraju
 *
 * @param <E>
 */
public class ImageWeightingStrategy extends WeightingStrategy<ImageClosure>
{

	@Override
	public double getWeight(ImageClosure e)
	{
		throw new RuntimeException("Not implemented.");
	}

}
