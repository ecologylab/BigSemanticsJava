/**
 * 
 */
package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.bigsemantics.model.text.TermVectorFeature;
import ecologylab.collections.GenericElement;

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
