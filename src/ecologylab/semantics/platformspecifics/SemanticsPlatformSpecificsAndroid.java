package ecologylab.semantics.platformspecifics;

public class SemanticsPlatformSpecificsAndroid implements ISemanticsPlatformSpecifics
{
	static final float	MIN_STROKE_VALUE	= .8f;

	static final float	MAX_STROKE_VALUE	= 1.0f;

	static final float	STEP_STROKE_VALUE	= .005f;

	static final float	MIN_STROKE_SAT		= .35f;

	static final float	MAX_STROKE_SAT		= .7f;

	static final float	STEP_STROKE_SAT		= .05f;

	public Object getStrokeColor(int generation, int maxgeneration, Object[] strokeColors,
			float strokeHue)
	{
		Object result = null;
		if (generation < maxgeneration)
			result = strokeColors[generation];
		// comment out for too many printouts during buzz which makes interaction really bad
		// -- eunyee
		// else
		// debug("WEIRD: generation="+generation);
		if (result == null)
		{
			result = calculateStrokeColor(generation, strokeHue);
			// debug("strokeColor(0)="+result);
			if (generation < maxgeneration)
				strokeColors[generation] = result;
		}
		return result;
	}

	private Object calculateStrokeColor(int generation, float strokeHue)
	{
		return null;
//		float strokeSat = MAX_STROKE_SAT - generation * STEP_STROKE_SAT;
//		if (strokeSat < MIN_STROKE_SAT)
//			strokeSat = MIN_STROKE_SAT;
//		float strokeValue = MAX_STROKE_VALUE - generation * STEP_STROKE_VALUE;
//		if (strokeValue < MIN_STROKE_VALUE)
//			strokeValue = MIN_STROKE_VALUE;
//
//		if ((strokeHue == MAGENTA) || (strokeHue == GREEN))
//		{
//			strokeSat -= .1f;
//		}
//		else if ((strokeHue == RED) || (strokeHue == BLUE_MAGENTA))
//		{
//			strokeSat -= .18f;
//		}
//		else if (strokeHue == BLUE)
//		{
//			strokeSat -= .25f;
//		}
//
//		return Palette.hsvColor(strokeHue, strokeSat, strokeValue);
	}
}
