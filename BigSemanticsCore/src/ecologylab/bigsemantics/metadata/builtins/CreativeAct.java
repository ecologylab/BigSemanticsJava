package ecologylab.bigsemantics.metadata.builtins;

import ecologylab.bigsemantics.metadata.builtins.declarations.CreativeActDeclaration;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;

public class CreativeAct extends CreativeActDeclaration
{
	public static enum CreativeAction
	{
		CURATE_CLIPPING(1), CURATE_LINK(2), ANNOTATE_ARTIFACT(3), NOTE(4), SKETCH(5), UPLOAD(6), EDIT(7), ASSIGN_PRIMARY_LINK(8);
		
		private int value;    

		private CreativeAction(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
	public CreativeAct() 
	{
		super();
	}
	
	public CreativeAct(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}
}
