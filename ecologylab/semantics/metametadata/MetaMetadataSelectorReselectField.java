package ecologylab.semantics.metametadata;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.element.Mappable;

@simpl_inherit
public class MetaMetadataSelectorReselectField extends ElementState implements Mappable<String>
{

	@simpl_scalar
	private String	name;

	@simpl_scalar
	private String	value;

	public MetaMetadataSelectorReselectField()
	{
		super();
	}

	@Override
	public String key()
	{
		return name;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

}
