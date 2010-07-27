package ecologylab.semantics.metametadata.example.bingImage.generated;

/**
This is a generated code. DO NOT edit or modify it.
 @author MetadataCompiler 

**/ 



import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.*;
import ecologylab.semantics.generated.library.bing.*;
import ecologylab.semantics.generated.library.flickr.*;
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

@simpl_inherit

public class  BingImageType
extends  Document
{

	@simpl_collection("bing_image") @mm_name("bing_images") private ArrayList<BingImage>	bingImages;

/**
	Constructor
**/ 

public BingImageType()
{
 super();
}

/**
	Constructor
**/ 

public BingImageType(MetaMetadataCompositeField metaMetadata)
{
super(metaMetadata);
}

/**
	Lazy Evaluation for bingImages
**/ 

public  ArrayList<BingImage>	bingImages()
{
 ArrayList<BingImage>	result	=this.bingImages;
if(result == null)
{
result = new  ArrayList<BingImage>();
this.bingImages	=	 result;
}
return result;
}

/**
	Set the value of field bingImages
**/ 

public void setBingImages(  ArrayList<BingImage> bingImages )
{
this.bingImages = bingImages ;
}

/**
	Get the value of field bingImages
**/ 

public  ArrayList<BingImage> getBingImages(){
return this.bingImages;
}

}

