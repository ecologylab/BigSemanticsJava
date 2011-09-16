package ecologylab.semantics.metametadata;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

@simpl_inherit
public class LinkWith extends ElementState implements IMappable<String>
{

	@simpl_scalar
	private String		name;

	@simpl_scalar
	private String		byId;

	private boolean		reverse	= false;

	private LinkWith	reverseLink;

	public String getName()
	{
		return name;
	}

	public String getById()
	{
		return byId;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	public LinkWith getReverseLink()
	{
		return reverseLink;
	}

	public void setReverseLink(LinkWith lw)
	{
		reverseLink = lw;
		lw.reverseLink = this;
	}

	public LinkWith createReverseLink(String sourceMetaMetadataName)
	{
		LinkWith lw = new LinkWith();

		lw.name = sourceMetaMetadataName;
		lw.byId = byId;
		lw.reverse = true;

		reverseLink = lw;
		lw.reverseLink = this;

		return lw;
	}

	@Override
	public String key()
	{
		return name;
	}

	public boolean tryLink(Metadata parsedMetadata, Metadata toMetadata)
	{
		String value = parsedMetadata.getNaturalIdValue(byId);
		String baseValue = toMetadata.getNaturalIdValue(byId);

		if (value != null && baseValue != null && value.equals(baseValue))
		{
			toMetadata.addLinkedMetadata(this, parsedMetadata);
			parsedMetadata.addLinkedMetadata(getReverseLink(), toMetadata);
			return true;
		}
		return false;
	}
}
