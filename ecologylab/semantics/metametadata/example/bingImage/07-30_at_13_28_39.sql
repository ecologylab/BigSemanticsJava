--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.Result]
CREATE TABLE Result (
summary text ,	/*xml_tag simpl_scalar simpl_hints */
title text ,	/*xml_tag simpl_scalar simpl_hints */
thumbnail YahooThumbnail ,	/*xml_tag simpl_composite mm_name */
refererUrl varchar(64) ,	/*xml_tag simpl_scalar simpl_hints */
url varchar(64) ,	/*xml_tag simpl_scalar simpl_hints */
mimeType text 	/*xml_tag simpl_scalar simpl_hints */
)INHERITS (Metadata); 

--MetadataClassDescriptor[ecologylab.semantics.metadata.builtins.Document]
CREATE TABLE Document (
pageStructure text ,	/*mm_name simpl_scalar */
title text ,	/*mm_name simpl_scalar */
location varchar(64) ,	/*mm_name simpl_scalar */
query text ,	/*simpl_scalar simpl_hints */
description text ,	/*mm_name simpl_scalar */
generation int4 	/*mm_name simpl_scalar */
)INHERITS (Metadata); 

--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.Search]
CREATE TABLE Search (
searchResults SearchResult[] 	/*simpl_collection simpl_nowrap mm_name */
)INHERITS (Document); 

--MetadataClassDescriptor[ecologylab.semantics.metadata.builtins.Media]
CREATE TABLE Media (
context text 	/*xml_tag simpl_scalar */
)INHERITS (Metadata); 

--MetadataClassDescriptor[ecologylab.semantics.metadata.builtins.DebugMetadata]
CREATE TABLE DebugMetadata (
newTermVector text 	/*simpl_scalar mm_name */
)INHERITS (Metadata); 

--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.YahooResultSet]
CREATE TABLE YahooResultSet (
results Result[] 	/*simpl_collection simpl_nowrap mm_name */
)INHERITS (Document); 

--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.YahooThumbnail]
CREATE TABLE YahooThumbnail (
height int4 ,	/*xml_tag simpl_scalar simpl_hints */
width int4 ,	/*xml_tag simpl_scalar simpl_hints */
thumbUrl varchar(64) 	/*xml_tag simpl_scalar simpl_hints */
)INHERITS (Metadata); 

--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.BingImage]
CREATE TABLE BingImage (
imgUrl varchar(64) ,	/*simpl_scalar */
imgProperty text ,	/*simpl_scalar */
imgRef varchar(64) ,	/*simpl_scalar */
caption text 	/*simpl_scalar */
)INHERITS (Metadata); 

--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.BingImageType]
CREATE TABLE BingImageType (
bingImages BingImage[] 	/*simpl_collection xml_tag mm_name */
)INHERITS (Document); 

--MetadataClassDescriptor[ecologylab.semantics.metadata.builtins.Image]
CREATE TABLE Image (
location varchar(64) ,	/*mm_name simpl_scalar */
localLocation text ,	/*mm_name simpl_scalar */
caption text 	/*mm_name simpl_scalar */
)INHERITS (Media); 

--MetadataClassDescriptor[ecologylab.semantics.metametadata.example.bingImage.generated.SearchResult]
CREATE TABLE SearchResult (
link varchar(64) ,	/*simpl_scalar */
snippet text ,	/*simpl_scalar */
heading text 	/*simpl_scalar */
)INHERITS (Document); 

--MetadataClassDescriptor[ecologylab.semantics.metadata.Metadata]
CREATE TABLE Metadata (
mixins Metadata[] ,	/*semantics_mixin simpl_collection mm_name */
loadedFromPreviousSession boolean ,
isTrueSeed boolean ,
seed Seed ,
metaMetadataName text ,	/*simpl_scalar xml_other_tags xml_tag */
isDnd boolean ,
termVector CompositeTermVector ,
MIXINS_FIELD_NAME text ,
MIXIN_TRANSLATION_STRING text ,
mixinClasses Class[] ,
repository MetaMetadataRepository ,
MIXIN_TRANSLATIONS TranslationScope ,
INITIAL_SIZE int4 ,
metaMetadata MetaMetadataCompositeField 
); 

--MetadataClassDescriptor[ecologylab.semantics.metadata.builtins.Entity]
CREATE TABLE Entity (
linkedDocument Document ,
location varchar(64) ,	/*simpl_scalar simpl_hints */
gist text 	/*simpl_scalar simpl_hints */
)INHERITS (Metadata); 

