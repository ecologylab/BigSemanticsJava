package ecologylab.semantics.actions;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.scalar.TypeRegistry;

@simpl_inherit
@xml_tag("parse_bibtex")
public class ParseBibtexSemanticAction<IC extends InfoCollector, SAH extends SemanticActionHandler>
		extends SemanticAction<IC, SAH>
{

	private static Map<String, String>	bibtexToMetadataNameMap	= new HashMap<String, String>();

	static
	{
		bibtexToMetadataNameMap.put("year", "year_of_publication");
		bibtexToMetadataNameMap.put("booktitle", "archive_name");
		bibtexToMetadataNameMap.put("pages", "pages");
		bibtexToMetadataNameMap.put("isbn", "isbn");
	}

	@Override
	public String getActionName()
	{
		return "parse_bibtex";
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		String bibtexMmdName = getArgumentValue("bibtex_meta_metadata_name");
		MetaMetadata bibtexMmd = infoCollector.metaMetaDataRepository().getByTagName(bibtexMmdName);
		if (bibtexMmd == null)
		{
			warning("meta-metadata for " + bibtexMmdName + " not found.");
			return null;
		}
		Metadata metadata = bibtexMmd.constructMetadata();
		
		String bibtexString = (String) getArgumentObject("bibtex_string");
		Map<String, String> bibtexKeyValuePairs = parseBibtexString(bibtexString);

		for (String bibtexKey : bibtexKeyValuePairs.keySet())
		{
			String valueString = bibtexKeyValuePairs.get(bibtexKey);

			if (bibtexToMetadataNameMap.containsKey(bibtexKey))
			{
				String metadataTagName = bibtexToMetadataNameMap.get(bibtexKey);
				String getterName = "get" + XMLTools.javaNameFromElementName(metadataTagName, true);
				Method getter = ReflectionTools.getMethod(metadata.getClass(), getterName, null);
				Class setterArgType = getter.getReturnType();
				String setterName = "set" + XMLTools.javaNameFromElementName(metadataTagName, true);
				Method setter = ReflectionTools.getMethod(metadata.getClass(), setterName, new Class[] { setterArgType });

				Object argValue = TypeRegistry.getType(setterArgType).getInstance(valueString);
				ReflectionTools.invoke(setter, metadata, argValue);
			}
		}

		return metadata;
	}

	private Map<String, String> parseBibtexString(String bibtexString)
	{
		Map<String, String> rst = new HashMap<String, String>();

		int paren0 = bibtexString.indexOf('{');
		int paren1 = bibtexString.lastIndexOf('}');
		String content = bibtexString.substring(paren0 + 1, paren1);
		String[] attrs = content.split(",\\s*");
		for (String attr : attrs)
		{
			String[] parts = attr.split("=");
			if (parts.length != 2)
				continue;

			String key = parts[0].trim();
			String value0 = parts[1].trim();
			String value1 = escapeFrom(value0, "{", "}");
			String value = escapeFrom(value1, "\"", "\"");
			rst.put(key, value);
		}

		return rst;
	}

	private String escapeFrom(String s, String opening, String closing)
	{
		if (s.startsWith(opening) && s.endsWith(closing))
			return s.substring(1, s.length() - 1);
		return s;
	}

}
