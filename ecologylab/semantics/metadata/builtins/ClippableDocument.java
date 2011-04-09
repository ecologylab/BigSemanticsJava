package ecologylab.semantics.metadata.builtins;

import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.tools.MetaMetadataCompilerUtils;

/**
 * Image extends this, so that each image keeps track of all the clippings it is a participant itn.
 * Other media, such as video, will do the same thing.
 * 
 * @author andruid
 *
 * @param <MC>	The MediaClipping Type
 * @param <ME>	The underling Media type
 */
public class ClippableDocument<ME extends Document> extends Document
{
	@simpl_collection
	@simpl_classes(ImageClipping.class)
	protected ArrayList<MediaClipping<ME>>		clippings;
	
 	protected int			width;
	
	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	protected int			height;
	
	
	
	public static final int		INITIAL_CAPACITY	= 2;
	
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
		ArrayList<MediaClipping<ME>> result	= this.clippings;
		if (result == null)
		{
			result							= new ArrayList<MediaClipping<ME>>(INITIAL_CAPACITY);
			this.clippings			= result;
		}
		return result;
	}
	protected boolean addClipping(MediaClipping<ME> clipping)
	{
		ArrayList<MediaClipping<ME>> clippings	= clippings();
		clipping.setParent(this);
		return(clippings.add(clipping));
	}
	
	public Document getClippingSource()
	{
		Document result	= null;
		if (clippings != null)
		{
			for (MediaClipping<ME> clipping : clippings)
			{
				result	= clipping.getSource();
				if (result != null)
					break;
			}
		}
		return result;
	}
}
