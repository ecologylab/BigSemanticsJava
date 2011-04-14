/**
 * 
 */
package ecologylab.semantics.connectors;

import ecologylab.collections.GenericElement;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorFeature;

/**
 * @author andruid
 *
 */
public class MetadataElement<M extends Metadata> extends GenericElement<M>
implements TermVectorFeature
{
	public MetadataElement(M genericObject)
	{
		super(genericObject);
	}

	@Override
	public ITermVector termVector()
	{
		return getGeneric().termVector();
	}

}
