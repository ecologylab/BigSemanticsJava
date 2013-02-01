/**
 * 
 */
package ecologylab.bigsemantics.metadata.builtins.old;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
 * This class represents a linked nested object in a metadata reference graph.
 * The object will be resolved for presentation to the user as a linked entity,
 * such as in InContextMetadata.
 * 
 * @author andruid
 */
@simpl_inherit
public class Entity<D extends Document> extends Metadata
implements IMappable<ParsedURL>
{
	@simpl_scalar @simpl_hints(Hint.XML_LEAF) MetadataString		gist;
	@simpl_scalar @simpl_hints(Hint.XML_LEAF) MetadataParsedURL	location;
	
	D															linkedDocument;
	
	
	/**
	 * 
	 */
	public Entity()
	{

	}

	/**
	 * @param metaMetadata
	 */
	public Entity(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

	public ParsedURL key()
	{
		return location != null ? location.getValue() : null;
	}

	/**
	 * @return the linkedDocument
	 */
	public D getLinkedDocument()
	{
		return linkedDocument;
	}
	
	public String getGist()
	{
		return gist == null ? null : gist.getValue();
	}

	public void setGist(MetadataString gist)
	{
		this.gist = gist;
	}

	public MetadataParsedURL getEntityLocation()
	{
		return location;
	}

}
