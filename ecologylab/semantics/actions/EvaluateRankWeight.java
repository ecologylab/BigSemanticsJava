/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * Semantic action to evualte weight basesd on rank.
 * 
 * @author amathur
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.EVALUATE_RANK_WEIGHT)
class EvaluateRankWeight
extends SemanticAction
{

	public static final double	TRANSFER_FUNC_CENTER	= .5; // the bend of the sigmoid

	public static final double	CURVE_AMOUNT					= 10;

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.EVALUATE_RANK_WEIGHT;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Function which evaluates rank weight Also applies a transfer function to make this sequence top
	 * heavy
	 * 
	 * http://www.wolframalpha.com/input/?i=Plot[(1+%2B+E^(-10*(x-.6)))^(-1),+{x,+0,+1}]
	 */
	@Override
	public Object perform(Object obj)
	{
		int index = getArgumentInteger(SemanticActionNamedArguments.INDEX, 0);
		int size = getArgumentInteger(SemanticActionNamedArguments.SIZE, 0);
		float result = ((float) size - index) / size;

		double e = Math.E;
		float val = (float)( 1 / (1 + Math.pow(e, CURVE_AMOUNT * (TRANSFER_FUNC_CENTER - result))));

		return Float.valueOf(val);
	}

}
