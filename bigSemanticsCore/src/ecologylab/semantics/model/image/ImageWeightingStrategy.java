package ecologylab.semantics.model.image;

import ecologylab.collections.WeightingStrategy;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

/**
 * This strategy uses mediaWeight from AbstractImgElement to sort all the elements in the set.
 * Alternate, more sophisticated strategies are sure to follow. 
 * @author damaraju
 *
 * @param <E>
 */
public class ImageWeightingStrategy extends WeightingStrategy<DocumentClosure>
{

	@Override
	public double getWeight(DocumentClosure e)
	{
		throw new RuntimeException("Not implemented.");
	}

}
