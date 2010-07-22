package ecologylab.semantics.metadata.builtins;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
public class Media extends Metadata
{

	/**
	 * Constructor
	 **/

	public Media()
	{
		super();
	}

	/**
	 * Constructor
	 **/

	public Media(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

	/**
	
**/

	@xml_tag("context")
	@simpl_scalar
	private MetadataString	context;

	/**
	 * Lazy Evaluation for context
	 **/

	public MetadataString context()
	{
		MetadataString result = this.context;
		if (result == null)
		{
			result = new MetadataString();
			this.context = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field context
	 **/

	public String getContext()
	{
		return context().getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	public void setContext(String context)
	{
		this.context().setValue(context);
	}

	/**
	 * The heavy weight setter method for field context
	 **/

	public void hwSetContext(String context)
	{
		this.context().setValue(context);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the context directly
	 **/

	public void setContextMetadata(MetadataString context)
	{
		this.context = context;
	}

	/**
	 * Heavy Weight Direct setter method for context
	 **/

	public void hwSetContextMetadata(MetadataString context)
	{
		if (this.context != null && this.context.getValue() != null && hasTermVector())
			termVector().remove(this.context.termVector());
		this.context = context;
		rebuildCompositeTermVector();
	}
}
