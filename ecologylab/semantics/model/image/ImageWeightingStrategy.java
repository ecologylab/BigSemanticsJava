package ecologylab.semantics.model.image;

import ecologylab.collections.WeightingStrategy;
import ecologylab.semantics.old.AbstractImgElement;

/**
 * This strategy uses mediaWeight from AbstractImgElement to sort all the elements in the set.
 * Alternate, more sophisticated strategies are sure to follow. 
 * @author damaraju
 *
 * @param <E>
 */
public class ImageWeightingStrategy<E extends AbstractImgElement> extends WeightingStrategy<E>
{

	@Override
	public double getWeight(E e)
	{
		return e.mediaWeight();
	}

}
