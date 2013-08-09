package ecologylab.bigsemantics.metadata.builtins;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.declarations.RichBookmarkDeclaration;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;

public class RichBookmark<RBM extends Metadata> extends RichBookmarkDeclaration<RBM>
{
	
	public RichBookmark()
	{
		super();
	}

	public RichBookmark(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}

}
