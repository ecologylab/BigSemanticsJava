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
public @xml_tag(SemanticActionStandardMethods.GET_FIELD_ACTION) 
class GetFieldSemanticAction extends SemanticAction 
implements SemanticActionStandardMethods
{


	@Override
	public String getActionName()
	{
		return GET_FIELD_ACTION;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	public Object handle(Object object)
	{
		try
		{
			String returnValueName = getReturnValue();
			String getterName = "get" + XMLTools.javaNameFromElementName(returnValueName, true);

			Method method = ReflectionTools.getMethod(object.getClass(), getterName, null);
			return method.invoke(object, null);
		}
		catch (Exception e)
		{
			System.err.println("oops! get_field action failed.");
			e.printStackTrace();
			return null;
		}
	}

}
