package ecologylab.semantics.actions;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import ecologylab.semantics.actions.exceptions.NestedActionException;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag(SemanticActionStandardMethods.CHOOSE)
public class ChooseSemanticAction extends SemanticAction
{

	@simpl_inherit
	@xml_tag(SemanticActionStandardMethods.OTHERWISE)
	public static class Otherwise extends NestedSemanticAction
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
	public Object handle(Object obj, Map<String, Object> args)
	{
		SemanticActionHandler handler = getSemanticActionHandler();
		DocumentParser parser = getDocumentParser();
		InfoCollector infoCollector = getInfoCollector();

		for (IfSemanticAction aCase : cases)
		{
			try
			{
				ArrayList<SemanticAction> nestedSemanticActions = aCase.getNestedSemanticActionList();

				// check if all the flags are true
				if (handler.checkPreConditionFlagsIfAny(aCase))
				{
					// handle each of the nested action
					for (SemanticAction nestedSemanticAction : nestedSemanticActions)
						handler.handleSemanticAction(nestedSemanticAction, parser, infoCollector);

					return null;
				}
			}
			catch (Exception e)
			{
				throw new NestedActionException(e, aCase, handler.getSemanticActionReturnValueMap());
			}
		}

		ArrayList<SemanticAction> otherwiseActions = otherwise.getNestedSemanticActionList();
		for (SemanticAction action : otherwiseActions)
		{
			try
			{
				handler.handleSemanticAction(action, parser, infoCollector);
			}
			catch (Exception e)
			{
				throw new NestedActionException(e, otherwise, handler.getSemanticActionReturnValueMap());
			}
		}
		return null;
	}

	@Test
	public void test() throws SIMPLTranslationException
	{
		String xml = "<choose><case><flag_check /><get_field /><for_each /></case><case><flag_check /><set_metadata /></case><otherwise><get_field /></otherwise></choose>";
		ChooseSemanticAction choose = (ChooseSemanticAction) NestedSemanticActionsTranslationScope
				.get().deserializeCharSequence(xml);
		System.out.println(choose);

		System.out.println(choose.serialize());
	}

}
