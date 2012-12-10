package ecologylab.semantics.seeding;

import java.io.File;

import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.namesandnums.SemanticsSessionObjectNames;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Specifies a named collection of seeds.
 * They are found in the registry of {@link CuratedSeedSets CuratedSeedings}
 * 
 * @author andruid
 */
@simpl_inherit
public class CuratedSeeding extends Seed 
implements SemanticsSessionObjectNames
{
	@simpl_scalar protected String		name;
	
	/**
	 * Blank constructor used by automatic ecologylab.serialization instantiations. 
	 */
	public CuratedSeeding()
	{
		super();
	}

	/**
	 * Check the validity of this seed.
	 */
	public boolean validate()
	{
		if (name == null)
		{
			debugA("ERROR: curated_seeding with name unspecified.");
			return false;
		}
		if (CuratedSeedSets.lookup(name) == null)
		{
			debugA("ERROR: Can't find a curated_seeding called " + name);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Bring this seed into the agent or directly into the composition.
	 * 
	 * @param objectRegistry		Context passed between services calls.
	 * @param infoCollector TODO
	 */
	public void performInternalSeedingSteps(SemanticsGlobalScope infoCollector)
	{
		final SeedSet<?> ss	= CuratedSeedSets.lookup(name);
		
		infoCollector.displayStatus("Downloading curated collection: " + name);
		
		final File curatedFile	= new File(name + ".xml");
		infoCollector.getSeeding().setCurrentFileFromUntitled(curatedFile);

		// In case for replace seed with the curated-seeds, SeedSet is reused. 
		// So, old resultDistributer is used without getting reset. 
		// Maybe there is better place to reset ResultDistributer -- eunyee
		ss.resetResultDistributer();

		ss.performSeeding(infoCollector);
	}

	/**
	 * @return
	 */
	public String valueString()
	{
		return name;
	}


	/**
	 * @param value
	 * @return
	 */
	public boolean setValue(String value)
	{
		// TODO Auto-generated method stub
		return false;
	}


	public boolean canChangeVisibility()
	{
		return true;
	}

	public boolean isDeletable()
	{
		return true;
	}

	public boolean isEditable()
	{
		return false;
	}
	
	public boolean isRejectable()
	{
		return false;
	}
}
