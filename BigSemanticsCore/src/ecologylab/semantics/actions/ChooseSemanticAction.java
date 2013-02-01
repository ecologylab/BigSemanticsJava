package ecologylab.semantics.actions;

import java.util.ArrayList;

import org.junit.Test;

import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.formatenums.StringFormat;

@simpl_inherit
@simpl_tag(SemanticActionStandardMethods.CHOOSE)
public class ChooseSemanticAction
		extends SemanticAction
{

	@simpl_inherit
	public static class Otherwise
			extends NestedSemanticAction
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
		int selectedCaseIndex = -1;
		int state = (Integer) semanticActionHandler.getActionState(this, "state", INIT);
		if (state == INIT)
		{
			state = INTER;

			if (cases != null)
				for (int i = 0; i < cases.size(); ++i)
				{
					if (semanticActionHandler.checkConditionsIfAny(cases.get(i)))
					{
						semanticActionHandler.setActionState(this, "select", i);
						break;
					}
				}
		}

		if (selectedCaseIndex >= 0)
		{
			IfSemanticAction aCase = cases.get(selectedCaseIndex);
			ArrayList<SemanticAction> nestedSemanticActions = aCase.getNestedSemanticActionList();
			for (SemanticAction nestedSemanticAction : nestedSemanticActions)
				semanticActionHandler.handleSemanticAction(nestedSemanticAction, documentParser,
						sessionScope);
		}
		else
		{
			if (otherwise != null)
			{
				ArrayList<SemanticAction> otherwiseActions = otherwise.getNestedSemanticActionList();
				for (SemanticAction action : otherwiseActions)
				{
					semanticActionHandler.handleSemanticAction(action, documentParser, sessionScope);
				}
			}
		}

		return null;
	}

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<choose><case><not_null /><get_field /><for_each /></case><case><not_null /><set_metadata /></case><otherwise><get_field /></otherwise></choose>";
		ChooseSemanticAction choose = (ChooseSemanticAction) MetaMetadataTranslationScope.get()
				.deserialize(xml, StringFormat.XML);
		System.out.println(choose);
		System.out.println(choose.cases);
		System.out.println(choose.otherwise);
		System.out.println(SimplTypesScope.serialize(choose, StringFormat.XML));
	}

	@Override
	void setNestedActionState(String name, Object value)
	{
		if (cases != null)
		{
			for (IfSemanticAction aCase : cases)
			{
				SemanticActionHandler handler = getSemanticActionHandler();
				aCase.setSemanticActionHandler(handler);
				semanticActionHandler.setActionState(aCase, name, value);
				aCase.setNestedActionState(name, value);
			}
		}
		if (otherwise != null)
		{
			semanticActionHandler.setActionState(otherwise, name, value);
			otherwise.setNestedActionState(name, value);
		}
	}

}
