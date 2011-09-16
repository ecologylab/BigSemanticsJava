package ecologylab.semantics.metametadata;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class UrlGenerator extends ElementState
{

	public static final String		TYPE_SEARCH		= "search";

	public static final String		TYPE_PATTERN	= "pattern";

	@simpl_scalar
	private String								type;

	@simpl_scalar
	private String								engine;

	@simpl_scalar
	private String								useId;

	@simpl_scalar
	private String								pattern;

	private String								patternInvolvedId;

	public String getType()
	{
		return type;
	}

	public String getEngine()
	{
		return engine;
	}

	public String getUseId()
	{
		return useId;
	}

	public String getPattern()
	{
		return pattern;
	}

	private String getPatternInvolvedId()
	{
		if (patternInvolvedId == null && pattern != null)
		{
			int begin = pattern.indexOf('{');
			int end = pattern.indexOf('}', begin + 1);
			if (begin >= 0 && end >= 0 && end > begin)
			{
				String id = pattern.substring(begin + 1, end);
				if (!StringTools.isNullOrEmpty(id))
				{
					patternInvolvedId = id;
				}
			}
		}
		return patternInvolvedId;
	}

	public boolean canGenerate(String naturalId)
	{
		if (naturalId == null)
			return false;
		if (TYPE_SEARCH.equals(getType()))
		{
			return naturalId.equals(getUseId());
		}
		else if (TYPE_PATTERN.equals(getType()))
		{
			return naturalId.equals(getPatternInvolvedId());
		}
		return false;
	}
	
	public ParsedURL generate(MetaMetadataRepository repository, String naturalId, String value)
	{
		if (naturalId == null || value == null)
			return null;
		
		if (TYPE_SEARCH.equals(getType()) && naturalId.equals(useId))
		{
			SearchEngine searchEngine = repository.getSearchEngine(engine);
			if (searchEngine != null)
			{
				return searchEngine.formSearchUrl(value, 0, 0);
			}
			else
			{
				warning("can't generate url because search engine not defined for " + engine);
			}
		}
		else if (TYPE_PATTERN.equals(getType()) && naturalId.equals(getPatternInvolvedId()))
		{
			String url = new String(pattern);
			url.replace("{" + naturalId + "}", value);
			return ParsedURL.getAbsolute(url);
		}
		
		return null;
	}
	
}
