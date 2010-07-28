package ecologylab.semantics.metametadata.example.bingImage.generated;

/**
This is a generated code. DO NOT edit or modify it.
 @author MetadataCompiler 

**/ 



import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.*;
import ecologylab.semantics.generated.library.bing.*;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBuiltinsTranslationScope;
import ecologylab.semantics.metadata.builtins.*;
import ecologylab.semantics.metadata.builtins.DebugMetadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Entity;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.Media;
import ecologylab.semantics.metadata.scalar.*;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
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
	{
	ecologylab.semantics.metametadata.example.bingImage.generated.BingImage.class, 
	
	ecologylab.semantics.metametadata.example.bingImage.generated.BingImageType.class,
	
	ecologylab.semantics.metametadata.example.bingImage.generated.Result.class,
	
	ecologylab.semantics.metametadata.example.bingImage.generated.Search.class,
	
	ecologylab.semantics.metametadata.example.bingImage.generated.SearchResult.class,
	
	ecologylab.semantics.metametadata.example.bingImage.generated.YahooResultSet.class,
	
	ecologylab.semantics.metametadata.example.bingImage.generated.YahooThumbnail.class

};
 
public static TranslationScope get()
{
return TranslationScope.get("generated_metadata_translations", MetadataBuiltinsTranslationScope.get(), TRANSLATIONS);
}
}