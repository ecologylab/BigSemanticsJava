package ecologylab.bigsemantics.metadata.builtins;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.declarations.RichArtifactDeclaration;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;

public class RichArtifact<M extends Metadata> extends RichArtifactDeclaration<M>
{

	public RichArtifact()
	{
		super();
	}
	
	public RichArtifact(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}

}
