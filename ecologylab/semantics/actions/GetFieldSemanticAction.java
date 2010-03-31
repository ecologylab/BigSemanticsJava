/**
 * 
 */
package ecologylab.semantics.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import ecologylab.generic.ReflectionTools;
import ecologylab.xml.XMLTools;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 * 
 */
@xml_inherit
public @xml_tag(SemanticActionStandardMethods.GET_FIELD_ACTION)
class GetFieldSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return GET_FIELD_ACTION;
	}

	@Override
	public void handleError()
	{
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		try
		{
			String returnValueName = getReturnValue();
			String getterName = "get" + XMLTools.javaNameFromElementName(returnValueName, true);

			Method method = ReflectionTools.getMethod(obj.getClass(), getterName, null);
			return method.invoke(obj, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err
					.format("The get_field semantic action cannot be handled. See the stack trace for details.");
			return null;
		}
	}

}
