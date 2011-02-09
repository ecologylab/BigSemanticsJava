package ecologylab.semantics.metametadata;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.element.Mappable;

@simpl_inherit
public class LinkWith extends ElementState implements Mappable<String>
{

	@simpl_scalar
	private String		name;

	@simpl_scalar
	private String		byId;

	@simpl_scalar
	private String		options;

	private boolean		reverse	= false;

	private LinkWith	reverseLink;

	// runtime flags:

	private boolean		optText;

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
		lw.options = options;
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

	public void processOptions()
	{
		if (options != null)
		{
			if ("text".equals(options))
			{
				optText = true;
			}
		}
	}

	public boolean tryLink(Metadata parsedMetadata, Metadata toMetadata)
	{
		String value = parsedMetadata.getNaturalIdValue(byId);
		String baseValue = toMetadata.getNaturalIdValue(byId);

		if (optText)
		{
			value = value.trim().replaceAll("\\s+", " ").toLowerCase();
			baseValue = baseValue.trim().replaceAll("\\s+", " ").toLowerCase();
		}

		if (value != null && baseValue != null && value.equals(baseValue))
		{
			toMetadata.addLinkedMetadata(this, parsedMetadata);
			parsedMetadata.addLinkedMetadata(getReverseLink(), toMetadata);
			return true;
		}
		return false;
	}
}
