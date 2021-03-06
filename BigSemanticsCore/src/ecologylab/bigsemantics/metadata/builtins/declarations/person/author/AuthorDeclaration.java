package ecologylab.bigsemantics.metadata.builtins.declarations.person.author;

/**
 * Automatically generated by MetaMetadataJavaTranslator
 *
 * DO NOT modify this code manually: All your changes may get lost!
 *
 * Copyright (2016) Interface Ecology Lab.
 */

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.MetadataBuiltinsTypesScope;
import ecologylab.bigsemantics.metadata.builtins.RichDocument;
import ecologylab.bigsemantics.metadata.builtins.creativeWork.CreativeWork;
import ecologylab.bigsemantics.metadata.builtins.person.Person;
import ecologylab.bigsemantics.metadata.mm_name;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_wrap;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 
 *An author of an article or creative work.
 */ 
@simpl_inherit
public class AuthorDeclaration extends Person
{
	@simpl_scalar
	private MetadataString affiliation;

	@simpl_scalar
	private MetadataString city;

	@simpl_collection("creative_work")
	@mm_name("creative_works")
	private List<CreativeWork> creativeWorks;

	@simpl_composite
	@simpl_wrap
	@simpl_scope("repository_documents")
	@mm_name("webpage")
	private Document webpage;

	@simpl_composite
	@mm_name("twitter_profile")
	private RichDocument twitterProfile;

	public AuthorDeclaration()
	{ super(); }

	public AuthorDeclaration(MetaMetadataCompositeField mmd) {
		super(mmd);
	}


	public MetadataString	affiliation()
	{
		MetadataString	result = this.affiliation;
		if (result == null)
		{
			result = new MetadataString();
			this.affiliation = result;
		}
		return result;
	}

	public String getAffiliation()
	{
		return this.affiliation == null ? null : affiliation().getValue();
	}

	public MetadataString getAffiliationMetadata()
	{
		return affiliation;
	}

	public void setAffiliation(String affiliation)
	{
		if (affiliation != null)
			this.affiliation().setValue(affiliation);
	}

	public void setAffiliationMetadata(MetadataString affiliation)
	{
		this.affiliation = affiliation;
	}

	public MetadataString	city()
	{
		MetadataString	result = this.city;
		if (result == null)
		{
			result = new MetadataString();
			this.city = result;
		}
		return result;
	}

	public String getCity()
	{
		return this.city == null ? null : city().getValue();
	}

	public MetadataString getCityMetadata()
	{
		return city;
	}

	public void setCity(String city)
	{
		if (city != null)
			this.city().setValue(city);
	}

	public void setCityMetadata(MetadataString city)
	{
		this.city = city;
	}

	public List<CreativeWork> getCreativeWorks()
	{
		return creativeWorks;
	}

  // lazy evaluation:
  public List<CreativeWork> creativeWorks()
  {
    if (creativeWorks == null)
      creativeWorks = new ArrayList<CreativeWork>();
    return creativeWorks;
  }

  // addTo:
  public void addToCreativeWorks(CreativeWork element)
  {
    creativeWorks().add(element);
  }

  // size:
  public int creativeWorksSize()
  {
    return creativeWorks == null ? 0 : creativeWorks.size();
  }

	public void setCreativeWorks(List<CreativeWork> creativeWorks)
	{
		this.creativeWorks = creativeWorks;
	}

	public Document getWebpage()
	{
		return webpage;
	}

	public void setWebpage(Document webpage)
	{
		this.webpage = webpage;
	}

	public RichDocument getTwitterProfile()
	{
		return twitterProfile;
	}

	public void setTwitterProfile(RichDocument twitterProfile)
	{
		this.twitterProfile = twitterProfile;
	}
}
