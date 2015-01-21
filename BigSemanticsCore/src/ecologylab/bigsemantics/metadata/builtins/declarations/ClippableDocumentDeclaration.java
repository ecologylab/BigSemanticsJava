package ecologylab.bigsemantics.metadata.builtins.declarations;

/**
 * Automatically generated by MetaMetadataJavaTranslator
 *
 * DO NOT modify this code manually: All your changes may get lost!
 *
 * Copyright (2015) Interface Ecology Lab.
 */

import ecologylab.bigsemantics.metadata.builtins.Clipping;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.mm_name;
import ecologylab.bigsemantics.metadata.scalar.MetadataInteger;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_other_tags;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@simpl_inherit
public class ClippableDocumentDeclaration extends Document
{
	/** 
	 *Clippings based on this.
	 */ 
	@simpl_collection
	@simpl_other_tags({"clippings"})
	@simpl_scope("repository_clippings")
	@mm_name("clippings_this_is_in")
	private List<Clipping> clippingsThisIsIn;

	@simpl_scalar
	private MetadataInteger width;

	@simpl_scalar
	private MetadataInteger height;

	public ClippableDocumentDeclaration()
	{ super(); }

	public ClippableDocumentDeclaration(MetaMetadataCompositeField mmd) {
		super(mmd);
	}


	public List<Clipping> getClippingsThisIsIn()
	{
		return clippingsThisIsIn;
	}

  // lazy evaluation:
  public List<Clipping> clippingsThisIsIn()
  {
    if (clippingsThisIsIn == null)
      clippingsThisIsIn = new ArrayList<Clipping>();
    return clippingsThisIsIn;
  }

  // addTo:
  public void addToClippingsThisIsIn(Clipping element)
  {
    clippingsThisIsIn().add(element);
  }

  // size:
  public int clippingsThisIsInSize()
  {
    return clippingsThisIsIn == null ? 0 : clippingsThisIsIn.size();
  }

	public void setClippingsThisIsIn(List<Clipping> clippingsThisIsIn)
	{
		this.clippingsThisIsIn = clippingsThisIsIn;
	}

	public MetadataInteger	width()
	{
		MetadataInteger	result = this.width;
		if (result == null)
		{
			result = new MetadataInteger();
			this.width = result;
		}
		return result;
	}

	public Integer getWidth()
	{
		return this.width == null ? 0 : width().getValue();
	}

	public MetadataInteger getWidthMetadata()
	{
		return width;
	}

	public void setWidth(Integer width)
	{
		if (width != 0)
			this.width().setValue(width);
	}

	public void setWidthMetadata(MetadataInteger width)
	{
		this.width = width;
	}

	public MetadataInteger	height()
	{
		MetadataInteger	result = this.height;
		if (result == null)
		{
			result = new MetadataInteger();
			this.height = result;
		}
		return result;
	}

	public Integer getHeight()
	{
		return this.height == null ? 0 : height().getValue();
	}

	public MetadataInteger getHeightMetadata()
	{
		return height;
	}

	public void setHeight(Integer height)
	{
		if (height != 0)
			this.height().setValue(height);
	}

	public void setHeightMetadata(MetadataInteger height)
	{
		this.height = height;
	}
}
