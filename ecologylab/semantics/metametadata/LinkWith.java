package ecologylab.semantics.metametadata;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
public class LinkWith extends ElementState
{

	@simpl_scalar
	private String				name;

	@simpl_scalar
	private String				type;

	@simpl_scalar
	private String				byId;

	private MetaMetadata	targetMmd;

	/**
	 * indicate if this link is a generated reverse one.
	 */
	private boolean				reversed				= false;

	/**
	 * if the target metadata has been linked. note that it could be linked to null, which means the
	 * target is not found but we tried.
	 */
	private boolean				linked					= false;

	/**
	 * the target metadata object. null value and linked==true indicates a failure.
	 */
	private Metadata			linkedMetadata	= null;

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public String getById()
	{
		return byId;
	}

	public boolean isReversed()
	{
		return reversed;
	}

	public MetaMetadata getTargetMetaMetadata(MetaMetadataRepository repository)
	{
		if (targetMmd == null)
		{
			MetaMetadata mmd = repository.getByTagName(type);
			assert mmd != null : "undefined meta-metadata type: " + type;
			targetMmd = mmd;
		}
		return targetMmd;
	}

	// note that synchronized is necessary on isLinked(), getLinkedMetadata() and setLinkedMetadata()
	// for data consistency.
	public synchronized boolean isLinked()
	{
		return linked;
	}

	public synchronized Metadata getLinkedMetadata()
	{
		return linkedMetadata;
	}

	public boolean canLink(Metadata metadata)
	{
		if (metadata != null)
		{
			MetaMetadataRepository repository = metadata.getMetaMetadata().getRepository();
			MetaMetadata mmd = getTargetMetaMetadata(repository);
			return mmd.getMetadataClass().isAssignableFrom(metadata.getClass());
		}
		return false;
	}

	public synchronized void setLinkedMetadata(Metadata metadata)
	{
		if (metadata != null)
		{
			linkedMetadata = metadata;
			linked = true;
		}
	}

	public LinkWith getReverse(String sourceMetaMetadataType)
	{
		LinkWith lw = new LinkWith();

		lw.name = name + "_reverse";
		lw.type = sourceMetaMetadataType;
		lw.byId = byId;
		lw.reversed = true;
		lw.linked = false;
		lw.linkedMetadata = null;

		return lw;
	}

}
