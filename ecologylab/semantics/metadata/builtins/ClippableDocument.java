package ecologylab.semantics.metadata.builtins;

import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata.mm_name;
import ecologylab.semantics.metadata.scalar.MetadataInteger;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

/**
 * Image extends this, so that each image keeps track of all the clippings it is a participant itn.
 * Other media, such as video, will do the same thing.
 * 
 * @author andruid
 * 
 * @param <MC>
 *          The MediaClipping Type
 * @param <ME>
 *          The underling Media type
 */
@simpl_inherit
public class ClippableDocument<ME extends Document> extends Document
{
	@mm_name("clippings") 
	@simpl_collection
	@simpl_classes(ImageClipping.class)
	protected ArrayList<MediaClipping<ME>>	clippings;

	@mm_name("width") 
	@simpl_scalar
	protected MetadataInteger								width;

	@mm_name("height") 
	@simpl_scalar
	protected MetadataInteger								height;

	public static final int									INITIAL_CAPACITY	= 2;

	public ClippableDocument()
	{

	}

	public ClippableDocument(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

	/**
	 * Construct an instance of this, the base document type, and set its location.
	 * 
	 * @param location
	 */
	public ClippableDocument(ParsedURL location)
	{
		super(location);
	}

	private ArrayList<MediaClipping<ME>> clippings()
	{
		ArrayList<MediaClipping<ME>> result = this.clippings;
		if (result == null)
		{
			result = new ArrayList<MediaClipping<ME>>(INITIAL_CAPACITY);
			this.clippings = result;
		}
		return result;
	}

	protected boolean addClipping(MediaClipping<ME> clipping)
	{
		ArrayList<MediaClipping<ME>> clippings = clippings();
		clipping.setParent(this);
		return (clippings.add(clipping));
	}

	public Document getClippingSource()
	{
		Document result = null;
		if (clippings != null)
		{
			for (MediaClipping<ME> clipping : clippings)
			{
				result = clipping.getSource();
				if (result != null)
					break;
			}
		}
		return result;
	}

	/**
	 * Lazy evaluation for width
	 */
	public MetadataInteger width()
	{
		MetadataInteger result = this.width;
		if (result == null)
		{
			result = new MetadataInteger();
			this.width = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field width
	 */
	public Integer getWidth()
	{
		return width == null ? 0 : this.width().getValue();
	}

	/**
	 * Sets the value of the field width
	 */
	public void setWidth(Integer width)
	{
		this.width().setValue(width);
	}

	/**
	 * Test to see if the value of the field is null, or if the field itself is null: width
	 */
	public boolean isNullWidth()
	{
		return width == null || width.getValue() == null;
	}

	/**
	 * The heavy weight setter method for field width
	 */
	public void hwSetWidth(Integer width)
	{
		this.width().setValue(width);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the width directly.
	 */
	public void setWidthMetadata(MetadataInteger width)
	{
		this.width = width;
	}

	/**
	 * Heavy Weight Direct setter method for width
	 */
	public void hwSetWidthMetadata(MetadataInteger width)
	{
		if (this.width != null && this.width.getValue() != null && hasTermVector())
			termVector().remove(this.width.termVector());
		this.width = width;
		rebuildCompositeTermVector();
	}

	/**
	 * Lazy evaluation for height
	 */
	public MetadataInteger height()
	{
		MetadataInteger result = this.height;
		if (result == null)
		{
			result = new MetadataInteger();
			this.height = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field height
	 */
	public Integer getHeight()
	{
		return height == null ? 0 : this.height().getValue();
	}

	/**
	 * Sets the value of the field height
	 */
	public void setHeight(Integer height)
	{
		this.height().setValue(height);
	}

	/**
	 * Test to see if the value of the field is null, or if the field itself is null: height
	 */
	public boolean isNullHeight()
	{
		return height == null || height.getValue() == null;
	}

	/**
	 * The heavy weight setter method for field height
	 */
	public void hwSetHeight(Integer height)
	{
		this.height().setValue(height);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the height directly.
	 */
	public void setHeightMetadata(MetadataInteger height)
	{
		this.height = height;
	}

	/**
	 * Heavy Weight Direct setter method for height
	 */
	public void hwSetHeightMetadata(MetadataInteger height)
	{
		if (this.height != null && this.height.getValue() != null && hasTermVector())
			termVector().remove(this.height.termVector());
		this.height = height;
		rebuildCompositeTermVector();
	}

}
