package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag(SemanticActionStandardMethods.CHOOSE)
public class ChooseSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH>
{

	@simpl_inherit
	public static class Otherwise<IC extends InfoCollector, SAH extends SemanticActionHandler>
			extends NestedSemanticAction<IC, SAH>
	{
		@Override
		public String getActionName()
		{
			return SemanticActionStandardMethods.OTHERWISE;
		}

		@Override
		public void handleError()
		{
			// TODO Auto-generated method stub

		}

		/**
		 * Otherwise.perform() does not do anything since Otherwise is merely a container for nested
		 * semantic actions.
		 */
		@Override
		public Object perform(Object obj)
		{
			// TODO Auto-generated method stub
			return null;
		}
	}

	@simpl_nowrap
	@simpl_collection("case")
	ArrayList<IfSemanticAction>	cases;

	@simpl_composite
	Otherwise										otherwise;

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.CHOOSE;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		for (IfSemanticAction aCase : cases)
		{
			ArrayList<SemanticAction> nestedSemanticActions = aCase.getNestedSemanticActionList();

			// check if all the flags are true
			if (semanticActionHandler.checkConditionsIfAny(aCase))
			{
				// handle each of the nested action
				for (SemanticAction nestedSemanticAction : nestedSemanticActions)
					semanticActionHandler.handleSemanticAction(nestedSemanticAction, documentParser,
							infoCollector);

				// end processing following cases
				return null;
			}
		}

		// if none cases are executed
		ArrayList<SemanticAction> otherwiseActions = otherwise.getNestedSemanticActionList();
		for (SemanticAction action : otherwiseActions)
		{
			semanticActionHandler.handleSemanticAction(action, documentParser, infoCollector);
		}
		return null;
	}

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<choose><case><not_null /><get_field /><for_each /></case><case><not_null /><set_metadata /></case><otherwise><get_field /></otherwise></choose>";
		ChooseSemanticAction choose = (ChooseSemanticAction) MetaMetadataTranslationScope.get()
				.deserializeCharSequence(xml);
		System.out.println(choose);
		System.out.println(choose.cases);
		System.out.println(choose.otherwise);
		System.out.println(choose.serialize());
	}

}
