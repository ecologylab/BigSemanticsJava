/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.ElementState.xml_leaf;

/**
 * @author andruid
 *
 */
public class Media extends Metadata
{

	@xml_leaf	String context;

	/**
	 * 
	 */
	public Media()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param metaMetadata
	 */
	public Media(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

	public String getContext()
	{
		return context;
	}

	public void setContext(String context)
	{
		this.context = context;
	}

}
