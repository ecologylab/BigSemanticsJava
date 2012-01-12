/**
 * 
 */
package ecologylab.semantics.actions;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ecologylab.generic.ReflectionTools;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * @author amathur
 * 
 */
@simpl_inherit
public @simpl_tag(SemanticActionStandardMethods.GET_FIELD_ACTION)
class GetFieldSemanticAction
extends SemanticAction
{

	private static Map<String, Method>	cachedGetterMethods	= new HashMap<String, Method>();

	private synchronized Method getGetterMethod(Class context, String getterName)
	{
		String id = context + "." + getterName;
		if (cachedGetterMethods.containsKey(id))
			return cachedGetterMethods.get(id);

		Method method = ReflectionTools.getMethod(context, getterName, null);
		cachedGetterMethods.put(id, method);
		return method;
	}

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.GET_FIELD_ACTION;
	}

	@Override
	public void handleError()
	{

	}

	@Override
	public Object perform(Object obj)
	{
		String returnObjectName = getReturnObjectName();
		String getterName = "get" + XMLTools.javaNameFromElementName(returnObjectName, true);
		Method method = getGetterMethod(obj.getClass(), getterName);
		try
		{
			return method.invoke(obj, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			error(String.format("get_field failed: object=%s, getter=%s()", obj, getterName));
			return null;
		}
	}

}
