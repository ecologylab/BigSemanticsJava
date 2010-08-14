/**
 * 
 */
package ecologylab.semantics.actions;

import java.lang.reflect.Method;
import java.util.Map;

import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * @author amathur
 */
@simpl_inherit
@xml_tag(SemanticActionStandardMethods.SET_FIELD_ACTION)
public class SetFieldSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH> implements SemanticActionStandardMethods
{

	public static final String	ARG_VALUE	= "value";

	@Override
	public String getActionName()
	{
		return SET_FIELD_ACTION;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		String setterName = "set" + XMLTools.javaNameFromElementName(getReturnObjectName(), true);
		Object value = getArgumentObject(ARG_VALUE);
		Method method = ReflectionTools.getMethod(obj.getClass(), setterName, new Class[]
		{ value.getClass() });
		try
		{
			method.invoke(obj, value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			error(String.format("set_field failed: object=%s, setter=%s, value=%s", obj, setterName,
					value));
		}
		return null;
	}

}
