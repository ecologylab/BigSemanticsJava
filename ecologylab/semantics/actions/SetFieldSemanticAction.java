/**
 * 
 */
package ecologylab.semantics.actions;

import java.lang.reflect.Method;

import ecologylab.generic.ReflectionTools;
import ecologylab.xml.XMLTools;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 *
 */
@xml_inherit
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
		try
		{
			String setterName = "set" + XMLTools.javaNameFromElementName(getReturnValue(), true);
			Method method = ReflectionTools.getMethod(object.getClass(), setterName, new Class[]
			{ value.getClass() });
			method.invoke(object, value);
		}
		catch (Exception e)
		{
			System.err.println("oops! set_field action failed.");
			e.printStackTrace();
		}
	}
}
