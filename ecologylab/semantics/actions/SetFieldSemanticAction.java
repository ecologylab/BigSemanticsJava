/**
 * 
 */
package ecologylab.semantics.actions;

import java.lang.reflect.Method;
import java.util.Map;

import ecologylab.generic.ReflectionTools;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * @author amathur
 *
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.SET_FIELD_ACTION) class SetFieldSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		// TODO Auto-generated method stub
		return SET_FIELD_ACTION;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	public void handle(Object object, Object value)
	{
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		try
		{
			String setterName = "set" + XMLTools.javaNameFromElementName(getReturnValue(), true);
			Object value = args.get("value");
			
			Method method = ReflectionTools.getMethod(obj.getClass(), setterName, new Class[]
			{ value.getClass() });
			method.invoke(obj, value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("oops! set_field action failed.");
		}
		return null;
	}
}
