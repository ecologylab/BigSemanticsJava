package ecologylab.semantics.metametadata.example;

import java.util.Map;

import ecologylab.semantics.actions.NestedSemanticAction;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metametadata.example.generated.WeatherReport;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("save_report")
public class SaveReportSemanticAction<SA extends SemanticAction> extends NestedSemanticAction<SA>
{

	@Override
	public String getActionName()
	{
		return "save_report";
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		if (obj instanceof WeatherReport)
			WeatherDataCollector.collected.add((WeatherReport)obj);
		return null;
	}
	
}
