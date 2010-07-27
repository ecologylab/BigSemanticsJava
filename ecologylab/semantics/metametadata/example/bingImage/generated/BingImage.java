package ecologylab.semantics.metametadata.example.bingImage.generated;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit

public class BingImage extends Metadata{

	@simpl_scalar private MetadataParsedURL	imgUrl;
	@simpl_scalar private MetadataParsedURL	imgRef;
	@simpl_scalar private MetadataString	imgProperty;
	@simpl_scalar private MetadataString	caption;

/**
	Constructor
**/ 

public BingImage()
{
 super();
}

/**
	Constructor
**/ 

public BingImage(MetaMetadataCompositeField metaMetadata)
{
super(metaMetadata);
}

/**
	Lazy Evaluation for imgUrl
**/ 

public MetadataParsedURL	imgUrl()
{
MetadataParsedURL	result	=this.imgUrl;
if(result == null)
{
result = new MetadataParsedURL();
this.imgUrl	=	 result;
}
return result;
}

/**
	Gets the value of the field imgUrl
**/ 

public ParsedURL getImgUrl(){
return imgUrl().getValue();
}

/**
	Sets the value of the field imgUrl
**/ 

public void setImgUrl( ParsedURL imgUrl )
{
this.imgUrl().setValue(imgUrl);
}

/**
	The heavy weight setter method for field imgUrl
**/ 

public void hwSetImgUrl( ParsedURL imgUrl )
{
this.imgUrl().setValue(imgUrl);
rebuildCompositeTermVector();
 }
/**
	 Sets the imgUrl directly
**/ 

public void setImgUrlMetadata(MetadataParsedURL imgUrl)
{	this.imgUrl = imgUrl;
}
/**
	Heavy Weight Direct setter method for imgUrl
**/ 

public void hwSetImgUrlMetadata(MetadataParsedURL imgUrl)
{	 if(this.imgUrl!=null && this.imgUrl.getValue()!=null && hasTermVector())
		 termVector().remove(this.imgUrl.termVector());
	 this.imgUrl = imgUrl;
	rebuildCompositeTermVector();
}
/**
	Lazy Evaluation for imgRef
**/ 

public MetadataParsedURL	imgRef()
{
MetadataParsedURL	result	=this.imgRef;
if(result == null)
{
result = new MetadataParsedURL();
this.imgRef	=	 result;
}
return result;
}

/**
	Gets the value of the field imgRef
**/ 

public ParsedURL getImgRef(){
return imgRef().getValue();
}

/**
	Sets the value of the field imgRef
**/ 

public void setImgRef( ParsedURL imgRef )
{
this.imgRef().setValue(imgRef);
}

/**
	The heavy weight setter method for field imgRef
**/ 

public void hwSetImgRef( ParsedURL imgRef )
{
this.imgRef().setValue(imgRef);
rebuildCompositeTermVector();
 }
/**
	 Sets the imgRef directly
**/ 

public void setImgRefMetadata(MetadataParsedURL imgRef)
{	this.imgRef = imgRef;
}
/**
	Heavy Weight Direct setter method for imgRef
**/ 

public void hwSetImgRefMetadata(MetadataParsedURL imgRef)
{	 if(this.imgRef!=null && this.imgRef.getValue()!=null && hasTermVector())
		 termVector().remove(this.imgRef.termVector());
	 this.imgRef = imgRef;
	rebuildCompositeTermVector();
}
/**
	Lazy Evaluation for imgProperty
**/ 

public MetadataString	imgProperty()
{
MetadataString	result	=this.imgProperty;
if(result == null)
{
result = new MetadataString();
this.imgProperty	=	 result;
}
return result;
}

/**
	Gets the value of the field imgProperty
**/ 

public String getImgProperty(){
return imgProperty().getValue();
}

/**
	Sets the value of the field imgProperty
**/ 

public void setImgProperty( String imgProperty )
{
this.imgProperty().setValue(imgProperty);
}

/**
	The heavy weight setter method for field imgProperty
**/ 

public void hwSetImgProperty( String imgProperty )
{
this.imgProperty().setValue(imgProperty);
rebuildCompositeTermVector();
 }
/**
	 Sets the imgProperty directly
**/ 

public void setImgPropertyMetadata(MetadataString imgProperty)
{	this.imgProperty = imgProperty;
}
/**
	Heavy Weight Direct setter method for imgProperty
**/ 

public void hwSetImgPropertyMetadata(MetadataString imgProperty)
{	 if(this.imgProperty!=null && this.imgProperty.getValue()!=null && hasTermVector())
		 termVector().remove(this.imgProperty.termVector());
	 this.imgProperty = imgProperty;
	rebuildCompositeTermVector();
}
/**
	Lazy Evaluation for caption
**/ 

public MetadataString	caption()
{
MetadataString	result	=this.caption;
if(result == null)
{
result = new MetadataString();
this.caption	=	 result;
}
return result;
}

/**
	Gets the value of the field caption
**/ 

public String getCaption(){
return caption().getValue();
}

/**
	Sets the value of the field caption
**/ 

public void setCaption( String caption )
{
this.caption().setValue(caption);
}

/**
	The heavy weight setter method for field caption
**/ 

public void hwSetCaption( String caption )
{
this.caption().setValue(caption);
rebuildCompositeTermVector();
 }
/**
	 Sets the caption directly
**/ 

public void setCaptionMetadata(MetadataString caption)
{	this.caption = caption;
}
/**
	Heavy Weight Direct setter method for caption
**/ 

public void hwSetCaptionMetadata(MetadataString caption)
{	 if(this.caption!=null && this.caption.getValue()!=null && hasTermVector())
		 termVector().remove(this.caption.termVector());
	 this.caption = caption;
	rebuildCompositeTermVector();
}}
