package ecologylab.semantics.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ecologylab.net.ParsedURL;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("concepts")
public class ConceptPool extends ElementState
{
	// sync
	private Lock								lock				= new ReentrantLock();

	// pool
	public int									bufferSize	= 10;

	@xml_attribute
	private int									start				= 1;

	@xml_attribute
	private int									end;

	@xml_collection("concept")
	private ArrayList<Concept>	pool				= new ArrayList<Concept>();

	public ArrayList<Concept> getPool()
	{
		return pool;
	}

	// current
	private Concept	current	= null;

	// operations on current
	public void beginNewConcept(String name, ParsedURL purl)
	{
		lock.lock();
		System.out.println("\ncreating concept: " + name + "...");
		current = new Concept(name, purl);
	}

	public void endNewConcept()
	{
		pool.add(current);
		current = null;
		
		// save buffered concepts
		if (pool.size() >= bufferSize)
		{
			save();
		}

		System.out.println("finished.");
		lock.unlock();
	}

	public void addOutlink(String surface, String targetConceptName)
	{
		Concept.Outlink outlink = new Concept.Outlink(surface, targetConceptName);
		current.addOutlink(outlink);
	}

	public void addCategory(String category)
	{
		current.addCategoryName(category);
	}

	public void save()
	{
		if (pool.size() <= 0)
			return;
		
		end = start + pool.size() - 1;
		try
		{
			this.translateToXML(new File(String.valueOf(start) + ".xml"));
			start += pool.size();
			pool.clear();
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// singleton
	private static ConceptPool	the	= null;

	public static ConceptPool get()
	{
		if (the == null)
			the = new ConceptPool();
		return the;
	}
}
