package ecologylab.semantics.connectors;

import ecologylab.collections.SetElement;
import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.model.text.TermVectorFeature;

public abstract class ContentElement<M extends Metadata> extends SetElement implements  TermVectorFeature,Hyperlink
{

	/**
	 * A priori weighting. A coefficient for expressing relative importances.
	 */
	protected float											bias						= 1;

	float											lnBias;

	/**
	 * number of hyperlinks that we've parsed from outside this domain that refer to this element.
	 * this is used for pageRank style calculations.
	 */
	protected int							numLinks				= 1;

	protected static int			MAX_LINK_COUNT	= 500;

	protected static float[]	linkFactorLUT		= new float[MAX_LINK_COUNT];

	protected static float		linkIncrement		= 1.5f;

	protected static float		lnLinkIncrement	= (float) Math.log(linkIncrement);

	public static float				LN_2						= (float) Math.log(2);

	public static final float	LN_MIN_WEIGHT		= -(Float.MAX_VALUE / 600);

	/**
	 * Flexible, extensible semantics base class.
	 */
	protected M								metadata ;

	public ContentElement(ContentElement progenitor)
	{
		if (progenitor != null)
		{
			bias = progenitor.bias;
			lnBias = progenitor.lnBias;
		}
	}

	/**
	 * Returns the Metadata stored about the content.
	 */
	public M getMetadata()
	{
		return metadata;
	}

	/**
	 * When this ContentElement is recycled, it should be removed from it's constituent
	 * <code> Term </code>s' reference vector.
	 */
	public void rebuildCompositeTermVector()
	{
		// If there is no compositeTermVector, it will create a new one.
		metadata.rebuildCompositeTermVector();
	}

	public void setMetadata(M metadata)
	{
		/*if (this.metadata != null && metadata != null)
		{
			metadata.setLocation(this.metadata.getLocation());
			metadata.setNavLocation(this.metadata.getNavLocation());
		}
		if (metadata.getClass().isInstance(this.metadata))
		{
			ReflectionTools.copyObject(metadata, this.metadata);
		}
		else*/
			this.metadata = metadata;
		// FIXME: consider this at the calling side??
		// metadata.initializeMetadataCompTermVector();
	}

	/**
	 * @return the hasMetadata
	 */
	public boolean isMetadataSet()
	{
		return metadata != null;
	}

	protected void reportBigError(String method)
	{
		debug(method + " BIG ERROR: recycled=" + recycled());
		// Thread.dumpStack();
	}

	abstract public Class getMetadataClass();

	public void setBias(float biasArg)
	{
		bias = biasArg;
		if (bias != 0)
			lnBias = (float) Math.log(bias);
	}

	public void setLnBias(float biasArg)
	{
		lnBias = biasArg;
		bias = (float) Math.exp(bias);
	}

	public String referenceString()
	{
		// return " > " + termVector.getWeight()+", " + getWeight();
		// return " > " + termVector.getWeight();
		return " > ";
	}
	
	public float bias()
	{
		return bias;
	}
	
}
