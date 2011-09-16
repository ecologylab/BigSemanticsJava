package ecologylab.semantics.seeding;


import ecologylab.collections.Scope;
import ecologylab.generic.Debug;
import ecologylab.oodss.messages.OkResponse;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class CfCollaborationGetSurrogate extends RequestMessage {

	@simpl_scalar @simpl_hints(Hint.XML_LEAF) protected String surrogateSetString;
	
	static TranslationScope translationScope;
	
	public CfCollaborationGetSurrogate()
	{
		super();
	}

	public String getSurrogateSetString() 
	{
		return surrogateSetString;
	}

	public void setSurrogateSetString(String surrogateSetString) 
	{
		this.surrogateSetString = surrogateSetString;
	}

	@Override
	public ResponseMessage performService(Scope objectRegistry) 
	{
		Debug.println("Received loud and clear: " + surrogateSetString);
		
		return OkResponse.get();
	}
	
	public CfCollaborationGetSurrogate (String surrogateSetString, TranslationScope translationScope) 
	throws SIMPLTranslationException
	{
		this(surrogateSetString);
		this.translationScope = translationScope;
	}
	
	public CfCollaborationGetSurrogate(String surrogateSetString)
	{
		super();
		this.surrogateSetString = surrogateSetString;
	}

	public static void main(String args[])
	{
		
		//final String test = "<surrogate_set_string><surrogate_set><surrogate containerURL=\"http://www.flickr.com/services/feeds/photos_public.gne?format=rss_200&amp;tags=sunset\" historyNum=\"1\"><image_element purl=\"http://farm1.static.flickr.com/162/413759072_bc04c76914_m.jpg\" href=\"http://www.flickr.com/photos/lupi75/413759072/\" bias=\"1.0\"><participant/><metadata><metadata_field name=\"Caption\" value=\"Travel to Valpopando\"/><metadata_field name=\"Tags\" value=\"sardegna ca travel sunset red sea sky reflection water clouds boat tramonto nuvole mare sardinia nave porto cielo acqua dedica viaggio cagliari sera riflesso lupi valpopando lupi75\"/><metadata_field name=\"Author\" value=\"LuPi75\"/></metadata></image_element><visual><extent x=\"291\" y=\"331\" width=\"138\" height=\"91\"/></visual></surrogate></surrogate_set></surrogate_set_string>";
		//final String test = "<surrogate_set_string>Will this work ?</surrogate_set_string>";
		final String test = "Will this work ?";
		try 
		{
			CfCollaborationGetSurrogate cfCollabGet = new CfCollaborationGetSurrogate (test, translationScope);
			System.out.println("cfCollabGet.get: " + cfCollabGet.getSurrogateSetString());
			System.out.println("cfCollabGet: " + ClassDescriptor.serialize(cfCollabGet, StringFormat.XML));
			
		} catch (SIMPLTranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
}
