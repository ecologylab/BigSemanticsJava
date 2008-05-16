/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.ElementState.xml_leaf;
import ecologylab.xml.ElementState.xml_nested;

/**
 * @author andruid
 *
 */
public class Media extends Metadata
{

//	@xml_leaf	String context;
//	@xml_nested MetadataString	context = new MetadataString();
	@xml_nested MetadataString	context;
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

	MetadataString context()
	{
		MetadataString result = this.context;
		if(result == null)
		{
			result 			= new MetadataString();
			this.context 	= result;
		}
		return result;
	}
	
	public String getContext()
	{
		return context().getValue();
	}

	public void setContext(String context)
	{
//		this.context = context;
		this.context().setValue(context);
	}
	
	public void hwSetContext(String context)
	{
//		this.context = context;
		this.context().setValue(context);
		rebuildCompositeTermVector();
	}

}
