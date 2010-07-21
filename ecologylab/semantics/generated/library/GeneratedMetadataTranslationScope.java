package ecologylab.semantics.generated.library;

/**
This is a generated code. DO NOT edit or modify it.
 @author MetadataCompiler 

**/ 



import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.*;
import ecologylab.semantics.generated.library.search.Result;
import ecologylab.semantics.generated.library.search.Search;
import ecologylab.semantics.generated.library.search.SearchResult;
import ecologylab.semantics.generated.library.search.YahooResultSet;
import ecologylab.semantics.generated.library.search.YahooThumbnail;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBuiltinsTranslationScope;
import ecologylab.semantics.metadata.builtins.*;
import ecologylab.semantics.metadata.scalar.*;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.Hint;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.element.Mappable;
import java.util.*;


/**
	
This is the tranlation scope class for generated files
.
**/ 

public class GeneratedMetadataTranslationScope
{protected static final Class TRANSLATIONS[]=
	{ecologylab.semantics.generated.library.Pdf.class,

Result.class,

YahooResultSet.class,

Search.class,

SearchResult.class,

YahooThumbnail.class,

WeatherReport.class,


};
 
public static TranslationScope get()
{
return TranslationScope.get("generated_metadata_translations", MetadataBuiltinsTranslationScope.get(), TRANSLATIONS);
}
}