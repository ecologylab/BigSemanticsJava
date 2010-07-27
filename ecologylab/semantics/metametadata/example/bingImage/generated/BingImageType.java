package ecologylab.semantics.metametadata.example.bingImage.generated;

/**
This is a generated code. DO NOT edit or modify it.
 @author MetadataCompiler 

**/ 



import java.util.ArrayList;

import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

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

