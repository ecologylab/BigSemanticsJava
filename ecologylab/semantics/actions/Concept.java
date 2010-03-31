package ecologylab.semantics.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;

@xml_inherit
public class Concept extends ElementState
{
	@xml_inherit
	public static class Outlink extends ElementState
	{
		@xml_attribute
		private String surface;
		public String getSurface()
		{
			return surface;
		}
		
		@xml_attribute
		private String targetConceptName;
		public String getTargetConceptName()
		{
			return targetConceptName;
		}
		
		public Outlink(String surface, String targetConceptName)
		{
			super();
			this.surface = surface;
			this.targetConceptName = targetConceptName;
		}
	}
	
	@xml_attribute
	private String name;
	public String getName()
	{
		return name;
	}
	
	@xml_attribute
	private ParsedURL purl;
	public ParsedURL getPurl()
	{
		return purl;
	}
	
	@xml_collection("outlink")
	private ArrayList<Outlink> outlinks;
	public ArrayList<Outlink> getOutlinks()
	{
		return outlinks;
	}
	public void addOutlink(Outlink outlink)
	{
		outlinks.add(outlink);
	}
	
	@xml_collection("category_name")
	private ArrayList<String> categoryNames;
	public ArrayList<String> getCategoryNames()
	{
		return categoryNames;
	}
	public void addCategoryName(String categoryName)
	{
		categoryNames.add(categoryName);
	}
	
	public Concept(String name, ParsedURL purl)
	{
		super();
		this.name = name;
		this.purl = purl;
		this.outlinks = new ArrayList<Outlink>();
		this.categoryNames = new ArrayList<String>();
	}
	
	public static void main(String[] args) throws XMLTranslationException, IOException
	{
		Outlink o = new Outlink("surface", "target");
		Concept c = new Concept("concept", ParsedURL.getAbsolute("http://tempurl/"));
		c.addCategoryName("cat1");
		
		c.translateToXML(new File("output.xml"));
	}
}
