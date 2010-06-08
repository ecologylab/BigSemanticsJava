package ecologylab.semantics.model;

import ecologylab.generic.Debug;
import ecologylab.xml.TranslationScope;

public class TextTokenTranslations extends Debug
{
	public static final String TEXT_TOKEN_SCOPE_NAME = "text_token_scope";
		
	public static final Class	TRANSLATIONS[]	= 
	{ 
		TextToken.class
	};

	/**
	 * 
	 */
	public TextTokenTranslations()
	{
		super();
	}

	public static TranslationScope get()
	{
		return TranslationScope.get(TEXT_TOKEN_SCOPE_NAME, TRANSLATIONS);
	}
}
