package ecologylab.semantics.actions;

import ecologylab.collections.Scope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag(SemanticActionStandardMethods.ADD_MIXIN)
public class AddMixinSemanticAction extends SemanticAction
{

	@simpl_scalar
	private String	mixin;

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.ADD_MIXIN;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		Metadata metadata = (Metadata) obj;
		Scope<Object> vars = semanticActionHandler.getSemanticActionVariableMap();
		Metadata mixinMetadata = (Metadata) vars.get(mixin);
		metadata.addMixin(mixinMetadata);
		return null;
	}

}
